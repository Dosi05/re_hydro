package com.example.rehydro;

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
import java.util.List;

public class AddDrinkFragment extends Fragment {

    // ── ViewModel + Prefs ─────────────────────────────────────────────────────
    private DrinkViewModel viewModel;
    private UserPrefs userPrefs;

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText etSearchDrink;
    private LinearLayout llDropdown;
    private LinearLayout layoutCustomSection;
    private EditText etCustomAbv;
    private TextView btnSaveYes;
    private TextView btnSaveNo;
    private EditText etVolume;
    private EditText etAbv;
    private Button btnMinus;
    private Button btnPlus;
    private TextView tvCount;
    private Button btnAddDrink;

    // ── State ─────────────────────────────────────────────────────────────────
    private int drinkCount        = 1;
    private boolean saveCustom    = true;
    private boolean isCustomDrink = false;
    private String selectedName   = "";

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        etSearchDrink      = view.findViewById(R.id.etSearchDrink);
        llDropdown         = view.findViewById(R.id.llDropdown);
        layoutCustomSection = view.findViewById(R.id.layoutCustomSection);
        etCustomAbv        = view.findViewById(R.id.etCustomAbv);
        btnSaveYes         = view.findViewById(R.id.btnSaveYes);
        btnSaveNo          = view.findViewById(R.id.btnSaveNo);
        etVolume           = view.findViewById(R.id.etVolume);
        etAbv              = view.findViewById(R.id.etAbv);
        btnMinus           = view.findViewById(R.id.btnMinus);
        btnPlus            = view.findViewById(R.id.btnPlus);
        tvCount            = view.findViewById(R.id.tvCount);
        btnAddDrink        = view.findViewById(R.id.btnAddDrink);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        etSearchDrink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    llDropdown.setVisibility(View.GONE);
                    layoutCustomSection.setVisibility(View.GONE);
                    clearFields();
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

        List<CustomDrink> results =
                CustomDrinkStorage.search(requireContext(), query);

        // Show matched drinks — max 5 results
        int limit = Math.min(results.size(), 5);
        for (int i = 0; i < limit; i++) {
            CustomDrink drink = results.get(i);
            addDropdownRow(drink, false);
        }

        // Always show "+ Add X as new drink" at the bottom
        addAddNewRow(query);
    }

    private void addDropdownRow(CustomDrink drink, boolean isAddNew) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_dropdown_row, llDropdown, false);

        TextView tvName   = row.findViewById(R.id.tvDropdownName);
        TextView tvDetail = row.findViewById(R.id.tvDropdownDetail);

        tvName.setText(drink.name);
        tvDetail.setText(drink.defaultVolumeMl + " ml · " + drink.abvPercent + "%");

        // Divider between rows
        if (llDropdown.getChildCount() > 0) {
            View divider = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(Color.parseColor("#22223a"));
            llDropdown.addView(divider);
        }

        row.setOnClickListener(v -> selectDrink(drink));
        llDropdown.addView(row);
    }

    private void addAddNewRow(String query) {
        // Divider
        if (llDropdown.getChildCount() > 0) {
            View divider = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(Color.parseColor("#22223a"));
            llDropdown.addView(divider);
        }

        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_dropdown_row, llDropdown, false);

        TextView tvName   = row.findViewById(R.id.tvDropdownName);
        TextView tvDetail = row.findViewById(R.id.tvDropdownDetail);

        tvName.setText("+ Add \"" + query + "\" as new drink");
        tvName.setTextColor(Color.parseColor("#5ab4ff"));
        tvDetail.setText("Enter ABV below");

        row.setOnClickListener(v -> selectAddNew(query));
        llDropdown.addView(row);
    }

    private void selectDrink(CustomDrink drink) {
        selectedName  = drink.name;
        isCustomDrink = false;

        etSearchDrink.setText(drink.name);
        etVolume.setText(String.valueOf(drink.defaultVolumeMl));
        etAbv.setText(String.valueOf(drink.abvPercent));

        llDropdown.setVisibility(View.GONE);
        layoutCustomSection.setVisibility(View.GONE);

        // Hide keyboard
        hideKeyboard();
    }

    private void selectAddNew(String name) {
        selectedName  = name.trim();
        isCustomDrink = true;

        etSearchDrink.setText(name);
        clearFields();

        llDropdown.setVisibility(View.GONE);
        layoutCustomSection.setVisibility(View.VISIBLE);

        hideKeyboard();
    }

    // ── Save toggle ───────────────────────────────────────────────────────────

    private void setupSaveToggle() {
        btnSaveYes.setOnClickListener(v -> setSaveToggle(true));
        btnSaveNo.setOnClickListener(v -> setSaveToggle(false));
    }

    private void setSaveToggle(boolean save) {
        saveCustom = save;

        if (save) {
            btnSaveYes.setBackground(
                    requireContext().getDrawable(
                            R.drawable.toggle_selected_background));
            btnSaveYes.setTextColor(Color.parseColor("#5ab4ff"));
            btnSaveNo.setBackground(
                    requireContext().getDrawable(
                            R.drawable.toggle_unselected_background));
            btnSaveNo.setTextColor(Color.parseColor("#444466"));
        } else {
            btnSaveNo.setBackground(
                    requireContext().getDrawable(
                            R.drawable.toggle_selected_background));
            btnSaveNo.setTextColor(Color.parseColor("#5ab4ff"));
            btnSaveYes.setBackground(
                    requireContext().getDrawable(
                            R.drawable.toggle_unselected_background));
            btnSaveYes.setTextColor(Color.parseColor("#444466"));
        }
    }

    // ── Count buttons ─────────────────────────────────────────────────────────

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

    // ── Add button ────────────────────────────────────────────────────────────

    private void setupAddButton() {
        btnAddDrink.setOnClickListener(v -> {
            if (!validateFields()) return;

            String name;
            double abv;

            if (isCustomDrink) {
                name = selectedName;
                String abvStr = etCustomAbv.getText().toString().trim();
                if (abvStr.isEmpty()) {
                    etCustomAbv.setError("Please enter ABV");
                    return;
                }
                abv = Double.parseDouble(abvStr);

                // Save to storage if user chose yes
                if (saveCustom) {
                    int vol = Integer.parseInt(
                            etVolume.getText().toString().trim());
                    CustomDrinkStorage.saveDrink(requireContext(),
                            new CustomDrink(name, abv, vol));
                }
            } else {
                name = selectedName;
                abv  = Double.parseDouble(
                        etAbv.getText().toString().trim());
            }

            int volumeMl = Integer.parseInt(
                    etVolume.getText().toString().trim());

            // Add drink to ViewModel — count means multiple entries
            for (int i = 0; i < drinkCount; i++) {
                viewModel.addDrink(
                        new DrinkEntry(name, abv, volumeMl, false),
                        userPrefs.getWeight(),
                        userPrefs.getSex()
                );
            }

            Toast.makeText(requireContext(),
                    drinkCount + "× " + name + " added!",
                    Toast.LENGTH_SHORT).show();

            resetForm();
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateFields() {
        if (selectedName.isEmpty()) {
            etSearchDrink.setError("Please select or add a drink");
            return false;
        }
        if (etVolume.getText().toString().trim().isEmpty()) {
            etVolume.setError("Please enter volume");
            return false;
        }
        if (!isCustomDrink &&
                etAbv.getText().toString().trim().isEmpty()) {
            etAbv.setError("Please enter ABV");
            return false;
        }
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void clearFields() {
        etVolume.setText("");
        etAbv.setText("");
    }

    private void resetForm() {
        etSearchDrink.setText("");
        etVolume.setText("");
        etAbv.setText("");
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
            imm.hideSoftInputFromWindow(
                    getView().getWindowToken(), 0);
        }
    }
}