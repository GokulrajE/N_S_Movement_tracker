package com.example.imu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class menuActivity extends AppCompatActivity {
    private View optionsViewNeck;
    private View optionsViewShoulder;
    private boolean neckOptionsVisible = false;
    private boolean shoulderOptionsVisible = false;
    private String receivedData; // Variable to store the received data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ViewGroup rootLayout = findViewById(R.id.main);
        Intent intent = getIntent();

        // Check if the intent has the extra data
        if (intent != null && intent.hasExtra("selectedData")) {
            // Extract the data
            receivedData = intent.getStringExtra("selectedData");
            Log.d("menuactivity", "Selected data: " + receivedData);
        }
        TextView neckTextView = findViewById(R.id.neck);
        TextView shoulderTextView = findViewById(R.id.shoulder);
        final RelativeLayout parentLayoutNeck = findViewById(R.id.neck1);
        final RelativeLayout parentLayoutShoulder = findViewById(R.id.shoulder1);
        final LayoutInflater inflater = LayoutInflater.from(this);
        optionsViewNeck = inflater.inflate(R.layout.layout, parentLayoutNeck, false);
        optionsViewShoulder = inflater.inflate(R.layout.layout2, parentLayoutShoulder, false);
        neckTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!neckOptionsVisible) {
                    showOptions(parentLayoutNeck, optionsViewNeck, v.getId());
                    neckOptionsVisible = true;
                    shoulderOptionsVisible = false;
                    parentLayoutShoulder.removeView(optionsViewShoulder);
                } else {
                    parentLayoutNeck.removeView(optionsViewNeck);
                    neckOptionsVisible = false;
                }
            }
        });

        shoulderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shoulderOptionsVisible) {
                    showOptions(parentLayoutShoulder, optionsViewShoulder, v.getId());
                    shoulderOptionsVisible = true;
                    neckOptionsVisible = false;
                    parentLayoutNeck.removeView(optionsViewNeck);
                } else {
                    parentLayoutShoulder.removeView(optionsViewShoulder);
                    shoulderOptionsVisible = false;
                }
            }
        });
        handleOptionClick(optionsViewNeck, R.id.option1, 1);
        handleOptionClick(optionsViewNeck, R.id.option2, 2);
        handleOptionClick(optionsViewNeck, R.id.option3, 3);
        handleOptionClick(optionsViewNeck,R.id.option4,4);
        handleOptionClick(optionsViewNeck,R.id.option5,5);
        handleOptionClick(optionsViewNeck,R.id.option6,6);

        // Handle clicks for options inside the shoulder inflated layout
        handleOptionClick(optionsViewShoulder, R.id.option7, 7);
        handleOptionClick(optionsViewShoulder, R.id.option8, 8);
        handleOptionClick(optionsViewShoulder, R.id.option9, 9);
        handleOptionClick(optionsViewShoulder, R.id.option10, 10);

        // Set a touch listener on the root layout to hide the options view
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (neckOptionsVisible) {
                    parentLayoutNeck.removeView(optionsViewNeck);
                    neckOptionsVisible = false;
                }
                if (shoulderOptionsVisible) {
                    parentLayoutShoulder.removeView(optionsViewShoulder);
                    shoulderOptionsVisible = false;
                }
                return false;
            }
        });



    }
    private void showOptions(RelativeLayout parentLayout, View optionsView, int anchorId) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.BELOW, anchorId);
        parentLayout.addView(optionsView, params);
    }

    // Method to handle option clicks
    private void handleOptionClick(View optionsView, int optionId, final int selectedOption) {
        TextView option = optionsView.findViewById(optionId);
        option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity4(selectedOption);
            }
        });
    }
    private void startMainActivity4(int selectedOption) {
        Intent intent = new Intent(menuActivity.this, MainActivity4.class);
        intent.putExtra("selectedOption", selectedOption);
        intent.putExtra("selectedData", receivedData); // Pass the received data
        startActivity(intent);
    }
}