package org.iyouport.relaybaton_android;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.iyouport.relaybaton_android.vpn.Service;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingsFragment settingsFragment = new SettingsFragment(((RelaybatonApplication) MainActivity.super.getApplication()).ra);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_main, settingsFragment)
                .commit();

        final FloatingActionButton fabStart = findViewById(R.id.fab_start);
        final FloatingActionButton fabStop = findViewById(R.id.fab_stop);
        fabStart.setOnClickListener(view -> {
            try {
                ((RelaybatonApplication) MainActivity.super.getApplication()).ra.save(
                        settingsFragment.getClientServer(),
                        settingsFragment.getClientUsername(),
                        settingsFragment.getClientPassword(),
                        settingsFragment.getClientProxyAll(),
                        settingsFragment.getDNSType(),
                        settingsFragment.getDNSServer(),
                        settingsFragment.getDNSAddr(),
                        settingsFragment.getLogLevel());
                fabStart.setVisibility(View.INVISIBLE);
                fabStop.setVisibility(View.VISIBLE);
                settingsFragment.setAllEnabled(false);
                Intent intent = VpnService.prepare(MainActivity.this);
                if (intent != null) {
                    startActivityForResult(intent, 4);
                } else {
                    onActivityResult(1, RESULT_OK, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                settingsFragment.setAllEnabled(true);
                Toast toast = Toast.makeText(this.getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
                toast.show();
            }
        });

        fabStop.setOnClickListener(view -> {
            fabStart.setVisibility(View.VISIBLE);
            fabStop.setVisibility(View.INVISIBLE);
            settingsFragment.setAllEnabled(true);
            Intent intent = VpnService.prepare(MainActivity.this);
            if (intent != null) {
                startActivityForResult(intent, 5);
            } else {
                onActivityResult(0, RESULT_OK, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result == RESULT_OK) {
            switch (request) {
                case 0:
                    startService(new Intent(this, Service.class).setAction("org.iyouport.relaybaton_android.vpn.STOP"));
                    break;
                case 1:
                    startService(new Intent(this, Service.class).setAction("org.iyouport.relaybaton_android.vpn.START"));
                    break;
                default:
            }
        }
    }
}