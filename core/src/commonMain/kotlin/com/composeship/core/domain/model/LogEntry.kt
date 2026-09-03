package com.composeship.core.domain.model

data class LogEntry(
    val message: String,
    val type: LogType = LogType.Info
)

enum class LogType {
    Info, Error, Success
}
