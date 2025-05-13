package com.example.recipebook;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// מגדיר מחלקת בסיס נתונים עם הישויות: משתמש, מתכון, ומתכון מועדף

@Database(entities = {User.class, Recipe.class, FavoriteRecipe.class}, version = 1, exportSchema = false)
// מגדיר את מחלקת הממירים שמטפלת בהמרות טיפוסי נתונים מורכבים
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    // מופע יחיד של בסיס הנתונים
    private static AppDatabase instance;

    // מתודות מופשטות לקבלת ממשקי גישה לטבלאות השונות
    public abstract UserDao userDao();
    public abstract RecipeDao recipeDao();
    public abstract FavoriteRecipeDao favoriteRecipeDao();

    // מתודה סינכרונית להשגת מופע בסיס הנתונים (מימוש תבנית סינגלטון)
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // יצירת מופע חדש של בסיס הנתונים אם עדיין לא קיים
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "recipe_database")
                    // אפשרות למחוק ולבנות מחדש את בסיס הנתונים במקרה של שינוי בסכמה
                    .fallbackToDestructiveMigration()
                    // מאפשר גישה לבסיס הנתונים מהתהליך הראשי
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    // מתודה לסגירת בסיס הנתונים ושחרור המשאבים
    public static void destroyInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}