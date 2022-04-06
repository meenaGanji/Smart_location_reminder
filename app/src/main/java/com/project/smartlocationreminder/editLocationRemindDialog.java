package com.project.smartlocationreminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class editLocationRemindDialog {
    Context context;
    Dialog dialog;
    EditText nameEt;
    Button btnSave, btnCancel;
    ImageView closeIv, dateIv, timeIv;
    TextView dateTv, timeTv;
    RefreshReminderList refreshReminderList;
    String selectedDate;
    String selectedTime;

    public editLocationRemindDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        refreshReminderList= (RefreshReminderList) context;
    }

    public void Update(Reminder previousReminder) {
        dialog.setContentView(R.layout.dialog_edit_reminder);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        nameEt = dialog.findViewById(R.id.nameEt);
        btnSave = dialog.findViewById(R.id.btnSave);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        closeIv = dialog.findViewById(R.id.closeIv);


        dateIv = dialog.findViewById(R.id.dateIv);
        dateTv = dialog.findViewById(R.id.dateTv);

        timeIv = dialog.findViewById(R.id.timeIv);
        timeTv = dialog.findViewById(R.id.timeTv);

        nameEt.setText(previousReminder.getTitle());
        timeTv.setText(previousReminder.getTime());
        dateTv.setText(previousReminder.getDate());
        selectedDate=previousReminder.getDate();
        selectedTime=previousReminder.getTime();

//        dateIv.setOnClickListener(view -> SelectDate());
//
//        dateTv.setOnClickListener(view -> SelectDate());
//
//        timeIv.setOnClickListener(view -> SelectTime());
//
//        timeTv.setOnClickListener(view -> SelectTime());

        selectedDate = getDate();
        selectedTime = getTime();

        timeIv = dialog.findViewById(R.id.timeIv);
        timeTv = dialog.findViewById(R.id.timeTv);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = nameEt.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(context, "Please add title!", Toast.LENGTH_SHORT).show();
                }else if (selectedDate==null)
                {
                    Toast.makeText(context, "Select Date", Toast.LENGTH_SHORT).show();
                }else if (selectedTime==null)
                {
                    Toast.makeText(context, "Select Time", Toast.LENGTH_SHORT).show();
                } else {
                    String location_id = System.currentTimeMillis() + "reminder";
                    Reminder reminder = new Reminder(location_id,title,selectedDate,selectedTime,previousReminder.getLatitude(),previousReminder.getLongitude());
                    new MyDb(context).updateReminder(previousReminder.getLocation_id(),reminder);
                    refreshReminderList.updatedItem(reminder.getLocation_id());
                    dialog.dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());
        closeIv.setOnClickListener(view -> dialog.dismiss());

        dialog.create();
        dialog.show();
    }


    private void SelectDate() {
        final Calendar currentDate = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            dateTv.setText(dayOfMonth+"-"+monthOfYear+"-"+year);
            selectedDate=dayOfMonth+"-"+monthOfYear+"-"+year;

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void SelectTime() {
        final Calendar currentDate = Calendar.getInstance();
        Calendar date = Calendar.getInstance();

        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);

                timeTv.setText(hourOfDay+":" +minute);
                selectedTime=hourOfDay+":" +minute;

            }
        }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
    }

    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);

    }

    private String getTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(date);

    }
}
