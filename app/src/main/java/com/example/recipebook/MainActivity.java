package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.lifecycle.Observer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // הגדרת משתנים גלובליים
    private RecyclerView recyclerView; // תצוגת רשימת המתכונים
    private RecipeAdapter adapter; // מתאם להצגת המתכונים
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים
    private FloatingActionButton addRecipeBtn; // כפתור הוספת מתכון
    private ImageButton backButtonList; // כפתור חזרה
    private FloatingActionButton filterButton; // כפתור סינון
    private TextView titleText; // כותרת המסך
    private int currentUserId;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // קבלת ה-ID של המשתמש המחובר
        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            // אם אין user_id, נחזור למסך הכניסה
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // אתחול בסיס הנתונים
        database = AppDatabase.getInstance(this);
        dataManager = DataManager.getInstance(this);

        // קישור המשתנים לאלמנטים בממשק המשתמש
        recyclerView = findViewById(R.id.rv_recipes);
        addRecipeBtn = findViewById(R.id.addRecipeBtn);
        backButtonList = findViewById(R.id.backButtonList);
        filterButton = findViewById(R.id.filterButton);
        titleText = findViewById(R.id.title);

        // הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, this::onRecipeClick, currentUserId);
        recyclerView.setAdapter(adapter);

        // הגדרת כפתור הוספת מתכון חדש
        FloatingActionButton addButton = findViewById(R.id.addRecipeBtn);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            intent.putExtra("user_id", currentUserId);
            startActivityForResult(intent, 1);
        });

        backButtonList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // הגדרת מאזין לחיצה לכפתור הסינון
        filterButton.setOnClickListener(v -> {
            // TODO: להוסיף פונקציונליות סינון
            Toast.makeText(this, "Filter functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        // טעינת המתכונים והמועדפים
        loadRecipes();
    }

    private void loadRecipes() {
        // טעינת המתכונים
        dataManager.getAllRecipes().observe(this, recipes -> {
            adapter.setRecipes(recipes);
        });

        // טעינת המועדפים
        dataManager.getFavoritesByUserId(currentUserId).observe(this, favorites -> {
            // עדכון סטטוס המועדפים בכל מתכון
            for (Recipe recipe : adapter.getRecipes()) {
                boolean isFavorite = false;
                for (FavoriteRecipe favorite : favorites) {
                    if (favorite.getRecipeId() == recipe.getRecipeId()) {
                        isFavorite = true;
                        break;
                    }
                }
                recipe.setFavorite(isFavorite);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe_id", recipe.getRecipeId());
        intent.putExtra("user_id", currentUserId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // רענון רשימת המתכונים בכל חזרה למסך
        loadRecipes();
    }
}