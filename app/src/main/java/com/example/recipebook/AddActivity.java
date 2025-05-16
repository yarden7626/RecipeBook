package com.example.recipebook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import android.os.Environment;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import android.app.TimePickerDialog;
import java.util.Arrays;
import android.app.AlertDialog;



 // מחלקה זו אחראית על מסך הוספת מתכון חדש
 // המשתמש יכול להזין שם מתכון, קטגוריה, זמן הכנה, רכיבים והוראות הכנה
 //  ניתן גם להוסיף תמונה ולהגדיר טיימר

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
    private static final int PERMISSION_REQUEST_CODE = 100; // קוד בקשת הרשאות
    private static final String PREFS_NAME = "RecipePrefs"; // שם קובץ ההעדפות
    private static final String CATEGORY_KEY = "selectedCategoryPosition"; // מפתח לשמירת מיקום הקטגוריה
    private static final String CURRENT_PHOTO_PATH = "current_photo_path"; // מפתח לשמירת נתיב התמונה
    private int timerDuration = 0; // זמן הטיימר בדקות
    private String currentPhotoPath; // נתיב לתמונה הנוכחית

    // הגדרת ActivityResultLauncher לבחירת תמונה מהגלריה
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // טעינת התמונה
                        new Thread(() -> {
                            try {
                                Bitmap bitmap;
                                // בדיקת גרסת אנדרואיד לשימוש בשיטה המתאימה לטעינת התמונה
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                                } else {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                }

                                // עדכון התמונה ב-ImageView בתהליך ה-UI הראשי
                                runOnUiThread(() -> {
                                    addImage.setImageBitmap(bitmap);
                                    addImage.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "Successfully loaded image preview");
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error loading image preview", e);
                                runOnUiThread(() -> Toast.makeText(this, "Error loading image preview. Try again", Toast.LENGTH_SHORT).show());
                            }
                        }).start();
                    }
                }
            }
    );

    // הגדרת ActivityResultLauncher לצילום תמונה חדשה
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // טעינת התמונה מהקובץ השמור
                    File photoFile = new File(currentPhotoPath);
                    if (photoFile.exists()) {
                        imageUri = Uri.fromFile(photoFile);
                        try {
                            Bitmap bitmap;
                            // בדיקת גרסת אנדרואיד לשימוש בשיטה המתאימה לטעינת התמונה
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            }

                            // עדכון התמונה ב-ImageView בתהליך ה-UI הראשי
                            runOnUiThread(() -> {
                                addImage.setImageBitmap(bitmap);
                                addImage.setVisibility(View.VISIBLE);
                                Log.d(TAG, "Successfully loaded camera image");
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading camera image", e);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Error loading camera image", Toast.LENGTH_SHORT).show();

                            });
                        }
                    }
                }
            }
    );

     // פונקציה הנקראת בעת יצירת האקטיביטי
     // מאתחלת את כל הרכיבים ומגדירה מאזינים

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

        // הגדרת מאזין לחיצה לכפתור שמירה
        saveButton.setOnClickListener(v -> saveRecipe());

        // הגדרת מאזיני לחיצה לתמונה ולכפתור הוספת תמונה
        View.OnClickListener imageSelectionListener = v -> checkAndRequestPermission();
        addImage.setOnClickListener(imageSelectionListener);
        AddPhotoBtn.setOnClickListener(imageSelectionListener);

        // הגדרת כפתור חזרה עם דיאלוג אישור
        backButtonAdd.setOnClickListener(v -> showExitDialog());

        // הגדרת כפתור קטגוריה - פותח את ה-spinner בלחיצה
        categoryButton.setOnClickListener(v -> categorySpinner.performClick());

        // הגדרת מאזיני לחיצה לכפתורי הטיימר
        AddTimer.setOnClickListener(v -> showTimerDialog());
        timerButton.setOnClickListener(v -> showTimerDialog());
    }


     // פונקציה להצגת דיאלוג אישור יציאה מהמסך
     // מציגה הודעת אזהרה שהנתונים לא יישמרו

    private void showExitDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(AddActivity.this);
        dialogBuilder.setMessage("Are you sure you want to exit?\nYour data will not be saved")
                .setCancelable(false)
                .setPositiveButton("yes", (dialog1, which) -> {
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    intent.putExtra("user_id", getIntent().getIntExtra("user_id", -1));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("cancel", null);

        androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(AddActivity.this, android.R.color.black));
            negativeButton.setTextColor(ContextCompat.getColor(AddActivity.this, android.R.color.black));
        });
        dialog.show();
    }


    //  פונקציה לבדיקת הרשאות הדרושות לגישה לתמונות
     // מבקשת הרשאות מהמשתמש אם הן עדיין לא ניתנו

    private void checkAndRequestPermission() {
        String permission;
        // בדיקת גרסת אנדרואיד לצורך בחירת ההרשאה המתאימה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // בדיקה אם יש צורך לבקש הרשאה
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            openImagePicker();
        }
    }


     // פונקציה לפתיחת בוחר התמונות מהגלריה

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }


      //  פונקציה ליצירת קובץ תמונה זמני עבור צילום מהמצלמה
    //   @return קובץ התמונה שנוצר
     // @throws IOException במקרה של שגיאה ביצירת הקובץ

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


     // פונקציה המופעלת בעת קבלת תוצאות בקשת ההרשאות
     // בודקת אם ההרשאות ניתנו ופועלת בהתאם

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    openImagePicker();
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * פונקציה להגדרת תפריט הקטגוריות (Spinner)
     * טוענת את רשימת הקטגוריות מהמשאבים ומגדירה את ההתנהגות בעת בחירה
     */
    private void setupCategorySpinner() {
        // קבלת רשימת הקטגוריות מקובץ המשאבים
        String[] categories = getResources().getStringArray(R.array.recipe_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // קריאת הבחירה השמורה מההעדפות המשותפות
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedPosition = prefs.getInt(CATEGORY_KEY, -1);

        // עדכון הבחירה ב־Spinner אם יש ערך שמור
        if (savedPosition != -1) {
            categorySpinner.setSelection(savedPosition, false);
        }

        // שמירת הבחירה החדשה בהעדפות המשותפות
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
    //  מאפשרת למשתמש לבחור שעות ודקות

    private void showTimerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // המרת השעות לדקות וחישוב הזמן הכולל בדקות
                    timerDuration = (hourOfDay * 60) + minute;
                    if (timerDuration > 0) {
                        String timeText = String.format("%02d:%02d", hourOfDay, minute);
                        Toast.makeText(this, "Timer set to " + timeText + " ", Toast.LENGTH_SHORT).show();
                    }
                },
                0, // שעה התחלתית
                0, // דקה התחלתית
                true // פורמט 24 שעות
        );
        timePickerDialog.setTitle("Set Timer Duration");

        // שינוי צבע הכפתורים לשחור
        timePickerDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = timePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = timePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(getResources().getColor(android.R.color.black));
            }
            if (negativeButton != null) {
                negativeButton.setTextColor(getResources().getColor(android.R.color.black));
            }
        });

        timePickerDialog.show();
    }


     // פונקציה לשמירת המתכון בבסיס הנתונים
     // בודקת תקינות הקלט ויוצרת אובייקט מתכון חדש
    private void saveRecipe() {
        // קבלת ערכי השדות מהמשתמש
        String name = editRecipeName.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String prepTime = editPreparationTime.getText().toString().trim();
        String directions = editDirections.getText().toString().trim();
        String ingredientsText = editIngredients.getText().toString().trim();

        // בדיקה שכל השדות הנדרשים מולאו
        if (name.isEmpty() || category.isEmpty() || prepTime.isEmpty() ||
                directions.isEmpty() || ingredientsText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // המרת מחרוזת הרכיבים לרשימה על ידי פיצול לפי שורות
        List<String> ingredients = Arrays.asList(ingredientsText.split("\\n"));

        // בדיקה אם נבחרה תמונה, אחרת משתמשים בתמונת ברירת מחדל
        String imagePath = (imageUri != null) ? imageUri.toString() : "android.resource://com.example.recipebook/drawable/plate_icon";

        // יצירת אובייקט מתכון חדש עם כל המידע שנאסף
        Recipe recipe = new Recipe(name, category, prepTime, directions,
                imagePath, false, ingredients, timerDuration);

        // שמירת המתכון בבסיס הנתונים בתהליך נפרד
        new Thread(() -> {
            try {
                database.recipeDao().insert(recipe);
                runOnUiThread(() -> {
                    Toast.makeText(AddActivity.this, "Recipe saved successfully", Toast.LENGTH_SHORT).show();
                    finish(); // סגירת האקטיביטי לאחר השמירה
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(AddActivity.this, "Error saving recipe: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}