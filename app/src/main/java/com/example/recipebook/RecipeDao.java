package com.example.recipebook;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao // מציין שמדובר ב-Data Access Object (DAO) עבור בסיס נתונים של Room
public interface RecipeDao {

    @Insert // מוסיף מתכון חדש לדאטה בייס
    void insert(Recipe recipe);

    @Update // מעדכן מתכון קיים
    void update(Recipe recipe);


    @Query("SELECT * FROM recipes") // מחזיר את כל המתכונים בטבלה
    List<Recipe> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE category = :category") // מחזיר את המתכונים לפי קטגוריה
    List<Recipe> getRecipesByCategory(String category);

    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId") // מחזיר מתכון ספציפי לפי ה-ID שהתקבל
    Recipe getRecipeById(int recipeId);
}