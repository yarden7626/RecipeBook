package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.CountDownTimer;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.lang.StringBuilder;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.Manifest;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity"; // תגית ללוגים

    // הגדרת משתנים גלובליים
    private TextView recipeTitle, cateRecipeValue, pTimeValue, ingrValue, direValue, timerText; // שדות טקסט למתכון
    private ImageView imageRecipe, isFavRecipe; // תמונות מתכון ואייקון מועדפים
    private ImageButton timerButton, backButtonRecipe; // כפתורים לפעולות
    private AppDatabase database; // גישה לבסיס הנתונים
    private Recipe currentRecipe; // האובייקט של המתכון הנוכחי
    private CountDownTimer countDownTimer; // טיימר
    private boolean isTimerRunning = false; // האם הטיימר פעיל
    private long timeLeftInMillis = 0; // הזמן שנותר בטיימר
    private int currentUserId; // מזהה המשתמש הנוכחי
    private boolean isFavorite; // האם המתכון מסומן כמועדף
    private boolean hasShownInitialDialog = false; // האם הודעת התחלת טיימר כבר הוצגה

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // קבלת מזהה המשתמש והמתכון מה-Intent
        currentUserId = getIntent().getIntExtra("user_id", -1);
        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        if (currentUserId == -1 || recipeId == -1) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // אתחול בסיס הנתונים
        database = AppDatabase.getInstance(this);

        // קישור רכיבי הממשק למשתנים
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

        // רישום BroadcastReceiver לבקשת הרשאות בהתאם לגרסת האנדרואיד
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                    permissionReceiver,
                    new IntentFilter("com.example.recipebook.REQUEST_NOTIFICATION_PERMISSION"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(
                    permissionReceiver,
                    new IntentFilter("com.example.recipebook.REQUEST_NOTIFICATION_PERMISSION")
            );
        }

        // טעינת נתוני המתכון מהמסד נתונים ובדיקת סטטוס מועדפים
        new Thread(() -> {
            currentRecipe = database.recipeDao().getRecipeById(recipeId);
            if (currentRecipe != null) {
                FavoriteRecipe favorite = database.favoriteRecipeDao().getFavorite(currentUserId, recipeId);
                isFavorite = (favorite != null);
                currentRecipe.setFavorite(isFavorite);

                runOnUiThread(() -> {
                    updateUI(); // עדכון הממשק עם נתוני המתכון
                    // אתחול הטיימר אם מוגדר
                    if (currentRecipe.getTimerDuration() > 0) {
                        timeLeftInMillis = (long) currentRecipe.getTimerDuration() * 60 * 1000;
                    }
                });
            }
        }).start();

        // טיפול בלחיצה על כפתור חזרה
        backButtonRecipe.setOnClickListener(v -> showExitRecipeDialog());

        // טיפול בלחיצה על אייקון מועדפים
        isFavRecipe.setOnClickListener(v -> toggleFavorite());

        // טיפול בלחיצה על כפתור הטיימר
        timerButton.setOnClickListener(v -> {
            if (currentRecipe != null && currentRecipe.getTimerDuration() > 0) {
                if (isTimerRunning) {
                    showStopTimerDialog(); // הצג הודעה להפסקת הטיימר
                } else {
                    if (!hasShownInitialDialog) {
                        showInitialTimerDialog(); // הפעל טיימר בפעם הראשונה
                        hasShownInitialDialog = true;
                    } else if (timeLeftInMillis > 0) {
                        showResumeTimerDialog(); // הצג הודעה להמשך טיימר
                    } else {
                        showInitialTimerDialog(); // הפעל טיימר מחדש
                    }
                }
            } else {
                Toast.makeText(this, "No timer is set for this recipe", Toast.LENGTH_SHORT).show();
            }
        });

        // אתחול הטיימר בלחיצה ארוכה
        timerButton.setOnLongClickListener(v -> {
            if (currentRecipe != null && currentRecipe.getTimerDuration() > 0) {
                resetTimer();
                return true;
            }
            return false;
        });
    }

    // BroadcastReceiver לבקשת הרשאות
    private final BroadcastReceiver permissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    };

    // עדכון הממשק עם נתוני המתכון
    private void updateUI() {
        recipeTitle.setText(currentRecipe.getRecipeName());
        cateRecipeValue.setText(currentRecipe.getCategory());
        pTimeValue.setText(currentRecipe.getPrepTime());

        // המרת רשימת רכיבים לפורמט טקסט
        List<String> ingredients = currentRecipe.getIngredients();
        StringBuilder ingredientsText = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientsText.append("• ").append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                ingredientsText.append("\n");
            }
        }
        ingrValue.setText(ingredientsText.toString());

        direValue.setText(currentRecipe.getDirections()); // הוראות הכנה
        updateFavoriteIcon(); // עדכון האייקון של מועדפים

        // טעינת תמונה מה-URI
        String imageUriString = currentRecipe.getImageUri();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                Log.d(TAG, "Loading image from URI: " + imageUri);

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
                imageRecipe.setImageResource(R.drawable.ic_launcher_foreground); // תמונת ברירת מחדל
            }
        } else {
            Log.d(TAG, "No image URI found");
            imageRecipe.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    // עדכון אייקון המועדפים
    private void updateFavoriteIcon() {
        if (isFavorite) {
            isFavRecipe.setImageResource(R.drawable.ic_star_filled);
        } else {
            isFavRecipe.setImageResource(R.drawable.ic_star_empty);
        }
    }

    // דיאלוג להתחלת טיימר
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black));
        });
        dialog.show();
    }

    // דיאלוג לעצירת טיימר
    private void showStopTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stop Timer")
                .setMessage("Are you sure you want to stop the timer?")
                .setPositiveButton("Yes", (dialog, which) -> stopTimer())
                .setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black));
        });
        dialog.show();
    }

    // דיאלוג להמשך טיימר
    private void showResumeTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resume Timer")
                .setMessage("Would you like to resume the timer?")
                .setPositiveButton("Yes", (dialog, which) -> resumeTimer())
                .setNegativeButton("No", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.black));
        });
        dialog.show();
    }

    // התחלת טיימר
    private void startTimer() {
        isTimerRunning = true;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                saveTimerState(); // שמירת מצב הטיימר
                updateTimerUI();  // עדכון ממשק
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                updateTimerUI();
                Toast.makeText(RecipeActivity.this, "Timer finished", Toast.LENGTH_SHORT).show();
            }
        }.start();
        saveTimerState();
        updateTimerUI();
    }

    // עצירת טיימר
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            saveTimerState();
            updateTimerUI();
        }
    }

    // המשך טיימר
    private void resumeTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startTimer();
    }

    // איפוס טיימר
    private void resetTimer() {
        timeLeftInMillis = (long) currentRecipe.getTimerDuration() * 60 * 1000;
        saveTimerState();
        updateTimerUI();
    }

    // שמירת מצב טיימר ב-SharedPreferences
    private void saveTimerState() {
        SharedPreferences sharedPreferences = getSharedPreferences("TimerPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("timeLeftInMillis", timeLeftInMillis);
        editor.putBoolean("isTimerRunning", isTimerRunning);
        editor.apply();
    }


    // עדכון טקסט הטיימר בממשק
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

    // דיאלוג יציאה מהמתכון
    private void showExitRecipeDialog() {
        if (isTimerRunning) {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(RecipeActivity.this);
            dialogBuilder.setMessage("Are you sure you want to exit?\nThe timer will stop ")
                    .setCancelable(false)
                    .setPositiveButton("yes", (dialog1, which) -> {
                        Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                        intent.putExtra("user_id", getIntent().getIntExtra("user_id", -1));
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("cancel", null);

            androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
            dialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(ContextCompat.getColor(RecipeActivity.this, android.R.color.black));
                negativeButton.setTextColor(ContextCompat.getColor(RecipeActivity.this, android.R.color.black));
            });

            dialog.show();
        } else {
            finish(); // סגירה מיידית אם אין טיימר פעיל
        }
    }

    // מוסיפה או מסירה את המתכון מרשימת המועדפים של המשתמש בהתאם למצב הנוכחי
    private void toggleFavorite() {
        new Thread(() -> {
            if (isFavorite) {
                // אם המתכון כבר מסומן כמועדף - הסרה מהטבלה של המועדפים במסד הנתונים
                 database.favoriteRecipeDao().deleteFavorite(currentUserId, currentRecipe.getRecipeId());
                isFavorite = false;
            } else {
                // אם המתכון לא במועדפים - הוספה של רשומה חדשה עם מזהה המשתמש והמתכון
                 database.favoriteRecipeDao().insert(new FavoriteRecipe(currentUserId, currentRecipe.getRecipeId()));
                isFavorite = true;
            }

            // לאחר שינוי הנתון במסד, עדכון אייקון הממשק - מתבצע על ה-thread הראשי
            runOnUiThread(this::updateFavoriteIcon);
        }).start();
    }
}