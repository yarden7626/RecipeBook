package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import java.util.List;
import java.lang.StringBuilder;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.Manifest;
import java.util.Locale;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity"; // תגית ללוגים

    // הגדרת משתנים גלובליים
    private TextView recipeTitle, cateRecipeValue, pTimeValue, ingrValue, direValue, timerText; // שדות טקסט למתכון
    private ImageView imageRecipe, isFavRecipe; // תמונות
    private ImageButton timerButton, backButtonRecipe; // כפתורים
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים
    private Recipe currentRecipe; // המתכון הנוכחי
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 0;
    private int currentUserId;
    private boolean isFavorite;
    private TimerService timerService;
    private boolean hasShownInitialDialog = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // קבלת ה-ID של המשתמש והמתכון
        currentUserId = getIntent().getIntExtra("user_id", -1);
        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        if (currentUserId == -1 || recipeId == -1) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // אתחול בסיס הנתונים
        database = AppDatabase.getInstance(this);

        // קישור המשתנים לאלמנטים בממשק המשתמש
        recipeTitle = findViewById(R.id.RecipeTitle);
        cateRecipeValue = findViewById(R.id.CateRecipeValue);
        pTimeValue = findViewById(R.id.pTimeValue);
        ingrValue = findViewById(R.id.ingrValue);
        direValue = findViewById(R.id.direValue);
        imageRecipe = findViewById(R.id.imageRecipe);
        isFavRecipe = findViewById(R.id.isFavRecipe);
        timerButton = findViewById(R.id.timerButton);
        timerText = findViewById(R.id.timerText);
        backButtonRecipe = findViewById(R.id.backButtonRecipe);

        // רישום BroadcastReceiver לבקשת הרשאות
        registerReceiver(permissionReceiver, new IntentFilter("com.example.recipebook.REQUEST_NOTIFICATION_PERMISSION"));

        // טעינת המתכון והסטטוס של המועדפים
        new Thread(() -> {
            currentRecipe = database.recipeDao().getRecipeById(recipeId);
            if (currentRecipe != null) {
                // בדיקת סטטוס המועדפים
                FavoriteRecipe favorite = database.favoriteRecipeDao().getFavorite(currentUserId, recipeId);
                isFavorite = (favorite != null);
                currentRecipe.setFavorite(isFavorite);
                
                // עדכון הממשק עם נתוני המתכון
                runOnUiThread(() -> {
                    updateUI();
                    // אתחול משתני הטיימר
                    if (currentRecipe.getTimerDuration() > 0) {
                        timeLeftInMillis = currentRecipe.getTimerDuration() * 60 * 1000;
                    }
                });
            }
        }).start();

        // הגדרת מאזיני לחיצה לכפתורים
        backButtonRecipe.setOnClickListener(v -> finish());

        isFavRecipe.setOnClickListener(v -> toggleFavorite());

        timerButton.setOnClickListener(v -> {
            if (currentRecipe != null && currentRecipe.getTimerDuration() > 0) {
                if (isTimerRunning) {
                    showStopTimerDialog();
                } else {
                    if (!hasShownInitialDialog) {
                        showInitialTimerDialog();
                        hasShownInitialDialog = true;
                    } else if (timeLeftInMillis > 0) {
                        showResumeTimerDialog();
                    } else {
                        showInitialTimerDialog();
                    }
                }
            } else {
                Toast.makeText(this, "No timer is set for this recipe", Toast.LENGTH_SHORT).show();
            }
        });

        // הוספת כפתור לאתחול מחדש של הטיימר
        timerButton.setOnLongClickListener(v -> {
            if (currentRecipe != null && currentRecipe.getTimerDuration() > 0) {
                resetTimer();
                return true;
            }
            return false;
        });
    }

    private final BroadcastReceiver permissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    };

    // פונקציה לעדכון הממשק עם נתוני המתכון
    private void updateUI() {
        recipeTitle.setText(currentRecipe.getRecipeName());
        cateRecipeValue.setText(currentRecipe.getCategory());
        pTimeValue.setText(currentRecipe.getPrepTime());
        
        // המרת רשימת הרכיבים למחרוזת מפורמטת
        List<String> ingredients = currentRecipe.getIngredients();
        StringBuilder ingredientsText = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientsText.append("• ").append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                ingredientsText.append("\n");
            }
        }
        ingrValue.setText(ingredientsText.toString());
        
        direValue.setText(currentRecipe.getDirections());
        updateFavoriteIcon();

        // עדכון תמונת המתכון
        String imageUriString = currentRecipe.getImageUri();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                Log.d(TAG, "Loading image from URI: " + imageUri);
                
                // טעינת התמונה ישירות מ-ContentProvider
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                }
                imageRecipe.setImageBitmap(bitmap);
                Log.d(TAG, "Successfully loaded image");
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // במקרה של שגיאה, נציג תמונה ברירת מחדל
                imageRecipe.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            Log.d(TAG, "No image URI found");
            imageRecipe.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    // פונקציה לעדכון אייקון המועדפים
    private void updateFavoriteIcon() {
        if (isFavorite) {
            isFavRecipe.setImageResource(R.drawable.ic_star_filled);
        } else {
            isFavRecipe.setImageResource(R.drawable.ic_star_empty);
        }
    }

    private void showInitialTimerDialog() {
        int minutes = currentRecipe.getTimerDuration();
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes / 60, minutes % 60);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Timer")
               .setMessage("This recipe has a timer set to " + timeText + "\nWould you like to start it?")
               .setPositiveButton("Start", (dialog, which) -> startTimer())
               .setNegativeButton("Not Now", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
        });
        dialog.show();
    }

    private void showStopTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stop Timer")
               .setMessage("Are you sure you want to stop the timer?")
               .setPositiveButton("Stop", (dialog, which) -> stopTimer())
               .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
        });
        dialog.show();
    }

    private void showResumeTimerDialog() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resume Timer")
               .setMessage("Timer is paused at " + timeLeftFormatted + "\nDo you want to resume it?")
               .setPositiveButton("Resume", (dialog, which) -> startTimer())
               .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
        });
        dialog.show();
    }

    private void updateTimerUI() {
        if (timeLeftInMillis > 0) {
            int minutes = (int) (timeLeftInMillis / 1000) / 60;
            int seconds = (int) (timeLeftInMillis / 1000) % 60;
            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            timerText.setText(timeLeftFormatted);
            timerText.setVisibility(View.VISIBLE);
        } else {
            timerText.setVisibility(View.GONE);
        }
    }

    private void startTimer() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra("timeLeftInMillis", timeLeftInMillis);
        try {
            startService(serviceIntent);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            isTimerRunning = true;
            updateTimerUI();
        } catch (SecurityException e) {
            Toast.makeText(this, "Could not start timer service", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTimer() {
        if (timerService != null) {
            try {
                unbindService(serviceConnection);
                stopService(new Intent(this, TimerService.class));
            } catch (IllegalArgumentException e) {
                // Service כבר לא מחובר, אפשר להתעלם מהשגיאה
            }
            timerService = null;
        }
        isTimerRunning = false;
        updateTimerUI();
    }

    private void resetTimer() {
        stopTimer();
        timeLeftInMillis = currentRecipe.getTimerDuration() * 60 * 1000;
        updateTimerUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // לא מנתקים את החיבור ל-Service כדי שהטיימר ימשיך לרוץ ברקע
        // רק מנתקים את ה-BroadcastReceiver
        unregisterReceiver(permissionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // רושמים מחדש את ה-BroadcastReceiver
        registerReceiver(permissionReceiver, new IntentFilter("com.example.recipebook.REQUEST_NOTIFICATION_PERMISSION"));
        
        // אם הטיימר רץ, מתחברים מחדש ל-Service
        if (isTimerRunning) {
            Intent serviceIntent = new Intent(this, TimerService.class);
            try {
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to bind to TimerService", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // מנתקים את החיבור ל-Service רק כשהאקטיביטי נהרס
        if (serviceConnection != null && timerService != null) {
            try {
                unbindService(serviceConnection);
            } catch (IllegalArgumentException e) {
                // Service כבר לא מחובר, אפשר להתעלם מהשגיאה
            }
        }
        unregisterReceiver(permissionReceiver);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
                timerService = binder.getService();
                timerService.setCallback(new TimerService.TimerCallback() {
                    @Override
                    public void onTimerUpdate(long millisUntilFinished) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timeLeftInMillis = millisUntilFinished;
                                updateTimerUI();
                            }
                        });
                    }
                });
                timeLeftInMillis = timerService.getTimeLeft();
                isTimerRunning = timerService.isRunning();
                updateTimerUI();
            } catch (Exception e) {
                // אם יש בעיה בהתחברות ל-Service, נעצור את הטיימר
                isTimerRunning = false;
                updateTimerUI();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
        }
    };

    private void toggleFavorite() {
        new Thread(() -> {
            if (isFavorite) {
                // הסרת המתכון מהמועדפים
                database.favoriteRecipeDao().deleteFavorite(currentUserId, currentRecipe.getRecipeId());
            } else {
                // הוספת המתכון למועדפים
                FavoriteRecipe favorite = new FavoriteRecipe(currentUserId, currentRecipe.getRecipeId());
                database.favoriteRecipeDao().insert(favorite);
            }
            isFavorite = !isFavorite;
            currentRecipe.setFavorite(isFavorite);
            
            runOnUiThread(() -> {
                updateFavoriteIcon();
                Toast.makeText(this, 
                    isFavorite ? "Added to favorites" : "Removed from favorites", 
                    Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}