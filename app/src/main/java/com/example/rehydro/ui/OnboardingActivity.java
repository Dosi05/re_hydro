package com.example.rehydro.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.rehydro.R;
import com.example.rehydro.data.prefs.UserPrefs;

public class OnboardingActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etWeight;
    private EditText etHeight;
    private EditText etAge;
    private TextView btnMale;
    private TextView btnFemale;
    private Button btnLetsGo;

    private String selectedGender = "M";
    private UserPrefs userPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw behind status bar
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(), false);

        setContentView(R.layout.activity_onboarding);

        userPrefs = new UserPrefs(this);

        bindViews();
        setupGenderButtons();
        setupLetsGoButton();
    }

    private void bindViews() {
        etName     = findViewById(R.id.etName);
        etWeight   = findViewById(R.id.etWeight);
        etHeight   = findViewById(R.id.etHeight);
        etAge      = findViewById(R.id.etAge);
        btnMale    = findViewById(R.id.btnMale);
        btnFemale  = findViewById(R.id.btnFemale);
        btnLetsGo  = findViewById(R.id.btnLetsGo);
    }

    private void setupGenderButtons() {
        btnMale.setOnClickListener(v -> selectGender("M"));
        btnFemale.setOnClickListener(v -> selectGender("F"));
    }

    private void selectGender(String gender) {
        selectedGender = gender;

        if ("M".equals(gender)) {
            btnMale.setBackground(
                    getDrawable(R.drawable.radio_selected_background));
            btnMale.setTextColor(Color.parseColor("#5ab4ff"));
            btnFemale.setBackground(
                    getDrawable(R.drawable.radio_unselected_background));
            btnFemale.setTextColor(Color.parseColor("#444466"));
        } else {
            btnFemale.setBackground(
                    getDrawable(R.drawable.radio_selected_background));
            btnFemale.setTextColor(Color.parseColor("#5ab4ff"));
            btnMale.setBackground(
                    getDrawable(R.drawable.radio_unselected_background));
            btnMale.setTextColor(Color.parseColor("#444466"));
        }
    }

    private void setupLetsGoButton() {
        btnLetsGo.setOnClickListener(v -> {
            if (!validateFields()) return;

            String name  = etName.getText()
                    .toString().trim();
            float weight = Float.parseFloat(
                    etWeight.getText().toString().trim());
            int height   = Integer.parseInt(
                    etHeight.getText().toString().trim());
            int age      = Integer.parseInt(
                    etAge.getText().toString().trim());

            // Save profile
            userPrefs.saveProfile(
                    name, weight, height, age, selectedGender);

            // Launch main app
            Intent intent = new Intent(
                    this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateFields() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Please enter your name");
            return false;
        }
        if (etWeight.getText().toString().trim().isEmpty()) {
            etWeight.setError("Please enter your weight");
            return false;
        }
        if (etHeight.getText().toString().trim().isEmpty()) {
            etHeight.setError("Please enter your height");
            return false;
        }
        if (etAge.getText().toString().trim().isEmpty()) {
            etAge.setError("Please enter your age");
            return false;
        }
        return true;
    }
}