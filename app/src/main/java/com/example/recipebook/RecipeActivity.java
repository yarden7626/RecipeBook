package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class RecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        ImageButton backBtnRecipe = findViewById(R.id.backButtonRecipe);
        backBtnRecipe.setOnClickListener(new View.OnClickListener() {
            //פונקצית העברה ממסך המתכון למסך של רשימת המתכונים, פועלת כאשר לוחצים על כפתור חזור
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent(RecipeActivity.this , MainActivity.class);
                startActivity(intent);
            }
        });
    }
}