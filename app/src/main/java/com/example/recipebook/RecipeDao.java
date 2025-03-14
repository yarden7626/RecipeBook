package com.example.recipebook;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    long insert(Recipe recipe);

    @Update
    void update(Recipe recipe);

    @Delete
    void delete(Recipe recipe);

    @Query("SELECT * FROM recipes WHERE userId = :userId")
    List<Recipe> getAllRecipes(String userId);

    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId")
    Recipe getRecipeById(int recipeId);
}