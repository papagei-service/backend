package space.zghoba.utils

import io.ktor.util.logging.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.io.File

private val LOGGER = KtorSimpleLogger("space.zghoba.utils")

/**
 * Executes database schema migration scripts located in the specified directory.
 *
 * @param scriptsDirectoryPath The path to the directory containing the migration scripts.
 * Each script should be a text file containing SQL queries.
 *
 * @throws IllegalArgumentException if the provided path is not a valid directory.
 */
suspend fun executeDbSchemaMigrations(
    scriptsDirectoryPath: String,
    withLogs: Boolean = true,
) {
    val directory = File(scriptsDirectoryPath)
    require(directory.exists() && directory.isDirectory) {
        "Invalid directory path: $scriptsDirectoryPath"
    }

    val scripts = directory.getSortedMigrationScripts()
    if (withLogs) LOGGER.info("Found ${scripts.size} migration scripts to execute.")
    suspendTransaction {
        scripts.forEach { script ->
            if (withLogs) LOGGER.info("Executing migration script: ${script.name}")
            val queries = script.readText()
            exec(queries)
        }
    }
}

private fun File.getSortedMigrationScripts(): List<File> {
    return this.listFiles { file -> file.isFile }
        ?.sortedBy { it.name }
        ?: emptyList()
}

