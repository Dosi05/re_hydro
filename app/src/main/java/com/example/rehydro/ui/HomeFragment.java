package com.example.rehydro.ui;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rehydro.util.HydrationCalculator;
import com.example.rehydro.data.entity.LocationEntry;
import com.example.rehydro.R;
import com.example.rehydro.data.entity.SessionLogEntity;
import com.example.rehydro.data.prefs.UserPrefs;
import com.example.rehydro.data.db.AppDatabase;
import com.example.rehydro.data.db.DatabaseHelper;
import com.example.rehydro.model.DrinkEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private DrinkViewModel viewModel;
    private UserPrefs userPrefs;

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

    private LinearLayout llEditPopup;
    private TextView tvEditTitle;
    private EditText etEditVolume;
    private EditText etEditAbv;
    private TextView tvEditCount;
    private Button btnEditMinus;
    private Button btnEditPlus;
    private Button btnEditCancel;
    private Button btnEditSave;

    private int editingIndex      = -1;
    private int editCount         = 1;
    private View currentSwipedRow = null;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private static final int[] ANIME_IMAGES = {
            R.drawable.anime_sober,
            R.drawable.anime_tipsy,
            R.drawable.anime_drunk,
            R.drawable.anime_wrecked
    };

    private static final String[] BAR_COLORS = {
            "#34d399",
            "#fbbf24",
            "#f97316",
            "#ef4444"
    };

    private String[] getStateNames() {
        return new String[]{
                getString(R.string.state_sober),
                getString(R.string.state_tipsy),
                getString(R.string.state_drunk),
                getString(R.string.state_wrecked)
        };
    }

    private String[] getStateMessages() {
        return new String[]{
                getString(R.string.msg_sober),
                getString(R.string.msg_tipsy),
                getString(R.string.msg_drunk),
                getString(R.string.msg_wrecked)
        };
    }

    private String[] getStateLevels() {
        return new String[]{
                getString(R.string.level_1),
                getString(R.string.level_2),
                getString(R.string.level_3),
                getString(R.string.level_4)
        };
    }

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
        buildEditPopup(view);
        setupButtons();
        setupGreeting();
        observeViewModel();
        startSessionTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupGreeting();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
    }

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

    private void buildEditPopup(View rootView) {
        LinearLayout innerLayout = rootView.findViewById(R.id.homeInnerLayout);

        llEditPopup = new LinearLayout(requireContext());
        llEditPopup.setOrientation(LinearLayout.VERTICAL);
        llEditPopup.setBackgroundResource(R.drawable.card_background);
        llEditPopup.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams popupParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupParams.topMargin = dp(10);
        llEditPopup.setLayoutParams(popupParams);
        llEditPopup.setVisibility(View.GONE);

        tvEditTitle = new TextView(requireContext());
        tvEditTitle.setTextColor(Color.parseColor("#ffffff"));
        tvEditTitle.setTextSize(14f);
        tvEditTitle.setPadding(0, 0, 0, dp(12));
        llEditPopup.addView(tvEditTitle);

        TextView tvVolLabel = new TextView(requireContext());
        tvVolLabel.setText(getString(R.string.volume_ml));
        tvVolLabel.setTextColor(Color.parseColor("#444466"));
        tvVolLabel.setTextSize(10f);
        tvVolLabel.setPadding(0, 0, 0, dp(6));
        llEditPopup.addView(tvVolLabel);

        etEditVolume = new EditText(requireContext());
        etEditVolume.setBackground(requireContext().getDrawable(R.drawable.input_background));
        etEditVolume.setTextColor(Color.parseColor("#ffffff"));
        etEditVolume.setHintTextColor(Color.parseColor("#333355"));
        etEditVolume.setHint("e.g. 330");
        etEditVolume.setInputType(InputType.TYPE_CLASS_NUMBER);
        etEditVolume.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams volParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        volParams.bottomMargin = dp(12);
        etEditVolume.setLayoutParams(volParams);
        llEditPopup.addView(etEditVolume);

        TextView tvAbvLabel = new TextView(requireContext());
        tvAbvLabel.setText(getString(R.string.abv_percent));
        tvAbvLabel.setTextColor(Color.parseColor("#444466"));
        tvAbvLabel.setTextSize(10f);
        tvAbvLabel.setPadding(0, 0, 0, dp(6));
        llEditPopup.addView(tvAbvLabel);

        etEditAbv = new EditText(requireContext());
        etEditAbv.setBackground(requireContext().getDrawable(R.drawable.input_background));
        etEditAbv.setTextColor(Color.parseColor("#ffffff"));
        etEditAbv.setHintTextColor(Color.parseColor("#333355"));
        etEditAbv.setHint("e.g. 5.0");
        etEditAbv.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etEditAbv.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams abvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        abvParams.bottomMargin = dp(12);
        etEditAbv.setLayoutParams(abvParams);
        llEditPopup.addView(etEditAbv);

        TextView tvCountLabel = new TextView(requireContext());
        tvCountLabel.setText(getString(R.string.count));
        tvCountLabel.setTextColor(Color.parseColor("#444466"));
        tvCountLabel.setTextSize(10f);
        tvCountLabel.setPadding(0, 0, 0, dp(6));
        llEditPopup.addView(tvCountLabel);

        LinearLayout countRow = new LinearLayout(requireContext());
        countRow.setOrientation(LinearLayout.HORIZONTAL);
        countRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams countRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        countRowParams.bottomMargin = dp(14);
        countRow.setLayoutParams(countRowParams);

        btnEditMinus = new Button(requireContext());
        btnEditMinus.setText("−");
        btnEditMinus.setTextColor(Color.parseColor("#5ab4ff"));
        btnEditMinus.setBackgroundResource(R.drawable.water_button_background);
        btnEditMinus.setTextSize(18f);
        btnEditMinus.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));

        tvEditCount = new TextView(requireContext());
        tvEditCount.setText("1");
        tvEditCount.setTextColor(Color.parseColor("#ffffff"));
        tvEditCount.setTextSize(18f);
        tvEditCount.setGravity(Gravity.CENTER);
        tvEditCount.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        btnEditPlus = new Button(requireContext());
        btnEditPlus.setText("+");
        btnEditPlus.setTextColor(Color.parseColor("#5ab4ff"));
        btnEditPlus.setBackgroundResource(R.drawable.water_button_background);
        btnEditPlus.setTextSize(18f);
        btnEditPlus.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));

        countRow.addView(btnEditMinus);
        countRow.addView(tvEditCount);
        countRow.addView(btnEditPlus);
        llEditPopup.addView(countRow);

        LinearLayout btnRow = new LinearLayout(requireContext());
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        btnEditCancel = new Button(requireContext());
        btnEditCancel.setText(getString(R.string.cancel));
        btnEditCancel.setTextColor(Color.parseColor("#666688"));
        btnEditCancel.setBackgroundResource(R.drawable.input_background);
        btnEditCancel.setTextSize(12f);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cancelParams.setMarginEnd(dp(8));
        btnEditCancel.setLayoutParams(cancelParams);

        btnEditSave = new Button(requireContext());
        btnEditSave.setText(getString(R.string.save_changes));
        btnEditSave.setTextColor(Color.WHITE);
        btnEditSave.setBackgroundColor(Color.parseColor("#f97316"));
        btnEditSave.setTextSize(12f);
        btnEditSave.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        btnRow.addView(btnEditCancel);
        btnRow.addView(btnEditSave);
        llEditPopup.addView(btnRow);

        int drinkLogIndex = innerLayout.indexOfChild(llDrinkLog);
        innerLayout.addView(llEditPopup, drinkLogIndex + 1);

        btnEditMinus.setOnClickListener(v -> {
            if (editCount > 1) {
                editCount--;
                tvEditCount.setText(String.valueOf(editCount));
            }
        });

        btnEditPlus.setOnClickListener(v -> {
            if (editCount < 20) {
                editCount++;
                tvEditCount.setText(String.valueOf(editCount));
            }
        });

        btnEditCancel.setOnClickListener(v -> hideEditPopup());
        btnEditSave.setOnClickListener(v -> saveEdit());
    }

    private void setupButtons() {
        btn250ml.setOnClickListener(v -> {
            viewModel.addWater(250, userPrefs.getWeight(), userPrefs.getSex());
            Toast.makeText(requireContext(),
                    getString(R.string.water_added_250),
                    Toast.LENGTH_SHORT).show();
        });

        btn500ml.setOnClickListener(v -> {
            viewModel.addWater(500, userPrefs.getWeight(), userPrefs.getSex());
            Toast.makeText(requireContext(),
                    getString(R.string.water_added_500),
                    Toast.LENGTH_SHORT).show();
        });

        btnSaveSession.setOnClickListener(v -> {
            if (!viewModel.hasActiveSession()) {
                Toast.makeText(requireContext(),
                        getString(R.string.no_session_to_save),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            List<DrinkEntry> drinks = viewModel.getDrinks().getValue();
            int alcoholCount = 0;
            if (drinks != null) {
                for (DrinkEntry d : drinks) {
                    if (!d.isWater) alcoholCount++;
                }
            }

            int needed = viewModel.getWaterNeededMl().getValue() != null
                    ? viewModel.getWaterNeededMl().getValue() : 0;
            int consumed = viewModel.getWaterConsumedMl().getValue() != null
                    ? viewModel.getWaterConsumedMl().getValue() : 0;

            SessionLogEntity session = new SessionLogEntity();
            session.startTimestamp   = viewModel.getSessionStartTime();
            session.endTimestamp     = System.currentTimeMillis();
            session.waterNeededMl    = needed;
            session.waterConsumedMl  = consumed;
            session.hydrationPercent =
                    HydrationCalculator.hydrationPercent(needed, consumed);
            session.drinkCount = alcoholCount;

            DatabaseHelper.saveSession(
                    requireContext(),
                    session,
                    drinks != null ? drinks : new ArrayList<>(),
                    sessionId -> {
                        if (getActivity() instanceof MainActivity) {
                            MainActivity act = (MainActivity) getActivity();
                            act.setCurrentSessionId(sessionId);
                            act.stopWaterReminder();
                            act.stopLocationService();
                        }

                        updatePendingLocations(sessionId);
                        viewModel.notifySessionSaved();
                        viewModel.clearSession();

                        Toast.makeText(requireContext(),
                                getString(R.string.session_saved),
                                Toast.LENGTH_SHORT).show();
                    }
            );
        });
    }

    private void updatePendingLocations(int realSessionId) {
        new Thread(() -> {
            List<LocationEntry> pending = AppDatabase
                    .getInstance(requireContext())
                    .locationDao()
                    .getPendingLocations();

            for (LocationEntry loc : pending) {
                loc.sessionId = realSessionId;
                loc.isPending = false;
                AppDatabase.getInstance(requireContext())
                        .locationDao()
                        .updateLocation(loc);
            }
        }).start();
    }

    private void setupGreeting() {
        String name = userPrefs.getName();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String timeOfDay;
        if (hour >= 5 && hour < 12)       timeOfDay = getString(R.string.good_morning);
        else if (hour >= 12 && hour < 17) timeOfDay = getString(R.string.good_afternoon);
        else if (hour >= 17 && hour < 21) timeOfDay = getString(R.string.good_evening);
        else                               timeOfDay = getString(R.string.good_night);

        tvGreeting.setText(name.isEmpty()
                ? timeOfDay : timeOfDay + ", " + name);
    }

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
            tvSessionTime.setText(getString(R.string.no_active_session));
            return;
        }
        long elapsedMs = System.currentTimeMillis() - startTime;
        long minutes   = (elapsedMs / 1000) / 60;
        long hours     = minutes / 60;
        long mins      = minutes % 60;

        if (hours > 0) {
            tvSessionTime.setText(getString(
                    R.string.session_started_hm, (int) hours, (int) mins));
        } else {
            tvSessionTime.setText(getString(
                    R.string.session_started_m, (int) mins));
        }
    }

    private void observeViewModel() {

        viewModel.getDrinks().observe(getViewLifecycleOwner(), drinks -> {
            updateDrinkLog(drinks);
            updateSessionTime();

            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                if (drinks != null && !drinks.isEmpty()) {
                    activity.startLocationService();
                    activity.startWaterReminder();
                } else {
                    activity.setCurrentSessionId(-1);
                    activity.stopLocationService();
                    activity.stopWaterReminder();
                }
            }
        });

        viewModel.getWaterNeededMl().observe(getViewLifecycleOwner(), ml -> {
            tvWaterNeeded.setText(ml + " ml");
            updateHydrationBar();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateServiceWaterData();
            }
        });

        viewModel.getWaterConsumedMl().observe(getViewLifecycleOwner(), ml -> {
            tvWaterConsumed.setText(ml + " ml");
            updateHydrationBar();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateServiceWaterData();
            }
        });

        viewModel.getBac().observe(getViewLifecycleOwner(), bacVal ->
                tvBac.setText(String.format("%.2f", bacVal)));

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
            tvCharacterState.setText(getStateNames()[stateIndex]);
            tvCharacterMessage.setText(getStateMessages()[stateIndex]);
            tvStateLevel.setText(getStateLevels()[stateIndex]);
            tvStateLevel.setTextColor(Color.parseColor(BAR_COLORS[stateIndex]));
        });
    }

    private void updateDrinkLog(List<DrinkEntry> drinks) {
        llDrinkLog.removeAllViews();
        hideEditPopup();

        if (drinks == null || drinks.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(getString(R.string.no_drinks_logged));
            empty.setTextColor(Color.parseColor("#444466"));
            empty.setTextSize(12f);
            empty.setPadding(dp(12), dp(12), dp(12), dp(12));
            llDrinkLog.addView(empty);
            return;
        }

        for (int i = 0; i < drinks.size(); i++) {
            DrinkEntry entry = drinks.get(i);

            if (i > 0) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(Color.parseColor("#22223a"));
                llDrinkLog.addView(divider);
            }

            llDrinkLog.addView(buildSwipeRow(entry, i));
        }
    }

    private View buildSwipeRow(DrinkEntry entry, int index) {
        final int swipeWidth = entry.isWater ? dp(60) : dp(120);

        FrameLayout frame = new FrameLayout(requireContext());
        frame.setClipChildren(true);
        frame.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout actionBtns = new LinearLayout(requireContext());
        actionBtns.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(
                swipeWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        actionParams.gravity = Gravity.END;
        actionBtns.setLayoutParams(actionParams);

        LinearLayout editBtn = new LinearLayout(requireContext());
        editBtn.setOrientation(LinearLayout.VERTICAL);
        editBtn.setGravity(Gravity.CENTER);
        editBtn.setBackgroundColor(Color.parseColor("#1a0d00"));
        editBtn.setLayoutParams(new LinearLayout.LayoutParams(
                dp(60), LinearLayout.LayoutParams.MATCH_PARENT));
        editBtn.setVisibility(entry.isWater ? View.GONE : View.VISIBLE);

        TextView editIcon = new TextView(requireContext());
        editIcon.setText("✎");
        editIcon.setTextColor(Color.parseColor("#f97316"));
        editIcon.setTextSize(16f);
        editIcon.setGravity(Gravity.CENTER);

        TextView editLabel = new TextView(requireContext());
        editLabel.setText("Edit");
        editLabel.setTextColor(Color.parseColor("#f97316"));
        editLabel.setTextSize(9f);
        editLabel.setGravity(Gravity.CENTER);

        editBtn.addView(editIcon);
        editBtn.addView(editLabel);

        LinearLayout deleteBtn = new LinearLayout(requireContext());
        deleteBtn.setOrientation(LinearLayout.VERTICAL);
        deleteBtn.setGravity(Gravity.CENTER);
        deleteBtn.setBackgroundColor(Color.parseColor("#1a0000"));
        deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
                dp(60), LinearLayout.LayoutParams.MATCH_PARENT));

        TextView deleteIcon = new TextView(requireContext());
        deleteIcon.setText("✕");
        deleteIcon.setTextColor(Color.parseColor("#ef4444"));
        deleteIcon.setTextSize(16f);
        deleteIcon.setGravity(Gravity.CENTER);

        TextView deleteLabel = new TextView(requireContext());
        deleteLabel.setText("Delete");
        deleteLabel.setTextColor(Color.parseColor("#ef4444"));
        deleteLabel.setTextSize(9f);
        deleteLabel.setGravity(Gravity.CENTER);

        deleteBtn.addView(deleteIcon);
        deleteBtn.addView(deleteLabel);

        actionBtns.addView(editBtn);
        actionBtns.addView(deleteBtn);

        LinearLayout frontRow = new LinearLayout(requireContext());
        frontRow.setOrientation(LinearLayout.HORIZONTAL);
        frontRow.setGravity(Gravity.CENTER_VERTICAL);
        frontRow.setBackgroundColor(Color.parseColor("#1a1a2e"));
        frontRow.setPadding(dp(12), dp(10), dp(12), dp(10));
        frontRow.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout leftCol = new LinearLayout(requireContext());
        leftCol.setOrientation(LinearLayout.VERTICAL);
        leftCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(requireContext());
        tvName.setTextSize(12f);

        TextView tvDetail = new TextView(requireContext());
        tvDetail.setTextSize(10f);
        tvDetail.setTextColor(Color.parseColor("#444466"));

        if (entry.isWater) {
            tvName.setText(getString(R.string.water));
            tvName.setTextColor(Color.parseColor("#5ab4ff"));
            tvDetail.setText(entry.volumeMl + " ml");
        } else {
            tvName.setText(entry.name);
            tvName.setTextColor(Color.parseColor("#ccccee"));
            tvDetail.setText(entry.volumeMl + " ml · " + entry.abvPercent + "%");
        }

        leftCol.addView(tvName);
        leftCol.addView(tvDetail);

        TextView tvImpact = new TextView(requireContext());
        tvImpact.setTextSize(11f);

        if (entry.isWater) {
            tvImpact.setText("+" + entry.volumeMl + " ml");
            tvImpact.setTextColor(Color.parseColor("#34d399"));
        } else {
            double pureAlcoholMl = (entry.abvPercent / 100.0) * entry.volumeMl;
            int waterCost = (int) Math.round(pureAlcoholMl * 0.789 * 8.0);
            tvImpact.setText("−" + waterCost + " ml");
            tvImpact.setTextColor(Color.parseColor("#444466"));
        }

        frontRow.addView(leftCol);
        frontRow.addView(tvImpact);

        frame.addView(actionBtns);
        frame.addView(frontRow);

        GestureDetector gestureDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        float diffX = e2.getX() - e1.getX();
                        if (diffX < -50 && Math.abs(velocityX) > 100) {
                            if (currentSwipedRow != null && currentSwipedRow != frontRow) {
                                ObjectAnimator.ofFloat(currentSwipedRow, "translationX", 0)
                                        .setDuration(200).start();
                            }
                            ObjectAnimator.ofFloat(frontRow, "translationX", -swipeWidth)
                                    .setDuration(200).start();
                            currentSwipedRow = frontRow;
                            return true;
                        } else if (diffX > 50 && Math.abs(velocityX) > 100) {
                            ObjectAnimator.ofFloat(frontRow, "translationX", 0)
                                    .setDuration(200).start();
                            currentSwipedRow = null;
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });

        frontRow.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        editBtn.setOnClickListener(v -> {
            ObjectAnimator.ofFloat(frontRow, "translationX", 0)
                    .setDuration(200).start();
            currentSwipedRow = null;
            showEditPopup(index, entry);
        });

        deleteBtn.setOnClickListener(v -> {
            ObjectAnimator.ofFloat(frontRow, "translationX", 0)
                    .setDuration(150).start();
            currentSwipedRow = null;
            viewModel.removeDrink(index, userPrefs.getWeight(), userPrefs.getSex());
            Toast.makeText(requireContext(),
                    getString(R.string.removed, entry.name),
                    Toast.LENGTH_SHORT).show();
        });

        return frame;
    }

    private void showEditPopup(int index, DrinkEntry entry) {
        editingIndex = index;
        editCount    = 1;

        tvEditTitle.setText(getString(R.string.edit_drink, entry.name));
        etEditVolume.setText(String.valueOf(entry.volumeMl));
        etEditAbv.setText(String.valueOf(entry.abvPercent));
        tvEditCount.setText("1");

        etEditAbv.setVisibility(View.VISIBLE);
        llEditPopup.setVisibility(View.VISIBLE);
    }

    private void hideEditPopup() {
        llEditPopup.setVisibility(View.GONE);
        editingIndex = -1;
        editCount    = 1;
    }

    private void saveEdit() {
        if (editingIndex < 0) return;

        List<DrinkEntry> drinks = viewModel.getDrinks().getValue();
        if (drinks == null || editingIndex >= drinks.size()) return;

        DrinkEntry original = drinks.get(editingIndex);

        String volStr = etEditVolume.getText().toString().trim();
        if (volStr.isEmpty()) {
            etEditVolume.setError(getString(R.string.please_enter_volume));
            return;
        }
        int newVolume = Integer.parseInt(volStr);

        double newAbv = original.abvPercent;
        if (!original.isWater) {
            String abvStr = etEditAbv.getText().toString().trim();
            if (abvStr.isEmpty()) {
                etEditAbv.setError(getString(R.string.please_enter_abv));
                return;
            }
            newAbv = Double.parseDouble(abvStr);
        }

        viewModel.removeDrink(editingIndex, userPrefs.getWeight(), userPrefs.getSex());

        for (int i = 0; i < editCount; i++) {
            viewModel.addDrink(
                    new DrinkEntry(original.name, newAbv, newVolume, false),
                    userPrefs.getWeight(), userPrefs.getSex());
        }

        hideEditPopup();
        Toast.makeText(requireContext(),
                getString(R.string.updated), Toast.LENGTH_SHORT).show();
    }

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
        setProgressBarColor(progressHydration, Color.parseColor(hydraColor));
        tvHydrationPercent.setText(hydration + "%");
        tvHydrationPercent.setTextColor(Color.parseColor(hydraColor));
    }

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