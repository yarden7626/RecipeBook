package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

public class AddActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RecipePrefs";
    private static final String CATEGORY_KEY = "selectedCategoryPosition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        Button categoryButton = findViewById(R.id.categoryButton);

        // יצירת האדפטר והגדרת רשימת הקטגוריות
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recipe_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // קריאת הבחירה השמורה
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedPosition = prefs.getInt(CATEGORY_KEY, -1);

        // טוענים את הבחירה הקודמת **אחרי שהאדפטר מחובר לספינר**
        if (savedPosition != -1) {
            categorySpinner.post(() -> categorySpinner.setSelection(savedPosition, false));
        }

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                // שמירת הבחירה
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(CATEGORY_KEY, position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // אין צורך בפעולה כאן
            }
        });

        // כפתור הבחירה – פותח את הספינר במקום להסתיר אותו
        categoryButton.setOnClickListener(v -> categorySpinner.performClick());

        ImageButton backBtnAdd = findViewById(R.id.backButtonAdd);
        backBtnAdd.setOnClickListener(v -> {
            // יצירת הדיאלוג באמצעות MaterialAlertDialogBuilder
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(AddActivity.this);
            dialogBuilder.setMessage("Are you sure you want to exit?                    Your data will not be saved")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog1, which) -> {
                        Intent intent = new Intent(AddActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null);

            // יצירת הדיאלוג
            androidx.appcompat.app.AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            // הגדרת צבעים לכפתורים
            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(AddActivity.this, R.color.green)); // כפתור "Yes"
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AddActivity.this, R.color.red)); // כפתור "Cancel"
            });
        });
    }
}
