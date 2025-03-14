package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Recipe> recipeList;  // הוספתי את הגדרת המשתנה
    private RecipeAdapter recipeAdapter;    // הוספתי את הגדרת ה-Adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeList = new ArrayList<>();  // אתחול של רשימת המתכונים

        // אתחול של ה-RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rv_recipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // יצירת ה-Adapter
        recipeAdapter = new RecipeAdapter(recipeList, this);

        // קישור ה-Adapter ל-RecyclerView
        recyclerView.setAdapter(recipeAdapter);

        FloatingActionButton addRecipeBtn = findViewById(R.id.addRecipeBtn);
        // פונקציה שפועלת כאשר לוחצים על כפתור הפלוס ומעבירה למסך הוספת מתכון חדש
        addRecipeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            startActivityForResult(intent, 1);  // שימוש ב-startActivityForResult
        });

        ImageButton backBtnList = findViewById(R.id.backButtonList);
        // פונקציית חזרה למסך הראשי של ההתחברות והרשמה
        backBtnList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent); //עובר למסך הכניסה
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // קבלת התוצאה מהמסך של הוספת מתכון, למשל נתוני המתכון שהוזן
                Recipe newRecipe = (Recipe) data.getSerializableExtra("newRecipe");

                // הוספת המתכון לרשימה ועדכון ה-Adapter
                if (newRecipe != null) {
                    recipeList.add(newRecipe);
                    recipeAdapter.notifyItemInserted(recipeList.size() - 1); // עדכון ה-RecyclerView
                }
            } else {
                // טיפול במצב בו לא התקבלה תוצאה תקינה
                Toast.makeText(this, "Failed to add recipe", Toast.LENGTH_SHORT).show();
            }
        }
    }
}