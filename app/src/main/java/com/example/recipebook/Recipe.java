package com.example.recipebook;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import java.util.List;

@Entity(tableName = "recipes") // מציין שזו ישות שקשורה לטבלת המתכונים בבסיס הנתונים
@TypeConverters({Converters.class}) // מציין שנעשה שימוש בממירי נתונים (Converters) אם יש צורך להמיר סוגים שונים כמו List
public class Recipe {

    @PrimaryKey(autoGenerate = true) // מגדיר את ה-recipeId כמפתח ראשי שמאופס באופן אוטומטי
    private int recipeId; // המספר זיהוי של המתכון
    private String recipeName; // שם המתכון
    private String category; // קטגוריה של המתכון
    private String prepTime; // זמן הכנה בדקות
    private String directions; // הוראות הכנה
    private String imageUri; // תמונה של המתכון
    private boolean isFavorite; // האם המתכון במועדפים
    private List<String> ingredients; // רשימה של רכיבים במתכון
    private int timerDuration; // זמן הטיימר בדקות

    // פעולה בונה
    @Ignore // מציין שלפונקציה זו אין צורך להיכלל בבסיס הנתונים של Room
    public Recipe(String recipeName, String category, String prepTime, String directions, String imageUri, boolean isFavorite, List<String> ingredients, int timerDuration) {
        this.recipeName = recipeName; // אתחול שם המתכון
        this.category = category; // אתחול קטגוריית המתכון
        this.prepTime = prepTime; // אתחול זמן ההכנה
        this.directions = directions; // אתחול הוראות ההכנה
        this.imageUri = (imageUri == null || imageUri.isEmpty()) ? "android.resource://com.example.recipebook/drawable/plate_icon" : imageUri; // אם אין URI לתמונה, משתמשים בתמונה ברירת מחדל
        this.isFavorite = isFavorite; // אתחול מצב המועדפים
        this.ingredients = ingredients; // אתחול רכיבי המתכון
        this.timerDuration = timerDuration; // אתחול זמן הטיימר
    }

    // פעולה בונה ריקה - זו שתהיה בשימוש על ידי Room
    public Recipe() {
        this.recipeName = ""; // אתחול שם המתכון לערך ברירת מחדל
        this.category = ""; // אתחול קטגוריית המתכון לערך ברירת מחדל
        this.prepTime = ""; // אתחול זמן ההכנה לערך ברירת מחדל
        this.directions = ""; // אתחול הוראות ההכנה לערך ברירת מחדל
        this.imageUri = "android.resource://com.example.recipebook/drawable/plate_icon"; // תמונה ברירת מחדל
        this.isFavorite = false; // אתחול מצב המועדפים לערך ברירת מחדל
        this.ingredients = null; // אתחול רכיבי המתכון לערך ברירת מחדל
        this.timerDuration = 0; // אתחול זמן הטיימר לערך ברירת מחדל
    }

    // גטרים וסטטרים לכל שדה ב-Class

    public int getRecipeId() {
        return recipeId; // מחזיר את ה-ID של המתכון
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId; // מגדיר את ה-ID של המתכון
    }

    public String getRecipeName() {
        return recipeName; // מחזיר את שם המתכון
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName; // מגדיר את שם המתכון
    }

    public String getCategory() {
        return category; // מחזיר את קטגוריית המתכון
    }

    public void setCategory(String category) {
        this.category = category; // מגדיר את קטגוריית המתכון
    }

    public String getPrepTime() {
        return prepTime; // מחזיר את זמן ההכנה של המתכון
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime; // מגדיר את זמן ההכנה של המתכון
    }

    public String getDirections() {
        return directions; // מחזיר את הוראות ההכנה
    }

    public void setDirections(String directions) {
        this.directions = directions; // מגדיר את הוראות ההכנה
    }

    public String getImageUri() {
        return imageUri; // מחזיר את ה-URI של התמונה של המתכון
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri; // מגדיר את ה-URI של התמונה של המתכון
    }

    public boolean isFavorite() {
        return isFavorite; // מחזיר אם המתכון במועדפים או לא
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite; // מגדיר אם המתכון במועדפים
    }

    public List<String> getIngredients() {
        return ingredients; // מחזיר את רשימת הרכיבים של המתכון
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients; // מגדיר את רשימת הרכיבים של המתכון
    }

    public int getTimerDuration() {
        return timerDuration; // מחזיר את זמן הטיימר של המתכון
    }

    public void setTimerDuration(int timerDuration) {
        this.timerDuration = timerDuration; // מגדיר את זמן הטיימר של המתכון
    }
}