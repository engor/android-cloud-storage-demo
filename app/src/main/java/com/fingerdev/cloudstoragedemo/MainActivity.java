package com.fingerdev.cloudstoragedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.fingerdev.cloudstoragedemo.cloud.CloudStorage;
import com.fingerdev.cloudstoragedemo.cloud.StorageLoadEventArgs;
import com.fingerdev.cloudstoragedemo.cloud.StorageSaveEventArgs;
import com.fingerdev.cloudstoragedemo.events.INotifier;

public class MainActivity extends AppCompatActivity {

    private static final int RC_CLOUD_STORAGE = 2;

    private CloudStorage cloudStorage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** save */
        View buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit = (EditText) findViewById(R.id.editTextForSave);
                String text = edit.getText().toString();
                edit = (EditText) findViewById(R.id.editTextFileName);
                String fileName = edit.getText().toString();
                // save to cloud
                showCloudProgressDialog();
                getCloudStorage().saveFile(fileName, text.getBytes(), getStorageSaveListener());
            }
        });

        /** load */
        View buttonLoad = findViewById(R.id.buttonLoad);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit;
                edit = (EditText) findViewById(R.id.editTextFileName);
                String fileName = edit.getText().toString();
                // load from cloud
                showCloudProgressDialog();
                getCloudStorage().loadFile(fileName, getStorageLoadListener());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.v("main", "activity result: "+requestCode+","+resultCode);

        switch (requestCode) {

            case RC_CLOUD_STORAGE:
                getCloudStorage().connect();
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private CloudStorage getCloudStorage() {
        if (cloudStorage == null) {
            cloudStorage = new CloudStorage.Builder(this).setRequestCode(RC_CLOUD_STORAGE).build();
        }
        return cloudStorage;
    }

    private ProgressDialog showCloudProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Cloud Storage");
            progressDialog.setMessage("Operation is in progress.\nPlease, wait...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
        return progressDialog;
    }

    private void hideCloudProgressDialog() {
        if (progressDialog != null)
            progressDialog.hide();
    }

    private INotifier<StorageLoadEventArgs> storageLoadListener;
    private INotifier<StorageLoadEventArgs> getStorageLoadListener() {
        if (storageLoadListener == null) {
            storageLoadListener = new INotifier<StorageLoadEventArgs>() {
                @Override
                public void notify(StorageLoadEventArgs args) {
                    hideCloudProgressDialog();
                    String err = args.getError();
                    EditText edit = (EditText) findViewById(R.id.editTextLoaded);
                    if (err == null) {
                        String data = new String(args.getData());
                        edit.setText(data);
                    } else {
                        showDialog("Storage.Load", err);
                    }
                }
            };
        }
        return storageLoadListener;
    }

    private INotifier<StorageSaveEventArgs> storageSaveListener;
    private INotifier<StorageSaveEventArgs> getStorageSaveListener() {
        if (storageSaveListener == null) {
            storageSaveListener = new INotifier<StorageSaveEventArgs>() {
                @Override
                public void notify(StorageSaveEventArgs args) {
                    hideCloudProgressDialog();
                    String err = args.getError();
                    if (err == null) {
                        showDialog("Storage.Save", "Successfully saved");
                    } else {
                        showDialog("Storage.Save", err);
                    }
                }
            };
        }
        return storageSaveListener;
    }

    private void showDialog(String title, String text) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("OK", null).create().show();
    }

}
