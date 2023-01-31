package com.example.dummybluetoothsafe;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    int requestEnableForBluetooth;
    Button buttonON, buttonOFF, scanButton;
    Intent btEnablingIntent;
    Map<String, String> BluetoothData;
    private int scanCount = 3;

    ListView scannedListView;
    ArrayList<String> deviceDetailsList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannedListView = (ListView) findViewById(R.id.scannedListView);


        buttonON = (Button) findViewById(R.id.buttonON);
        buttonOFF = (Button) findViewById(R.id.buttonOFF);
        scanButton = (Button) findViewById(R.id.buttonScan);

        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestEnableForBluetooth = 1;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothONMethod();
        bluetoothOFFMethod();
        //permissions granted at runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        }

//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        }

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        bluetoothSCANMethod();

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceDetailsList);
        scannedListView.setAdapter(arrayAdapter);

    }

    private boolean CheckEnableGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled;
        try {
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception npe) {
            isEnabled = false;
        }
        return isEnabled;
    }

    private void bluetoothSCANMethod() {
        scanButton.setOnClickListener(view -> {

            if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                return;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                startActivity(btEnablingIntent);
                Toast.makeText(getApplicationContext(), "Enabling the Bluetooth", Toast.LENGTH_LONG).show();
                return;
            }
            if (!CheckEnableGPS()) {
                Toast.makeText(getApplicationContext(), "GPS Not Enabled, Please enable first", Toast.LENGTH_LONG).show();
                Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsIntent);
                return;
            }
            try {
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                }
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                scanCount = 1; //for ex change the scan count number to 3 then aftre pressing the scan button (once) it will scan three time 12*3=36 seconds
                mBluetoothAdapter.startDiscovery();
            } catch (Exception npe) {
                Toast.makeText(getApplicationContext(), "Exception, Scan Again", Toast.LENGTH_LONG).show();
            }
        });
    }

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                Toast.makeText(getApplicationContext(), "Device Found " + device.getName() + " RSSI: " + rssi + "dBm ", Toast.LENGTH_SHORT).show();

                //input data to display
                deviceDetailsList.add("Device Name: "+ device.getName() + "\nDevice Address: " + device.getAddress()+" RSSI: "+ rssi);

                arrayAdapter.notifyDataSetChanged();
//                scannedListView.setAdapter(arrayAdapter);
            }
            else if (mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Discovery Finished", Toast.LENGTH_SHORT).show();
                scanCount=scanCount-1;
                if(scanCount>0){
                    mBluetoothAdapter.startDiscovery();
                }
            }
            else if (mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onDestroy() {
        //Toasting all the nearby discovered devices
        Toast.makeText(getApplicationContext(), "Toasting all nearby discovered ", Toast.LENGTH_SHORT).show();
        for (Map.Entry<String, String> entry : BluetoothData.entrySet()) {
            Toast.makeText(getApplicationContext(), "Found MAC: " + entry.getKey() + " RSSI: " + entry.getValue() + "dBm ", Toast.LENGTH_SHORT).show();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestEnableForBluetooth) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabling cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void bluetoothONMethod() {
        buttonON.setOnClickListener(v -> {
            if (mBluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    //code for bluetooth enables
                    startActivity(btEnablingIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth already enabled", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void bluetoothOFFMethod() {
        buttonOFF.setOnClickListener(view -> {
            if (mBluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.BLUETOOTH}, 1);
                }
                mBluetoothAdapter.disable();
                Toast.makeText(getApplicationContext(), "Bluetooth is Disabled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth is Already Disabled", Toast.LENGTH_LONG).show();
            }

        });
    }


}












//
//
//
//
//public class MainActivity extends AppCompatActivity {
//    BluetoothAdapter bluetoothAdapter;
//    private ActivityResultLauncher<Intent> bluetoothLauncher;
//
//    Button scanButton;
//    ListView scanListView;
//    ArrayList<String> stringArrayList = new ArrayList<String>();
//      ArrayAdapter<String> arrayAdapter;
//    //String stringArrayList [] = {"Device Name:","\nMac Address:"," \nRSSI:","\nDistance:"};
//
//    BroadcastReceiver bluetoothReceiver;
//
//    @SuppressLint("MissingPermission")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        stringArrayList.add(stringArrayList.size(), "My Device name: " + bluetoothAdapter.getName() + "\nAddress: " + bluetoothAdapter.getAddress());
//
//
//        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//
//
//
//        registerReceiver(bluetoothReceiver, intentFilter);
//
//        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArrayList);
//        scanListView.setAdapter(arrayAdapter);
//
//        scanButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!bluetoothAdapter.isEnabled()) {
//                    Toast.makeText(getApplicationContext(), "Please enable the Bluetooth First", Toast.LENGTH_LONG).show();
//                } else {
//                    bluetoothAdapter.startDiscovery();
//                }
//            }
//        });
//
//
//        bluetoothReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//
//                stringArrayList.add(stringArrayList.size(),"string added in onRecieve");
//                arrayAdapter.add("Device Name: indside onreceive" + "\nDevice Address: " );
//                scanListView.setAdapter(arrayAdapter);
//                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
//                    if (bluetoothAdapter.isDiscovering()) {
//                        bluetoothAdapter.cancelDiscovery();
//                        Toast.makeText(getApplicationContext(), "Bluetooth cancelled onrecieve discovering", Toast.LENGTH_LONG).show();
//                    }
//                    bluetoothAdapter.startDiscovery();
//                }
//                else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    stringArrayList.add("Device Name: "+ device.getName() + "\nDevice Address: " + device.getAddress());
//                    arrayAdapter.add("Device Name: "+ device.getName() + "\nDevice Address: " + device.getAddress());
//                    arrayAdapter.notifyDataSetChanged();
//                    scanListView.setAdapter(arrayAdapter);
//                }
//            }
//        };
//    }
//}
