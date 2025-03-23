package com.example.recipebook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class AddActivity extends AppCompatActivity {

    // הגדרת משתנים גלובליים
    private EditText editRecipeName, editPreparationTime, editIngredients, editDirections; // שדות קלט למתכון
    private Spinner categorySpinner; // תפריט בחירת קטגוריה
    private Button saveButton, categoryButton, AddPhotoBtn, AddTimer; // כפתורים
    private ImageButton backButtonAdd, timerButton; // כפתורי תמונה
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים
    private ImageView addImage; // תצוגת התמונה
    private Uri imageUri; // URI של התמונה שנבחרה
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "RecipePrefs";
    private static final String CATEGORY_KEY = "selectedCategoryPosition";

    // יצירת Contract חדש לבחירת תמונה
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        }
                        addImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // אתחול בסיס הנתונים
        database = AppDatabase.getInstance(this);

        // קישור המשתנים לאלמנטים בממשק המשתמש
        editRecipeName = findViewById(R.id.editRecipeName);
        editPreparationTime = findViewById(R.id.editPreparationTime);
        editIngredients = findViewById(R.id.editIngredients);
        editDirections = findViewById(R.id.editDirections);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        backButtonAdd = findViewById(R.id.backButtonAdd);
        addImage = findViewById(R.id.addImage);
        AddPhotoBtn = findViewById(R.id.AddPhotoBtn);
        categoryButton = findViewById(R.id.categoryButton);
        AddTimer = findViewById(R.id.AddTimer);
        timerButton = findViewById(R.id.timerButton);

        // הגדרת תפריט הקטגוריות
        setupCategorySpinner();

        // הגדרת מאזיני לחיצה לכפתורים
        saveButton.setOnClickListener(v -> saveRecipe());
        
        // הגדרת מאזיני לחיצה לתמונה
        View.OnClickListener imageSelectionListener = v -> checkAndRequestPermission();
        addImage.setOnClickListener(imageSelectionListener);
        AddPhotoBtn.setOnClickListener(imageSelectionListener);

        // הגדרת כפתור חזרה עם דיאלוג
        backButtonAdd.setOnClickListener(v -> showExitDialog());

        // הגדרת כפתור קטגוריה
        categoryButton.setOnClickListener(v -> categorySpinner.performClick());
    }

    // פונקציה להצגת דיאלוג יציאה
    private void showExitDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(AddActivity.this);
        dialogBuilder.setMessage("האם אתה בטוח שברצונך לצאת? הנתונים לא יישמרו")
                .setCancelable(false)
                .setPositiveButton("כן", (dialog1, which) -> {
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("ביטול", null);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    // פונקציה לבדיקת הרשאות
    private void checkAndRequestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            openImagePicker();
        }
    }

    // פונקציה לפתיחת בוחר התמונות
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // פונקציה להגדרת תפריט הקטגוריות
    private void setupCategorySpinner() {
        // קבלת רשימת הקטגוריות מקובץ המשאבים
        String[] categories = getResources().getStringArray(R.array.recipe_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // קריאת הבחירה השמורה
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedPosition = prefs.getInt(CATEGORY_KEY, -1);

        // עדכון הבחירה ב־Spinner אם יש ערך שמור
        if (savedPosition != -1) {
            categorySpinner.setSelection(savedPosition, false);
        }

        // שמירת הבחירה החדשה
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(CATEGORY_KEY, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    // פונקציה לשמירת המתכון
    private void saveRecipe() {
        // קבלת ערכים משדות הקלט
        String name = editRecipeName.getText().toString().trim();
        String time = editPreparationTime.getText().toString().trim();
        String ingredients = editIngredients.getText().toString().trim();
        String instructions = editDirections.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        // בדיקת תקינות הקלט
        if (name.isEmpty() || time.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת אובייקט מתכון חדש עם כל הפרמטרים הנדרשים
        Recipe recipe = new Recipe(
            name,           // recipeName
            category,       // category
            time,          // prepTime
            instructions,  // directions
            imageUri != null ? imageUri.toString() : "",  // image
            false,         // isFavorite
            ingredients,   // ingredients
            "1"           // userId (משתמש קבוע כרגע)
        );

        // שמירת המתכון בבסיס הנתונים
        new Thread(() -> {
            database.recipeDao().insert(recipe);
            runOnUiThread(() -> {
                Toast.makeText(AddActivity.this, "Recipe saved successfully", Toast.LENGTH_SHORT).show();
                // החזרת המתכון החדש למסך הראשי
                Intent resultIntent = new Intent();
                resultIntent.putExtra("recipeId", recipe.getRecipeId());
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }).start();
    }
}