package com.example.recipebook;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {
    private static final String TAG = "TimerService";
    private static final String CHANNEL_ID = "TimerServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "TimerPreferences";
    private static final String KEY_TIME_LEFT = "timeLeftInMillis";

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isRunning = false;
    private final IBinder binder = new TimerBinder();
    private TimerCallback callback;

    public interface TimerCallback {
        void onTimerUpdate(long millisUntilFinished);
    }

    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        loadTimeLeft();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            timeLeftInMillis = intent.getLongExtra("timeLeftInMillis", timeLeftInMillis);
            startTimer();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(TimerCallback callback) {
        this.callback = callback;
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        startForeground(NOTIFICATION_ID, createNotification());

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateNotification();
                saveTimeLeft();
                if (callback != null) {
                    callback.onTimerUpdate(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isRunning = false;
                updateNotification();
                saveTimeLeft();
                if (callback != null) {
                    callback.onTimerUpdate(0);
                }
                stopSelf();
            }
        }.start();

        isRunning = true;
    }

    private void updateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, RecipeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis),
                TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recipe Timer")
                .setContentText("Time left: " + timeLeftFormatted)
                .setSmallIcon(R.drawable.timer_icon_active)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Timer service channel for recipe timers");
            serviceChannel.enableLights(true);
            serviceChannel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public long getTimeLeft() {
        return timeLeftInMillis;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        saveTimeLeft();  // Save the current time before the service is destroyed
    }

    // Load the time left from SharedPreferences
    private void loadTimeLeft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        timeLeftInMillis = prefs.getLong(KEY_TIME_LEFT, 0);  // Default to 0 if not found
    }

    // Save the time left to SharedPreferences
    private void saveTimeLeft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_TIME_LEFT, timeLeftInMillis);
        editor.apply();
    }
}