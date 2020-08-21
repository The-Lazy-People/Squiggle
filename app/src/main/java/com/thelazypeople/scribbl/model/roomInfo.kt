package com.thelazypeople.scribbl.model
import com.thelazypeople.scribbl.WaitingActivity

/**
 * Stores the information about each Room.
 *
 * @param gamestarted: Variable used to know whether a game is started or players are still in [WaitingActivity].
 * @param password: Whether the room is Password protected. If yes, we receive a numeric Hash-coded password. Else receive [NO].
 * @param reference: Reference of that Room. It is [UUID_of_ADMIN_AND_TIMESTAMP]. This is used by other players when they join a room.
 * @param roomname: Name of the room.
 */

data class roomInfo(
    var gamestarted: Int = 0,
    var password: String = "NO",
    var reference: String = "DEFAULT",
    var roomname: String = "DEFAULT"
)
