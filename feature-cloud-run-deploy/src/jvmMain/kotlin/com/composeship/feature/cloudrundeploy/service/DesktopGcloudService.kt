package com.composeship.feature.cloudrundeploy.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.cloudrundeploy.domain.GcloudService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class DesktopGcloudService(
    private val processService: ProcessService
) : GcloudService {
    override fun deploy(
        projectRoot: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String
    ): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("Building and pushing container..."))
        
        val buildCommand = listOf(
            "gcloud", "builds", "submit",
            "--tag", "gcr.io/$gcloudProjectId/$serviceName",
            "--project", gcloudProjectId,
            "--verbosity", "info", // Higher verbosity to ensure stream is flushed
            projectRoot
        )

        var buildSuccess = false
        processService.execute(buildCommand, directory = projectRoot).collect { output ->
            emit(output)
            if (output is ProcessOutput.Complete && output.exitCode == 0) {
                buildSuccess = true
            }
        }

        if (buildSuccess) {
            emit(ProcessOutput.Stdout("Deploying to Cloud Run..."))
            val deployCommand = listOf(
                "gcloud", "run", "deploy", serviceName,
                "--image", "gcr.io/$gcloudProjectId/$serviceName",
                "--platform", "managed",
                "--region", region,
                "--project", gcloudProjectId,
                "--verbosity", "info",
                "--allow-unauthenticated"
            )
            emitAll(processService.execute(deployCommand, directory = projectRoot))
        }
    }

    override fun deployFromGitHub(
        githubRepoUrl: String,
        githubBranch: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String
    ): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("Deploying from GitHub: $githubRepoUrl (branch: $githubBranch)..."))
        
        // This requires the repo to be connected to Cloud Build or just use the --source flag with a URL if supported
        // Note: gcloud run deploy --source https://github.com/user/repo.git is not directly supported without local checkout
        // BUT Cloud Build can do it. For CLI simplicity, we'll use a Cloud Build trigger or suggest it.
        // Actually, the easiest CLI way to trigger a remote build from a URL is 'gcloud builds submit' with a trigger
        // or just 'gcloud run deploy' with '--source' pointing to a LOCAL checkout.
        
        // Given we are on Desktop here, we *could* clone it, but that's complex.
        // A better agnostic way is 'gcloud alpha builds submit --repo=...' if using the new repositories feature.
        
        // For now, let's use a command that works if the repo is already connected to GCP via 'Repositories' (2nd gen)
        val deployCommand = listOf(
            "gcloud", "run", "deploy", serviceName,
            "--source", githubRepoUrl, // Some gcloud versions support URL here, or we use Cloud Build API
            "--platform", "managed",
            "--region", region,
            "--project", gcloudProjectId,
            "--verbosity", "info",
            "--allow-unauthenticated"
        )
        emitAll(processService.execute(deployCommand))
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
