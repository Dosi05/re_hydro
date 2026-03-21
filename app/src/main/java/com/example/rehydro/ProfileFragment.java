package com.example.rehydro;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // ── ViewModel + Prefs ─────────────────────────────────────────────────────
    private DrinkViewModel viewModel;
    private UserPrefs userPrefs;

    // ── Views ─────────────────────────────────────────────────────────────────
    private LinearLayout layoutStatChips;
    private TextView tvChipWeight;
    private TextView tvChipHeight;
    private TextView tvChipAge;
    private TextView tvChipGender;
    private EditText etName;
    private EditText etWeight;
    private EditText etHeight;
    private EditText etAge;
    private TextView btnMale;
    private TextView btnFemale;
    private Button btnSaveProfile;
    private LinearLayout llSessionsList;

    // ── State ─────────────────────────────────────────────────────────────────
    private String selectedGender = "M";

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userPrefs = new UserPrefs(requireContext());
        viewModel = new ViewModelProvider(requireActivity())
                .get(DrinkViewModel.class);

        bindViews(view);
        setupGenderButtons();
        loadSavedProfile();
        setupSaveButton();
        loadSessionHistory();
        observeSessionSaved();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSessionHistory();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        layoutStatChips = view.findViewById(R.id.layoutStatChips);
        tvChipWeight    = view.findViewById(R.id.tvChipWeight);
        tvChipHeight    = view.findViewById(R.id.tvChipHeight);
        tvChipAge       = view.findViewById(R.id.tvChipAge);
        tvChipGender    = view.findViewById(R.id.tvChipGender);
        etName          = view.findViewById(R.id.etName);
        etWeight        = view.findViewById(R.id.etWeight);
        etHeight        = view.findViewById(R.id.etHeight);
        etAge           = view.findViewById(R.id.etAge);
        btnMale         = view.findViewById(R.id.btnMale);
        btnFemale       = view.findViewById(R.id.btnFemale);
        btnSaveProfile  = view.findViewById(R.id.btnSaveProfile);
        llSessionsList  = view.findViewById(R.id.llSessionsList);
    }

    private void setupGenderButtons() {
        btnMale.setOnClickListener(v -> selectGender("M"));
        btnFemale.setOnClickListener(v -> selectGender("F"));
    }

    private void selectGender(String gender) {
        selectedGender = gender;

        if ("M".equals(gender)) {
            btnMale.setBackground(
                    requireContext().getDrawable(
                            R.drawable.radio_selected_background));
            btnMale.setTextColor(Color.parseColor("#5ab4ff"));
            btnFemale.setBackground(
                    requireContext().getDrawable(
                            R.drawable.radio_unselected_background));
            btnFemale.setTextColor(Color.parseColor("#444466"));
        } else {
            btnFemale.setBackground(
                    requireContext().getDrawable(
                            R.drawable.radio_selected_background));
            btnFemale.setTextColor(Color.parseColor("#5ab4ff"));
            btnMale.setBackground(
                    requireContext().getDrawable(
                            R.drawable.radio_unselected_background));
            btnMale.setTextColor(Color.parseColor("#444466"));
        }
    }

    // ── Load saved profile ────────────────────────────────────────────────────

    private void loadSavedProfile() {
        if (!userPrefs.isComplete()) return;

        etName.setText(userPrefs.getName());
        etWeight.setText(String.valueOf(userPrefs.getWeight()));
        etHeight.setText(String.valueOf(userPrefs.getHeight()));
        etAge.setText(String.valueOf(userPrefs.getAge()));

        selectGender(userPrefs.getSex());
        updateStatChips();

        btnSaveProfile.setText("Update profile");
    }

    private void updateStatChips() {
        layoutStatChips.setVisibility(View.VISIBLE);
        tvChipWeight.setText(userPrefs.getWeight() + " kg");
        tvChipHeight.setText(userPrefs.getHeight() + " cm");
        tvChipAge.setText(String.valueOf(userPrefs.getAge()));
        tvChipGender.setText("M".equals(userPrefs.getSex()) ? "Male" : "Female");
    }

    // ── Save button ───────────────────────────────────────────────────────────

    private void setupSaveButton() {
        btnSaveProfile.setOnClickListener(v -> {
            if (!validateFields()) return;

            String name  = etName.getText().toString().trim();
            float weight = Float.parseFloat(
                    etWeight.getText().toString().trim());
            int height   = Integer.parseInt(
                    etHeight.getText().toString().trim());
            int age      = Integer.parseInt(
                    etAge.getText().toString().trim());

            userPrefs.saveProfile(name, weight, height, age, selectedGender);

            updateStatChips();
            btnSaveProfile.setText("Update profile");

            Toast.makeText(requireContext(),
                    "Profile saved!", Toast.LENGTH_SHORT).show();
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

    // ── Session history ───────────────────────────────────────────────────────

    private void observeSessionSaved() {
        viewModel.getSessionSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved) {
                loadSessionHistory();
                viewModel.resetSessionSaved();
            }
        });
    }

    private void loadSessionHistory() {
        llSessionsList.removeAllViews();

        List<SessionLog> sessions =
                SessionStorage.getSessions(requireContext());

        if (sessions.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No sessions saved yet");
            empty.setTextColor(Color.parseColor("#444466"));
            empty.setTextSize(12f);
            empty.setPadding(dp(14), dp(14), dp(14), dp(14));
            llSessionsList.addView(empty);
            return;
        }

        for (int i = 0; i < sessions.size(); i++) {
            SessionLog session = sessions.get(i);

            // Divider between rows
            if (i > 0) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(Color.parseColor("#22223a"));
                llSessionsList.addView(divider);
            }

            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_session_row,
                            llSessionsList, false);

            TextView tvDate      = row.findViewById(R.id.tvSessionDate);
            TextView tvDetail    = row.findViewById(R.id.tvSessionDetail);
            TextView tvHydration = row.findViewById(R.id.tvSessionHydration);
            TextView btnSeeMore  = row.findViewById(R.id.btnSeeMore);

            // Format date
            String date = new SimpleDateFormat(
                    "MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(session.endTimestamp));
            tvDate.setText(date);

            // Duration
            long durationMs = session.endTimestamp - session.startTimestamp;
            long totalMins  = (durationMs / 1000) / 60;
            long hours      = totalMins / 60;
            long mins       = totalMins % 60;
            String duration = hours > 0
                    ? hours + "h " + mins + "m"
                    : mins + "m";

            tvDetail.setText(session.drinkCount + " drinks · " + duration);

            // Hydration color
            int hydration = Math.round(session.hydrationPercent);
            String hydraColor;
            if (hydration >= 80)      hydraColor = "#34d399";
            else if (hydration >= 40) hydraColor = "#fbbf24";
            else                      hydraColor = "#ef4444";

            tvHydration.setText(hydration + "%");
            tvHydration.setTextColor(Color.parseColor(hydraColor));

            // See more button
            final int sessionId = session.id;
            btnSeeMore.setOnClickListener(v ->
                    Toast.makeText(requireContext(),
                            "Session detail coming soon",
                            Toast.LENGTH_SHORT).show()
            );

            llSessionsList.addView(row);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}