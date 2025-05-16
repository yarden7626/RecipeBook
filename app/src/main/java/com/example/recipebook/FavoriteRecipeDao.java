package com.example.recipebook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao  // מציין שזהו ממשק (Interface) של Room DAO (Data Access Object)
public interface FavoriteRecipeDao {

    @Insert  // פעולה להוספת מתכון מועדף למסד הנתונים
    void insert(FavoriteRecipe favoriteRecipe);


    // פונקציה שמחזירה את כל המתכונים המועדפים של משתמש לפי מזהה המשתמש
    @Query("SELECT * FROM favorite_recipes WHERE userId = :userId")
    LiveData<List<FavoriteRecipe>> getFavoritesByUserId(int userId);

    // פונקציה שמחזירה את המתכון המועדף לפי מזהה המשתמש ומזהה המתכון
    @Query("SELECT * FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId")
    FavoriteRecipe getFavorite(int userId, int recipeId);

    // פונקציה למחיקת מתכון מהמועדפים לפי מזהה המשתמש ומזהה המתכון
    @Query("DELETE FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId")
    void deleteFavorite(int userId, int recipeId);

    // פונקציה שמחזירה את המתכונים המועדפים של משתמש עם פרטי המתכון עצמם
    @Query("SELECT r.* FROM recipes r INNER JOIN favorite_recipes f ON r.recipeId = f.recipeId WHERE f.userId = :userId")
    List<Recipe> getFavoriteRecipes(int userId);
}