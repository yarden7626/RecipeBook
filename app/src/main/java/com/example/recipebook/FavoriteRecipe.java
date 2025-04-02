package com.example.recipebook;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_recipes",
        foreignKeys = {
            @ForeignKey(entity = User.class,
                    parentColumns = "id",
                    childColumns = "userId",
                    onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Recipe.class,
                    parentColumns = "recipeId",
                    childColumns = "recipeId",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index("userId"),
            @Index("recipeId")
        })
public class FavoriteRecipe {
    @PrimaryKey(autoGenerate = true)
    private int favoriteId;
    private String userId;
    private int recipeId;

    public FavoriteRecipe(String userId, int recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
} 