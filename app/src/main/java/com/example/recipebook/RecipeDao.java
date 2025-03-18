package com.example.recipebook;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert //מוסיף מתכון חדש לדאטה בייס ומחזיר את הID שלו
    long insert(Recipe recipe);

    @Update //מעדכן מתכון קיים
    void update(Recipe recipe);

    @Delete //מוחק מתכון מהדאטה בייס
    void delete(Recipe recipe);

    @Query("SELECT * FROM recipes WHERE userId = :userId") //מחזיר רשימה של כל המתכונים של משתמש מסויים
    List<Recipe> getAllRecipes(String userId);

    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId") //מחזיר מתכון מסויים לפי הID שלו
    Recipe getRecipeById(int recipeId);
}