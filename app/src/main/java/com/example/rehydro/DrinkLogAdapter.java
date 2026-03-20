package com.example.rehydro;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DrinkLogAdapter extends
        RecyclerView.Adapter<DrinkLogAdapter.ViewHolder> {

    private List<DrinkEntry> items = new ArrayList<>();

    public void setItems(List<DrinkEntry> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drink_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrinkEntry entry = items.get(position);

        if (entry.isWater) {
            holder.tvDrinkName.setText("Water");
            holder.tvDrinkName.setTextColor(Color.parseColor("#5ab4ff"));
            holder.tvDrinkDetail.setText(entry.volumeMl + " ml");
            holder.tvDrinkWaterImpact.setText("+" + entry.volumeMl + " ml");
            holder.tvDrinkWaterImpact.setTextColor(Color.parseColor("#34d399"));
        } else {
            holder.tvDrinkName.setText(entry.name);
            holder.tvDrinkName.setTextColor(Color.parseColor("#ccccee"));

            holder.tvDrinkDetail.setText(
                    entry.volumeMl + " ml · " + entry.abvPercent + "%");

            // Calculate how much water this drink costs
            double pureAlcoholMl = (entry.abvPercent / 100.0) * entry.volumeMl;
            double grams = pureAlcoholMl * 0.789;
            int waterCost = (int) Math.round(grams * 8.0);
            holder.tvDrinkWaterImpact.setText("−" + waterCost + " ml");
            holder.tvDrinkWaterImpact.setTextColor(Color.parseColor("#444466"));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrinkName, tvDrinkDetail, tvDrinkWaterImpact;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDrinkName        = itemView.findViewById(R.id.tvDrinkName);
            tvDrinkDetail      = itemView.findViewById(R.id.tvDrinkDetail);
            tvDrinkWaterImpact = itemView.findViewById(R.id.tvDrinkWaterImpact);
        }
    }
}