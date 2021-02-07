package com.example.mybluetoothcon;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_ENABLE_BT = 1212;
    private static final int PERM_REQ_CODE = 1234;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice connectingDevice;
    ArrayAdapter<BluetoothDevice> discoveredDevicesAdapter;
    BroadcastReceiver discoveryReceiver;
    Button fab;
    Dialog dialog;
    Handler handler;
    ChatController chatController;
    TextView tv_status;
    RecyclerView chatlist;
    Button sendBtn;
    EditText input;
    MsgAdapter msgAdapter;
    public static final int MESSAGE_STATE_CHANGED = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (Button) findViewById(R.id.btn_fab);
        chatlist = (RecyclerView) findViewById(R.id.chat_list);
        tv_status = (TextView) findViewById(R.id.tv_status);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        input = (EditText) findViewById(R.id.input_message);
        sendBtn = (Button) findViewById(R.id.sendbtn);

        msgAdapter = new MsgAdapter();
        chatlist.setLayoutManager(new LinearLayoutManager(this));
        chatlist.setItemAnimator(new DefaultItemAnimator());
        chatlist.setAdapter(msgAdapter);


        if(bluetoothAdapter == null){
            Snackbar.make(fab, "Bluetooth is not available.", Snackbar.LENGTH_LONG).show();
            Toast.makeText(this, "closing application", Toast.LENGTH_SHORT).show();
            finish();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDevicesDialog();
            }
        });

        initReceiver();
        initHandler();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_REQ_CODE);
            }
        }
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if(text.isEmpty()){
            return;
        }
        chatController.write(text.getBytes());
        input.getText().clear();
    }


    public void setStatus(String status){
        tv_status.setText(status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(! bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQ_ENABLE_BT);
        } else {
            chatController = new ChatController(handler);
        }

        if(chatController != null && chatController.getState() == ChatController.STATE_NONE){
            chatController.start();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_ENABLE_BT){
            if(resultCode == RESULT_OK){
                Snackbar.make(fab, "BT is active", Snackbar.LENGTH_SHORT).show();
                chatController = new ChatController(handler);
            } else {
                Snackbar.make(fab, "BT is still disabled.", Snackbar.LENGTH_LONG).show();
                Toast.makeText(this, "closing application", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERM_REQ_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Snackbar.make(fab, "Permission Granted", Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied, closing application", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initReceiver() {
        discoveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    Toast.makeText(context, "device found", Toast.LENGTH_SHORT).show();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    discoveredDevicesAdapter.add(device);
                } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Toast.makeText(context,
                            "discovery finished. " + discoveredDevicesAdapter.getCount() + " items found.",
                            Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void initHandler(){
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGE_STATE_CHANGED:
                        switch (msg.arg1){
                            case ChatController.STATE_CONNECTED :
                                setStatus("Connected to " + connectingDevice.getName());
                                break;
                            case ChatController.STATE_CONNECTING:
                                setStatus("Connecting");
                                break;
                            case ChatController.STATE_LISTEN:
                            case ChatController.STATE_NONE:
                                setStatus("Not Connected");
                                break;
                        }
                        break;
                    case MESSAGE_READ :
                        byte[] buffer = (byte[]) msg.obj;
                        String readmessage = new String(buffer, 0, msg.arg1);
                        MainActivity.this.showReadMessage(readmessage);
                        break;
                    case MESSAGE_WRITE :
                        byte[] writeBuffer = (byte[]) msg.obj;
                        String writemessage = new String(writeBuffer);
                        MainActivity.this.showWriteMessage(writemessage);
                        break;
                    case MESSAGE_DEVICE_OBJECT :
                        connectingDevice = msg.getData().getParcelable("device");
                        if(connectingDevice != null){
                            Snackbar.make(fab, "Connected to " + connectingDevice.getName(), Snackbar.LENGTH_LONG).show();
                        }
                        break;
                    case MESSAGE_TOAST :
                        Toast.makeText(MainActivity.this, msg.getData().getString("toast"),
                                Toast.LENGTH_SHORT).show();
                        break;
                    default: break;
                }
                return false;
            }
        });
    }

    private void showWriteMessage(String writemessage) {
        msgAdapter.addMessage(writemessage, true);
    }

    private void showReadMessage(String readmessage) {
        msgAdapter.addMessage(readmessage, false);
    }


    private void showDevicesDialog() {
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.devices_dialog);
        TextView text = (TextView) dialog.findViewById(R.id.text);
        ListView listview = (ListView) dialog.findViewById(R.id.list_view);

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
        discoveredDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setText(getItem(position).getName());
                return v;
            }
        };

        listview.setAdapter(discoveredDevicesAdapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, filter);

//        dialog.setCancelable(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                bluetoothAdapter.cancelDiscovery();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                chatController.connect(discoveredDevicesAdapter.getItem(position));
                dialog.dismiss();
            }
        });

        resizeDialog(dialog);
        dialog.show();
    }




    private void resizeDialog(Dialog dialog){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Window window = dialog.getWindow();
        if(window != null){
            window.setLayout((int) (0.9 * metrics.widthPixels) , ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatController != null){
            chatController.stop();
        }
    }
}
