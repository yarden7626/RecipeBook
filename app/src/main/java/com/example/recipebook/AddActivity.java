package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class AddActivity extends AppCompatActivity {

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


        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parentView, View view, int position, long id) {
                // מקבלים את הקטגוריה שנבחרה
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                // מעדכנים את הטקסט של הכפתור עם שם הקטגוריה שנבחרה
                categoryButton.setText(selectedCategory);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parentView) {
                // אם לא נבחר כלום (אם המשתמש לא בחר קטגוריה)
            }
        });

        // כפתור הספינר – הצגת והסתרת ה-Spinner בלחיצה
        categoryButton.setOnClickListener(v -> {
            if (categorySpinner.getVisibility() == View.VISIBLE) {
                categorySpinner.setVisibility(View.GONE);  // הסתרת ה-Spinner
            } else {
                categorySpinner.setVisibility(View.VISIBLE); // הצגת ה-Spinner
            }
        });
    }
}
