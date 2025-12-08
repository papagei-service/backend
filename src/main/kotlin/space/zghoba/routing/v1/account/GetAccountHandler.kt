package space.zghoba.routing.v1.account

import space.zghoba.mappers.toAccount
import space.zghoba.model.Repository
import space.zghoba.routing.RouteHandlersProvider
import space.zghoba.security.sessions.UserSession
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

@Suppress("UnusedReceiverParameter")
fun RouteHandlersProvider.V1.Account.getAccount(
    repository: Repository,
): suspend RoutingContext.() -> Unit = getAccountHandler@{
    val session = call.sessions.get<UserSession>()

    // Return 401 if there is no user corresponding to the session
    val correspondingUser = repository.getUserById(id = session!!.userId)
    if (correspondingUser == null) {
        val message = mapOf("message" to "The user with the corresponding session does not exist")
        call.respond(status = HttpStatusCode.Unauthorized, message = message)
        return@getAccountHandler
    }

    val message = correspondingUser.toAccount()
    call.respond(status = HttpStatusCode.OK, message = message)
}