package org.iyouport.relaybaton_android;

import android.content.Context;
import android.widget.Toast;

import relaybaton_mobile.RelaybatonAndroid;

public class AppRun implements Runnable {
    private Context context;
    private RelaybatonAndroid ra;

    public AppRun(Context applicationContext, RelaybatonAndroid ra) {
        this.context = applicationContext;
        this.ra = ra;
    }

    @Override
    public void run() {
        try {
            ra.run();
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.context, e.getLocalizedMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
