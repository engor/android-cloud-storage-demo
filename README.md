# android-cloud-storage-demo
Demo of using Google Drive API for saving and loading files in AppFolder scope.

Read documentation: https://developers.google.com/drive/android/get-started


## What do you need to use

1. Create OAuth 2.0 client ID - read about it by the link above.
2. Add in gradle 
dependencies {
    compile 'com.google.android.gms:play-services-drive:8.4.0'
}
3. Add permission into manifest
android.permission.INTERNET
android.permission.GET_ACCOUNTS


## What's inside

There are 2 interfaces for working with cloud - ICloudApi and INotifier.

```java
public interface ICloudApi extends IDisposable {
    void saveFile(String fileName, byte[] fileContent, INotifier<StorageSaveEventArgs> resultListener);
    void loadFile(String fileName, INotifier<StorageLoadEventArgs> resultListener);
    void connect();
}

public interface INotifier<TArgs extends EventArgs> {
    void notify(TArgs args);
}
```

Using class CloudStorage we can replace inner realization without changing outside logic.
So it possible to replace Drive API with other one - easy.

```java
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
```
