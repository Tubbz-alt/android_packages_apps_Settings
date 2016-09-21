/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.content.Context;
import android.content.ComponentName;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.app.AlertDialog;
import android.util.Log;
import android.os.Process;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStream;

import java.io.IOException;



/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the initial screen.
 */
public class ChrootClear extends Fragment {
    private static final String TAG = "ChrootClear";

    private static final int KEYGUARD_REQUEST = 55; 
    private static final int LOCK_REQUEST = 56; 

    private View mContentView;
    private Button mInitiateButton;

    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper(getActivity(), this)
                .launchConfirmationActivity(request, null,
                        res.getText(R.string.master_clear_gesture_explanation));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCK_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                getActivity().finish();
            }
            return;
        } else if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
            showFinalConfirmation();
        } else {
            establishInitialState();
        }
    }

    private void showFinalConfirmation() {
        /* MasterClearConfirm.createInstance(mInternalStorage.isChecked(),
                mExternalStorage.isChecked()) .show(getFragmentManager(),
                MasterClearConfirm.class.getSimpleName()); */
        //Toast.makeText(getActivity(), "Imagine it worked.", Toast.LENGTH_SHORT).show();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        AlertDialog errorDialog = new AlertDialog.Builder(getActivity()).setTitle("Reset Pwnix Environment").setMessage(R.string.pwnix_reset_warning_text_message).setPositiveButton(R.string.pwnix_reset_warning_text_reset_now,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        doWipe();
                                        
                                    }
                                }).setCancelable(true).setNegativeButton("CANCEL", 
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        
                                    }
                                }).show();
    }

    public void doWipe(){

         //enable PwnixInstaller(GUI)
            //shellOut("rm -rf /data/data/com.pwnieexpress.android.pwnixinstaller/shared_prefs\n");

       // shellOut("echo 'cmd 'rm -rf /data/local/kali/'' >> " +"/cache/recovery/openrecoveryscript\n");

        //Lock orientation here
        try{
             int flag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                ComponentName component= new ComponentName("com.pwnieexpress.android.pwnixinstaller","com.pwnieexpress.android.pwnixinstaller.AutoStart");
                 getActivity().getPackageManager()
                .setComponentEnabledSetting(component, flag,
                        PackageManager.DONT_KILL_APP);
            }catch (Exception e){
                Log.d("FATAL ERROR ENABLING","PWNIXINSTALLER");
            }
        
        getActivity().sendBroadcast(new Intent().setAction("com.pwnieexpress.android.pxinstaller.action.RESET"));
        thread();
    }

    private long startnow;
    private long endnow;
    private static ShellThread thread1; 
    public void thread(){

       startnow = android.os.SystemClock.uptimeMillis();
       if(thread1 == null){
       thread1 = new ShellThread(this);
        }
       thread1.setRef(this);
       thread1.start();
    }

     class ShellThread extends Thread {
         ChrootClear ref;
         ShellThread(ChrootClear cref) {
             this.ref = cref;
         }
         public void setRef(ChrootClear cref){
            this.ref = cref;
         }

            public static final String USER_SETUP_COMPLETE_FLAG_0 = "su -c 'settings put secure user_setup_complete 0'\n";
            private static final String USER_SETUP_COMPLETE_FLAG_1 = "su -c 'settings put secure user_setup_complete 1'\n";
            public static final String PROVISIONED_FLAG_0 = "su -c 'settings put global device_provisioned 0'\n";
            private static final String PROVISIONED_FLAG_1 = "su -c 'settings put global device_provisioned 1'\n";
            private static final String START_SYSTEMUI = "am startservice --user 0 -n com.android.systemui/.SystemUIService\n";
            //private static final String DISABLE_SYSTEMUI = "su -c 'pm disable com.android.systemui'\n";
            //private static final String ENABLE_SYSTEMUI = "su -c 'pm enable com.android.systemui'\n";
            public static final String RELOAD_SYSTEMUI = "su -c 'killall com.android.systemui'\n";
            public static final String DISABLE_LOCKSCREEN = "su -c 'settings put secure lockscreen.disabled 1'\n";
            private static final String ENABLE_LOCKSCREEN = "su -c 'settings put secure lockscreen.disabled 0'\n";

           
            public  boolean shellOut(String[] commands){
                java.lang.Process process = null;
                int k =0;
                OutputStream outputStream = null;
                boolean returnval;
                try {
                    process = Runtime.getRuntime().exec("su");
                    outputStream = process.getOutputStream();
                    for(int i =0; i< commands.length; i++){
                        outputStream.write((commands[i]).getBytes());
                        outputStream.flush();
                    }
                    outputStream.write("sync\n".getBytes());
                    outputStream.write("exit\n".getBytes());
                    outputStream.flush();
                    process.waitFor();
                    k = process.exitValue();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (k != 0) {
                        Log.d("commands", "Exit value: " + k);
                        returnval=false;
                    } else {
                        Log.d("commands", "Worker thread exited with value " + k);
                       
                       PowerManager powerManager =
(PowerManager) (ref.getActivity()).getSystemService(Context.POWER_SERVICE);
powerManager.reboot("recovery");

                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); //probably wont get here
                        returnval = true;
                    }
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (process != null) {
                            process.destroy();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return returnval;
            }

         public void run() {
            //prioritize writing openrecovery script, then locking, then removing prefs etc
                if(shellOut(new String[]{"echo 'cmd 'rm -rf /data/local/kali/'' >> " +
                            "/cache/recovery/openrecoveryscript\n",USER_SETUP_COMPLETE_FLAG_0, PROVISIONED_FLAG_0, DISABLE_LOCKSCREEN,"rm -rf /data/data/com.pwnieexpress.android.pwnixinstaller/shared_prefs\n","rm -f /cache/recovery/last_log\n"})) {
                    endnow = android.os.SystemClock.uptimeMillis();
                    Log.d("THREADING", "Execution time: " + (endnow - startnow) + " ms");
                }
         }

     }

     private boolean shellOut(String command){
        java.lang.Process process = null;
        int k =0;
        OutputStream outputStream = null;
        boolean returnval;
        try {
            process = Runtime.getRuntime().exec("su");
            outputStream = process.getOutputStream();
            outputStream.write((command+"\n").getBytes());
            outputStream.write("sync\n".getBytes());
            outputStream.write("exit\n".getBytes());
            outputStream.flush();
            process.waitFor();
            k = process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (k != 0) {
                Log.d(command, "Exit value: " + k);
              returnval=false;
            } else {
                Log.d(command, "Worker thread exited with value " + k);
                returnval = true;
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnval;
    }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private final Button.OnClickListener mInitiateListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                showFinalConfirmation();
            }
        }
    };


    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        mInitiateButton = (Button) mContentView.findViewById(R.id.initiate_master_clear);
        mInitiateButton.setOnClickListener(mInitiateListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (!Process.myUserHandle().isOwner()
                || UserManager.get(getActivity()).hasUserRestriction(
                UserManager.DISALLOW_FACTORY_RESET)) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, null);
        }

        mContentView = inflater.inflate(R.layout.master_clear_pwnie, null);

        establishInitialState();
        return mContentView;
    }

    private View newTitleView(ViewGroup parent, LayoutInflater inflater) {
        final TypedArray a = inflater.getContext().obtainStyledAttributes(null,
                com.android.internal.R.styleable.Preference,
                com.android.internal.R.attr.preferenceCategoryStyle, 0);
        final int resId = a.getResourceId(com.android.internal.R.styleable.Preference_layout,
                0);
        return inflater.inflate(resId, parent, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SecuritySettings.isDeviceLocked()) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.lock_to_cyanogen_master_clear_warning)
                    .setNegativeButton(R.string.wizard_back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    })
                    .setPositiveButton(R.string.lockpassword_continue_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SecuritySettings.updateCyanogenDeviceLockState(ChrootClear.this,
                                    false, LOCK_REQUEST);
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();

        }
    }
}
