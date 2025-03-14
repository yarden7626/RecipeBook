package com.example.recipebook;

import android.content.Context;

import androidx.room.Room;

import java.util.List;

public class DataManager {

    Context context;
    AppDatabase appDatabase;

    public DataManager(Context context) {
        this.context = context;
        appDatabase = Room.databaseBuilder(context, AppDatabase.class, "recipes.db").build();
    }

    public Recipe getInfo(int index) {
        // שולף מידע ממתכון לפי ID
        return appDatabase.recipeDao().getRecipeById(index);
    }

    public List<Recipe> getRecipeListInfo(String userID) {
        // מחזירה את כל המתכונים לפי userID
        return appDatabase.recipeDao().getAllRecipes(userID);
    }

    public void upDateRecipe(Recipe recipe) {
        // מעדכן מתכון
        appDatabase.recipeDao().update(recipe);
    }

    public long addRecipe(Recipe recipe) {
        // מוסיף מתכון חדש
        return appDatabase.recipeDao().insert(recipe);
    }
}
