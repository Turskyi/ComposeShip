package com.composeship.feature.macosrelease.data.service

import com.composeship.feature.macosrelease.domain.service.ProcessOutput
import com.composeship.feature.macosrelease.domain.service.ProcessService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class DesktopProcessService : ProcessService {
    override fun execute(
        command: List<String>,
        directory: String?,
        env: Map<String, String>
    ): Flow<ProcessOutput> = callbackFlow {
        val processBuilder = ProcessBuilder(command)
        directory?.let { processBuilder.directory(File(it)) }
        processBuilder.environment().putAll(env)
        processBuilder.redirectErrorStream(false)

        try {
            val process = processBuilder.start()

            val stdoutJob = launch(Dispatchers.IO) {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        trySend(ProcessOutput.Stdout(line!!))
                    }
                }
            }

            val stderrJob = launch(Dispatchers.IO) {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        trySend(ProcessOutput.Stderr(line!!))
                    }
                }
            }

            launch(Dispatchers.IO) {
                val exitCode = process.waitFor()
                stdoutJob.join()
                stderrJob.join()
                trySend(ProcessOutput.Complete(exitCode))
                close()
            }
        } catch (e: Exception) {
            trySend(ProcessOutput.Error(e))
            close(e)
        }

        awaitClose {
            // No direct way to stop process easily without reference, 
            // but for this tool, processes are usually short-lived or crucial.
        }
    }
}
