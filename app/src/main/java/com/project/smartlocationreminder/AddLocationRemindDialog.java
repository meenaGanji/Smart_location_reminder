package com.project.smartlocationreminder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

public class AddLocationRemindDialog {
    Context context;
    Dialog dialog;
    EditText nameEt;
    Button btnSave,btnCancel;
    ImageView closeIv;

    public AddLocationRemindDialog(Context context) {
        this.context = context;
        dialog=new Dialog(context);
    }

    public void Save(LatLng latLng){
        dialog.setContentView(R.layout.dialog_add_reminder);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        nameEt=dialog.findViewById(R.id.nameEt);
        btnSave=dialog.findViewById(R.id.btnSave);
        btnCancel=dialog.findViewById(R.id.btnCancel);
        closeIv=dialog.findViewById(R.id.closeIv);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());
        closeIv.setOnClickListener(view -> dialog.dismiss());

        dialog.create();
        dialog.show();
    }
}