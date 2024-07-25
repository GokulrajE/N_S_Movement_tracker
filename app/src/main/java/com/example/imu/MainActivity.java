package com.example.imu;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static int session_number;
    private AutoCompleteTextView autoCompleteTextViewUniqueId;
    private List<String> Ids = new ArrayList<>();
    private ArrayAdapter<String> adapterUniqueId;
    private Button saveButton;
    private String selectedUniqueId;
    List<String> menu_N;
    List<String> menu_S;
    private Button reviewbutton;
    private Button assessment;
    public static final int YOUR_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Request manage all files access permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        FileHandling filehandler = new FileHandling();
        filehandler.createCSVFile();
        autoCompleteTextViewUniqueId = findViewById(R.id.myEditText);
        saveButton = findViewById(R.id.Create_id);
        assessment = findViewById(R.id.start_assessment);
        reviewbutton = findViewById(R.id.review);
        menu_N = new ArrayList<>();
        menu_S = new ArrayList<>();
        Ids = new ArrayList<>();

        // Read uniqueIds from idfile.csv and populate suggestions
        readUniqueIdFromFile();

        adapterUniqueId = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Ids);
        autoCompleteTextViewUniqueId.setAdapter(adapterUniqueId);

        autoCompleteTextViewUniqueId.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Handle item selection (if needed)
                selectedUniqueId = adapterView.getItemAtPosition(position).toString();
                closeKeyboard();
                // Uncomment the next line if you want to read data when an item is selected
                // readDataFromJson(selectedUniqueId);
                //sendDataToActivity(selectedUniqueId);
            }
        });


        // Add a TextWatcher to enable/disable the button based on text length
        autoCompleteTextViewUniqueId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveButton.setEnabled(editable.length() == 0);
            }
        });

        adapterUniqueId.notifyDataSetChanged();

        // Set OnClickListener for saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add code to start MainActivity2
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);

                // Pass a unique identifier or any necessary data to identify MainActivity
                intent.putExtra("MainActivityIdentifier", "uniqueIdentifier");

                startActivityForResult(intent, YOUR_REQUEST_CODE);
            }
        });
        assessment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Check if selectedUniqueId is not null
                    if (Ids.contains(selectedUniqueId)) {
                        // Send data to MainActivity3
                        sendDataToActivity(selectedUniqueId);
                        autoCompleteTextViewUniqueId.getText().clear();
                        session();
                    } else {
                        // Handle case where selectedUniqueId is null
                        Toast.makeText(MainActivity.this, "Please select a valid unique ID", Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        });
        reviewbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             if(Ids.contains(selectedUniqueId)){
                 getAssessmentData();

             }
             else{
                 Toast.makeText(MainActivity.this, "Please select a valid unique ID", Toast.LENGTH_SHORT).show();
             }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String newUniqueId = data.getStringExtra("NewUniqueId");
                if (newUniqueId != null) {
                    autoCompleteTextViewUniqueId.setText(newUniqueId);
                    updateAutoCompleteSuggestions(); // Reload array adapter
                }
            }
        }
    }

    private void readUniqueIdFromFile() {
        File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
        File secondaryFolder = new File(mainFolder, "json file");
        File uniqueIdFile = new File(secondaryFolder, "idfile.csv");

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(uniqueIdFile));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                // Add each uniqueId to the suggestions list
                Ids.add(line);
            }

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    // After adding a new unique ID, call this method to update the AutoCompleteTextView
    public void updateAutoCompleteSuggestions() {
        setupAutoCompleteTextView();
    }
    private void setupAutoCompleteTextView() {
        autoCompleteTextViewUniqueId.setAdapter(null); // Clear the existing adapter
        Ids.clear(); // Clear the existing list
        // Read uniqueIds from idfile.csv and populate suggestions
        readUniqueIdFromFile();
        adapterUniqueId = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Ids);
        autoCompleteTextViewUniqueId.setAdapter(adapterUniqueId);
        adapterUniqueId.notifyDataSetChanged();
    }
    private void session(){
        new Thread(() -> {
            File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
            String dirname = selectedUniqueId.substring(0, 7);
            File dirnameFolder = new File(mainFolder, dirname);
            File AssesmentFile = new File(dirnameFolder,"Assessment.csv");
            if (AssesmentFile.exists()) {
              readSessionNumber(AssesmentFile);

            }
            else{
                try {
                    AssesmentFile.createNewFile();
                    session_number = 1;
                } catch (IOException e) {
                    Log.e(TAG, "Error creating CSV file", e);
                    return;
                }
            }


        }).start();
    }
public void readSessionNumber(File AssessmentFile){
    try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(AssessmentFile));
        String line;

        session_number = 1;
        while ((line = bufferedReader.readLine()) != null) {
            // Add each uniqueId to the suggestions list
            String lastline = line;
            String [] parts = lastline.split(",");
            session_number = Integer.parseInt(parts[0])+1;
        }


        bufferedReader.close();

    } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(this, "Error reading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
    public void getAssessmentData(){
        new Thread(()->{
            File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
            String dirname = selectedUniqueId.substring(0, 7);
            File dirnameFolder = new File(mainFolder, dirname);
            File AssesmentFile = new File(dirnameFolder,"Assessment.csv");
            System.out.println(dirname);
            if (AssesmentFile.exists()) {
                try  {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(AssesmentFile));
                    String line;
                    boolean check_parts_N;
                    boolean check_parts_S;

                    while ((line = bufferedReader.readLine()) != null) {
                        // Add each uniqueId to the suggestions list
                        String [] parts = line.split(",");
                        String [] type = parts[2].split("/");
                        String part = type[0];
                        String movement = type[1];
                        check_parts_N = part.equals("neck");
                        check_parts_S = part.equals("shoulder");
                        if(check_parts_N){
                            if(!menu_N.contains(movement))
                                menu_N.add(movement);
                        } else if (check_parts_S) {
                            if(!menu_S.contains(movement))
                                menu_S.add(movement);

                        }

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!menu_N.isEmpty() || !menu_S.isEmpty()) {
                                review(selectedUniqueId, menu_N, menu_S);
                                menu_S.clear();
                                menu_N.clear();
                            }else{
                                Toast.makeText(getApplicationContext(),"New_user",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    bufferedReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error reading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"New_user",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).start();
    }



public void sendDataToActivity(String data) {
        Intent intent = new Intent(this, MainActivity3.class);
        intent.putExtra("selectedData", data);

        startActivity(intent);
    }
    public void review(String data, List<String> meanu_n, List<String> meanu_s){
        Intent intent=new Intent(this, review.class);
        intent.putExtra("selectedData", data);
        intent.putStringArrayListExtra("neckData", (ArrayList<String>) meanu_n);
        intent.putStringArrayListExtra("shoulderData", (ArrayList<String>) meanu_s);
        startActivity(intent);
    }
    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

}