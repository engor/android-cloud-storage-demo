package com.fingerdev.cloudstoragedemo.cloud;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fingerdev.cloudstoragedemo.events.INotifier;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by nerobot on 31.03.2016.
 */
public class GoogleDriveHelper implements ICloudApi {

    private GoogleApiClient googleApiClient;
    private Activity activity;
    private int requestCode;
    private Runnable runMeWhenConnected;

    public GoogleDriveHelper(Activity activity, int requestCode) {

        this.activity = activity;
        this.requestCode = requestCode;

    }

    @Override
    public void connect() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // using the appFolder
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
        }

        googleApiClient.connect();
    }

    @Override
    public void dispose() {
        if (googleApiClient != null)
            googleApiClient.disconnect();
    }

    /** ConnectionCallbacks */
    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if (runMeWhenConnected != null) {
                runMeWhenConnected.run();
                runMeWhenConnected = null;
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    /** OnConnectionFailedListener */
    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (connectionResult.hasResolution()) {
                try {
                    //Log.v("driveHelper", "try to resolve: "+connectionResult.getErrorMessage());
                    connectionResult.startResolutionForResult(activity, requestCode);
                } catch (IntentSender.SendIntentException e) {
                    // Unable to resolve, message user appropriately
                }
            } else {
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
            }
        }
    };

    @Override
    public void saveFile(final String fileName, final byte[] fileContent, final INotifier<StorageSaveEventArgs> resultListener) {
        // if is connecting now
        if (googleApiClient != null && googleApiClient.isConnecting()) {
            if (resultListener != null)
                resultListener.notify(new StorageSaveEventArgs("Please, wait while current task ended."));
            return;
        }
        if (googleApiClient == null) {
            // if not connected - statrt connection and create pending operation
            connect();
            runMeWhenConnected = new Runnable() {
                @Override
                public void run() {
                    saveFileInternal(fileName, fileContent, resultListener);
                }
            };
        } else if (googleApiClient.isConnected()) {
            // if connected - do operation right now
            saveFileInternal(fileName, fileContent, resultListener);
        } else {
            if (resultListener != null)
                resultListener.notify(new StorageSaveEventArgs("Error - google drive isn't set up!"));
        }
    }

    private void saveFileInternal(final String fileName, final byte[] fileContent, final INotifier<StorageSaveEventArgs> resultListener) {
        final MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .setMimeType("text/plain")
                .build();
        Drive.DriveApi.getAppFolder(googleApiClient)
                .createFile(googleApiClient, changeSet, null)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(@NonNull final DriveFolder.DriveFileResult driveFileResult) {
                        if (!driveFileResult.getStatus().isSuccess()) {
                            if (resultListener != null)
                                resultListener.notify(new StorageSaveEventArgs("Error while trying to create the file!"));
                            return;
                        }
                        driveFileResult.getDriveFile().open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                                if (!driveContentsResult.getStatus().isSuccess()) {
                                    if (resultListener != null)
                                        resultListener.notify(new StorageSaveEventArgs("Can't open requested file!"));
                                    return;
                                }
                                DriveContents contents = driveContentsResult.getDriveContents();
                                try {
                                    ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                                    FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                                    fileOutputStream.write(fileContent);
                                    fileOutputStream.flush();
                                    parcelFileDescriptor.close();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // закинули данные, коммитим
                                contents.commit(googleApiClient, changeSet).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        if (resultListener != null)
                                            resultListener.notify(new StorageSaveEventArgs(status.isSuccess() ? null : status.getStatusMessage()));
                                    }
                                });
                            }
                        });
                    }
                });
    }

    @Override
    public void loadFile(final String fileName, final INotifier<StorageLoadEventArgs> resultListener) {
        // if is connecting now
        if (googleApiClient != null && googleApiClient.isConnecting()) {
            if (resultListener != null)
                resultListener.notify(new StorageLoadEventArgs(null, "Please, wait while current task ended."));
            return;
        }
        if (googleApiClient == null) {
            // if not connected - statrt connection and create pending operation
            connect();
            runMeWhenConnected = new Runnable() {
                @Override
                public void run() {
                    loadFileInternal(fileName, resultListener);
                }
            };
        } else if (googleApiClient.isConnected()) {
            // if connected - do operation right now
            loadFileInternal(fileName, resultListener);
        } else {
            if (resultListener != null)
                resultListener.notify(new StorageLoadEventArgs(null, "Error - google drive isn't set up!"));
        }
    }

    public void loadFileInternal(final String fileName, final INotifier<StorageLoadEventArgs> resultListener) {
        Drive.DriveApi.getAppFolder(googleApiClient).listChildren(googleApiClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                if (!metadataBufferResult.getStatus().isSuccess()) {
                    if (resultListener != null)
                        resultListener.notify(new StorageLoadEventArgs(null, "Can't load list of files!"));
                    return;
                }
                /** check list of files */
                int count = metadataBufferResult.getMetadataBuffer().getCount();
                DriveId driveId = null;
                for (int k = 0; k < count; ++k) {
                    Metadata meta = metadataBufferResult.getMetadataBuffer().get(k);
                    String t = meta.getTitle();
                    if (t.equals(fileName)) {
                        driveId = meta.getDriveId();
                        break;
                    }
                }
                if (driveId != null) {
                    DriveFile file = driveId.asDriveFile();
                    /** read file */
                    file.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                            if (!driveContentsResult.getStatus().isSuccess()) {
                                if (resultListener != null)
                                    resultListener.notify(new StorageLoadEventArgs(null, "Can't open requested file!"));
                                return;
                            }
                            // DriveContents object contains pointers
                            // to the actual byte stream
                            DriveContents contents = driveContentsResult.getDriveContents();
                            try {
                                ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                                FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                // Read to the end of the file.
                                byte[] buf = new byte[fileInputStream.available()];
                                fileInputStream.read(buf);
                                if (resultListener != null)
                                    resultListener.notify(new StorageLoadEventArgs(buf, null));
                                parcelFileDescriptor.close();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    if (resultListener != null)
                        resultListener.notify(new StorageLoadEventArgs(null, "Requested file not found on the server!"));
                }
            }
        });
    }
}
