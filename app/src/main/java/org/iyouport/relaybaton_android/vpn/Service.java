package org.iyouport.relaybaton_android.vpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class Service extends VpnService implements Handler.Callback {
    private Thread thread;

    @Override
    public boolean handleMessage(@NonNull Message message) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "org.iyouport.relaybaton_android.vpn.STOP".equals(intent.getAction())) {
            CharSequence text = "Relaybaton Stopped";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.getApplicationContext(), text, duration);
            toast.show();
            thread.interrupt();
            return START_NOT_STICKY;
        } else {
            CharSequence text = "Relaybaton Started";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.getApplicationContext(), text, duration);
            toast.show();
            thread = new Thread(new Connection(this), "VPNThread");
            thread.start();
            return START_STICKY;
        }
    }
}
