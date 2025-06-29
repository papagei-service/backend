package com.yaroslavzghoba.routing.v1.collections

import com.yaroslavzghoba.mappers.toCardCollection
import com.yaroslavzghoba.model.CardCollectionInsertRequest
import com.yaroslavzghoba.model.Repository
import com.yaroslavzghoba.routing.RouteHandlersProvider
import com.yaroslavzghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Collections.postCollection(
    repository: Repository,
): suspend RoutingContext.() -> Unit = postCollectionHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 400 if the request body cannot be converted to a collection
    val body = runCatching { call.receive<CardCollectionInsertRequest>() }.getOrNull()
    if (body == null) {
        val message = mapOf("message" to "The request body cannot be converted to a collection")
        call.respond(status = HttpStatusCode.BadRequest, message = message)
        return@postCollectionHandler
    }

    // Insert the collection into the storage
    val collectionToInsert = body.toCardCollection(id = null, ownerId = session!!.userId)
    val insertedCollection = repository.insertCollection(collection = collectionToInsert)

    call.respond(status = HttpStatusCode.Created, message = insertedCollection)
}