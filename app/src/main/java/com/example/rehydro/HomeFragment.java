package com.example.rehydro;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    // ── ViewModel + Prefs ─────────────────────────────────────────────────────
    private DrinkViewModel viewModel;
    private UserPrefs userPrefs;

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvGreeting;
    private TextView tvSessionTime;
    private TextView tvBac;
    private TextView tvWaterNeeded;
    private TextView tvWaterConsumed;
    private TextView tvCharacterState;
    private TextView tvCharacterMessage;
    private TextView tvStateLevel;
    private TextView tvDrunkenessPercent;
    private TextView tvHydrationPercent;
    private ImageView ivAnimeCharacter;
    private ProgressBar progressDrunkeness;
    private ProgressBar progressHydration;
    private Button btn250ml;
    private Button btn500ml;
    private Button btnSaveSession;
    private LinearLayout llDrinkLog;

    // ── Session timer ─────────────────────────────────────────────────────────
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    // ── Character state data ──────────────────────────────────────────────────
    private static final int[] ANIME_IMAGES = {
            R.drawable.anime_sober,
            R.drawable.anime_tipsy,
            R.drawable.anime_drunk,
            R.drawable.anime_wrecked
    };

    private static final String[] STATE_NAMES = {
            "Perfectly sober",
            "Getting tipsy",
            "Noticeably drunk",
            "Very drunk"
    };

    private static final String[] STATE_MESSAGES = {
            "You're doing great! Stay hydrated.",
            "Start sipping water between drinks.",
            "Slow down. Drink water and eat something.",
            "Please stop drinking. Find somewhere safe."
    };

    private static final String[] STATE_LEVELS = {
            "Level 1 / 4",
            "Level 2 / 4",
            "Level 3 / 4",
            "Level 4 / 4"
    };

    private static final String[] BAR_COLORS = {
            "#34d399",
            "#fbbf24",
            "#f97316",
            "#ef4444"
    };

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(DrinkViewModel.class);
        userPrefs = new UserPrefs(requireContext());

        bindViews(view);
        setupButtons();
        setupGreeting();
        observeViewModel();
        startSessionTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvGreeting          = view.findViewById(R.id.tvGreeting);
        tvSessionTime       = view.findViewById(R.id.tvSessionTime);
        tvBac               = view.findViewById(R.id.tvBac);
        tvWaterNeeded       = view.findViewById(R.id.tvWaterNeeded);
        tvWaterConsumed     = view.findViewById(R.id.tvWaterConsumed);
        tvCharacterState    = view.findViewById(R.id.tvCharacterState);
        tvCharacterMessage  = view.findViewById(R.id.tvCharacterMessage);
        tvStateLevel        = view.findViewById(R.id.tvStateLevel);
        tvDrunkenessPercent = view.findViewById(R.id.tvDrunkenessPercent);
        tvHydrationPercent  = view.findViewById(R.id.tvHydrationPercent);
        ivAnimeCharacter    = view.findViewById(R.id.ivAnimeCharacter);
        progressDrunkeness  = view.findViewById(R.id.progressDrunkeness);
        progressHydration   = view.findViewById(R.id.progressHydration);
        btn250ml            = view.findViewById(R.id.btn250ml);
        btn500ml            = view.findViewById(R.id.btn500ml);
        btnSaveSession      = view.findViewById(R.id.btnSaveSession);
        llDrinkLog          = view.findViewById(R.id.llDrinkLog);
    }

    private void setupButtons() {
        btn250ml.setOnClickListener(v -> {
            viewModel.addWater(250,
                    userPrefs.getWeight(),
                    userPrefs.getSex());
            Toast.makeText(requireContext(),
                    "250 ml water added", Toast.LENGTH_SHORT).show();
        });

        btn500ml.setOnClickListener(v -> {
            viewModel.addWater(500,
                    userPrefs.getWeight(),
                    userPrefs.getSex());
            Toast.makeText(requireContext(),
                    "500 ml water added", Toast.LENGTH_SHORT).show();
        });

        btnSaveSession.setOnClickListener(v -> {
            if (!viewModel.hasActiveSession()) {
                Toast.makeText(requireContext(),
                        "No active session to save",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.clearSession();
            Toast.makeText(requireContext(),
                    "Session saved!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupGreeting() {
        String name = userPrefs.getName();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String timeOfDay;
        if (hour >= 5 && hour < 12)       timeOfDay = "Good morning";
        else if (hour >= 12 && hour < 17) timeOfDay = "Good afternoon";
        else if (hour >= 17 && hour < 21) timeOfDay = "Good evening";
        else                               timeOfDay = "Good night";

        tvGreeting.setText(name.isEmpty()
                ? timeOfDay
                : timeOfDay + ", " + name);
    }

    // ── Session timer ─────────────────────────────────────────────────────────

    private void startSessionTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateSessionTime();
                timerHandler.postDelayed(this, 60_000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void updateSessionTime() {
        long startTime = viewModel.getSessionStartTime();
        if (startTime == 0) {
            tvSessionTime.setText("No active session");
            return;
        }
        long elapsedMs = System.currentTimeMillis() - startTime;
        long minutes   = (elapsedMs / 1000) / 60;
        long hours     = minutes / 60;
        long mins      = minutes % 60;

        if (hours > 0) {
            tvSessionTime.setText(
                    "Session started " + hours + "h " + mins + "m ago");
        } else {
            tvSessionTime.setText(
                    "Session started " + mins + "m ago");
        }
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private void observeViewModel() {

        viewModel.getDrinks().observe(getViewLifecycleOwner(), drinks -> {
            updateDrinkLog(drinks);
            updateSessionTime();
        });

        viewModel.getWaterNeededMl().observe(getViewLifecycleOwner(), ml -> {
            tvWaterNeeded.setText(ml + " ml");
            updateHydrationBar();
        });

        viewModel.getWaterConsumedMl().observe(getViewLifecycleOwner(), ml -> {
            tvWaterConsumed.setText(ml + " ml");
            updateHydrationBar();
        });

        viewModel.getBac().observe(getViewLifecycleOwner(), bacVal -> {
            tvBac.setText(String.format("%.2f", bacVal));
        });

        viewModel.getDrunkenness().observe(getViewLifecycleOwner(), level -> {
            int percent    = Math.round(level * 100);
            int stateIndex = getStateIndex(percent);

            progressDrunkeness.setProgress(percent);
            setProgressBarColor(progressDrunkeness,
                    Color.parseColor(BAR_COLORS[stateIndex]));

            tvDrunkenessPercent.setText(percent + "%");
            tvDrunkenessPercent.setTextColor(
                    Color.parseColor(BAR_COLORS[stateIndex]));

            ivAnimeCharacter.setImageResource(ANIME_IMAGES[stateIndex]);
            tvCharacterState.setText(STATE_NAMES[stateIndex]);
            tvCharacterMessage.setText(STATE_MESSAGES[stateIndex]);

            tvStateLevel.setText(STATE_LEVELS[stateIndex]);
            tvStateLevel.setTextColor(
                    Color.parseColor(BAR_COLORS[stateIndex]));
        });
    }

    // ── Drink log ─────────────────────────────────────────────────────────────

    private void updateDrinkLog(List<DrinkEntry> drinks) {
        llDrinkLog.removeAllViews();

        if (drinks == null || drinks.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No drinks logged yet");
            empty.setTextColor(Color.parseColor("#444466"));
            empty.setTextSize(12f);
            empty.setPadding(dp(12), dp(12), dp(12), dp(12));
            llDrinkLog.addView(empty);
            return;
        }

        for (int i = 0; i < drinks.size(); i++) {
            DrinkEntry entry = drinks.get(i);

            // Divider between rows
            if (i > 0) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(Color.parseColor("#22223a"));
                llDrinkLog.addView(divider);
            }

            // Row
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(10), dp(12), dp(10));

            // Left column — name + detail
            LinearLayout leftCol = new LinearLayout(requireContext());
            leftCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftParams =
                    new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            leftCol.setLayoutParams(leftParams);

            TextView tvName = new TextView(requireContext());
            tvName.setTextSize(12f);

            TextView tvDetail = new TextView(requireContext());
            tvDetail.setTextSize(10f);
            tvDetail.setTextColor(Color.parseColor("#444466"));

            if (entry.isWater) {
                tvName.setText("Water");
                tvName.setTextColor(Color.parseColor("#5ab4ff"));
                tvDetail.setText(entry.volumeMl + " ml");
            } else {
                tvName.setText(entry.name);
                tvName.setTextColor(Color.parseColor("#ccccee"));
                tvDetail.setText(
                        entry.volumeMl + " ml · " + entry.abvPercent + "%");
            }

            leftCol.addView(tvName);
            leftCol.addView(tvDetail);

            // Right — water impact
            TextView tvImpact = new TextView(requireContext());
            tvImpact.setTextSize(11f);

            if (entry.isWater) {
                tvImpact.setText("+" + entry.volumeMl + " ml");
                tvImpact.setTextColor(Color.parseColor("#34d399"));
            } else {
                double pureAlcoholMl =
                        (entry.abvPercent / 100.0) * entry.volumeMl;
                int waterCost =
                        (int) Math.round(pureAlcoholMl * 0.789 * 8.0);
                tvImpact.setText("−" + waterCost + " ml");
                tvImpact.setTextColor(Color.parseColor("#444466"));
            }

            row.addView(leftCol);
            row.addView(tvImpact);
            llDrinkLog.addView(row);
        }
    }

    // ── Hydration bar ─────────────────────────────────────────────────────────

    private void updateHydrationBar() {
        Integer needed   = viewModel.getWaterNeededMl().getValue();
        Integer consumed = viewModel.getWaterConsumedMl().getValue();

        if (needed == null || consumed == null || needed == 0) {
            progressHydration.setProgress(0);
            tvHydrationPercent.setText("—");
            tvHydrationPercent.setTextColor(Color.parseColor("#5ab4ff"));
            return;
        }

        int hydration = Math.min(
                Math.round((consumed / (float) needed) * 100), 100);

        String hydraColor;
        if (hydration >= 80)      hydraColor = "#34d399";
        else if (hydration >= 40) hydraColor = "#fbbf24";
        else                      hydraColor = "#ef4444";

        progressHydration.setProgress(hydration);
        setProgressBarColor(progressHydration,
                Color.parseColor(hydraColor));
        tvHydrationPercent.setText(hydration + "%");
        tvHydrationPercent.setTextColor(Color.parseColor(hydraColor));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int getStateIndex(int percent) {
        if (percent < 25) return 0;
        if (percent < 55) return 1;
        if (percent < 80) return 2;
        return 3;
    }

    private void setProgressBarColor(ProgressBar bar, int color) {
        bar.getProgressDrawable().setColorFilter(
                new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}