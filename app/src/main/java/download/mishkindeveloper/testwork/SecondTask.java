package download.mishkindeveloper.testwork;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import download.mishkindeveloper.testwork.databinding.ActivitySecondTaskBinding;

public class SecondTask extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private static final int REQUEST_ENABLE_BT = 1;
    private ActivitySecondTaskBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> discoveredDevicesList;
    private ListView listBluetoothDevicesView;
    private boolean isReceiverRegistered = false;
    private BroadcastReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecondTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        binding.buttonFind.setOnClickListener(v -> {
            if (bluetoothAdapter.isEnabled()) {
                if (checkBluetoothPermissions()) {
                    startDiscovery();
                    binding.buttonFind.setVisibility(View.GONE);
                    binding.devicesListView.setVisibility(View.VISIBLE);
                }
            } else {
                pleaseOnBluetooth();
            }
        });

        binding.devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDevice = discoveredDevicesList.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("result_key_from_second_task", selectedDevice);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_dont_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        listBluetoothDevicesView = binding.devicesListView;
        discoveredDevicesList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevicesList);
        listBluetoothDevicesView.setAdapter(arrayAdapter);
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean hasConnectPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
            boolean hasScanPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

            if (!hasConnectPermission || !hasScanPermission) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_BLUETOOTH_PERMISSIONS);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasFineLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (!hasFineLocationPermission) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUETOOTH_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private void startDiscovery() {
        Log.d("Bluetooth devices", "Starting discovery...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Bluetooth devices", "BLUETOOTH_SCAN permission is not granted.");
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Bluetooth devices", "Location permission is not granted.");
                return;
            }
        }

        if (bluetoothAdapter.isDiscovering()) {
            Log.d("Bluetooth devices", "isDiscovering() - stop old scanning");
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, R.string.search_started, Toast.LENGTH_LONG).show();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    try {
                        String deviceName = device.getName() != null ? device.getName() : "Unknown device";
                        String deviceInfo = deviceName + " (" + device.getAddress() + ")";
                        if (!discoveredDevicesList.contains(deviceInfo)) {
                            discoveredDevicesList.add(deviceInfo);
                            arrayAdapter.notifyDataSetChanged();
                            Log.d("Bluetooth devices", "Device found: " + deviceInfo);
                        }
                    } catch (SecurityException e) {
                        Log.d("Bluetooth devices", "SecurityException: " + e.getMessage());
                        throw new RuntimeException("problem in onReceive method",e);
                    }
                } else {
                    Log.d("Bluetooth devices", "Device not found");
                }
            }
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        } catch (SecurityException e) {
            Log.d("Bluetooth devices", "SecurityException: " + e.getMessage());
            throw new RuntimeException("problem in onDestroy method",e);
        }
        if (isReceiverRegistered) {
            unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                if (bluetoothAdapter.isEnabled()) {
                    startDiscovery();
                } else {
                    pleaseOnBluetooth();
                }
            } else {
                Toast.makeText(this, R.string.dont_required_permissions, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void pleaseOnBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_dont_supported, Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}
