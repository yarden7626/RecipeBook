package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Recipe> recipeList = new ArrayList<>();
        for (int i=0; i<5; i++)
        {
            recipeList.add(new Recipe(String.valueOf(i)));
        }

        // אתחול של ה-RecyclerView
      RecyclerView recyclerView = findViewById(R.id.rv_recipes);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // יצירת ה-Adapter
        RecipeAdapter recipeAdapter = new RecipeAdapter(recipeList, this);

        // קישור ה-Adapter ל-RecyclerView
        recyclerView.setAdapter(recipeAdapter);
    }
}