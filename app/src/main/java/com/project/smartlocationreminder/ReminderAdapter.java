package com.project.smartlocationreminder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.MyViewHolder> {
    List<Reminder> list;
    Context context;
    RefreshReminderList refreshReminderList;

    public ReminderAdapter(List<Reminder> list, Context context) {
        this.list = list;
        this.context = context;
        refreshReminderList= (RefreshReminderList) context;
    }

    @NonNull
    @Override
    public ReminderAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_reminder, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.titleTv.setText(list.get(position).getTitle());
        holder.latLngTv.setText(list.get(position).getLatitude() + ", " + list.get(position).getLongitude());
        holder.deleteReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Alert!!!");
                builder.setMessage("Are you sure you want to delete?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new MyDb(context).deleteReminder(list.get(position).location_id);
                        refreshReminderList.deletedItem(list.get(position).getLocation_id());
                       // notifyItemRemoved(position);

                    }
                }).setNegativeButton("No",null);
                builder.create();
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv, latLngTv;
        ImageView deleteReminder, editReminder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            latLngTv = itemView.findViewById(R.id.latLngTv);
            editReminder = itemView.findViewById(R.id.editReminder);
            deleteReminder = itemView.findViewById(R.id.deletReminder);
        }
    }
}
