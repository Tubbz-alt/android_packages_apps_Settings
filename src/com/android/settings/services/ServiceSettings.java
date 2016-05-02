/*
 * Copyright (C) 2016 The Android Open Pwn Project
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

package com.android.settings.services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Services preference fragment
 */
public class ServiceSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener {

    private static final String TAG = "ServiceSettings";

    private static final String KEY_SERVICE = "service";

    private static final String ADD_SERVICE_ACTION =
            "com.pwnieexpress.android.pxinstaller.action.SERVICE";

    private static final int ORDER_LAST = 1001;
    private static final int ORDER_NEXT_TO_LAST = 1000;

    private AlertDialog addDialog, listDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        cleanUpPreferences();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final Context context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle("Add a service");
        
        ListView listView = new ListView(context);
        List<Map<String, String>> services = new ArrayList<Map<String, String>>();
	Map<String, String> data = new HashMap<String, String>(2);
	data.put("title", "Google Play Services");
	data.put("summary", "Google apps and services");
        services.add(data);

	SimpleAdapter listAdapter = new SimpleAdapter(context, services, R.layout.services_list_item,
							new String[] {"title", "summary"},
							new int[] {R.id.title, R.id.summary});
	

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
	    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                showAddDialog(context);
                listDialog.dismiss();
            }
	});

        builder.setView(listView);

        listDialog = builder.create();
        listDialog.show();

        return false;
    }

    void showAddDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Google Play Services?");
	builder.setMessage("Google Play Services includes core Google apps and services, such as Google Play Store, Calendar, Gmail and Search.\n\nYou can also add other apps and services by adding Google Play  Services. Each user has access to the app store, which they can use to add their own apps, wallpapers and so on. Users can also manage their Google account and device settings.\n\nAny user can add and update apps for all other users.");
	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
	        context.sendBroadcast(new Intent("com.pwnieexpress.android.pxinstaller.action.SERVICE"));
                addDialog.dismiss();
            }
        });
	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                addDialog.dismiss();
            }
        });
        addDialog = builder.create();
        addDialog.show();
    }

    void updatePreferences() {
	final Context context = getActivity();
	addPreferencesFromResource(R.xml.service_settings);

	final PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference(KEY_SERVICE);
        final Preference addPreference = new Preference(context);
        addPreference.setTitle(R.string.print_menu_item_add_service);
        addPreference.setIcon(R.drawable.ic_menu_add_dark);
        addPreference.setOnPreferenceClickListener(this);
        addPreference.setOrder(ORDER_LAST);
        preferenceScreen.addPreference(addPreference);

	Process process = null;
	StringBuilder builder = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec("cat /data/system/packages.list");
	    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    String line = null;
	    while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
	    String output = builder.toString();
	    if (output.contains("gsf") || output.contains("gms")) {
	        Log.d(TAG, "Google Services Framework detected");
		Preference servicePreference = new Preference(context);
                servicePreference.setTitle("Google Play Services");
                servicePreference.setIcon(R.drawable.common_ic_googleplayservices);
                servicePreference.setOrder(ORDER_NEXT_TO_LAST);
                preferenceScreen.addPreference(servicePreference);
	    }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanUpPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.removeAll();
        }
    }
}
