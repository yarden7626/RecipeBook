package com.example.recipebook;

import android.content.Context;

import androidx.room.Room;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DataManager {

    Context context;
    AppDatabase appDatabase;

    public DataManager(Context context) {
        appDatabase = AppDatabase.getInstance(context);
    }

    public Recipe getInfo(int index) {
        // שולף מידע ממתכון לפי ID
        return appDatabase.recipeDao().getRecipeById(index);
    }

    public LiveData<List<Recipe>> getAllRecipes(int userId) {
        return appDatabase.recipeDao().getAllRecipes(userId);
    }

    public void upDateRecipe(Recipe recipe) {
        // מעדכן מתכון
        appDatabase.recipeDao().update(recipe);
    }

    public void addRecipe(Recipe recipe) {
        // מוסיף מתכון חדש
        appDatabase.recipeDao().insert(recipe);
    }
}
