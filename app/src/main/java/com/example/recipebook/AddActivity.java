package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

        ImageButton backBtnAdd = findViewById(R.id.backButtonAdd);
        //פונקציה שפותחת דיאלוג כאשר המשתמש לוחץ על כפתור חזור
        backBtnAdd.setOnClickListener( v -> {
            AlertDialog dialog = new AlertDialog.Builder(AddActivity.this)
                    .setMessage("Are you sure you wanna to out? Your data will not be saved")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        //אם המשתמש בחר שכן , הוא יעבור למסך של רשימת המתכונים והמסך הנוכחי ייסגר והנתונים שערך לא יישמרו
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(AddActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", null) //אם המשתמש בחר לבטל נשארים במסך הנוכחי
                    .create();
                     dialog.show();
        });

    }
}
