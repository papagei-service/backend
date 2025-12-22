package space.zghoba.routing.v1.account

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.deleteAccount(
    repository: Repository,
): suspend RoutingContext.() -> Unit = deleteAccountHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 404 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session!!.userId)
    repository.deleteUserById(id = correspondingUser!!.id!!)
    val message = mapOf("message" to "You literally do not need to handle this response")
    call.respond(status = HttpStatusCode.NoContent, message = message)
}