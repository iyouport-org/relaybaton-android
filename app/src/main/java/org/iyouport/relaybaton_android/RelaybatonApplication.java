package org.iyouport.relaybaton_android;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import relaybaton_mobile.RelaybatonAndroid;

public class RelaybatonApplication extends Application {
    public RelaybatonAndroid ra;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            FileInputStream fis;
            try {
                fis = openFileInput("config.toml");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                copyAssets();
                fis = openFileInput("config.toml");
            }
            assert fis != null;
            BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            String content = sb.toString();
            this.ra = relaybaton_mobile.Relaybaton_mobile.newAndroid(content);
            buf.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                in = assetManager.open(filename);
                out = openFileOutput(filename, Context.MODE_PRIVATE);
                copyFile(in, out);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
