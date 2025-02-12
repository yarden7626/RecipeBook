package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private ArrayList<Recipe> recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // הכוונה ל-XML

        // אתחול של ה-RecyclerView
        recyclerView = findViewById(R.id.rv_recipes);

        // יצירת ה-Adapter
        recipeAdapter = new RecipeAdapter(recipeList, this);

        // הגדרת ה-LayoutManager (כיצד לסדר את הפריטים)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // קישור ה-Adapter ל-RecyclerView
        recyclerView.setAdapter(recipeAdapter);
    }
}