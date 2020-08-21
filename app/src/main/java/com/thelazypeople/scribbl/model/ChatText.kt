package com.thelazypeople.scribbl.model
import com.thelazypeople.scribbl.adapters.ChatAdapter

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
