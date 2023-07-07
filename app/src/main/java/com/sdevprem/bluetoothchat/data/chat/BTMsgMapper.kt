package com.sdevprem.bluetoothchat.data.chat

import com.sdevprem.bluetoothchat.domain.chat.BTMsg

fun String.toBTMsg(isFromLocalUser: Boolean): BTMsg {
    val name = substringBeforeLast("#")
    val message = substringAfter("#")
    return BTMsg(
        senderName = name,
        msg = message,
        isFromLocalUser = isFromLocalUser
    )
}

fun BTMsg.toByteArray(): ByteArray {
    return "$senderName#$msg".encodeToByteArray()
}