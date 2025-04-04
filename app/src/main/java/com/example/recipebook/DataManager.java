package com.example.recipebook;

import android.content.Context;

import androidx.room.Room;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataManager {
    private static DataManager instance;
    private final AppDatabase database;
    private final ExecutorService executorService;

    private DataManager(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    public Recipe getInfo(int index) {
        // שולף מידע ממתכון לפי ID
        return database.recipeDao().getRecipeById(index);
    }

    public LiveData<List<Recipe>> getAllRecipes() {
        MutableLiveData<List<Recipe>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Recipe> recipes = database.recipeDao().getAllRecipes();
            liveData.postValue(recipes);
        });
        return liveData;
    }

    public LiveData<List<Recipe>> getRecipesByCategory(String category) {
        MutableLiveData<List<Recipe>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Recipe> recipes = database.recipeDao().getRecipesByCategory(category);
            liveData.postValue(recipes);
        });
        return liveData;
    }

    public LiveData<List<FavoriteRecipe>> getFavoritesByUserId(int userId) {
        return database.favoriteRecipeDao().getFavoritesByUserId(userId);
    }

    public void insertRecipe(Recipe recipe) {
        executorService.execute(() -> database.recipeDao().insert(recipe));
    }

    public void updateRecipe(Recipe recipe) {
        executorService.execute(() -> database.recipeDao().update(recipe));
    }

    public void deleteRecipe(Recipe recipe) {
        executorService.execute(() -> database.recipeDao().delete(recipe));
    }

    public void insertFavorite(FavoriteRecipe favorite) {
        executorService.execute(() -> database.favoriteRecipeDao().insert(favorite));
    }

    public void deleteFavorite(int userId, int recipeId) {
        executorService.execute(() -> database.favoriteRecipeDao().deleteFavorite(userId, recipeId));
    }
}
