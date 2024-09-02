package com.example.imu;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Random;

public class MainActivity2 extends AppCompatActivity {

    private Button saveButton;
    private Spinner genderSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        saveButton=findViewById(R.id.Create_id);
        genderSpinner = findViewById(R.id.gender1);
// Define an array of gender options
        String[] genders = {"select gender","Male", "Female", "Others"};

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        genderSpinner.setAdapter(adapter);

        final File mainFolder;
        mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");
        File secondaryFolder = new File(mainFolder, "json file");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToJson();
            }
        });

        // Retrieve the identifier from the intent
        Intent intent = getIntent();
        String mainActivityIdentifier = intent.getStringExtra("MainActivityIdentifier");

        // Now you have the identifier, and you can use it as needed
    }
    private void saveDataToJson() {
        EditText nameEditText = findViewById(R.id.name1);
        EditText ageEditText = findViewById(R.id.age1);

        EditText conditionEditText = findViewById(R.id.condition1);

        String name = nameEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString().trim();
        String condition = conditionEditText.getText().toString().trim();

        // Validate user input
        if (name.isEmpty() || age.isEmpty() || gender.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!gender.equals("Male")&&!gender.equals("Female")&&!gender.equals("Others")){
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show();
            return;
        }
        // Generate a unique id
        final String uniqueId = UniqueIdGenerator.generateUniqueId();

        // Create a JSON object
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("age", age);
            jsonObject.put("gender", gender);
            jsonObject.put("condition", condition);
            jsonObject.put("uniqueId", uniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON object", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the main folder
        final File mainFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");

        if (!mainFolder.exists() && !mainFolder.mkdirs()) {
            Toast.makeText(this, "Error creating main folder", Toast.LENGTH_SHORT).show();
            return;
        }
        File secondaryFolder = new File(mainFolder, "json file");
        if (!secondaryFolder.exists()) {
            secondaryFolder.mkdirs();
        }
        File jsonFile = new File(secondaryFolder, uniqueId + ".json");
        try {
            FileWriter fileWriter = new FileWriter(jsonFile);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();
            // Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
        }
        File uniqueIdFile=new File(secondaryFolder,"idfile.csv");
        try {
            FileWriter csvWriter = new FileWriter(uniqueIdFile,true);
            csvWriter.append(uniqueId).append('-').append(name).append("\n");
            csvWriter.flush();
            csvWriter.close();
            // Toast.makeText(this, "Test data written successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error writing test data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        // Create a folder with the uniqueId
        File patientFolder = new File(mainFolder, uniqueId);
        if (!patientFolder.exists() && !patientFolder.mkdirs()) {
            Toast.makeText(this, "Error creating patient folder", Toast.LENGTH_SHORT).show();
            return;
        }

// Create folders for neck and shoulder inside the patient's folder
        File RawFolder = new File (patientFolder, "Raw_Data");
        File AssessmentFile = new File(patientFolder,"Assessment.csv");
        if(!AssessmentFile.exists()){
           System.out.println("assessment file not created");
        }

        File neckFolder = new File(RawFolder, "neck");
        File shoulderFolder = new File(RawFolder, "shoulder");

        if (!neckFolder.exists() && !neckFolder.mkdirs()) {
            Toast.makeText(this, "Error creating neck folder", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!shoulderFolder.exists() && !shoulderFolder.mkdirs()) {
            Toast.makeText(this, "Error creating shoulder folder", Toast.LENGTH_SHORT).show();
            return;
        }
        //create movement folders
        File Flexion=new File(neckFolder,"Flexion");
        File Extension=new File(neckFolder,"Extension");
        File Right_Lateral_Flexion = new File(neckFolder,"Right Lateral Flexion");
        File Left_Lateral_Flexion = new File(neckFolder,"Left Lateral Flexion");
        File Right_Rotation = new File(neckFolder,"Right Rotation");
        File Left_Rotation = new File(neckFolder,"Left Rotation");
        if(!Flexion.exists()&&!Flexion.mkdirs()){
            Toast.makeText(this, "Error creating movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Extension.exists()&&!Extension.mkdirs()){
            Toast.makeText(this, "Error creating movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Right_Lateral_Flexion.exists()&&!Right_Lateral_Flexion.mkdirs()){
            Toast.makeText(this, "Error Creating movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Left_Lateral_Flexion.exists()&&!Left_Lateral_Flexion.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Right_Rotation.exists()&&!Right_Rotation.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Left_Rotation.exists()&&!Left_Rotation.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }

        File Abduction_Adduction=new File(shoulderFolder,"Abduction Adduction");
        File Flexion_Extension=new File(shoulderFolder,"Flexion Extension");
        File External_Rotation = new File(shoulderFolder,"External Rotation");
        File Internal_Rotation = new File(shoulderFolder,"Internal Rotation");

        if(!Abduction_Adduction.exists()&&!Abduction_Adduction.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Flexion_Extension.exists()&&!Flexion_Extension.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!External_Rotation.exists()&&!External_Rotation.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Internal_Rotation.exists()&&!Internal_Rotation.mkdirs()){
            Toast.makeText(this, "Error creating Movement folder", Toast.LENGTH_SHORT).show();
            return;
        }



// Show a toast with the patient's unique ID
        Toast.makeText(this, "Your Patient ID: " + uniqueId, Toast.LENGTH_LONG).show();

        // Clear input fields
        nameEditText.getText().clear();
        ageEditText.getText().clear();
        genderSpinner.setSelection(0);
        conditionEditText.getText().clear();
        // Add the new uniqueId to the suggestion list

        if (!uniqueId.isEmpty()) {
            Intent intent = new Intent();
            intent.putExtra("NewUniqueId", uniqueId+'-'+name);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }


    }

    // Function to create a CSV file
    private void createCSVFile(File folder, String fileName) {
        File csvFile = new File(folder, fileName);
        try {
            if (!csvFile.exists()) {
                csvFile.createNewFile();
                // Now you can write data to this file if needed
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating CSV file " + fileName, Toast.LENGTH_SHORT).show();
        }
    }

    public static class UniqueIdGenerator {
        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static String generateUniqueId() {
            // Generate a random 6-digit number
            String randomNumber = generateRandomNumber();

            // Generate a random alphabet character (capital)
            char randomLetter = generateRandomLetter();

            return randomNumber + randomLetter;
        }

        private static String generateRandomNumber() {
            Random random = new Random();
            StringBuilder randomNumber = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                randomNumber.append(random.nextInt(10)); // generate a random digit (0-9)
            }

            return randomNumber.toString();
        }

        private static char generateRandomLetter() {
            Random random = new Random();
            return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
    }
}



