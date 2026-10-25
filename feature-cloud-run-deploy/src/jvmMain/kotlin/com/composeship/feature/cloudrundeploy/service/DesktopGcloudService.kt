package com.composeship.feature.cloudrundeploy.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.cloudrundeploy.domain.GcloudService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.nio.file.Files

class DesktopGcloudService(
    private val processService: ProcessService
) : GcloudService {
    override fun deploy(
        projectRoot: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String,
        envVars: Map<String, String>
    ): Flow<ProcessOutput> = flow {
        // Check if it's a Gradle project and needs a build
        if (java.io.File(projectRoot, "gradlew").exists() && java.io.File(projectRoot, "server").exists()) {
            emit(ProcessOutput.Stdout("Building Gradle server distribution..."))
            val gradlew = if (System.getProperty("os.name").lowercase().contains("win")) "gradlew.bat" else "./gradlew"
            val gradleCommand = listOf(gradlew, ":server:installDist", "--no-daemon")

            var gradleExitCode = -1
            processService.execute(gradleCommand, directory = projectRoot).collect { output ->
                if (output is ProcessOutput.Complete) {
                    gradleExitCode = output.exitCode
                } else {
                    emit(output)
                }
            }

            if (gradleExitCode != 0) {
                emit(ProcessOutput.Error(Exception("Gradle build failed with exit code $gradleExitCode")))
                return@flow
            }
        }

        emit(ProcessOutput.Stdout("Building and pushing container..."))
        
        val buildCommand = listOf(
            "gcloud", "builds", "submit",
            "--tag", "gcr.io/$gcloudProjectId/$serviceName",
            "--project", gcloudProjectId,
            "--verbosity", "info", // Higher verbosity to ensure stream is flushed
            projectRoot
        )

        var buildExitCode = -1
        processService.execute(buildCommand, directory = projectRoot).collect { output ->
            if (output is ProcessOutput.Complete) {
                buildExitCode = output.exitCode
            } else {
                emit(output)
            }
        }

        if (buildExitCode == 0) {
            emit(ProcessOutput.Stdout("Deploying to Cloud Run..."))
            val deployCommand = mutableListOf(
                "gcloud", "run", "deploy", serviceName,
                "--image", "gcr.io/$gcloudProjectId/$serviceName",
                "--platform", "managed",
                "--region", region,
                "--project", gcloudProjectId,
                "--verbosity", "info",
                "--allow-unauthenticated",
                "--memory", "1Gi",
                "--cpu", "1",
                "--timeout", "300"
            )

            if (envVars.isNotEmpty()) {
                val envString = envVars.entries.joinToString(",") { "${it.key}=${it.value}" }
                deployCommand.add("--set-env-vars=$envString")
            }

            emit(ProcessOutput.Stdout("Executing: ${deployCommand.joinToString(" ")}"))
            emitAll(processService.execute(deployCommand, directory = projectRoot))
        } else if (buildExitCode != -1) {
            emit(ProcessOutput.Complete(buildExitCode))
        }
    }

    override fun deployFromGitHub(
        githubRepoUrl: String,
        githubBranch: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String,
        envVars: Map<String, String>
    ): Flow<ProcessOutput> = flow {
        val tempDir = Files.createTempDirectory("composeship-cloud-run")
        val tempPath = tempDir.toAbsolutePath().toString()

        try {
            emit(ProcessOutput.Stdout("Cloning repository $githubRepoUrl (branch: ${githubBranch.ifBlank { "default" }})..."))

            val cloneCommand = if (githubBranch.isNotBlank()) {
                listOf("git", "clone", "-b", githubBranch, "--single-branch", githubRepoUrl, ".")
            } else {
                listOf("git", "clone", "--single-branch", githubRepoUrl, ".")
            }

            var cloneExitCode = -1
            processService.execute(cloneCommand, directory = tempPath).collect { output ->
                if (output is ProcessOutput.Complete) {
                    cloneExitCode = output.exitCode
                } else {
                    emit(output)
                }
            }

            if (cloneExitCode != 0) {
                if (cloneExitCode != -1) {
                    emit(ProcessOutput.Complete(cloneExitCode))
                }
                return@flow
            }

            val repository = java.io.File(tempPath)
            if (java.io.File(repository, "gradlew").exists() &&
                java.io.File(repository, "server").isDirectory
            ) {
                emit(ProcessOutput.Stdout("Building Gradle server distribution for the Dockerfile..."))
                val gradlew = if (System.getProperty("os.name").lowercase().contains("win")) {
                    "gradlew.bat"
                } else {
                    "./gradlew"
                }
                var buildExitCode: Int? = null
                var buildFailedToStart = false
                processService.execute(
                    listOf(gradlew, ":server:installDist", "--no-daemon"),
                    directory = tempPath
                ).collect { output ->
                    when (output) {
                        is ProcessOutput.Complete -> buildExitCode = output.exitCode
                        is ProcessOutput.Error -> {
                            buildFailedToStart = true
                            emit(output)
                        }
                        else -> emit(output)
                    }
                }

                if (buildFailedToStart) return@flow
                if (buildExitCode != 0) {
                    if (buildExitCode == null) {
                        emit(ProcessOutput.Error(IllegalStateException(
                            "Gradle server build finished without an exit code"
                        )))
                    } else {
                        emit(ProcessOutput.Complete(buildExitCode))
                    }
                    return@flow
                }
            }

            val sourceDirectory = findDeploySourceDirectory(tempPath)
                ?: run {
                    emit(ProcessOutput.Error(IllegalStateException(
                        "GitHub repository does not contain a Dockerfile. Cloud Run source deploy requires a Dockerfile in the repository root or a project subdirectory."
                    )))
                    return@flow
                }

            val deployCommand = mutableListOf(
                "gcloud", "run", "deploy", serviceName,
                "--source", sourceDirectory,
                "--platform", "managed",
                "--region", region,
                "--project", gcloudProjectId,
                "--verbosity", "info",
                "--allow-unauthenticated",
                "--quiet"
            )

            if (envVars.isNotEmpty()) {
                val envString = envVars.entries.joinToString(",") { "${it.key}=${it.value}" }
                deployCommand.add("--set-env-vars=$envString")
            }

            emit(ProcessOutput.Stdout("Using source directory: $sourceDirectory"))
            emit(ProcessOutput.Stdout("Executing: ${deployCommand.joinToString(" ")}"))
            emit(ProcessOutput.Stdout("Note: Cloud Build from the cloned repository may take 3-10 minutes. Streaming output below..."))
            emitAll(processService.execute(deployCommand, directory = tempPath))
        } catch (e: Exception) {
            emit(ProcessOutput.Error(e))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    private fun findDeploySourceDirectory(repoPath: String): String? {
        val repoDir = java.io.File(repoPath)
        val rootDockerfile = java.io.File(repoDir, "Dockerfile")
        if (rootDockerfile.exists()) return repoDir.absolutePath

        val serverDockerfile = java.io.File(repoDir, "server/Dockerfile")
        if (serverDockerfile.exists()) return java.io.File(repoDir, "server").absolutePath

        return repoDir.walkTopDown()
            .filter { it.isFile && it.name == "Dockerfile" }
            .map { it.parentFile?.absolutePath }
            .firstOrNull()
    }

    override fun getCurrentProjectId(): Flow<ProcessOutput> =
        processService.execute(listOf("gcloud", "config", "get-value", "project"))

    override fun listServices(gcloudProjectId: String, region: String): Flow<ProcessOutput> =
        processService.execute(
            listOf(
                "gcloud", "run", "services", "list",
                "--project", gcloudProjectId,
                "--region", region,
                "--format", "value(SERVICE)"
            )
        )
}
