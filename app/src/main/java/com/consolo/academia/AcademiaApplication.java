package com.consolo.academia;

import android.app.Application;
import android.content.Intent;

import io.realm.Realm;

public class AcademiaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        MyFirebaseInstanceService myFirebaseInstanceService = new MyFirebaseInstanceService();
        Intent intent= new Intent(getApplicationContext(), myFirebaseInstanceService.getClass());
        startService(intent);
    }
}
