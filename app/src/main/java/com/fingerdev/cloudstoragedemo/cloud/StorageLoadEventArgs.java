package com.fingerdev.cloudstoragedemo.cloud;

import com.fingerdev.cloudstoragedemo.events.EventArgs;

/**
 * Created by nerobot on 31.03.2016.
 */
public class StorageLoadEventArgs extends EventArgs {

    private byte[] data;
    private String error;

    public StorageLoadEventArgs(byte[] data, String error) {
        this.data = data;
        this.error = error;
    }

    public byte[] getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
