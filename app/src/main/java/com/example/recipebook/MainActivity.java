package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Recipe> recipeList = new ArrayList<>();


        // אתחול של ה-RecyclerView
      RecyclerView recyclerView = findViewById(R.id.rv_recipes);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // יצירת ה-Adapter
        RecipeAdapter recipeAdapter = new RecipeAdapter(recipeList, this);

        // קישור ה-Adapter ל-RecyclerView
        recyclerView.setAdapter(recipeAdapter);

        FloatingActionButton addRecipeBtn = findViewById(R.id.addRecipeBtn);
       //פונקציה שפועלת כאשר לוחצים על כפתור הפלוס ומעבירה למסך הוספת מתכון חדש
       addRecipeBtn.setOnClickListener(v -> {
           Intent intent = new Intent(MainActivity.this , AddActivity.class);
           startActivity(intent);//עובר למסך הוספת מתכון
       });

        ImageButton backBtnList = findViewById(R.id.backButtonList);
        //פונקציית חזרה למסך הראשי של ההתחברות והרשמה, פועלת כאשר לוחצים על כפתור חזור
        backBtnList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent); //עובר למסך הכניסה
        });
    }
}