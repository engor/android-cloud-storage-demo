package com.fingerdev.cloudstoragedemo.events;

/**
 * Created by nerobot on 17.10.2015.
 */
public interface INotifier<TArgs extends EventArgs> {
    void notify(TArgs args);
}
