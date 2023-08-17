# Bluetooth Chat

A simple chatting android app that uses bluetooth to connect with
other devices and transfer chats. It is made in Jetpack Compose and follows MVVM
Architecture for the flow of data.

## Screenshots

|                                                                                                                       |                                                                                                                           |
|-----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| ![home_screen](https://github.com/sDevPrem/bluetooth-chat-mvvm/assets/130966261/952a6416-5f9b-4b0b-85c5-5124d5e09a60) | ![chatting_screen](https://github.com/sDevPrem/bluetooth-chat-mvvm/assets/130966261/f69605b8-bede-49bc-9147-660e7b0178f5) |

## Features

* Scan nearby bluetooth devices.
* Pair with nearby devices.
* Once paired, start chat with the paired device.

## How to use

1. Give the appropriate permission asked at startup.
2. Open BluetoothChat app on both the devices and turn on bluetooth.
3. Click on start server button in one of the device.
4. On the other device, click on start scan.
    * If the device is paired it will be shown
      in the paired devices and clicking it will open the chat.
    * If not, wait for the scan and when the device appears below the scanned devices
      click and pair it. Upon pairing, you will be navigated to chatting screen.
5. After completing the chat, click on the cross button at the top-right corner
   that will close the chat on both the devices.

## Build With

[Jetpack Compose](https://developer.android.com/jetpack/compose) : To build UI.  
[Android Bluetooth API](https://developer.android.com/guide/topics/connectivity/bluetooth) : To
connect devices and share data.  
[Kotlin Coroutines and Flow](https://developer.android.com/kotlin/flow) : For asynchronous
programming.  
[Hilt](https://developer.android.com/training/dependency-injection/hilt-android) : For dependency
injection.

## Architecture

1. Client-Server architecture to transfer chats from one device to another
2. MVVM (Model-View-ViewModel) architecture.
3. UDF (Unidirectional Data Flow) Pattern.

## Classes And Roles

* [AndroidBTController](app/src/main/java/com/sdevprem/bluetoothchat/data/chat/AndroidBTController.kt)
    * Get Paired devices
    * To start and stop scans for new bluetooth devices
    * To connect and disconnect bluetooth devices
* [BTDataTransferService](app/src/main/java/com/sdevprem/bluetoothchat/data/chat/BTDataTransferService.kt)
    * To send messages
    * Listen for incoming messages
