package org.iyouport.relaybaton_android.vpn;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import org.iyouport.relaybaton_android.AppRun;
import org.iyouport.relaybaton_android.RelaybatonApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import relaybaton_mobile.RelaybatonAndroid;

public class Connection implements Runnable {
    private Service mService;

    Connection(Service mService) {
        this.mService = mService;
    }

    @Override
    public void run() {
        try {
            RelaybatonAndroid ra = ((RelaybatonApplication) mService.getApplication()).ra;
            ParcelFileDescriptor iface;
            // Authenticate and configure the virtual network interface.
            VpnService.Builder builder = mService.new Builder();
            if (ra.getDNSType().equals("default")) {
                iface = builder
                        .addAddress("172.19.0.1", 30)
                        .addRoute("0.0.0.0", 0)
                        .addDisallowedApplication("org.iyouport.relaybaton_android")
                        .establish();
            } else {
                iface = builder
                        .addAddress("172.19.0.1", 30)
                        .addRoute("0.0.0.0", 0)
                        .addDnsServer("172.19.0.2")
                        .addDisallowedApplication("org.iyouport.relaybaton_android")
                        .establish();
            }
            assert iface != null;
            FileInputStream in = new FileInputStream(iface.getFileDescriptor());
            final FileOutputStream[] out = {new FileOutputStream(iface.getFileDescriptor())};
            new Thread(new AppRun(mService.getApplicationContext(), ra)).start();
            ra.startSocks(packet -> {
                try {
                    out[0].write(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (!Thread.interrupted()) {
                        out[0] = new FileOutputStream(iface.getFileDescriptor());
                    }
                }
            }, "127.0.0.1", 1080);
            ByteBuffer packet = ByteBuffer.allocate(2 << 16);
            while (!Thread.interrupted()) {
                try {
                    int length = in.read(packet.array());
                    if (length > 0 && packet.get(0) != 0) {
                        ra.inputPacket(Arrays.copyOfRange(packet.array(), 0, length));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    in = new FileInputStream(iface.getFileDescriptor());
                }
            }
            ra.shutdown();
            in.close();
            out[0].close();
            iface.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
