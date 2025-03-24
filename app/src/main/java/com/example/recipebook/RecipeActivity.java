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

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity"; // תגית ללוגים

    // הגדרת משתנים גלובליים
    private TextView recipeTitle, cateRecipeValue, pTimeValue, ingrValue, direValue; // שדות טקסט למתכון
    private ImageView imageRecipe, isFavRecipe; // תמונות
    private ImageButton timerButton, backButtonRecipe; // כפתורים
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים
    private Recipe currentRecipe; // המתכון הנוכחי

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

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

        // קבלת מזהה המתכון מהמסך הקודם
        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        if (recipeId != -1) {
            // טעינת המתכון מבסיס הנתונים
            new Thread(() -> {
                currentRecipe = database.recipeDao().getRecipeById(recipeId);
                if (currentRecipe != null) {
                    // עדכון הממשק עם נתוני המתכון
                    runOnUiThread(() -> {
                        updateUI();
                    });
                }
            }).start();
        }

        // הגדרת מאזיני לחיצה לכפתורים
        backButtonRecipe.setOnClickListener(v -> finish());

        isFavRecipe.setOnClickListener(v -> {
            if (currentRecipe != null) {
                currentRecipe.setFavorite(!currentRecipe.getIsFavorite());
                updateFavoriteIcon();
                // שמירת השינוי בבסיס הנתונים
                new Thread(() -> {
                    database.recipeDao().update(currentRecipe);
                }).start();
            }
        });

        timerButton.setOnClickListener(v -> {
            // TODO: להוסיף פונקציונליות טיימר
            Toast.makeText(this, "Timer functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    // פונקציה לעדכון הממשק עם נתוני המתכון
    private void updateUI() {
        recipeTitle.setText(currentRecipe.getRecipeName());
        cateRecipeValue.setText(currentRecipe.getCategory());
        pTimeValue.setText(currentRecipe.getPrepTime());
        ingrValue.setText(currentRecipe.getIngredients());
        direValue.setText(currentRecipe.getDirections());
        updateFavoriteIcon();

        // עדכון תמונת המתכון
        String imageUriString = currentRecipe.getImageUri();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                Log.d(TAG, "Loading image from URI: " + imageUri);
                
                Bitmap bitmap;
                if (imageUriString.startsWith("file://")) {
                    // טעינת תמונה מנתיב קובץ
                    java.io.File file = new java.io.File(imageUri.getPath());
                    if (file.exists()) {
                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        imageRecipe.setImageBitmap(bitmap);
                        Log.d(TAG, "Successfully loaded image from file");
                    } else {
                        Log.e(TAG, "File does not exist: " + file.getAbsolutePath());
                        Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // טעינת תמונה מ-ContentProvider
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    }
                    imageRecipe.setImageBitmap(bitmap);
                    Log.d(TAG, "Successfully loaded image from content provider");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "No image URI found");
            imageRecipe.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    // פונקציה לעדכון אייקון המועדפים
    private void updateFavoriteIcon() {
        if (currentRecipe.getIsFavorite()) {
            isFavRecipe.setImageResource(R.drawable.ic_star_filled);
        } else {
            isFavRecipe.setImageResource(R.drawable.ic_star_empty);
        }
    }
}