package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.lifecycle.Observer;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // אתחול בסיס הנתונים
        database = AppDatabase.getInstance(this);

        // קישור המשתנים לאלמנטים בממשק המשתמש
        recyclerView = findViewById(R.id.rv_recipes);
        addRecipeBtn = findViewById(R.id.addRecipeBtn);
        backButtonList = findViewById(R.id.backButtonList);
        filterButton = findViewById(R.id.filterButton);
        titleText = findViewById(R.id.title);

        // הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recyclerView.setAdapter(adapter);

        // טעינת המתכונים מבסיס הנתונים
        loadRecipes();

        // הגדרת מאזיני לחיצה לכפתורים
        addRecipeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
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
    }

    // פונקציה לטעינת המתכונים מבסיס הנתונים
    private void loadRecipes() {
        database.recipeDao().getAllRecipes().observe(this, recipes -> {
            adapter.setRecipes(recipes);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // מתכון חדש נוסף - רענון הרשימה
            loadRecipes();
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