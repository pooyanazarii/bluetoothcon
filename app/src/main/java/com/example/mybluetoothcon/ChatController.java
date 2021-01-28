package com.example.mybluetoothcon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ChatController {
    private static final String APP_NAME = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-300a-11e0-ac64-0800200c9a66");
    private AcceptThread acceptThread;
    private ConnectThread connectThread;

    private ReadWriteThread readWriteThread;
    private ChatController
    private int state;
    private BluetoothAdapter bluetoothAdapter;

    public ChatController() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    }

    private static final int STATE_NONE = 0;
    private static final int STATE_LISTEN = 1;
    private static final int STATE_CONNECTING = 2;
    private static final int STATE_CONNECTED = 3;


    private void connectionFaild() {
    }

    private void connected(BluetoothSocket socket, BluetoothDevice device) {
    }


    public class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();

            }
            serverSocket = tmp;

        }

        @Override
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;
            while (state != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                if (socket != null) {
                    synchronized (ChatController.this) {
                        switch (state) {
                            case STATE_NONE:
                                break;
                            case STATE_LISTEN:
                                //connect

                                break;
                            case STATE_CONNECTING:
                                break;
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }

            }
            super.run();
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ConnectThread extends Thread {
        BluetoothSocket socket;
        BluetoothDevice device;

        @Override
        public void run() {
            setName("Connectthread");

            try {
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                // connection failed
                connectionFaild();
                return;
            }
            synchronized (ChatController.this) {
                connectThread = null;
            }
            //connect
            connected(socket, device);
        }


        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            this.device = device;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }
    }

    public class ReadWriteThread extends Thread {
        private BluetoothSocket socket;
        private InputStream inStream;
        private OutputStream outStream;

        public ReadWriteThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inStream = tmpIn;
            outStream = tmpOut;


        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len;

            while (true) {
                try {
                    len = inStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace(); break;
                    //start again and listen
                    ChatContrtbller.this.start():
                }
                //send to UI -> Activity

            }
        }
    }

//    private void start({
//
//    })


}



