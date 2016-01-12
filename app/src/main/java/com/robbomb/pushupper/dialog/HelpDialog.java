package com.robbomb.pushupper.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.robbomb.pushupper.R;

/**
 * Created by NewRob on 1/9/2016.
 */
public class HelpDialog {

    public static Dialog showHelp(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View helpLayout = inflater.inflate(R.layout.help_layout, null);

        AlertDialog.Builder db = new AlertDialog.Builder(context);
        db.setView(helpLayout);
        db.setTitle("settings");
        db.setPositiveButton("Got It!", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return db.show();
    }


}
