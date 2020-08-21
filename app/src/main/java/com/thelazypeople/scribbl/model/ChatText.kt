package com.thelazypeople.scribbl.model

/**
 * Stores the chat message to be populated in [ChatAdapter].
 *
 * @param UID: Unique ID of the chat sender.
 * @param userName: Name of the chat sender.
 * @param text: Text part of the chat.
 */

data class ChatText(
    var UID: String = "",
    var userName: String = "",
    var text: String = ""
)
