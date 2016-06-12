/*
* Copyright (C) 2015-2016 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.cyanogenmod.settings.device;

import android.os.Bundle;
import android.preference.Preference;

import android.preference.MultiSelectListPreference;

import android.preference.ListPreference;

import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.provider.Settings;

import com.cyanogenmod.settings.device.utils.NodePreferenceActivity;

import cyanogenmod.providers.CMSettings;

import org.cyanogenmod.internal.util.ScreenType;

public class TouchscreenGestureSettings extends NodePreferenceActivity {
    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";

    private static final String KEY_CAMERA_LAUNCH_INTENT =
    "touchscreen_gesture_camera_launch_intent";
    private static final String KEY_TORCH_LAUNCH_INTENT = "touchscreen_gesture_torch_launch_intent";
    private static final String KEY_PLAY_PAUSE_LAUNCH_INTENT =
    "touchscreen_gesture_play_pause_launch_intent";
    private static final String KEY_PREVIOUS_LAUNCH_INTENT =
    "touchscreen_gesture_previous_launch_intent";
    private static final String KEY_NEXT_LAUNCH_INTENT = "touchscreen_gesture_next_launch_intent";

    private static final int REQUEST_PICK_SHORTCUT = 100;
    private static final int REQUEST_CREATE_SHORTCUT = 101;

    private MultiSelectListPreference mHapticFeedback;
    private ListPreference mCameraLaunchIntent;
    private ListPreference mTorchLaunchIntent;
    private ListPreference mPlayPauseLaunchIntent;
    private ListPreference mPreviousLaunchIntent;
    private ListPreference mNextLaunchIntent;

	private String preferenceKeyLastChangedShortcut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.touchscreen_panel);

        mHapticFeedback = (MultiSelectListPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        mHapticFeedback.setOnPreferenceChangeListener(this);

        mCameraLaunchIntent = (ListPreference) findPreference(KEY_CAMERA_LAUNCH_INTENT);
        mCameraLaunchIntent.setOnPreferenceChangeListener(this);

        mTorchLaunchIntent = (ListPreference) findPreference(KEY_TORCH_LAUNCH_INTENT);
        mTorchLaunchIntent.setOnPreferenceChangeListener(this);

        mPlayPauseLaunchIntent = (ListPreference) findPreference(KEY_PLAY_PAUSE_LAUNCH_INTENT);
        mPlayPauseLaunchIntent.setOnPreferenceChangeListener(this);

        mPreviousLaunchIntent = (ListPreference) findPreference(KEY_PREVIOUS_LAUNCH_INTENT);
        mPreviousLaunchIntent.setOnPreferenceChangeListener(this);

        mNextLaunchIntent = (ListPreference) findPreference(KEY_NEXT_LAUNCH_INTENT);
        mNextLaunchIntent.setOnPreferenceChangeListener(this);

        new InitListTask().execute();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        if (KEY_HAPTIC_FEEDBACK.equals(key)) {
            final Set<String> value = (Set<String>) newValue;
            final CharSequence[] valueOptions = mHapticFeedback.getEntryValues();
            if(!value.isEmpty()){
                CMSettings.System.putInt(getContentResolver(),
						CMSettings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 1);
                for (CharSequence valueOption : valueOptions) {
                    if (value.contains(valueOption.toString())) {
                        Settings.System.putInt(getContentResolver(), valueOption.toString(), 1);
                    } else {
                        Settings.System.putInt(getContentResolver(), valueOption.toString(), 0);
                    }
                }
            }
            else{
                CMSettings.System.putInt(getContentResolver(),
						CMSettings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 0);
            }
            return true;
        }
        if(KEY_CAMERA_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_CAMERA_LAUNCH_INTENT);
            }
            else{
                Settings.System.putString(getContentResolver(), KEY_CAMERA_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        if(KEY_TORCH_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_TORCH_LAUNCH_INTENT);
            }
            else{
                Settings.System.putString(getContentResolver(), KEY_TORCH_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        if(KEY_PLAY_PAUSE_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_PLAY_PAUSE_LAUNCH_INTENT);
            }
            else{
                Settings.System.putString(getContentResolver(), KEY_PLAY_PAUSE_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        if(KEY_PREVIOUS_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_PREVIOUS_LAUNCH_INTENT);
            }
            else{
                Settings.System.putString(getContentResolver(), KEY_PREVIOUS_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        if(KEY_NEXT_LAUNCH_INTENT.equals(key)){
            final String value = (String) newValue;
            if(value.equals("shortcut")){
                createShortcutPicked(KEY_NEXT_LAUNCH_INTENT);
            }
            else{
                Settings.System.putString(getContentResolver(), KEY_NEXT_LAUNCH_INTENT, value);
                reloadSummarys();
            }
            return true;
        }
        return super.onPreferenceChange(preference, newValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If running on a phone, remove padding around the listview
        if (!ScreenType.isTablet(this)) {
            getListView().setPadding(0, 0, 0, 0);
        }
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_SHORTCUT) {
                startActivityForResult(data, REQUEST_CREATE_SHORTCUT);
            }
            if(requestCode == REQUEST_CREATE_SHORTCUT){
                Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, data.getStringExtra(
                        Intent.EXTRA_SHORTCUT_NAME));
                String uri = intent.toUri(Intent.URI_INTENT_SCHEME);
                if(preferenceKeyLastChangedShortcut != null){
                    Settings.System.putString(getContentResolver(),
							preferenceKeyLastChangedShortcut, uri);
                    reloadSummarys();
                }
            }
        }
        else{
            Settings.System.putString(getContentResolver(),
                    preferenceKeyLastChangedShortcut, "default");
            reloadSummarys();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createShortcutPicked(String key){
        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, "Select shortcut");
        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
        preferenceKeyLastChangedShortcut = key;
    }

    private List<String> getPackageNames(){
        List<String> packageNameList = new ArrayList<String>();
        List<PackageInfo> packs =
				getApplicationContext().getPackageManager().getInstalledPackages(0);
        for(int i = 0; i < packs.size(); i++){
            String packageName = packs.get(i).packageName;
            Intent launchIntent = getApplicationContext().getPackageManager()
            .getLaunchIntentForPackage(packageName);
            if(launchIntent != null){
                packageNameList.add(packageName);
            }
        }
        return packageNameList;
    }

    private String getAppnameFromPackagename(String packagename){
        if(packagename == null || "".equals(packagename)){
            return getResources().getString(R.string.touchscreen_action_default);
        }
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packagename, 0);
        } catch (final Exception e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : packagename);
    }

    private String getSummary(String key){
        String summary = Settings.System.getString(getContentResolver(), key);
        if(summary == null){
            return getResources().getString(R.string.touchscreen_action_unkownappforpackagename);
        }
        else if(summary.startsWith("intent:")){
            return getResources().getString(R.string.touchscreen_action_shortcut);
        }
        else if(summary.equals("default")){
            return getResources().getString(R.string.touchscreen_action_default);
        }
        return getAppnameFromPackagename(summary);
    }

    private void reloadSummarys(){
        mCameraLaunchIntent.setSummary(getSummary(KEY_CAMERA_LAUNCH_INTENT));
        mTorchLaunchIntent.setSummary(getSummary(KEY_TORCH_LAUNCH_INTENT));
        mPlayPauseLaunchIntent.setSummary(getSummary(KEY_PLAY_PAUSE_LAUNCH_INTENT));
        mPreviousLaunchIntent.setSummary(getSummary(KEY_PREVIOUS_LAUNCH_INTENT));
        mNextLaunchIntent.setSummary(getSummary(KEY_NEXT_LAUNCH_INTENT));
    }

    private class InitListTask extends AsyncTask<Void, Void, Void> {
        private int defaultValuesCount = 2;

        @Override
        protected Void doInBackground(Void... voids) {
            TreeMap<String, String> treemap = new TreeMap<String, String>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.toLowerCase().compareTo(o2.toLowerCase());
                }
            });

            List<String> listPackageNames = getPackageNames();
            for (String name : listPackageNames){
                treemap.put(getAppnameFromPackagename(name), name);
            }

            final CharSequence[] packageNames = new CharSequence[listPackageNames.size() + defaultValuesCount];
            final CharSequence[] hrblPackageNames = new CharSequence[listPackageNames.size() + defaultValuesCount];

			hrblPackageNames[0] = getResources().getString(R.string.touchscreen_action_default);
            hrblPackageNames[1] = getResources().getString(R.string.touchscreen_action_shortcut);
            packageNames[0] = "default";
            packageNames[1] = "shortcut";
            int counter = defaultValuesCount;
            Iterator ittwo = treemap.entrySet().iterator();
            while (ittwo.hasNext()) {
                Map.Entry pairs = (Map.Entry)ittwo.next();
                hrblPackageNames[counter] = (CharSequence)pairs.getKey();
                packageNames[counter] = (CharSequence)pairs.getValue();
                ittwo.remove();
                counter++;
            }

            mCameraLaunchIntent.setEntries(hrblPackageNames);
            mCameraLaunchIntent.setEntryValues(packageNames);

            mTorchLaunchIntent.setEntries(hrblPackageNames);
            mTorchLaunchIntent.setEntryValues(packageNames);

            mPlayPauseLaunchIntent.setEntries(hrblPackageNames);
            mPlayPauseLaunchIntent.setEntryValues(packageNames);

            mPreviousLaunchIntent.setEntries(hrblPackageNames);
            mPreviousLaunchIntent.setEntryValues(packageNames);

            mNextLaunchIntent.setEntries(hrblPackageNames);
            mNextLaunchIntent.setEntryValues(packageNames);

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            reloadSummarys();
            mCameraLaunchIntent.setEnabled(true);
            mTorchLaunchIntent.setEnabled(true);
            mPlayPauseLaunchIntent.setEnabled(true);
            mPreviousLaunchIntent.setEnabled(true);
            mNextLaunchIntent.setEnabled(true);
        }
    }
}
