package com.example.rehydro.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.rehydro.R;
import com.example.rehydro.data.db.AppDatabase;
import com.example.rehydro.data.entity.LocationEntry;
import com.example.rehydro.data.entity.SessionDrinkItemEntity;
import com.example.rehydro.data.entity.SessionLogEntity;
import com.example.rehydro.data.prefs.UserPrefs;
import com.example.rehydro.model.DrinkEntry;
import com.example.rehydro.util.HydrationCalculator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionDetailFragment extends Fragment implements OnMapReadyCallback {

    private int sessionId = 0;

    private TextView tvDate;
    private TextView tvDuration;
    private TextView tvDrinkCount;
    private LinearLayout llDrinkRows;
    private TextView tvWaterNeeded;
    private TextView tvWaterConsumed;
    private TextView tvBacPeak;
    private TextView tvHydrationPercent;
    private ProgressBar progressHydration;
    private ImageView btnBack;
    private MapView mapView;

    private GoogleMap googleMap;
    private List<LocationEntry> locationEntries = new ArrayList<>();

    private List<SessionDrinkItemEntity> drinkItems = new ArrayList<>();
    private SessionLogEntity session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_detail,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            sessionId = getArguments().getInt("sessionId", 0);
        }

        bindViews(view);

        // Динамичен padding за navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(requireView(), (v, insets) -> {
            int navBarHeight = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()).bottom;
            View scrollContent = requireView().findViewById(R.id.scrollContent);
            if (scrollContent != null) {
                scrollContent.setPadding(
                        dp(16), dp(6), dp(16), navBarHeight + dp(24));
            }
            return insets;
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        btnBack.setOnClickListener(v ->
                requireActivity().onBackPressed());

        loadSessionData();
    }

    private void bindViews(View view) {
        tvDate             = view.findViewById(R.id.tvDate);
        tvDuration         = view.findViewById(R.id.tvDuration);
        tvDrinkCount       = view.findViewById(R.id.tvDrinkCount);
        llDrinkRows        = view.findViewById(R.id.llDrinkRows);
        tvWaterNeeded      = view.findViewById(R.id.tvWaterNeeded);
        tvWaterConsumed    = view.findViewById(R.id.tvWaterConsumed);
        tvBacPeak          = view.findViewById(R.id.tvBacPeak);
        tvHydrationPercent = view.findViewById(R.id.tvHydrationPercent);
        progressHydration  = view.findViewById(R.id.progressHydration);
        btnBack            = view.findViewById(R.id.btnBack);
        mapView            = view.findViewById(R.id.mapView);
    }

    private void loadSessionData() {
        new Thread(() -> {
            session = AppDatabase.getInstance(requireContext())
                    .sessionDao()
                    .getSessionById(sessionId);

            drinkItems = AppDatabase.getInstance(requireContext())
                    .sessionDao()
                    .getDrinksForSession(sessionId);

            locationEntries = AppDatabase.getInstance(requireContext())
                    .locationDao()
                    .getLocationsForSession(sessionId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    populateHeader();
                    populateDrinkRows();
                    populateSummary();
                    updateMap();
                });
            }
        }).start();
    }

    private void populateHeader() {
        if (session == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(session.startTimestamp)));

        long durationMs = session.endTimestamp - session.startTimestamp;
        long hours   = durationMs / 3_600_000;
        long minutes = (durationMs % 3_600_000) / 60_000;
        tvDuration.setText(hours + "h " + minutes + "m");

        tvDrinkCount.setText(String.valueOf(session.drinkCount));
    }

    private void populateDrinkRows() {
        llDrinkRows.removeAllViews();
        if (drinkItems == null) return;

        for (SessionDrinkItemEntity item : drinkItems) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(8), 0, dp(8));

            TextView tvName = new TextView(requireContext());
            tvName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvName.setTextSize(12f);

            if (item.isWater) {
                tvName.setText(getString(R.string.water));
                tvName.setTextColor(Color.parseColor("#5ab4ff"));
            } else {
                tvName.setText(item.drinkName);
                tvName.setTextColor(Color.parseColor("#ccccee"));
            }

            TextView tvMl = new TextView(requireContext());
            tvMl.setLayoutParams(new LinearLayout.LayoutParams(
                    dp(56), LinearLayout.LayoutParams.WRAP_CONTENT));
            tvMl.setText(item.volumeMl + " ml");
            tvMl.setTextColor(Color.parseColor("#666688"));
            tvMl.setTextSize(11f);
            tvMl.setGravity(android.view.Gravity.END);

            TextView tvAbv = new TextView(requireContext());
            tvAbv.setLayoutParams(new LinearLayout.LayoutParams(
                    dp(56), LinearLayout.LayoutParams.WRAP_CONTENT));
            tvAbv.setText(item.isWater ? "—" : item.abvPercent + "%");
            tvAbv.setTextColor(Color.parseColor("#666688"));
            tvAbv.setTextSize(11f);
            tvAbv.setGravity(android.view.Gravity.END);

            row.addView(tvName);
            row.addView(tvMl);
            row.addView(tvAbv);

            View divider = new View(requireContext());
            LinearLayout.LayoutParams divParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
            divider.setLayoutParams(divParams);
            divider.setBackgroundColor(Color.parseColor("#22223a"));

            llDrinkRows.addView(row);
            llDrinkRows.addView(divider);
        }
    }

    private void populateSummary() {
        if (session == null) return;

        tvWaterNeeded.setText(session.waterNeededMl + " ml");
        tvWaterConsumed.setText(session.waterConsumedMl + " ml");

        if (drinkItems != null && !drinkItems.isEmpty()) {
            UserPrefs prefs = new UserPrefs(requireContext());
            List<DrinkEntry> entries = new ArrayList<>();
            for (SessionDrinkItemEntity item : drinkItems) {
                entries.add(new DrinkEntry(
                        item.drinkName,
                        item.abvPercent,
                        item.volumeMl,
                        item.isWater));
            }
            float bac = HydrationCalculator.calculateBAC(
                    entries, prefs.getWeight(), prefs.getSex());
            tvBacPeak.setText(String.format("%.2f g/100ml", bac));
        } else {
            tvBacPeak.setText("0.00 g/100ml");
        }

        int hydration = 0;
        if (session.waterNeededMl > 0) {
            hydration = Math.min(Math.round(
                            (session.waterConsumedMl / (float) session.waterNeededMl) * 100),
                    100);
        }

        progressHydration.setProgress(hydration);
        tvHydrationPercent.setText(hydration + "%");

        String color;
        if (hydration >= 80)      color = "#34d399";
        else if (hydration >= 40) color = "#fbbf24";
        else                      color = "#ef4444";
        tvHydrationPercent.setTextColor(Color.parseColor(color));
    }

    private void updateMap() {
        if (googleMap == null) return;

        if (locationEntries == null || locationEntries.isEmpty()) {
            if (mapView != null) mapView.setVisibility(View.GONE);
            return;
        }

        if (mapView != null) mapView.setVisibility(View.VISIBLE);

        googleMap.clear();

        List<LatLng> points = new ArrayList<>();
        for (LocationEntry entry : locationEntries) {
            points.add(new LatLng(entry.latitude, entry.longitude));
        }

        PolylineOptions polyline = new PolylineOptions()
                .addAll(points)
                .color(Color.parseColor("#5ab4ff"))
                .width(6f);
        googleMap.addPolyline(polyline);

        for (int i = 0; i < locationEntries.size(); i++) {
            LocationEntry entry = locationEntries.get(i);
            LatLng point = new LatLng(entry.latitude, entry.longitude);

            SimpleDateFormat sdf = new SimpleDateFormat(
                    "HH:mm", Locale.getDefault());
            String time = sdf.format(new Date(entry.timestamp));

            float markerColor = (i == 0)
                    ? BitmapDescriptorFactory.HUE_GREEN
                    : (i == locationEntries.size() - 1)
                    ? BitmapDescriptorFactory.HUE_RED
                    : BitmapDescriptorFactory.HUE_AZURE;

            googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(i == 0 ? "Start"
                            : i == locationEntries.size() - 1 ? "End"
                            : "Stop " + i)
                    .snippet(time)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }

        if (points.size() == 1) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    points.get(0), 15f));
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : points) builder.include(point);
            LatLngBounds bounds = builder.build();
            mapView.post(() -> {
                try {
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, dp(60)));
                } catch (Exception e) {
                    googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(points.get(0), 14f));
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (mapView != null) {
                mapView.getParent().requestDisallowInterceptTouchEvent(true);
            }
        });

        googleMap.setOnCameraIdleListener(() -> {
            if (mapView != null) {
                mapView.getParent().requestDisallowInterceptTouchEvent(false);
            }
        });

        try {
            googleMap.setMapStyle(
                    com.google.android.gms.maps.model.MapStyleOptions
                            .loadRawResourceStyle(requireContext(), R.raw.map_style));
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}