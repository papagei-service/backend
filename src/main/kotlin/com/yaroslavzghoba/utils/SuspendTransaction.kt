package com.yaroslavzghoba.utils

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Runs a block of code within a database transaction, through the IO Dispatcher.
 * This is designed to offload blocking jobs of work onto a thread pool.
 *
 * @param block A block of code that must be launched within a database transaction.
 */
suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)