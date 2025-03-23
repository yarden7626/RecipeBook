package com.example.recipebook;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Recipe.class, User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract RecipeDao recipeDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "recipe_database"
            ).build();
        }
        return instance;
    }
}