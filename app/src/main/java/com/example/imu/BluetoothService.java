package com.example.imu;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private ViewModel viewModel;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt1;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private String deviceAddress;
    boolean isReceiving;
    int connected = 0;
    public static final String BLUETOOTH_CONNECTED = "DEVICE_CONNECTED";
    public static final String BLUETOOTH_DISCONNECTED = "DEVICE_DISCONNECTED";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device doesn't support Bluetooth");
            stopSelf(); // Stop service if the device doesn't support Bluetooth
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String device = intent.getStringExtra("device_address");
        if (device != null) {
//            connectToDevice(device);
//            Log.e("connection","connect"+device.getAddress());
            System.out.println(device);
            connectToDevice1(device);
        } else {
            System.out.println("address is null");
        }
        return START_STICKY;
    }

    private void connectToDevice1(String deviceAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothGatt1 = device.connectGatt(this, false, mygattCallback1);
        // connect to device
    }

    private final BluetoothGattCallback mygattCallback1 = new BluetoothGattCallback() {
        //once device get connected to app, callback fuctions will call for each state
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // call`s when state of connection change CONNECT AND DISCONNECT
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("conncetion", "connected successfully");
                sendBroadcastData(BLUETOOTH_CONNECTED);
                if (ActivityCompat.checkSelfPermission(BluetoothService.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("conncetion", "disconnceted");
                sendBroadcastData(BLUETOOTH_DISCONNECTED);
                stopSelf();
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //call`s when required service is discovered
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString("7271f06e-5088-46c9-ab77-4e246b3ea3cb"));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("660c4a6f-16d8-4e57-8fdb-a4058934242d"));
                    if (characteristic != null) {
                        if (ActivityCompat.checkSelfPermission(BluetoothService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        //        //call`s when data received from ble device
        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);

//            Log.d("onCharacteristicChanged", "Received data: " + Arrays.toString(value));
            if (value.length >= 10) {
                byte[] xvalue = Arrays.copyOfRange(value,0,2);
                byte[] yvalue= Arrays.copyOfRange(value,2,4);
                byte[] zvalue= Arrays.copyOfRange(value,4,6);
                byte[] epoch = Arrays.copyOfRange(value,6,14);
                ByteBuffer epochBuffer = ByteBuffer.wrap(epoch).order(ByteOrder.LITTLE_ENDIAN);
                long epochValue = epochBuffer.getLong();
                short x = ByteBuffer.wrap(xvalue).order(ByteOrder.LITTLE_ENDIAN).getShort();
                short y = ByteBuffer.wrap(yvalue).order(ByteOrder.LITTLE_ENDIAN).getShort();
                short z = ByteBuffer.wrap(zvalue).order(ByteOrder.LITTLE_ENDIAN).getShort();
                short[] data = new short[3];
                 data[0] = x;
                 data[1] = y;
                 data[2] = z;
                sendBroadcastData(data,epochValue);
//                sendBroadcastData(epochValue);
//                System.out.println(x+" "+y+" "+z+" "+epochValue);
            }
        }
    };


    public void connectToDevice(BluetoothDevice device) {

        try {
            if (ActivityCompat.checkSelfPermission(BluetoothService.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID); // Replace with your UUID
            bluetoothSocket.connect();
            sendBroadcastData(BLUETOOTH_CONNECTED);

            inputStream = bluetoothSocket.getInputStream();
            isReceiving = true;
            receiveData();
        } catch (IOException e) {
            Log.e(TAG, "Error setting up Bluetooth connection", e);
            sendBroadcastData(BLUETOOTH_DISCONNECTED);
            stopSelf();
        }
    }

    private void receiveData() {

        byte[] buffer = new byte[1024];
        List<Byte> byteList = new ArrayList<>();
        new Thread(()->{
            while (isReceiving) {
                if (inputStream != null) {
                    try {
                        int numBytes = inputStream.read(buffer);
                        if (numBytes == -1) {
                            sendBroadcastData(BLUETOOTH_DISCONNECTED);
                            handleDisconnection();
                            break;
                        }

                        for (int i = 0; i < numBytes; i++) {
                            byteList.add(buffer[i]);
                        }

                        while (byteList.size() >= 12) {
                            if (byteList.get(0) == (byte) 255 && byteList.get(1) == (byte) 255) {
                                int payloadSize = byteList.get(2) & 0xFF;
                                if (byteList.size() >= 12) {
                                    int checksum = 255 + 255 + payloadSize;
                                    byte[] payload = new byte[8];
                                    for (int i = 0; i < 8; i++) {
                                        payload[i] = byteList.get(i + 3);
                                        checksum += payload[i] & 0xFF;
                                    }
                                    checksum = checksum % 256;
                                    if (checksum == (byteList.get(11) & 0xFF)) {
                                        short[] data = new short[4];
                                        ByteBuffer.wrap(payload).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(data);
//                                        sendBroadcastData(data);

                                        for (int i = 0; i < 12; i++) {
                                            byteList.remove(0);
                                        }
                                    }
                                }
                            } else {
                                byteList.remove(0);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading data", e);
                        System.out.println("from try");
                        sendBroadcastData(BLUETOOTH_DISCONNECTED);
                       handleDisconnection();
                        break;
                    }
                }
                else{
//                    showToast("inputstream is null");
                }
            }


        }).start();
    }

    private void handleDisconnection() {
        isReceiving = false;
        connected = 0;

        try {
            if (inputStream != null) {
                inputStream.close();
            }

            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing streams", e);
        }

    }
    private void sendBroadcastData(String action){
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(BluetoothService.this).sendBroadcast(intent);
    }
    private void sendBroadcastData(short[] data,Long time){
        Intent intent = new Intent("sendData");
        intent.putExtra("data",data);
        intent.putExtra("time",time);
        LocalBroadcastManager.getInstance(BluetoothService.this).sendBroadcast(intent);
    }
    private void sendBroadcastData(long time){
        Intent intent = new Intent("sendData");
        intent.putExtra("time",time);
        LocalBroadcastManager.getInstance(BluetoothService.this).sendBroadcast(intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
       handleDisconnection();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}