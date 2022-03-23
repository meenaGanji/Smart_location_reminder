package com.project.smartlocationreminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ListReminderActivity extends AppCompatActivity implements RefreshReminderList{

    RecyclerView recyclerView;
    ImageView backIv;
    FloatingActionButton fabAddLocationReminder;
    List<Reminder>list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_reminder);


        recyclerView=findViewById(R.id.recyclerview);
        backIv=findViewById(R.id.backButton);
        fabAddLocationReminder=findViewById(R.id.addLocationReminder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backIv.setOnClickListener(view -> onBackPressed());
        LoadReminders();
        fabAddLocationReminder.setOnClickListener(view -> startActivity(new Intent(ListReminderActivity.this, AddReminderActivity.class)));

    }

    private void LoadReminders() {
        list=new ArrayList<>();
        list.clear();
        list=new MyDb(ListReminderActivity.this).getReminderList();
        recyclerView.setAdapter(new ReminderAdapter(list,ListReminderActivity.this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadReminders();
    }

    @Override
    public void deletedItem(String location_id) {
        LoadReminders();
    }

    @Override
    public void updatedItem(String location_id) {
        LoadReminders();
    }
}