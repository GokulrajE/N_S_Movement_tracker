package com.example.imu;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private ViewModel viewModel;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private String deviceAddress;
    boolean isReceiving;
    int connected =0;
    public static final String BLUETOOTH_CONNECTED="DEVICE_CONNECTED";
    public static final String BLUETOOTH_DISCONNECTED="DEVICE_DISCONNECTED";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    public void onCreate() {
        super.onCreate();

//      viewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ViewModel.class);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BluetoothDevice device = intent.getParcelableExtra("device_address");
        if (device != null) {
            connectToDevice(device);
            Log.e("connection","connect"+device.getAddress());
        }
        else{
            System.out.println("address is null");
        }
        return START_STICKY;
    }

    public void connectToDevice(BluetoothDevice device) {

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
                                        sendBroadcastData(data);

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
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void sendBroadcastData(short[] data){
        Intent intent = new Intent("sendData");
        intent.putExtra("data",data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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