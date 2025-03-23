package com.example.recipebook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert //מוסיף מתכון חדש לדאטה בייס    
    void insert(Recipe recipe);

    @Update //מעדכן מתכון קיים
    void update(Recipe recipe);

    @Delete //מוחק מתכון מהדאטה בייס
    void delete(Recipe recipe);

    @Query("SELECT * FROM recipes") //מחזיר רשימה של כל המתכונים בטבלה
    LiveData<List<Recipe>> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId") //מחזיר מתכון ספציפי לפי האיידי שהתקבל
    Recipe getRecipeById(int recipeId);

    @Query("SELECT * FROM recipes WHERE category = :category") //מחזיר רשימה של כל המתכונים בטבלה לפי הקטגוריה שהתקבלה
    LiveData<List<Recipe>> getRecipesByCategory(String category);
}