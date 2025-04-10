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
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class TimerService extends Service {
    private static final String CHANNEL_ID = "TimerChannel";
    private static final int NOTIFICATION_ID = 1;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private final IBinder binder = new TimerBinder();
    private NotificationManager notificationManager;

    public class TimerBinder extends Binder {
        public long getTimeLeft() {
            return timeLeftInMillis;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            timeLeftInMillis = intent.getLongExtra("time_left", 0);
            int recipeId = intent.getIntExtra("recipe_id", -1);
            
            if (checkNotificationPermission()) {
                startForeground(NOTIFICATION_ID, createNotification(timeLeftInMillis));
                startTimer();
            }
        }
        return START_STICKY;
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // אם אין הרשאה, נשלח broadcast שיבקש את ההרשאה
                Intent permissionIntent = new Intent("com.example.recipebook.REQUEST_NOTIFICATION_PERMISSION");
                sendBroadcast(permissionIntent);
                return false;
            }
        }
        return true;
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                if (checkNotificationPermission()) {
                    updateNotification(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                stopSelf();
            }
        }.start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(long timeLeft) {
        Intent notificationIntent = new Intent(this, RecipeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Recipe Timer")
            .setContentText(formatTime(timeLeft))
            .setSmallIcon(R.drawable.timer_icon_active)
            .setContentIntent(pendingIntent)
            .build();
    }

    private void updateNotification(long timeLeft) {
        Notification notification = createNotification(timeLeft);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
} 