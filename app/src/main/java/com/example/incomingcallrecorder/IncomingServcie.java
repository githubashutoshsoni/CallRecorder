package com.example.incomingcallrecorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import kotlin.jvm.JvmStatic;

import static android.util.Log.e;
import static com.example.incomingcallrecorder.MyIntentBuilder.containsCommand;
import static com.example.incomingcallrecorder.PhoneCallReceiver.ACTION_IN;
import static com.example.incomingcallrecorder.PhoneCallReceiver.ACTION_OUT;

public class IncomingServcie extends Service {


    boolean serviceStarted = false;

    BroadcastReceiver brandcastReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @JvmStatic
    public static void startService(Context context) {
        try {
            context.startService(MyIntentBuilder.getExplicitIntentToStartService(context));
        } catch (IllegalStateException e) {
            // More info: https://developer.android.com/about/versions/oreo/background
            e.printStackTrace();

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");


        routeIntentToCommand(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void routeIntentToCommand(Intent intent) {
        if (intent == null) {
            return;
        }
        // Process command.
        if (containsCommand(intent)) {
            processCommand(MyIntentBuilder.getCommand(intent));
        }
        // Process message.

    }

    MediaRecorder recorder = null;
    boolean isRecordStarted = false;
    File audioFile;

    private void startRecord(String seed, String phoneNumber, String time) {
        try {


            if (isRecordStarted) {
                try {
                    if (recorder != null)
                        recorder.stop();  // stop the recording
                } catch (RuntimeException e) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                    if (audioFile != null)
                        audioFile.delete();
                }

                releaseMediaRecorder();
                isRecordStarted = false;
            } else {
                if (prepareAudioRecorder(seed, phoneNumber)) {
                    recorder.start();
                    isRecordStarted = true;
                    Log.i(TAG, "record start");

                } else {
                    releaseMediaRecorder();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        } catch (RuntimeException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }
    }

    private void stopRecord() {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder();
                isRecordStarted = false;
                Log.i(TAG, "record stop");
            }
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
        }
    }


    private Boolean prepareAudioRecorder(
            String seed, String phoneNumber
    ) {
        try {

            Log.d(TAG, "prepareAudioRecorder: ");

            String fileName = "In";
            File dirPath = this.getFilesDir();
            String dirName = "IncomingCall";
            int outputFormat = MediaRecorder.OutputFormat.THREE_GPP;
            int audioSource = MediaRecorder.AudioSource.MIC;
            int audioEncoder = MediaRecorder.AudioEncoder.AMR_NB;

            File sampleDir = new File("$dirPath/$dirName");

            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            StringBuilder fileNameBuilder = new StringBuilder();
            fileNameBuilder.append(fileName);
            fileNameBuilder.append("_");

            fileNameBuilder.append(seed);
            fileNameBuilder.append("_");

            fileNameBuilder.append(phoneNumber);
            fileNameBuilder.append("_");


            fileName = fileNameBuilder.toString();

            String suffix = ".3gp";


            audioFile = File.createTempFile(fileName, suffix, sampleDir);

            recorder = new MediaRecorder();
            ;

            recorder.setAudioSource(audioSource);
            recorder.setOutputFormat(outputFormat);
            recorder.setAudioEncoder(audioEncoder);
            recorder.setOutputFile(audioFile.getAbsolutePath());
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {

                }
            });

            try {
                if (recorder != null)
                    recorder.prepare();

            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void releaseMediaRecorder() {
        recorder.reset();
        recorder.release();
        recorder = null;
    }


    void commandPlay() {

//        todo get this from a broadcast receiver
        startRecord("NORMAL RECORDING", "9840112321", new Date().toString());
    }

    void commandPause() {

        stopRecord();
    }

    private void processCommand(int command) {
        try {
            switch (command) {
                case Command.START:
                    commandStart();
                    break;
                case Command.STOP:
                    commandStop();
                    break;

                case Command.PAUSE:
                    commandPause();
                    break;
                case Command.RECORD:
                    commandPlay();

            }
        } catch (Exception e) {
            e(TAG, "processCommand: exception", e);
        }
    }


    private void commandStop() {
        if (!serviceStarted) {
            return;
        }
        try {

            stopForeground(true);
            stopSelf();

        } finally {
            serviceStarted = false;
        }
    }

    void commandStart() {


        if (serviceStarted) {
        } else {

            Log.d(TAG, "startCallReceiver: ");
            moveToStartedState();
            moveToForegroundAndShowNotification();

            if (brandcastReceiver == null) {
                Log.d(TAG, "startCallReceiver: ");
                brandcastReceiver = new BroadCaseCallReceiver();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_IN);
                intentFilter.addAction(ACTION_OUT);
                this.registerReceiver(brandcastReceiver, intentFilter);
                serviceStarted = true;
            }
        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregsiter();
    }


    private static final String TAG = "IncomingServcie";


    void moveToStartedState() {

        Log.d(TAG, "moveToStartedState: ");
        if (!isPreAndroidO())
            startForegroundService(MyIntentBuilder.getExplicitIntentToStartService(this));
        else
            startService(MyIntentBuilder.getExplicitIntentToStartService(this));

    }

    boolean isPreAndroidO() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1;
    }

    void moveToForegroundAndShowNotification() {
        if (isPreAndroidO()) {
            Log.d(TAG, "moveToForegroundAndShowNotification: ");
            HandleNotifications.PreO.createNotification(this);
        } else {
            Log.d(TAG, "moveToForegroundAndShowNotification: ");
            HandleNotifications.O.createNotification(this);
        }
    }

    void unregsiter() {


        if (brandcastReceiver != null) {
            Log.d("Incoming Service", "unregsiter: the broadcast receiver");
            this.unregisterReceiver(brandcastReceiver);
        }
    }

}
