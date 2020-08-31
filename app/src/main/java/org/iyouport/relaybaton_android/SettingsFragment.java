package org.iyouport.relaybaton_android;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import relaybaton_mobile.RelaybatonAndroid;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String DIALOG_FRAGMENT_TAG =
            "androidx.preference.PreferenceFragment.DIALOG";

    private static final String CLIENT_SERVER = "client_server";
    private static final String DNS_SERVER = "dns_server";
    private static final String DNS_ADDR = "dns_addr";
    private RelaybatonAndroid ra;
    private EditTextPreference clientServer;
    private EditTextPreference clientUsername;
    private EditTextPreference clientPassword;
    private SwitchPreferenceCompat clientProxyAll;
    private ListPreference dnsType;
    private EditTextPreference dnsServer;
    private EditTextPreference dnsAddr;
    private ListPreference logLevel;

    SettingsFragment(RelaybatonAndroid ra) {
        this.ra = ra;
    }

    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
     * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           {@link PreferenceScreen} with this key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_main, rootKey);
        clientServer = findPreference(CLIENT_SERVER);
        clientUsername = findPreference("client_username");
        clientPassword = findPreference("client_password");
        clientProxyAll = findPreference("client_proxy_all");
        dnsType = findPreference("dns_type");
        dnsServer = findPreference(DNS_SERVER);
        dnsAddr = findPreference(DNS_ADDR);
        logLevel = findPreference("log_level");
        assert clientServer != null;
        clientServer.setText(ra.getClientServer());
        assert clientUsername != null;
        clientUsername.setText(ra.getClientUsername());
        assert clientPassword != null;
        clientPassword.setText(ra.getClientPassword());
        clientPassword.setSummaryProvider(preference -> {
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < clientPassword.getText().length(); s++) {
                sb.append("*");
            }
            return sb.toString();
        });
        clientPassword.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        assert clientProxyAll != null;
        clientProxyAll.setChecked(ra.getClientProxyAll());
        assert dnsType != null;
        dnsType.setValue(ra.getDNSType());
        assert dnsServer != null;
        dnsServer.setText(ra.getDNSServer());
        assert dnsAddr != null;
        dnsAddr.setText(ra.getDNSAddr());
        assert logLevel != null;
        logLevel.setValue(ra.getLogLevel());
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment())
                    .onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity())
                    .onPreferenceDisplayDialog(this, preference);
        }

        if (handled) {
            return;
        }

        // check if dialog is already showing
        if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        final DialogFragment f;

        switch (preference.getKey()) {
            case CLIENT_SERVER:
            case DNS_SERVER:
            case DNS_ADDR:
                f = LETPDFC.newInstance(preference.getKey(), getTextValidator(preference.getKey()));
                break;
            default:
                if (preference instanceof EditTextPreference) {
                    f = EditTextPreferenceDialogFragmentCompat.newInstance(preference.getKey());
                } else if (preference instanceof ListPreference) {
                    f = ListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
                } else if (preference instanceof MultiSelectListPreference) {
                    f = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
                } else {
                    throw new IllegalArgumentException(
                            "Cannot display dialog for an unknown Preference type: "
                                    + preference.getClass().getSimpleName()
                                    + ". Make sure to implement onPreferenceDisplayDialog() to handle "
                                    + "displaying a custom dialog for this Preference.");
                }
        }
        f.setTargetFragment(this, 0);
        f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    private TextValidator getTextValidator(String key) {
        return text -> {
            try {
                String clientServerStr = getClientServer();
                String dnsServerStr = getDNSServer();
                String dnsAddrStr = getDNSAddr();
                switch (key) {
                    case CLIENT_SERVER:
                        clientServerStr = text;
                        break;
                    case DNS_SERVER:
                        dnsServerStr = text;
                        break;
                    case DNS_ADDR:
                        dnsAddrStr = text;
                        break;
                    default:
                }
                ra.save(
                        clientServerStr,
                        getClientUsername(),
                        getClientPassword(),
                        getClientProxyAll(),
                        getDNSType(),
                        dnsServerStr,
                        dnsAddrStr,
                        getLogLevel());
            } catch (Exception e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
                toast.show();
                return false;
            }
            return true;
        };
    }

    @Override
    public Fragment getCallbackFragment() {
        return null;
    }

    public void setAllEnabled(boolean enabled) {
        clientServer.setEnabled(enabled);
        clientUsername.setEnabled(enabled);
        clientPassword.setEnabled(enabled);
        clientProxyAll.setEnabled(enabled);
        dnsType.setEnabled(enabled);
        dnsServer.setEnabled(enabled);
        dnsAddr.setEnabled(enabled);
        logLevel.setEnabled(enabled);
    }

    public String getClientServer() {
        return clientServer.getText();
    }

    public String getClientUsername() {
        return clientUsername.getText();
    }

    public String getClientPassword() {
        return clientPassword.getText();
    }

    public boolean getClientProxyAll() {
        return clientProxyAll.isChecked();
    }

    public String getDNSType() {
        return dnsType.getValue();
    }

    public String getDNSServer() {
        return dnsServer.getText();
    }

    public String getDNSAddr() {
        return dnsAddr.getText();
    }

    public String getLogLevel() {
        return logLevel.getValue();
    }
}
