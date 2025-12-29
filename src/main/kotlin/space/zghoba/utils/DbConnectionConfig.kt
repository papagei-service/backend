package space.zghoba.utils

data class DbConnectionConfig(
    val driver: String,
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
)
