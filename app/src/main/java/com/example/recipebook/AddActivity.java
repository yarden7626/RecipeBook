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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RecipePrefs";
    private static final String CATEGORY_KEY = "selectedCategoryPosition";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private ImageView imageView;
    private Button addPhotoBtn;
    private Button categoryButton;

    // יצירת Contract חדש
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                        imageView.setImageBitmap(bitmap);
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

        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        Button saveButton = findViewById(R.id.saveButton);
        EditText nameInput = findViewById(R.id.editRecipeName);
        EditText timeInput = findViewById(R.id.editPreparationTime);
        EditText ingredientsInput = findViewById(R.id.editIngredients);
        EditText instructionsInput = findViewById(R.id.editDirections);
        imageView = findViewById(R.id.addImage);
        addPhotoBtn = findViewById(R.id.AddPhotoBtn);
        categoryButton = findViewById(R.id.categoryButton);

        // חיבור למסד הנתונים של Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Recipes");
        storageReference = FirebaseStorage.getInstance().getReference("RecipeImages");

        // יצירת האדפטר והגדרת רשימת הקטגוריות
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recipe_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // קריאת הבחירה השמורה
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedPosition = prefs.getInt(CATEGORY_KEY, -1);

        // עדכון הבחירה ב־Spinner אם יש ערך שמור
        if (savedPosition != -1) {
            categorySpinner.setSelection(savedPosition, false); // עדכון הבחירה בספינר
        }
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                // שמירת המיקום שנבחר ב־SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(CATEGORY_KEY, position); // ישירות שמירת position
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        View.OnClickListener imageSelectionListener = v -> checkAndRequestPermission();
        
        // Set click listeners for both image selection UI elements
        imageView.setOnClickListener(imageSelectionListener);
        addPhotoBtn.setOnClickListener(imageSelectionListener);

        // Add click listener for category button
        categoryButton.setOnClickListener(v -> {
            categorySpinner.performClick();
        });

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String ingredients = ingredientsInput.getText().toString().trim();
            String instructions = instructionsInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();

            if (name.isEmpty() || time.isEmpty() || ingredients.isEmpty() || instructions.isEmpty()) {
                Toast.makeText(AddActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // יצירת מפתח ייחודי לכל מתכון
            String recipeId = databaseReference.push().getKey();
            if (recipeId != null) {
                Map<String, Object> recipeData = new HashMap<>();
                recipeData.put("id", recipeId);
                recipeData.put("name", name);
                recipeData.put("time", time);
                recipeData.put("ingredients", ingredients);
                recipeData.put("instructions", instructions);
                recipeData.put("category", category);

                // שמירת תמונה ב-Firebase Storage
                if (imageUri != null) {
                    StorageReference imageRef = storageReference.child(recipeId + ".jpg");
                    imageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    recipeData.put("imageUrl", uri.toString());
                                    saveRecipeToFirebase(recipeData);
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                saveRecipeToFirebase(recipeData);
                            });
                } else {
                    saveRecipeToFirebase(recipeData);
                }
            }
        });

        ImageButton backBtnAdd = findViewById(R.id.backButtonAdd);
        backBtnAdd.setOnClickListener(v -> {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(AddActivity.this);
            dialogBuilder.setMessage("Are you sure you want to exit? Your data will not be saved")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog1, which) -> {
                        Intent intent = new Intent(AddActivity.this, MainActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null);

            androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(AddActivity.this, R.color.green));
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AddActivity.this, R.color.red));
            });
        });
    }

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

    private void saveRecipeToFirebase(Map<String, Object> recipeData) {
        String recipeId = (String) recipeData.get("id");

        // אם ה-id לא קיים, ניצור מפתח חדש
        if (recipeId == null) {
            recipeId = databaseReference.push().getKey(); // יצירת מפתח חדש אם id לא קיים
            recipeData.put("id", recipeId); // עדכון ה־id במפה
        }

        // אם עדיין יש מצב ש-id הוא null, ניצור מפתח חדש
        if (recipeId == null) {
            // במקרה כזה, אין איך להמשיך בלי id תקני
            Toast.makeText(AddActivity.this, "Failed to generate recipe ID", Toast.LENGTH_SHORT).show();
            return; // יציאה מהפונקציה במקרה של כשלון
        }

        // שמירת המתכון ב-Firebase
        String finalRecipeId = recipeId;
        databaseReference.child(recipeId).setValue(recipeData)
                .addOnSuccessListener(unused -> {
                    // הודעת הצלחה
                    Toast.makeText(AddActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();

                    // יצירת Intent ושליחה לפעילות ראשית (MainActivity) עם תוצאה
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("recipeId", finalRecipeId); // החזרת ה-ID של המתכון שנשמר
                    setResult(RESULT_OK, resultIntent); // הגדרת תוצאה חיובית
                    finish(); // סגירת הפעילות הנוכחית (AddActivity)
                })
                .addOnFailureListener(e -> {
                    // הודעת שגיאה אם משהו משתבש
                    Toast.makeText(AddActivity.this, "Error saving recipe", Toast.LENGTH_SHORT).show();
                });
    }
}