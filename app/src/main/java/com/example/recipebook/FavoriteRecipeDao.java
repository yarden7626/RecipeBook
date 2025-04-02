package com.example.recipebook;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteRecipeDao {
    @Insert
    void insert(FavoriteRecipe favoriteRecipe);

    @Delete
    void delete(FavoriteRecipe favoriteRecipe);

    @Query("SELECT * FROM favorite_recipes WHERE userId = :userId")
    LiveData<List<FavoriteRecipe>> getFavoritesByUserId(String userId);

    @Query("SELECT * FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId")
    FavoriteRecipe getFavorite(String userId, int recipeId);

    @Query("DELETE FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId")
    void deleteFavorite(String userId, int recipeId);
} 