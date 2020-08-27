package com.arobit.chatall;

import com.google.firebase.database.FirebaseDatabase;

public class OfflineSync extends android.app.Application  {

    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
