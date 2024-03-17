package com.mediteam.meditime.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mediteam.meditime.Activity.ScheduleReceipt;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.Helper.ScheduleItem;
import com.mediteam.meditime.R;

import java.util.ArrayList;
import java.util.Arrays;

public class PillViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<ScheduleItem> items;
    MedReminder items2;
    Context context;
    private static final int VIEW_TYPE_EVERYDAY = 1;
    private static final int VIEW_TYPE_CUSTOM = 2;

    public PillViewAdapter (ArrayList<ScheduleItem> items, MedReminder items2) {
        this.items = items;
        this.items2 = items2;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
//        View inflate = LayoutInflater.from(context).inflate(R.layout.receipt_item_everyday, parent, false);
//        return new viewholder(inflate);

        if (viewType == VIEW_TYPE_EVERYDAY) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.receipt_item_everyday, parent, false);
            return new EverydayViewHolder(inflate);
        } else if (viewType == VIEW_TYPE_CUSTOM) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.receipt_item_custom, parent, false);
            return new CustomViewHolder(inflate);
        }
        return null;
    }

    @Override
    public void onBindViewHolder (@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == VIEW_TYPE_EVERYDAY) {

            ((EverydayViewHolder) holder).everydayTime.setText(items.get(position).getTime());
            ((EverydayViewHolder) holder).everydayPill.setText(String.valueOf(items.get(position).getPillQuantities()));
        } else if (viewType == VIEW_TYPE_CUSTOM) {
            ((CustomViewHolder) holder).customDay.setText(items.get(position).getDay());
            ((CustomViewHolder) holder).customTime.setText(items.get(position).getTime());
            ((CustomViewHolder) holder).customPill.setText(String.valueOf(items.get(position).getPillQuantities()));
        }
    }

    @Override
    public int getItemCount () {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        String repeat = items2.getRepeat();
        if (repeat.equals("Everyday")) {
            return VIEW_TYPE_EVERYDAY;
        } else if (repeat.equals("Custom")) {
            return VIEW_TYPE_CUSTOM;
        }
        return super.getItemViewType(position);
    }

    public class EverydayViewHolder extends RecyclerView.ViewHolder{
        TextView everydayTime, everydayPill;

        public EverydayViewHolder (@NonNull View itemView) {
            super(itemView);
            everydayTime = itemView.findViewById(R.id.receipt_everyday_time);
            everydayPill = itemView.findViewById(R.id.receipt_everyday_pill);
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView customDay, customTime, customPill;

        public CustomViewHolder (@NonNull View itemView) {
            super(itemView);
            customDay = itemView.findViewById(R.id.receipt_custom_day);
            customTime = itemView.findViewById(R.id.receipt_custom_time);
            customPill = itemView.findViewById(R.id.receipt_custom_pill);
        }
    }
}
