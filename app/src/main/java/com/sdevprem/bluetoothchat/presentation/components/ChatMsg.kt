package com.sdevprem.bluetoothchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdevprem.bluetoothchat.domain.chat.BTMsg
import com.sdevprem.bluetoothchat.ui.theme.BluetoothChatTheme
import com.sdevprem.bluetoothchat.ui.theme.OldRose
import com.sdevprem.bluetoothchat.ui.theme.Vanilla

@Composable
fun ChatMsg(
    msg: BTMsg,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (msg.isFromLocalUser) 15.dp else 0.dp,
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (msg.isFromLocalUser) 0.dp else 15.dp
                )
            )
            .background(
                if (msg.isFromLocalUser) OldRose else Vanilla
            )
            .padding(16.dp)
    ) {
        Text(
            text = msg.senderName,
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = msg.msg,
            color = Color.Black,
            modifier = Modifier
                .widthIn(max = 250.dp)
        )

    }
}

@Composable
@Preview(showBackground = true)
private fun ChatMsgPreview() {
    BluetoothChatTheme {
        ChatMsg(
            msg = BTMsg(
                msg = "Hello World!",
                senderName = "Prem ",
                isFromLocalUser = true
            )
        )
    }
}