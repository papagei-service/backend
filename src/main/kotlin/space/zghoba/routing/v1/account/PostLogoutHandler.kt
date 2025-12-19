package space.zghoba.routing.v1.account

import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.utils.Constants

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.postLogout(
    sessionStorage: SessionStorage,
): suspend RoutingContext.() -> Unit = postLogoutHandler@{
    call.attributes.allKeys
        .firstOrNull { it.name == Constants.USER_SESSION_ID_ATTRIBUTE_KEY }
        ?.also { sessionStorage.invalidate(call.sessionId<UserSession>()!!) }

    val message = mapOf("message" to "The user session was successfully deleted")
    call.respond(status = HttpStatusCode.OK, message = message)
}