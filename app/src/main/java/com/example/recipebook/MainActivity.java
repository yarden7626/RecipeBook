package com.example.recipebook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    // הגדרת משתנים גלובליים
    private RecyclerView recyclerView; // תצוגת רשימת המתכונים
    private RecipeAdapter adapter; // מתאם להצגת המתכונים
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים
    private FloatingActionButton addRecipeBtn; // כפתור הוספת מתכון
    private ImageButton backButtonList; // כפתור חזרה
    private FloatingActionButton filterButton; // כפתור סינון
    private TextView titleText; // כותרת המסך
    private int currentUserId; // מזהה המשתמש הנוכחי
    private ActivityResultLauncher<Intent> addRecipeLauncher; // משגר לפתיחת AddActivity וקבלת תוצאה בפורמט החדש
    private DataManager dataManager; // אובייקט לניהול הנתונים
    private String currentFilter = "Show All"; // פילטר נוכחי

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

        // אתחול ה-ActivityResultLauncher
        addRecipeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        // טיפול בתוצאה אם צריך
                        loadRecipes();
                    }
                }
        );

        // הגדרת כפתור הוספת מתכון חדש עם השימוש ב-ActivityResultLauncher
        addRecipeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            intent.putExtra("user_id", currentUserId);
            addRecipeLauncher.launch(intent);
        });

        backButtonList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // הגדרת מאזין לחיצה לכפתור הסינון
        filterButton.setOnClickListener(v -> showFilterDialog());

        // טעינת המתכונים והמועדפים
        loadRecipes();
    }

    // הצגת דיאלוג המאפשר לבחור פילטר (הצגת כל המתכונים, מועדפים או לפי קטגוריה)
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Show Only");

        // יצירת רשימת אפשרויות לפילטור
        String[] filterOptions = {
                getString(R.string.show_all),
                getString(R.string.filter_by_favorites),
                getString(R.string.filter_by_category)
        };

        builder.setItems(filterOptions, (dialog, which) -> {
            switch (which) {
                case 0: // Show All
                    currentFilter = getString(R.string.show_all);
                    loadRecipes(); // טעינת כל המתכונים
                    break;
                case 1: // Favorites
                    currentFilter = getString(R.string.filter_by_favorites);
                    loadFavoriteRecipes(); // טעינת המתכונים המועדפים
                    break;
                case 2: // Filter by Category
                    showCategoryDialog(); // הצגת דיאלוג לבחור קטגוריה
                    break;
            }
        });

        builder.show();
    }

    // הצגת דיאלוג לבחירת קטגוריית המתכונים
    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.filter_by_category);

        // קבלת הקטגוריות מהמשאבים
        String[] categories = getResources().getStringArray(R.array.recipe_categories);

        builder.setItems(categories, (dialog, which) -> {
            currentFilter = categories[which]; // עדכון הפילטר לפי הקטגוריה שנבחרה
            loadRecipesByCategory(categories[which]); // טעינת המתכונים לפי קטגוריה
        });

        builder.show();
    }

    // טעינת כל המתכונים
    @SuppressLint("NotifyDataSetChanged")
    private void loadRecipes() {
        dataManager.getAllRecipes().observe(this, recipes -> {
            if (currentFilter.equals(getString(R.string.show_all))) {
                adapter.setRecipes(recipes); // הצגת כל המתכונים ברשימה
            }
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

    // טעינת המתכונים המועדפים של המשתמש
    @SuppressLint("NotifyDataSetChanged")
    private void loadFavoriteRecipes() {
        new Thread(() -> {
            List<Recipe> favoriteRecipes = database.favoriteRecipeDao().getFavoriteRecipes(currentUserId);
            runOnUiThread(() -> {
                adapter.setRecipes(favoriteRecipes);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    // טעינת מתכונים לפי קטגוריה
    private void loadRecipesByCategory(String category) {
        new Thread(() -> {
            List<Recipe> categoryRecipes = database.recipeDao().getRecipesByCategory(category);
            runOnUiThread(() -> {
                adapter.setRecipes(categoryRecipes);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    // טיפול בלחיצה על מתכון מתוך הרשימה
    private void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe_id", recipe.getRecipeId()); // שליחה של מזהה המתכון
        intent.putExtra("user_id", currentUserId); // שליחה של מזהה המשתמש
        startActivity(intent); // מעבר למסך המתכון
    }

    // פונקציה שמקבלת את התוצאה ממסך אחר (למשל, הוספת מתכון) ומטפלת בה
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // אם התוצאה היא עבור בקשה להוספת מתכון (requestCode == 1) והתוצאה היא הצלחה (RESULT_OK)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // הצגת הודעת הצלחה על כך שהמתכון נוסף בהצלחה
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