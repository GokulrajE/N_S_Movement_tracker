package com.example.imu;

import android.os.Environment;
import java.io.File;

public class FileHandling {
    public void createCSVFile() {
        if (isExternalStorageWritable() && isExternalStorageReadable()) {
            // Get the external storage directory
            File myFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "imupatientdata");

            // Check if the folder exists, if not, create it
            if (!myFolder.exists()) {
                myFolder.mkdirs();
            }

            // Now you can perform read and write operations within 'myFolder'
        } else {
            // External storage is not available or not writable
            System.out.println("Folder error");
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
