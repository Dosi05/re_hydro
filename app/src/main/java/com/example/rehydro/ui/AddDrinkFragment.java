package com.example.rehydro.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.rehydro.model.CustomDrink;
import com.example.rehydro.data.entity.CustomDrinkEntity;
import com.example.rehydro.storage.CustomDrinkStorage;
import com.example.rehydro.data.db.DatabaseHelper;
import com.example.rehydro.model.DrinkEntry;
import com.example.rehydro.R;
import com.example.rehydro.data.prefs.UserPrefs;

import java.util.ArrayList;
import java.util.List;

public class AddDrinkFragment extends Fragment {

    private static final String TAG = "REHYDRO";

    private DrinkViewModel viewModel;
    private UserPrefs userPrefs;

    private EditText etSearchDrink;
    private LinearLayout llDropdown;
    private LinearLayout layoutCustomSection;
    private EditText etCustomAbv;
    private TextView btnSaveYes;
    private TextView btnSaveNo;
    private EditText etVolume;
    private TextView tvAbvLabel;
    private EditText etAbv;
    private Button btnMinus;
    private Button btnPlus;
    private TextView tvCount;
    private Button btnAddDrink;

    private int drinkCount          = 1;
    private boolean saveCustom      = true;
    private boolean isCustomDrink   = false;
    private String selectedName     = "";
    private boolean suppressWatcher = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_drink, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(DrinkViewModel.class);
        userPrefs = new UserPrefs(requireContext());

        bindViews(view);
        setupSearch();
        setupCountButtons();
        setupSaveToggle();
        setupAddButton();
    }

    private void bindViews(View view) {
        etSearchDrink       = view.findViewById(R.id.etSearchDrink);
        llDropdown          = view.findViewById(R.id.llDropdown);
        layoutCustomSection = view.findViewById(R.id.layoutCustomSection);
        etCustomAbv         = view.findViewById(R.id.etCustomAbv);
        btnSaveYes          = view.findViewById(R.id.btnSaveYes);
        btnSaveNo           = view.findViewById(R.id.btnSaveNo);
        etVolume            = view.findViewById(R.id.etVolume);
        tvAbvLabel          = view.findViewById(R.id.tvAbvLabel);
        etAbv               = view.findViewById(R.id.etAbv);
        btnMinus            = view.findViewById(R.id.btnMinus);
        btnPlus             = view.findViewById(R.id.btnPlus);
        tvCount             = view.findViewById(R.id.tvCount);
        btnAddDrink         = view.findViewById(R.id.btnAddDrink);
    }

    private void setupSearch() {
        etSearchDrink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                if (suppressWatcher) return;

                String query = s.toString().trim();
                if (query.isEmpty()) {
                    llDropdown.setVisibility(View.GONE);
                    layoutCustomSection.setVisibility(View.GONE);
                    clearFields();
                    selectedName  = "";
                    isCustomDrink = false;
                    return;
                }
                showDropdown(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showDropdown(String query) {
        llDropdown.removeAllViews();
        llDropdown.setVisibility(View.VISIBLE);
        layoutCustomSection.setVisibility(View.GONE);
        isCustomDrink = false;

        List<CustomDrink> presetResults =
                CustomDrinkStorage.searchPresetsOnly(query);

        DatabaseHelper.searchCustomDrinks(
                requireContext(), query, dbResults -> {
                    if (!isAdded()) return;
                    llDropdown.removeAllViews();

                    List<String> added = new ArrayList<>();
                    int count = 0;

                    for (CustomDrink d : presetResults) {
                        if (count >= 5) break;
                        addDropdownRow(d.name, d.abvPercent, d.defaultVolumeMl);
                        added.add(d.name.toLowerCase());
                        count++;
                    }

                    for (CustomDrinkEntity d : dbResults) {
                        if (count >= 5) break;
                        if (!added.contains(d.name.toLowerCase())) {
                            addDropdownRow(d.name, d.abvPercent, d.defaultVolumeMl);
                            count++;
                        }
                    }

                    addAddNewRow(query);
                });
    }

    private void addDropdownRow(String name, double abv, int volumeMl) {
        if (llDropdown.getChildCount() > 0) {
            View divider = new View(requireContext());
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(Color.parseColor("#22223a"));
            llDropdown.addView(divider);
        }

        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_dropdown_row, llDropdown, false);

        TextView tvName   = row.findViewById(R.id.tvDropdownName);
        TextView tvDetail = row.findViewById(R.id.tvDropdownDetail);

        tvName.setText(name);
        tvDetail.setText(volumeMl + " ml · " + abv + "%");

        row.setOnClickListener(v -> selectDrink(name, abv, volumeMl));
        llDropdown.addView(row);
    }

    private void addAddNewRow(String query) {
        if (llDropdown.getChildCount() > 0) {
            View divider = new View(requireContext());
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(Color.parseColor("#22223a"));
            llDropdown.addView(divider);
        }

        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_dropdown_row, llDropdown, false);

        TextView tvName   = row.findViewById(R.id.tvDropdownName);
        TextView tvDetail = row.findViewById(R.id.tvDropdownDetail);

        tvName.setText("+ " + getString(R.string.new_drink) + " \"" + query + "\"");
        tvName.setTextColor(Color.parseColor("#5ab4ff"));
        tvDetail.setText(getString(R.string.abv_percent));

        row.setOnClickListener(v -> selectAddNew(query));
        llDropdown.addView(row);
    }

    private void selectDrink(String name, double abv, int volumeMl) {
        selectedName  = name;
        isCustomDrink = false;

        suppressWatcher = true;
        etSearchDrink.setText(name);
        suppressWatcher = false;

        etVolume.setText(String.valueOf(volumeMl));
        etAbv.setText(String.valueOf(abv));

        tvAbvLabel.setVisibility(View.VISIBLE);
        etAbv.setVisibility(View.VISIBLE);

        llDropdown.setVisibility(View.GONE);
        layoutCustomSection.setVisibility(View.GONE);

        hideKeyboard();
    }

    private void selectAddNew(String name) {
        selectedName  = name.trim();
        isCustomDrink = true;

        suppressWatcher = true;
        etSearchDrink.setText(name);
        suppressWatcher = false;

        clearFields();

        llDropdown.setVisibility(View.GONE);
        layoutCustomSection.setVisibility(View.VISIBLE);

        tvAbvLabel.setVisibility(View.GONE);
        etAbv.setVisibility(View.GONE);

        hideKeyboard();
    }

    private void setupSaveToggle() {
        btnSaveYes.setOnClickListener(v -> setSaveToggle(true));
        btnSaveNo.setOnClickListener(v -> setSaveToggle(false));
    }

    private void setSaveToggle(boolean save) {
        saveCustom = save;

        if (save) {
            btnSaveYes.setBackground(requireContext().getDrawable(
                    R.drawable.toggle_selected_background));
            btnSaveYes.setTextColor(Color.parseColor("#5ab4ff"));
            btnSaveNo.setBackground(requireContext().getDrawable(
                    R.drawable.toggle_unselected_background));
            btnSaveNo.setTextColor(Color.parseColor("#444466"));
        } else {
            btnSaveNo.setBackground(requireContext().getDrawable(
                    R.drawable.toggle_selected_background));
            btnSaveNo.setTextColor(Color.parseColor("#5ab4ff"));
            btnSaveYes.setBackground(requireContext().getDrawable(
                    R.drawable.toggle_unselected_background));
            btnSaveYes.setTextColor(Color.parseColor("#444466"));
        }
    }

    private void setupCountButtons() {
        btnMinus.setOnClickListener(v -> {
            if (drinkCount > 1) {
                drinkCount--;
                tvCount.setText(String.valueOf(drinkCount));
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (drinkCount < 20) {
                drinkCount++;
                tvCount.setText(String.valueOf(drinkCount));
            }
        });
    }

    private void setupAddButton() {
        btnAddDrink.setOnClickListener(v -> {
            if (!validateFields()) return;

            String name;
            double abv;
            int volumeMl;

            String volStr = etVolume.getText().toString().trim();
            if (volStr.isEmpty()) {
                etVolume.setError(getString(R.string.enter_volume));
                return;
            }
            volumeMl = Integer.parseInt(volStr);

            if (isCustomDrink) {
                name = selectedName;
                String abvStr = etCustomAbv.getText().toString().trim();
                if (abvStr.isEmpty()) {
                    etCustomAbv.setError(getString(R.string.please_enter_abv));
                    return;
                }
                abv = Double.parseDouble(abvStr);

                if (saveCustom) {
                    final String finalName = name;
                    final double finalAbv  = abv;
                    final int finalVol     = volumeMl;

                    CustomDrinkEntity entity = new CustomDrinkEntity();
                    entity.name            = finalName;
                    entity.abvPercent      = finalAbv;
                    entity.defaultVolumeMl = finalVol;

                    DatabaseHelper.saveCustomDrink(
                            requireContext(), entity, () -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(),
                                        getString(R.string.added_to_session, finalName),
                                        Toast.LENGTH_SHORT).show();
                            });
                }

            } else {
                name = selectedName;
                String abvStr = etAbv.getText().toString().trim();
                if (abvStr.isEmpty()) {
                    etAbv.setError(getString(R.string.please_enter_abv));
                    return;
                }
                abv = Double.parseDouble(abvStr);
            }

            final String finalName  = name;
            final double finalAbv   = abv;
            final int finalVolumeMl = volumeMl;

            for (int i = 0; i < drinkCount; i++) {
                viewModel.addDrink(
                        new DrinkEntry(finalName, finalAbv, finalVolumeMl, false),
                        userPrefs.getWeight(),
                        userPrefs.getSex()
                );
            }

            Toast.makeText(requireContext(),
                    getString(R.string.added_to_session, drinkCount + "× " + name),
                    Toast.LENGTH_SHORT).show();

            resetForm();
        });
    }

    private boolean validateFields() {
        if (selectedName.isEmpty()) {
            etSearchDrink.setError(getString(R.string.select_drink_first));
            return false;
        }
        if (etVolume.getText().toString().trim().isEmpty()) {
            etVolume.setError(getString(R.string.enter_volume));
            return false;
        }
        if (!isCustomDrink && etAbv.getText().toString().trim().isEmpty()) {
            etAbv.setError(getString(R.string.please_enter_abv));
            return false;
        }
        return true;
    }

    private void clearFields() {
        etVolume.setText("");
        etAbv.setText("");
    }

    private void resetForm() {
        suppressWatcher = true;
        etSearchDrink.setText("");
        suppressWatcher = false;

        etVolume.setText("");
        etAbv.setText("");
        etAbv.setVisibility(View.VISIBLE);
        tvAbvLabel.setVisibility(View.VISIBLE);
        etCustomAbv.setText("");
        llDropdown.setVisibility(View.GONE);
        layoutCustomSection.setVisibility(View.GONE);
        drinkCount    = 1;
        isCustomDrink = false;
        selectedName  = "";
        tvCount.setText("1");
        setSaveToggle(true);
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(
                                android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}