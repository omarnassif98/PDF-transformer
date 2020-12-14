package com.omz.pdf_transformer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Settings extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public static Settings singleton;
    public static JSONObject staticTemplatePrefs = new JSONObject(), dynamicTemplatePrefs = new JSONObject();

    public Settings() {
        singleton = this;
        Log.d("SETtings", "Settings: SINGLETON SET");
    }
    public String[] ListFiles(){
        return getBaseContext().fileList();
    }
    public void LoadFile(String fileName){
        try {
            InputStream configFile = getBaseContext().openFileInput(fileName);
            int configFileSize = configFile.available();
            byte[] rawData = new byte[configFileSize];
            configFile.read(rawData);
            configFile.close();
            JSONObject configJSON = new JSONObject(new String(rawData, "UTF-8"));
            staticTemplatePrefs = new JSONObject(configJSON.getJSONObject("passive_span_templates").toString());
            dynamicTemplatePrefs = new JSONObject(configJSON.getJSONObject("active_span_templates").toString());
            Log.d("LOAD", "Static: " + staticTemplatePrefs.toString());
            Log.d("LOAD", "Dynamic: " + dynamicTemplatePrefs.toString());
            SharedPreferences formatPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor= formatPref.edit();
            editor.putString("Json_format_file", fileName);
            editor.apply();
        }catch (Exception ex){
            Log.d("JSON", "ERROR");
        }
    }
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        Log.d("PREF", "onPreferenceStartFragment: " + pref.toString());
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new BaseSettingsFragment())
                .commit();
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        SharedPreferences formatPref = getPreferences(MODE_PRIVATE);
        LoadFile(formatPref.getString("Json_format_file", "ReaderViewPreference.json"));

    }
    public void SaveJSONPrefs() throws IOException {
        JSONObject newConfig = new JSONObject();
        try {
            newConfig.put("passive_span_templates", staticTemplatePrefs);
            newConfig.put("active_span_templates", dynamicTemplatePrefs);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences formatPref = getPreferences(MODE_PRIVATE);
        FileOutputStream outputStream = openFileOutput(formatPref.getString("Json_format_file", "ReaderViewPreference.json"), Context.MODE_PRIVATE);
        outputStream.write(newConfig.toString().getBytes());
        outputStream.close();
        Log.d("TAG", "SaveJSONPrefs: WRITTEN");
    }

    public static class BaseSettingsFragment extends PreferenceFragmentCompat {
        private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Log.d("Frag", "Base");
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            String[] entries = Settings.singleton.ListFiles();
            ((ListPreference)getPreferenceScreen().getPreference(0)).setEntries(entries);
            ((ListPreference)getPreferenceScreen().getPreference(0)).setEntryValues(entries);


            getPreferenceScreen().getPreference(0).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.singleton.LoadFile((String)newValue);
                    return true;
                }
            });
        }

        @Override
        public void onDestroyView(){
            try {
                Settings.singleton.SaveJSONPrefs();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.onDestroyView();
        }
    }

    public static class SubSettingsFragment extends PreferenceFragmentCompat{
        public JSONObject associatedJSON;
        public int prefRef;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Log.d("Static SETTING", "onCreatePreferences: " + rootKey);
            setPreferencesFromResource(prefRef, rootKey);
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                final String catName = getPreferenceScreen().getPreference(i).getKey();
                if (!associatedJSON.has(catName)) {
                    try {
                        associatedJSON.put(catName, new JSONObject());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!catName.equals("cascade")) {
                    Log.d("CREATION", "Category: " + getPreferenceScreen().getPreference(i).getKey());
                    for (int j = 0; j < ((PreferenceGroup) getPreferenceScreen().getPreference(i)).getPreferenceCount(); j++) {

                        final Preference entry = ((PreferenceGroup) getPreferenceScreen().getPreference(i)).getPreference(j);

                        String prefName = "";
                        switch (entry.getKey().split("_")[0]) {
                            case "color":
                                prefName = "text_color";
                                break;
                            case "decoration":
                                prefName = "decorations";
                                break;
                            case "size":
                                prefName = "text_size";
                                break;
                            case "spacing":
                                prefName = "line_spacing";
                                break;
                            default:
                                Log.d("Passive pref found?", entry.getKey());
                        }

                        try {
                            if (entry instanceof ListPreference) {
                                Log.d("TAG", "onCreatePreferences: Added " + prefName);
                                Log.d("TAG", "onCreatePreferences: " + associatedJSON.getJSONObject(catName).get(prefName));
                                ((ListPreference) entry).setValue((String) associatedJSON.getJSONObject(catName).get(prefName));
                                ((ListPreference) entry).setSummary((String) ((ListPreference) entry).getValue());
                            } else if (entry instanceof MultiSelectListPreference) {
                                JSONArray vals = (JSONArray) associatedJSON.getJSONObject(catName).get(prefName);
                                HashSet<String> loadedVals = new HashSet<String>();
                                for (int v = 0; v < vals.length(); v++) {
                                    loadedVals.add(vals.getString(v));
                                }
                                ((MultiSelectListPreference) entry).setValues((Set<String>) loadedVals);
                            } else if (entry instanceof EditTextPreference) {
                                int val = associatedJSON.getJSONObject(catName).getInt(prefName);
                                ((EditTextPreference) entry).setText(String.valueOf(val));
                                ((EditTextPreference) entry).setSummary((String) ((EditTextPreference) entry).getText());
                            }
                        } catch (JSONException e) {

                        }


                        final String finalPrefname = prefName;
                        ((PreferenceGroup) getPreferenceScreen().getPreference(i)).getPreference(j).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                            @Override
                            public boolean onPreferenceChange(Preference preference, Object newValue) {
                                try {
                                    if (newValue instanceof Set) {
                                        JSONArray vals = new JSONArray();
                                        for (Object val : ((Set) newValue).toArray()) {
                                            vals.put(val);
                                        }
                                        associatedJSON.getJSONObject(catName).put(finalPrefname, vals);
                                    } else {
                                        entry.setSummary((String) newValue);
                                        try {
                                            associatedJSON.getJSONObject(catName).put(finalPrefname, Integer.parseInt((String) newValue));
                                        } catch (Exception e) {
                                            associatedJSON.getJSONObject(catName).put(finalPrefname, newValue);
                                        }

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }

                        });
                    }
                }else {
                    Log.d("Cascade", ((PreferenceGroup) getPreferenceScreen().getPreference(i)).getPreference(0).getKey());
                    SwitchPreference enabled = ((SwitchPreference)((PreferenceGroup) getPreferenceScreen().getPreference(i)).getPreference(0));
                    try {
                        enabled.setChecked((Boolean) associatedJSON.getJSONObject(catName).get("enabled"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    enabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            try {
                                associatedJSON.getJSONObject(catName).put("enabled", (Boolean) newValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                }
            }
        }
    }
    public static class StaticSettingsFragment extends SubSettingsFragment{
        @Override
        public void onCreate(Bundle savedInstance){
            associatedJSON = staticTemplatePrefs;
            prefRef = R.xml.static_template_preferences;
            super.onCreate(savedInstance);
        }
    }
    public static class DynamicSettingsFragment extends SubSettingsFragment{
        @Override
        public void onCreate(Bundle savedInstance){
            associatedJSON = dynamicTemplatePrefs;
            prefRef = R.xml.dynamic_template_preferences;
            super.onCreate(savedInstance);
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}