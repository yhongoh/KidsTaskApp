package jp.happyandsmile.kidstaskapp;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by hongoyuka on 2018/02/16.
 */

public class TaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}