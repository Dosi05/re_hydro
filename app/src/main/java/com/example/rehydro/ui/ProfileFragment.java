package com.example.rehydro.ui;

import android.content.Intent;
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
import androidx.navigation.Navigation;

import com.example.rehydro.R;
import com.example.rehydro.data.entity.SessionLogEntity;
import com.example.rehydro.data.prefs.UserPrefs;
import com.example.rehydro.data.db.DatabaseHelper;
import com.example.rehydro.data.prefs.LocaleHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private DrinkViewModel viewModel;
    private UserPrefs userPrefs;

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
    private TextView btnLangEn;
    private TextView btnLangBg;
    private Button btnSaveProfile;
    private LinearLayout llSessionsList;

    private String selectedGender  = "M";
    private String selectedLanguage = "en";

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
        setupLanguageButtons();
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
        btnLangEn       = view.findViewById(R.id.btnLangEn);
        btnLangBg       = view.findViewById(R.id.btnLangBg);
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
            btnMale.setBackground(requireContext().getDrawable(
                    R.drawable.radio_selected_background));
            btnMale.setTextColor(Color.parseColor("#5ab4ff"));
            btnFemale.setBackground(requireContext().getDrawable(
                    R.drawable.radio_unselected_background));
            btnFemale.setTextColor(Color.parseColor("#444466"));
        } else {
            btnFemale.setBackground(requireContext().getDrawable(
                    R.drawable.radio_selected_background));
            btnFemale.setTextColor(Color.parseColor("#5ab4ff"));
            btnMale.setBackground(requireContext().getDrawable(
                    R.drawable.radio_unselected_background));
            btnMale.setTextColor(Color.parseColor("#444466"));
        }
    }

    // ── Language switcher ─────────────────────────────────────────────────────

    private void setupLanguageButtons() {
        selectedLanguage = LocaleHelper.getLanguage(requireContext());
        updateLanguageButtons(selectedLanguage);

        btnLangEn.setOnClickListener(v -> switchLanguage("en"));
        btnLangBg.setOnClickListener(v -> switchLanguage("bg"));
    }

    private void switchLanguage(String langCode) {
        if (langCode.equals(selectedLanguage)) return;

        selectedLanguage = langCode;
        LocaleHelper.setLocale(requireContext(), langCode);

        // Рестартиране на MainActivity за прилагане на промяната
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
    }

    private void updateLanguageButtons(String langCode) {
        if ("en".equals(langCode)) {
            btnLangEn.setBackground(requireContext().getDrawable(
                    R.drawable.radio_selected_background));
            btnLangEn.setTextColor(Color.parseColor("#5ab4ff"));
            btnLangBg.setBackground(requireContext().getDrawable(
                    R.drawable.radio_unselected_background));
            btnLangBg.setTextColor(Color.parseColor("#444466"));
        } else {
            btnLangBg.setBackground(requireContext().getDrawable(
                    R.drawable.radio_selected_background));
            btnLangBg.setTextColor(Color.parseColor("#5ab4ff"));
            btnLangEn.setBackground(requireContext().getDrawable(
                    R.drawable.radio_unselected_background));
            btnLangEn.setTextColor(Color.parseColor("#444466"));
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

        btnSaveProfile.setText(getString(R.string.save_profile));
    }

    private void updateStatChips() {
        layoutStatChips.setVisibility(View.VISIBLE);
        tvChipWeight.setText(userPrefs.getWeight() + " kg");
        tvChipHeight.setText(userPrefs.getHeight() + " cm");
        tvChipAge.setText(String.valueOf(userPrefs.getAge()));
        tvChipGender.setText("M".equals(userPrefs.getSex())
                ? getString(R.string.male)
                : getString(R.string.female));
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
            btnSaveProfile.setText(getString(R.string.save_profile));

            Toast.makeText(requireContext(),
                    getString(R.string.profile_saved),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateFields() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError(getString(R.string.fill_all_fields));
            return false;
        }
        if (etWeight.getText().toString().trim().isEmpty()) {
            etWeight.setError(getString(R.string.fill_all_fields));
            return false;
        }
        if (etHeight.getText().toString().trim().isEmpty()) {
            etHeight.setError(getString(R.string.fill_all_fields));
            return false;
        }
        if (etAge.getText().toString().trim().isEmpty()) {
            etAge.setError(getString(R.string.fill_all_fields));
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
        if (llSessionsList == null) return;
        llSessionsList.removeAllViews();

        DatabaseHelper.getAllSessions(requireContext(), sessions -> {
            if (!isAdded()) return;

            if (sessions.isEmpty()) {
                TextView empty = new TextView(requireContext());
                empty.setText(getString(R.string.no_sessions_yet));
                empty.setTextColor(Color.parseColor("#444466"));
                empty.setTextSize(12f);
                empty.setPadding(dp(14), dp(14), dp(14), dp(14));
                llSessionsList.addView(empty);
                return;
            }

            for (int i = 0; i < sessions.size(); i++) {
                SessionLogEntity session = sessions.get(i);

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

                String date = new SimpleDateFormat(
                        "MMM dd, yyyy", Locale.getDefault())
                        .format(new Date(session.endTimestamp));
                tvDate.setText(date);

                long durationMs = session.endTimestamp - session.startTimestamp;
                long totalMins  = (durationMs / 1000) / 60;
                long hours      = totalMins / 60;
                long mins       = totalMins % 60;
                String duration = hours > 0
                        ? hours + "h " + mins + "m"
                        : mins + "m";

                tvDetail.setText(getString(R.string.drinks_count,
                        session.drinkCount) + " · " + duration);

                int hydration = Math.round(session.hydrationPercent);
                String hydraColor;
                if (hydration >= 80)      hydraColor = "#34d399";
                else if (hydration >= 40) hydraColor = "#fbbf24";
                else                      hydraColor = "#ef4444";

                tvHydration.setText(hydration + "%");
                tvHydration.setTextColor(Color.parseColor(hydraColor));

                final int sessionId = session.id;
                btnSeeMore.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putInt("sessionId", sessionId);
                    Navigation.findNavController(requireView())
                            .navigate(R.id.sessionDetailFragment, args);
                });

                llSessionsList.addView(row);
            }
        });
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}