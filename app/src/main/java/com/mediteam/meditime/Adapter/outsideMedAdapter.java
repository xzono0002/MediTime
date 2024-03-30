package com.mediteam.meditime.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mediteam.meditime.Activity.OutScheduleReceipt;
import com.mediteam.meditime.Helper.MedReminder;
import com.mediteam.meditime.R;

import java.util.ArrayList;

public class outsideMedAdapter extends RecyclerView.Adapter<outsideMedAdapter.viewholder> {
    ArrayList<MedReminder> items;
    Context context;

    public outsideMedAdapter (ArrayList<MedReminder> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public outsideMedAdapter.viewholder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_pill, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder (@NonNull outsideMedAdapter.viewholder holder, int position) {
        holder.medItem.setText(items.get(position).getMedicine());
        holder.day.setText(items.get(position).getRepeat());

        holder.itemView.setOnClickListener(view -> {
           Intent intent = new Intent(context, OutScheduleReceipt.class);
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
           intent.putExtra("object", items.get(position));
           context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount () {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder{
        TextView medItem, day;

        public viewholder (@NonNull View itemView) {
            super(itemView);
            medItem = itemView.findViewById(R.id.med_item);
            day = itemView.findViewById(R.id.med_schedule);
        }
    }
}
