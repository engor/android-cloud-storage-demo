package com.fingerdev.cloudstoragedemo.cloud;

import android.app.Activity;
import com.fingerdev.cloudstoragedemo.events.INotifier;

/**
 * Created by nerobot on 23.03.2016.
 */
public class CloudStorage implements ICloudApi {

    private ICloudApi cloudApi;

    /** ctor */
    private CloudStorage(Activity activity, int requestCode) {
        cloudApi = new GoogleDriveHelper(activity, requestCode);
    }

    @Override
    public void dispose() {
        cloudApi.dispose();
    }

    @Override
    public void saveFile(String fileName, byte[] fileContent, INotifier<StorageSaveEventArgs> resultListener) {
        cloudApi.saveFile(fileName, fileContent, resultListener);
    }

    @Override
    public void loadFile(String fileName, INotifier<StorageLoadEventArgs> resultListener) {
        cloudApi.loadFile(fileName, resultListener);
    }

    @Override
    public void connect() {
        cloudApi.connect();
    }

    /** Builder */
    public static class Builder {

        private Activity activity;
        private int requestCode;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setRequestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public CloudStorage build() {
            return new CloudStorage(activity, requestCode);
        }
    }

}
