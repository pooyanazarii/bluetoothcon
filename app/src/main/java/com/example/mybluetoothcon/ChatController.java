package com.example.mybluetoothcon;

import java.util.UUID;

public class ChatController {
    private static final String APP_NAME = "BluetoothChat";
    private  static final UUID MY_UUID = UUID.fromString("8ce255c0-300a-11e0-ac64-0800200c9a66");

    private int state;
    private static final int STATE_NONE = 0;
    private static final int STATE_LISTEN = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;


}
