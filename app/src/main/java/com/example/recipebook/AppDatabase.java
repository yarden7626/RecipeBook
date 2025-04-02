package com.example.recipebook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Recipe.class, FavoriteRecipe.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract UserDao userDao();
    public abstract RecipeDao recipeDao();
    public abstract FavoriteRecipeDao favoriteRecipeDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "recipe_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}