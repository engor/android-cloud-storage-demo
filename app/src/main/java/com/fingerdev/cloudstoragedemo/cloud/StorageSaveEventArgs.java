package com.fingerdev.cloudstoragedemo.cloud;

import com.fingerdev.cloudstoragedemo.events.EventArgs;

/**
 * Created by nerobot on 31.03.2016.
 */
public class StorageSaveEventArgs extends EventArgs {

    private String error;

    public StorageSaveEventArgs(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
