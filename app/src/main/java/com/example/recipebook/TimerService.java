package com.example.recipebook;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class TimerService extends Service {
    private static final String TAG = "TimerService";
    private static final String CHANNEL_ID = "TimerChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private int recipeId;
    private final IBinder binder = new TimerBinder();
    
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
            timeLeftInMillis = intent.getLongExtra("time_left", 0);
            recipeId = intent.getIntExtra("recipe_id", -1);
            
            // בדיקה אם יש הרשאות נדרשות
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(NOTIFICATION_ID, createNotification());
                startTimer();
            } else {
                startTimer();
            }
        }
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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
            }
            
            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateNotification();
                stopSelf();
            }
        }.start();
    }
    
    private void updateNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, RecipeActivity.class);
        notificationIntent.putExtra("recipe_id", recipeId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftText = String.format("%02d:%02d", minutes, seconds);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recipe Timer")
                .setContentText("Time left: " + timeLeftText)
                .setSmallIcon(R.drawable.timer_icon_active)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel for recipe timer notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    
    public long getTimeLeft() {
        return timeLeftInMillis;
    }
} 