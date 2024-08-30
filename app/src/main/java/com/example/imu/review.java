package com.example.imu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class review extends AppCompatActivity {
     private String receivedData;
     LinearLayout menuLayout_s;
     LinearLayout menuLayout_N;
     List<String> menu_N;
     List<String> menu_S;
     Boolean newSessionN  = true;
     Boolean newSessionS = true;
     Boolean isMenuVisiblen = false;
     Boolean isMenuVisibles = false;
     String [] menu;
     RelativeLayout parentLayoutNeck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main5);
        menu_N = new ArrayList<>();
        menu_S = new ArrayList<>();
        Intent intent = getIntent();
        // Check if the intent has the extra data
        if (intent != null && intent.hasExtra("selectedData")) {
            // Extract the data
            receivedData = intent.getStringExtra("selectedData");
            Log.d("menuactivity", "Selected data: " + receivedData);
        }
        if (intent != null && intent.hasExtra("neckData")) {
            // Extract the data
            ArrayList<String> receviedlistn = intent.getStringArrayListExtra("neckData");
            for(String item : receviedlistn){
                System.out.println(item);
                menu_N.add(item);
            }
            Log.d("menuactivity", "Selected data: " + receviedlistn);
        }
        if (intent != null && intent.hasExtra("shoulderData")) {
            // Extract the data
            ArrayList<String> receviedlists = intent.getStringArrayListExtra("shoulderData");
            for(String item : receviedlists){
                System.out.println(item);
                menu_S.add(item);
            }
            Log.d("menuactivity", "Selected data: " + receviedlists);
        }
        menuLayout_N = findViewById(R.id.menuLayoutn);
        menuLayout_s = findViewById(R.id.menuLayouts);
        TextView neckTextView = findViewById(R.id.neck);
        TextView shoulderTextView = findViewById(R.id.shoulder);
        TextView name = findViewById(R.id.name);
        name.setText(receivedData);
        neckTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMenuVisibles){
                    isMenuVisibles = false;
                    closeMenu(menuLayout_s);
                }
                if(newSessionN){
                    createMenuOptions(menu_N,menuLayout_N);
                    newSessionN = false;
                    isMenuVisiblen = true;
                }
                else if(isMenuVisiblen){
                    isMenuVisiblen = false;
                    closeMenu(menuLayout_N);
                }else{
                    openMenu(menuLayout_N);
                    isMenuVisiblen = true;
                }
            }
        });
        shoulderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMenuVisiblen){
                    isMenuVisiblen = false;
                    closeMenu(menuLayout_N);
                }
                   if(newSessionS){
                       createMenuOptions(menu_S,menuLayout_s);
                       newSessionS = false;
                   }else if (isMenuVisibles) {
                       isMenuVisibles = false;
                       closeMenu(menuLayout_s);
                   }
                   else {
                       openMenu(menuLayout_s);
                       isMenuVisibles = true;
                        }
            }
        });

    }
    private void closeMenu(LinearLayout menulayout) {
        menulayout.setVisibility(View.GONE);
    }
    private void openMenu(LinearLayout menulayout) {
        menulayout.setVisibility(View.VISIBLE);
    }
    private void createMenuOptions(List<String> menuOptions , LinearLayout menulayout) {

        for (String option : menuOptions) {
            View menuItemView = getLayoutInflater().inflate(R.layout.movements, menulayout, false);
            TextView menuItemText = menuItemView.findViewById(R.id.menuItemText);
            menuItemText.setText(option);
            menuItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle the click event for the menu_N item
                    getGraphData(option);
                    // You can also perform other actions here
                }
            });
            menulayout.addView(menuItemView);
        }

        menulayout.setVisibility(View.VISIBLE);
    }
    private void showGraphDialog(String option,List<String> lable,List<Entry> entries,List<Entry> Scatterentries) {
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.graph, null);

        // Initialize the LineChart
        CombinedChart lineChart = dialogView.findViewById(R.id.lineChart);
        setupGraph(lineChart,lable,entries,Scatterentries);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set the cancel button listener
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
       TextView title = dialogView.findViewById(R.id.title);
       title.setText(option);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });

        dialog.show(); // Show the dialog
    }
    private void getGraphData(String option){
        new Thread(()->{
            List<String> labels = new ArrayList<>();
            List<Entry> entries = new ArrayList<>();
            List<Entry> scatterdata = new ArrayList<>();
            Map<Integer, List<Float>> angleMap = new HashMap<>();
            int mxvalue = 0;

            File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
            String dirname = receivedData.substring(0, 7);
            File dirnameFolder = new File(mainFolder, dirname);
            File AssesmentFile = new File(dirnameFolder,"Assessment.csv");
            if (AssesmentFile.exists()) {
                try  {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(AssesmentFile));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        // Add each uniqueId to the suggestions list
                        String [] parts = line.split(",");
                        String [] type = parts[2].split("/");
                        String movement = type[1];
                        float y = Float.parseFloat(parts[4]);
                        if (movement.equals(option)) {
                            if(labels.contains(parts[1])){
                                int x_value = labels.indexOf(parts[1]);
                                scatterdata.add(new Entry(x_value,y));
                                Objects.requireNonNull(angleMap.get(x_value)).add(y);
                            }
                            else {
                               labels.add(parts[1]);
                                scatterdata.add(new Entry(mxvalue,y));
                                angleMap.putIfAbsent( mxvalue, new ArrayList<>());
                                Objects.requireNonNull(angleMap.get(mxvalue)).add(y);
                                mxvalue ++;
                            }

                        }

                    }
                    for (Map.Entry<Integer, List<Float>> entry : angleMap.entrySet()) {
                        long xValue = entry.getKey();
                        List<Float> angles = entry.getValue();
                        float median = calculateMedian(angles);
                        entries.add(new Entry(xValue, median)); // Use the same X value for the line entry
                    }
                    System.out.println(labels);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showGraphDialog(option,labels,entries,scatterdata);
                        }
                    });
                    bufferedReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error reading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }).start();

    }
    private float calculateMedian(List<Float> angles) {
        int size = angles.size();
        Collections.sort(angles);
        if (size % 2 == 0) {
            return (angles.get(size / 2 - 1) + angles.get(size / 2)) / 2;
        } else {
            return angles.get(size / 2);
        }
    }
    private void setupGraph(CombinedChart lineChart,List<String> lable,List<Entry> entries,List<Entry> scatterEntries) {
        XAxis xAxis = lineChart.getXAxis();
        lineChart.getDescription().setEnabled(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(360f);
        leftAxis.setAxisMinimum(-1f);
        xAxis.setAxisMinimum(-0.2f); // Set minimum value to create space on the left
        xAxis.setAxisMaximum(entries.size() - 0.2f); // Set maximum value to create space on the right
        leftAxis.setGranularity(1f); // Set granularity to prevent overlapping
        lineChart.setExtraOffsets(10, 10, 10, 10); // (left, top, right, bottom)
        leftAxis.setDrawLabels(true);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        rightAxis.setDrawGridLines(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        ScatterDataSet scatterDataSet = new ScatterDataSet(scatterEntries, "");
        scatterDataSet.setColors(Color.BLUE);
        scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        scatterDataSet.setScatterShapeSize(30);
        scatterDataSet.setDrawValues(false);
        scatterDataSet.setScatterShapeHoleRadius(3);
        scatterDataSet.setScatterShapeHoleColor(Color.YELLOW);
        LineDataSet dataSet = new LineDataSet(entries, " ");
        dataSet.setColor(Color.rgb(192,107,172));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.GREEN);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        LineData lineData = new LineData(dataSet);
        ScatterData scatterData = new ScatterData(scatterDataSet);
        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);
        combinedData.setData(scatterData);
        lineChart.setData(combinedData);
        xAxis.setValueFormatter(new XAxisValueFormatter(lable));
        lineChart.invalidate(); // Refresh the chart
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}