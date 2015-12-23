/*
 * Copyright (C) 2015 The CyanogenMod Project
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

import com.cyanogenmod.settings.device.utils.NodePreferenceActivity;

import org.cyanogenmod.internal.util.ScreenType;

import android.os.Bundle;
import android.provider.Settings;
import android.preference.Preference;

import android.preference.MultiSelectListPreference;

import android.preference.ListPreference;

import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;

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

    private MultiSelectListPreference mHapticFeedback;
    private ListPreference mCameraLaunchIntent;
    private ListPreference mTorchLaunchIntent;
    private ListPreference mPlayPauseLaunchIntent;
    private ListPreference mPreviousLaunchIntent;
    private ListPreference mNextLaunchIntent;

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
				Settings.System.putInt(getContentResolver(), KEY_HAPTIC_FEEDBACK, 1);
				for(int i = 0; i < valueOptions.length; i++){
					if(value.contains(valueOptions[i].toString())){
						Settings.System.putInt(getContentResolver(), valueOptions[i].toString(), 1);
					}
					else{
						Settings.System.putInt(getContentResolver(), valueOptions[i].toString(), 0);
					}
				}
			}
			else{
				Settings.System.putInt(getContentResolver(), KEY_HAPTIC_FEEDBACK, 0);
			}
            return true;
        }
		if(KEY_CAMERA_LAUNCH_INTENT.equals(key)){
			final String value = (String) newValue;
			findPreference(KEY_CAMERA_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
			Settings.System.putString(getContentResolver(), KEY_CAMERA_LAUNCH_INTENT, value);
			return true;
		}
		if(KEY_TORCH_LAUNCH_INTENT.equals(key)){
			final String value = (String) newValue;
			findPreference(KEY_TORCH_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
			Settings.System.putString(getContentResolver(), KEY_TORCH_LAUNCH_INTENT, value);
			return true;
		}
		if(KEY_PLAY_PAUSE_LAUNCH_INTENT.equals(key)){
			final String value = (String) newValue;
			findPreference(KEY_PLAY_PAUSE_LAUNCH_INTENT).setSummary(
					getAppnameFromPackagename(value));
			Settings.System.putString(getContentResolver(), KEY_PLAY_PAUSE_LAUNCH_INTENT, value);
			return true;
		}
		if(KEY_PREVIOUS_LAUNCH_INTENT.equals(key)){
			final String value = (String) newValue;
			findPreference(KEY_PREVIOUS_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
			Settings.System.putString(getContentResolver(), KEY_PREVIOUS_LAUNCH_INTENT, value);
			return true;
		}
		if(KEY_NEXT_LAUNCH_INTENT.equals(key)){
			final String value = (String) newValue;
			findPreference(KEY_NEXT_LAUNCH_INTENT).setSummary(getAppnameFromPackagename(value));
			Settings.System.putString(getContentResolver(), KEY_NEXT_LAUNCH_INTENT, value);
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
    
    private List<String> getPackageNames(){
		List<String> packageNameList = new ArrayList<String>();
		List<PackageInfo> packs = 
			getApplicationContext().getPackageManager().getInstalledPackages(0);
		packageNameList.add("");
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
		return (String) (ai != null ? pm.getApplicationLabel(ai) : 
				getResources().getString(R.string.touchscreen_action_unkownappforpackagename));
	}
	
	private String getSummary(String key){
		String summary = Settings.System.getString(getContentResolver(), key);
		if(summary != null){
			return getAppnameFromPackagename(summary);
		}
		return getResources().getString(R.string.touchscreen_action_unkownappforpackagename);
	}
	
	private class InitListTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... voids) {
			List<String> listPackageNames = getPackageNames();        
			final CharSequence[] packageNames = 
					listPackageNames.toArray(new CharSequence[listPackageNames.size()]);
			final CharSequence[] hrblPackageNames = new CharSequence[listPackageNames.size()];
			hrblPackageNames[0] = "Default action";
			
			for(int i = 1; i < listPackageNames.size(); i++){
				 hrblPackageNames[i] = getAppnameFromPackagename(listPackageNames.get(i));
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
			mCameraLaunchIntent.setSummary(getSummary(KEY_CAMERA_LAUNCH_INTENT));  
			mCameraLaunchIntent.setEnabled(true);
			
			mTorchLaunchIntent.setSummary(getSummary(KEY_TORCH_LAUNCH_INTENT));
			mTorchLaunchIntent.setEnabled(true);
			
			mPlayPauseLaunchIntent.setSummary(getSummary(KEY_PLAY_PAUSE_LAUNCH_INTENT));  
			mPlayPauseLaunchIntent.setEnabled(true);
			
			mPreviousLaunchIntent.setSummary(getSummary(KEY_PREVIOUS_LAUNCH_INTENT));   
			mPreviousLaunchIntent.setEnabled(true);
			
			mNextLaunchIntent.setSummary(getSummary(KEY_NEXT_LAUNCH_INTENT));
			mNextLaunchIntent.setEnabled(true);
		}
	}
}
