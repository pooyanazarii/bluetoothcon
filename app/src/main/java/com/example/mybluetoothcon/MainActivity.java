package com.example.mybluetoothcon;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_ENABLE_BLUETOOTH = 1212;
    private static final int PERM_REQ_CODE = 1234;
    Dialog dialog;
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter<String> discoveredDevicesAdapter;
    BroadcastReceiver discoveryReceiver;
    Button fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         fab = findViewById(R.id.btn_fab);
        Snackbar.make(fab, "Welcome", Snackbar.LENGTH_SHORT).show();
        if (bluetoothAdapter == null) {
            // Snackbar.make(fab,"Bluetooth is not available" , Snackbar.LENGTH_LONG).show();
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "closing application", Toast.LENGTH_SHORT).show();
            finish();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevicesDialog();
            }
        });

        initReciver();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERM_REQ_CODE);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (! bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent , REQ_ENABLE_BLUETOOTH);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQ_ENABLE_BLUETOOTH){
            if(resultCode == RESULT_OK){
                Snackbar.make(fab,"Bluetooth is Active", Snackbar.LENGTH_SHORT).show();

            }else{
                Snackbar.make (fab,"Bluetooth is Disable!", Snackbar.LENGTH_SHORT);
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
                Snackbar.make(fab,"Permission Granted" , Snackbar.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission Deneid, closing application", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initReciver() {
        discoveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    Toast.makeText(context, "device found", Toast.LENGTH_SHORT).show();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    discoveredDevicesAdapter.add(device.getName());
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Toast.makeText(context,
                            "discovery finished "+ discoveredDevicesAdapter.getCount()+" items found",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void showDevicesDialog() {
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.devices_dialog);
        TextView text = dialog.findViewById(R.id.text);
        ListView listView = dialog.findViewById(R.id.list_view);
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        discoveredDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(discoveredDevicesAdapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver,filter);

        //dialog.setCancelable(false);
        resizeDialog(dialog);
        dialog.show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void resizeDialog(Dialog dialog){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Window window = dialog.getWindow();
        if (window != dialog.getWindow()){
            window.setLayout((int) (0.9 * metrics.widthPixels), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

}