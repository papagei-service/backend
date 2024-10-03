package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.mappers.toCardCollection
import com.yaroslavzghoba.model.CollectionRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.postCollection(
    repository: Repository,
): suspend RoutingContext.() -> Unit = postCollectionHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if the user is not authenticated
    if (session == null) {
        val message = mapOf("message" to "You must be authenticated using sessions to get access")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@postCollectionHandler
    }

    // Return 400 if the request body cannot be converted to a collection
    val body = try {
        call.receive<CollectionRequest>()
    } catch (exception: BadRequestException) {
        val message = mapOf("message" to "The request body cannot be converted to a collection")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postCollectionHandler
    }

    // Return 404 if there is no user corresponding to the session
    val user = repository.getUserByUsername(username = session.username)
    if (user == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.NotFound, message = message)
        return@postCollectionHandler
    }

    // Insert the collection into the storage
    val collectionToInsert = body.toCardCollection(ownerUsername = user.username)
    val insertedCollection = try {
        repository.insertCollection(collectionToInsert)
    } catch (exception: ExposedSQLException) {
        val message = mapOf("message" to "A collection with the corresponding identifier already exists")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postCollectionHandler
    }

    call.respond(status = HttpStatusCode.Created, message = insertedCollection)
}