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
import android.util.Log;
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
import android.app.TimePickerDialog;

public class AddActivity extends AppCompatActivity {

    private static final String TAG = "AddActivity"; // תגית ללוגים

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
    private int timerDuration = 0; // זמן הטיימר בדקות

    // יצירת Contract חדש לבחירת תמונה
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Log.d(TAG, "Selected image URI: " + imageUri);
                    try {
                        // שמירת ה-URI הקבוע של התמונה
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                        Log.d(TAG, "Got persistent permission for URI");

                        // הצגת התמונה בתצוגה המקדימה
                        Bitmap bitmap;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Log.d(TAG, "Using ImageDecoder for Android P and above");
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                        } else {
                            Log.d(TAG, "Using MediaStore for older Android versions");
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        }
                        addImage.setImageBitmap(bitmap);
                        Log.d(TAG, "Successfully loaded image preview");
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading image preview", e);
                        Toast.makeText(this, "Error loading image preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

        // הגדרת מאזיני לחיצה לכפתור טיימר
        AddTimer.setOnClickListener(v -> showTimerDialog()); // הוספתי את הכפתור AddTimer לדיאלוג טיימר
        timerButton.setOnClickListener(v -> showTimerDialog());
    }

    // פונקציה להצגת דיאלוג יציאה
    private void showExitDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(AddActivity.this);
        dialogBuilder.setMessage("Are you sure you want to exit?\nYour data will not be saved")
                .setCancelable(false)
                .setPositiveButton("yes", (dialog1, which) -> {
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("cancel", null);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(android.R.color.black));
            negativeButton.setTextColor(getResources().getColor(android.R.color.black));
        });
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
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

    // פונקציה להצגת דיאלוג הגדרת טיימר
    private void showTimerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // המרת השעות לדקות
                    timerDuration = (hourOfDay * 60) + minute;
                    if (timerDuration > 0) {
                        String timeText = String.format("%02d:%02d", hourOfDay, minute);
                        Toast.makeText(this, "Timer set to " + timeText, Toast.LENGTH_SHORT).show();
                    }
                },
                0, // שעה התחלתית
                0, // דקה התחלתית
                true // פורמט 24 שעות
        );
        timePickerDialog.setTitle("Set Timer Duration");
        timePickerDialog.show();
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

        // שמירת ה-URI של התמונה
        String imageUriString = "";
        if (imageUri != null) {
            imageUriString = imageUri.toString();
            Log.d(TAG, "Saving image URI: " + imageUriString);
        }

        // יצירת אובייקט מתכון חדש עם כל הפרמטרים הנדרשים
        Recipe recipe = new Recipe(
                name,           // recipeName
                category,       // category
                time,          // prepTime
                instructions,  // directions
                imageUriString,  // image
                false,         // isFavorite
                ingredients,   // ingredients
                "1",          // userId (משתמש קבוע כרגע)
                timerDuration // timerDuration
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