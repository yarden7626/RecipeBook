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

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity"; // תגית ללוגים

    // הגדרת משתנים גלובליים
    private TextView recipeTitle, cateRecipeValue, pTimeValue, ingrValue, direValue; // שדות טקסט למתכון
    private ImageView imageRecipe, isFavRecipe; // תמונות
    private ImageButton timerButton, backButtonRecipe; // כפתורים
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים
    private Recipe currentRecipe; // המתכון הנוכחי
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 0;
    private int currentUserId;
    private boolean isFavorite;

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
        backButtonRecipe = findViewById(R.id.backButtonRecipe);

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
                });
            }
        }).start();

        // הגדרת מאזיני לחיצה לכפתורים
        backButtonRecipe.setOnClickListener(v -> finish());

        isFavRecipe.setOnClickListener(v -> toggleFavorite());

        timerButton.setOnClickListener(v -> {
            if (currentRecipe != null && currentRecipe.getTimerDuration() > 0) {
                if (isTimerRunning) {
                    stopTimer();
                } else {
                    startTimer();
                }
            } else {
                Toast.makeText(this, "No timer set for this recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

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

    private void startTimer() {
        if (timeLeftInMillis == 0) {
            timeLeftInMillis = currentRecipe.getTimerDuration() * 60 * 1000; // המרה לדקות
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerUI();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timerButton.setImageResource(R.drawable.timer_icon_active);
                Toast.makeText(RecipeActivity.this, "Timer finished!", Toast.LENGTH_SHORT).show();
            }
        }.start();

        isTimerRunning = true;
        timerButton.setImageResource(R.drawable.timer_icon_active);
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            timerButton.setImageResource(R.drawable.timer_icon_active);
        }
    }

    private void updateTimerUI() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftText = String.format("%02d:%02d", minutes, seconds);
        Toast.makeText(this, timeLeftText, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTimerRunning) {
            stopTimer();
        }
    }
;
    @Override
    protected void onResume() {
        super.onResume();
        if (timeLeftInMillis > 0 && !isTimerRunning) {
            startTimer();
        }
    }

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