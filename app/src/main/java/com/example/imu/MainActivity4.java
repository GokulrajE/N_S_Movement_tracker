package com.example.imu;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;

public class MainActivity4 extends AppCompatActivity {


    private static final String TAG = "MainActivity4";

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayAdapter<String> bluetoothArrayAdapter;
    private boolean isReceiverRegistered = false;
    private static  final int Calibaration_data_size = 1000;
    private LineChart lineChart;
    private LineDataSet lineDataSet1;
    private ArrayList<Entry> chartEntries1;
    int sessionNumber;

    private GyroProcessor gyroProcessor;
    private String filename;
    private String Movementdir;
    private String insidedir;
    private String dirname;
    TextView movement;
    TextView parts;
    TextView angle;
    float gxsum=0;
    float gysum=0;
    float gzsum=0;
    float meanx=0;
    float meany=0;
    float meanz=0;
    String an_dis;
    File csvFile;
    private static final int CHART_UPDATE_BUFFER_SIZE = 100; // 50 data points

    private static final int CSV_WRITE_BUFFER_SIZE = 500; // 100 data points
   List<short[]>calibrationData;
    private List<Entry> chartDataBuffer = new ArrayList<>();
    private List<String> csvDataBuffer = new ArrayList<>();
    LineData lineData;
    String fname;

    boolean ToUpdate;
    boolean Toclear;
    boolean ToCalibrate;
    private BroadcastReceiver broadcastReceiver;
    double[] offset;
    boolean str_sto;
    private ToneGenerator toneGenerator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        gyroProcessor = new GyroProcessor();
        calibrationData = new ArrayList<>();
        Intent intent = getIntent();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        broadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                if("sendData".equals(intent.getAction())){
                    short[] data = intent.getShortArrayExtra("data");
                    Long time = intent.getLongExtra("time",0);
//                    Log.d(TAG,"received data"+Arrays.toString(data));
//                    System.out.println(time);
//                    Log.d(TAG,"received time"+time);

                    ReceiveData(data,time);
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("sendData"));

        // Check if the intent has the extra data
        if (intent != null && intent.hasExtra("selectedData")) {
            // Extract the data
            filename = intent.getStringExtra("selectedData");
            Log.d(TAG, "Selected data: " + filename);
        }
        dirname = filename.substring(0, 7); // Extract the first 7 characters

// Print to verify
        Log.d("MainActivity3", "First 7 characters: " + dirname);
        int selectedOption = getIntent().getIntExtra("selectedOption", 1);
        if(selectedOption<=6){
            insidedir="neck";
        }
        else{
            insidedir="shoulder";
        }
        Log.d(TAG,"selectedoption "+insidedir);
        if(selectedOption==1){
            Movementdir="Flexion";
        } else if (selectedOption==2) {
            Movementdir = "Extension";
        } else if (selectedOption==3) {
            Movementdir="Right Lateral Flexion";
        } else if (selectedOption==4) {
            Movementdir="Left Lateral Flexion";
        } else if (selectedOption==5) {
            Movementdir="Right Rotation";
        } else if (selectedOption==6) {
            Movementdir="Left Rotation";
        } else if (selectedOption==7) {
            Movementdir="Abduction Adduction";
        } else if (selectedOption==8) {
            Movementdir="Flexion Extension";
        } else if (selectedOption==9) {
            Movementdir="External Rotation";
        } else if (selectedOption==10) {
            Movementdir="Internal Rotation";
        }
        Log.d(TAG,"Movementdir "+Movementdir);
        int imageResource;
        switch (selectedOption) {
            case 1:
                imageResource = R.drawable.img1;
                break;
            case 2:
                imageResource = R.drawable.img2;
                break;
            case 3:
                imageResource = R.drawable.img3;
                break;
            case 4:
                imageResource = R.drawable.img4;
                break;
            case 5:
                imageResource = R.drawable.img5;
                break;
            case 6:
                imageResource = R.drawable.img6;
                break;
            case 7:
                imageResource = R.drawable.img6;
                break;
            case 8:
                imageResource = R.drawable.img6;
                break;
            case 9:
                imageResource = R.drawable.img6;
                break;
            case 10:
                imageResource = R.drawable.img6;
                break;

            default:
                imageResource = R.drawable.img1;
        }

        ImageView imageView = findViewById(R.id.pic);
        imageView.setImageResource(imageResource);
       sessionNumber = MainActivity.session_number;
        Button start_stop = findViewById(R.id.but);
        start_stop.setBackgroundColor(Color.rgb(76,175,80));
//        Button stop = findViewById(R.id.stop);
        Button clear = findViewById(R.id.clear);
        movement = findViewById(R.id.text1);
        parts = findViewById(R.id.text2);
        angle = findViewById(R.id.text3);
        str_sto = true;
        start_stop.setOnClickListener(v -> {
                        if(str_sto) {
                            ToCalibrate=true;
                            offset = loadCalibrationData();
                            fname = createfilename();
                            ToUpdate = true;
                            gyroProcessor.setupQInt();
                            if (lineChart != null && lineDataSet1 != null) {
                                lineChart.clear();
                                lineDataSet1.clear();
                                chartEntries1.clear();
                                chartDataBuffer.clear();
                            }
                            start_stop.setBackgroundColor(Color.RED);
                            start_stop.setText("stop");
                            str_sto = false;
                        }else{
                            ToUpdate = false;
                            Toclear = true;

                            if(lineData!=null){
                                cardDisplay();
                                writeAssessmentData();
                                mxvalue=0;
                            }
                            start_stop.setBackgroundColor(Color.rgb(76,175,80));
                            start_stop.setText("start");
                            str_sto = true;
                        }
                });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Toclear){
                    if (lineChart != null && lineDataSet1 != null) {
                        lineChart.clear();
                        lineDataSet1.clear();
                        chartEntries1.clear();
                        chartDataBuffer.clear();
                        movement.setText("movement");
                        parts.setText("Parts");
                        angle.setText("Angle");
                        Toclear = false;
                    }

                }
            }
        });
       System.out.println(MainActivity.session_number);

        bluetoothArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        // Initialize the chart
        lineChart = findViewById(R.id.lineChart);
        chartEntries1 = new ArrayList<>();
        // Customize the chart
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
       leftAxis.setAxisMaximum(200f);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        rightAxis.setDrawGridLines(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.invalidate();
    }

    private  void cardDisplay(){
        movement.setText(Movementdir);
        parts.setText(insidedir);
        parts.setAllCaps(true);
        float anglev = lineData.getYMax();
        an_dis = String.format("%.2f", anglev) ;
        angle.setText(an_dis);

    }
    private String currentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(new Date());
    }


    float mxvalue=0;
   private void ReceiveData(short[] data,long time){
       if(ToUpdate){
           float gx = data[0];
           float gy = data[1];
           float gz = data[2];

           float gyroAng = gyroProcessor.rom(data, offset,time);
           if(ToCalibrate) {
               float x = (float) (gx/65.5);
               float y = (float) (gy/65.5);
               float z = (float) (gz/65.5);
               gxsum += x * x;
               meanx += x;
               gysum += y * y;
               meany += y;
               gzsum += z * z;
               meanz += z;
              calibrationData.add(data);
              if(calibrationData.size()==Calibaration_data_size){
                  float P_M_gx = meanx/Calibaration_data_size;
                  float P_M_gy = meany/Calibaration_data_size;
                  float P_M_gz = meanz/Calibaration_data_size;
                  float sdx = (gxsum/Calibaration_data_size) - (P_M_gx * P_M_gx);
                  float sdy = (gysum/Calibaration_data_size) - (P_M_gy * P_M_gy);
                  float sdz = (gzsum/Calibaration_data_size) - (P_M_gz * P_M_gz);
                  float max = Math.max(Math.max(sdx,sdy),sdz);
                  if(max<1){
                      offset = calculateAndStoreCalibrationData(calibrationData);
                  }
               calibrationData.clear();
               gxsum =0;
               meanx =0;
               gysum =0;
               meany =0;
               gzsum =0;
               meanz =0;
               ToCalibrate = false;
//                  TONE_CDMA_PIP
               toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 150);
              }
           }
           chartDataBuffer.add(new Entry(mxvalue, gyroAng));
           csvDataBuffer.add(gx + "," + gy + "," + gz + "," + gyroAng + "\n");
           mxvalue++;
           updateChart();
           writeCSVData();
       }

   }
    private  String createfilename(){
        File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
        File dirnameFolder = new File(mainFolder, dirname);
        File Raw_data_folder = new File(dirnameFolder,"Raw_Data");
        File insideDirFolder = new File(Raw_data_folder, insidedir);
        File movementFolder= new File(insideDirFolder, Movementdir);
        File[] files = movementFolder.listFiles();
        int size = files.length+1;
        String filename = "data"+size+".csv";
        return filename;
    }

    private void writeAssessmentData(){
       new Thread(()->{
           File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
           File dirnameFolder = new File(mainFolder, dirname);
           File AssementFile = new File(dirnameFolder,"Assessment.csv");
           String date = currentDate();
           String type = insidedir+"/"+Movementdir;
           String file_path = csvFile.getAbsolutePath();
          String data = sessionNumber+","+date+","+type+","+file_path+","+an_dis;
          Log.d("Assessment data",data);
           try (FileWriter writer = new FileWriter(AssementFile, true)) {
               writer.append(data).append("\n");
               writer.flush();
               writer.close();
               Log.i(TAG, "Assessment_Data appended to CSV");
           } catch (IOException e) {
               Log.e(TAG, "Error Assessment Data writing to CSV file", e);
           }
       }).start();
    }

    private void writeCSVData() {
        if (csvDataBuffer.size() >= CSV_WRITE_BUFFER_SIZE) {
            List<String> dataToWrite = new ArrayList<>(csvDataBuffer);
            csvDataBuffer.clear();

            new Thread(() -> {
                File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
                File dirnameFolder = new File(mainFolder, dirname);
                File Raw_data_folder = new File(dirnameFolder,"Raw_Data");
                File insideDirFolder = new File(Raw_data_folder, insidedir);
                File movementFolder= new File(insideDirFolder, Movementdir);
                 csvFile=new File(movementFolder,fname);

                if (!csvFile.exists()) {

                    try {
                        csvFile.createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG, "Error creating CSV file", e);
                        return;
                    }
                }

                try (FileWriter writer = new FileWriter(csvFile, true)) {
                    for (String data : dataToWrite) {
                        writer.append(data);
                    }
                    writer.flush();
                    Log.i(TAG, "Data appended to CSV");
                } catch (IOException e) {
                    Log.e(TAG, "Error writing to CSV file", e);
                }
           }).start();
        }
//        csvWriteHandler.postDelayed(csvWriteRunnable, CSV_WRITE_INTERVAL);
    }
    private double[] calculateAndStoreCalibrationData(List<short[]> calibrationData) {
        long sumGx = 0, sumGy = 0, sumGz = 0;
        double[] offset = {0, 0, 0};
        for (short[] data : calibrationData) {
            sumGx += data[0];
            sumGy += data[1];
            sumGz += data[2];
        }

        float meanGx = sumGx / 1000.0f;
        float meanGy = sumGy / 1000.0f;
        float meanGz = sumGz / 1000.0f;
        offset[0] = meanGx;
        offset[1] = meanGy;
        offset[2] = meanGz;
  return offset;
    }

    private double[] loadCalibrationData() {
        double[] offset = {0, 0, 0};
        try {
            String device_Address = MainActivity3.device();
            File internalStorage = getFilesDir();
            String filename = device_Address+".json";
           File folder = new File(internalStorage,device_Address);
           if(folder.exists()){
               File file = new File(folder,filename);
               if (file.exists()){
                   FileInputStream fis = openFileInput(filename);
                   byte[] buffer = new byte[fis.available()];
                   fis.read(buffer);
                   fis.close();

                   String jsonString = new String(buffer, "UTF-8");
                   JSONObject jsonObject = new JSONObject(jsonString);
                   System.out.println("from start");
                   System.out.println(jsonString);

                   offset[0] = jsonObject.getDouble("meanGx");
                   offset[1] = jsonObject.getDouble("meanGy");
                   offset[2] = jsonObject.getDouble("meanGz");
                   System.out.println(Arrays.toString(offset));
               }
           }

        } catch (Exception e) {
            Log.e(TAG, "Error loading calibration data", e);
        }
        return offset;
    }


    private void updateChart() {
        if (chartDataBuffer.size() >= CHART_UPDATE_BUFFER_SIZE) {
            runOnUiThread(() -> {
                List<Entry> newEntries = new ArrayList<>(chartDataBuffer);
                chartDataBuffer.clear();

                chartEntries1.addAll(newEntries);
                lineDataSet1 = new LineDataSet(chartEntries1, "IMU Data");
                 lineData = new LineData(lineDataSet1);

                lineDataSet1.setLineWidth(2);
                lineDataSet1.setDrawCircles(false);
                lineDataSet1.setColor(Color.rgb(255,193,7));
                lineChart.setData(lineData);
                lineChart.invalidate();
            });
        }
//        chartUpdateHandler.postDelayed(chartUpdateRunnable, CHART_UPDATE_INTERVAL);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered) {
            unregisterReceiver(myReceiver);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

    }

    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity4.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity4.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (device != null && device.getName() != null) {
                    deviceList.add(device);
                    bluetoothArrayAdapter.add(device.getName());
                    bluetoothArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity4.this, message, Toast.LENGTH_SHORT).show());
    }
}