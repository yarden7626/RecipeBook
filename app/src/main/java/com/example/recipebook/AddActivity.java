package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RecipePrefs";
    private static final String CATEGORY_KEY = "selectedCategoryPosition";
    private static final int IMAGE_REQUEST_CODE = 1;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;

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
        ImageView imageView = findViewById(R.id.addImage);

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

        if (savedPosition != -1) {
            categorySpinner.post(() -> categorySpinner.setSelection(savedPosition, false));
        }

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

        // בחירת תמונה מהגלריה
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_REQUEST_CODE);
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
            Map<String, Object> recipeData = new HashMap<>();
            recipeData.put("id", recipeId);
            recipeData.put("name", name);
            recipeData.put("time", time);
            recipeData.put("ingredients", ingredients);
            recipeData.put("instructions", instructions);
            recipeData.put("category", category);

            if (recipeId != null) {
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

    private void saveRecipeToFirebase(Map<String, Object> recipeData) {
        databaseReference.child((String) recipeData.get("id")).setValue(recipeData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(AddActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddActivity.this, "Failed to save recipe", Toast.LENGTH_SHORT).show());
    }

    // קבלת תוצאה לאחר בחירת תמונה
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imageView = findViewById(R.id.addImage);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}