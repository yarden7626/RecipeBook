package com.example.recipebook;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            timeLeftInMillis = intent.getLongExtra("timeLeftInMillis", 0);
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
        
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateNotification();
                if (callback != null) {
                    callback.onTimerUpdate(millisUntilFinished);
                }
            }
            
            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isRunning = false;
                updateNotification();
                if (callback != null) {
                    callback.onTimerUpdate(0);
                }
                stopSelf();
            }
        }.start();
        
        isRunning = true;
        startForeground(NOTIFICATION_ID, createNotification());
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
                .build();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
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
    }
} 
