package com.example.recipebook;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataManager {
    private static DataManager instance;  // משתנה סטטי לשמירת מופע יחיד של DataManager
    private final AppDatabase database;   // אובייקט גישה למסד הנתונים
    private final ExecutorService executorService;  // Executor לביצוע משימות על חוט נפרד

    // קונסטרוקטור פרטי, מקבל Context ומאתחל את בסיס הנתונים ואת ה-Executor
    private DataManager(Context context) {
        database = AppDatabase.getInstance(context);  // מאתחל את בסיס הנתונים
        executorService = Executors.newSingleThreadExecutor();  // יוצר Executor עם חוט יחיד
    }

    // פונקציה ליצירת מופע יחיד של DataManager (Singleton)
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {  // אם המופע לא קיים, יוצר מופע חדש
            instance = new DataManager(context);
        }
        return instance;  // מחזיר את המופע הקיים או החדש
    }

    // פונקציה שמחזירה את המידע של מתכון לפי ה-ID שלו
    public Recipe getInfo(int index) {
        return database.recipeDao().getRecipeById(index);  // שולף את המתכון לפי ID
    }

    // פונקציה שמחזירה את כל המתכונים כ-LiveData
    public LiveData<List<Recipe>> getAllRecipes() {
        MutableLiveData<List<Recipe>> liveData = new MutableLiveData<>();  // יצירת MutableLiveData
        executorService.execute(() -> {  // מבצע את השאילתה על חוט נפרד
            List<Recipe> recipes = database.recipeDao().getAllRecipes();  // שולף את כל המתכונים
            liveData.postValue(recipes);  // מעדכן את ה-LiveData עם רשימת המתכונים
        });
        return liveData;  // מחזיר את ה-LiveData
    }

    // פונקציה שמחזירה את המתכונים לפי קטגוריה כ-LiveData
    public LiveData<List<Recipe>> getRecipesByCategory(String category) {
        MutableLiveData<List<Recipe>> liveData = new MutableLiveData<>();  // יצירת MutableLiveData
        executorService.execute(() -> {  // מבצע את השאילתה על חוט נפרד
            List<Recipe> recipes = database.recipeDao().getRecipesByCategory(category);  // שולף מתכונים לפי קטגוריה
            liveData.postValue(recipes);  // מעדכן את ה-LiveData עם המתכונים שנמצאו
        });
        return liveData;  // מחזיר את ה-LiveData
    }

    // פונקציה שמחזירה את המתכונים המועדפים של משתמש מסוים לפי ה-ID של המשתמש
    public LiveData<List<FavoriteRecipe>> getFavoritesByUserId(int userId) {
        return database.favoriteRecipeDao().getFavoritesByUserId(userId);  // שולף את המתכונים המועדפים של המשתמש
    }

    // פונקציה להוספת מתכון חדש למסד הנתונים
    public void insertRecipe(Recipe recipe) {
        executorService.execute(() -> database.recipeDao().insert(recipe));  // הוספת המתכון על חוט נפרד
    }

    // פונקציה להוספת מתכון למועדפים
    public void insertFavorite(FavoriteRecipe favorite) {
        executorService.execute(() -> database.favoriteRecipeDao().insert(favorite));  // הוספת המתכון למועדפים על חוט נפרד
    }

    // פונקציה למחיקת מתכון מהמועדפים לפי ID של המשתמש והמתכון
    public void deleteFavorite(int userId, int recipeId) {
        executorService.execute(() -> database.favoriteRecipeDao().deleteFavorite(userId, recipeId));  // מחיקת המתכון מהמועדפים
    }
}