package com.example.pc.bucketdrops.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.Context;

import com.example.pc.bucketdrops.ActivityMain;
import com.example.pc.bucketdrops.R;
import com.example.pc.bucketdrops.beans.Drop;

import br.com.goncalves.pugnotification.notification.PugNotification;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends IntentService {


    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = null;

        try {

            realm = Realm.getDefaultInstance();
            RealmResults<Drop> mResult = realm.where(Drop.class).equalTo("completed", false).findAll();
            for (Drop current : mResult) {
                if (isNotificationNeeded(current.getAdded(), current.getAdded())) {
                    fireNotification(current);
                }
            }
        }finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void fireNotification(Drop drop) {
        String message = getString(R.string.notif_message) + "\"" + drop.getWhat() + "\"";

        PugNotification.with(this)
                .load()
                .title(R.string.notif_title)
                .message(message)
                .smallIcon(R.drawable.ic_drop)
                .largeIcon(R.drawable.ic_drop)
                .flags(Notification.DEFAULT_ALL)
                .autoCancel(true)
                .click(ActivityMain.class)
                .simple()
                .build();

    }

    private boolean isNotificationNeeded(long added, long when) {
        long now = System.currentTimeMillis();
        if (now > when) {
            return false;
        } else {
            long difference90 = (long) (0.9 * (when - added));
            return (now > (added + difference90)) ? true : false;
        }
    }
}
