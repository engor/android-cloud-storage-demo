package com.fingerdev.cloudstoragedemo.cloud;

import com.fingerdev.cloudstoragedemo.IDisposable;
import com.fingerdev.cloudstoragedemo.events.INotifier;

/**
 * Created by nerobot on 31.03.2016.
 */
public interface ICloudApi extends IDisposable {

    void saveFile(String fileName, byte[] fileContent, INotifier<StorageSaveEventArgs> resultListener);
    void loadFile(String fileName, INotifier<StorageLoadEventArgs> resultListener);
    void connect();

}
