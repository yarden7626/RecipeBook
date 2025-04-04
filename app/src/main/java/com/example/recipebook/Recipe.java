package com.example.recipebook;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import java.util.List;

@Entity(tableName = "recipes")
@TypeConverters({Converters.class})
public class Recipe {

    @PrimaryKey(autoGenerate = true)
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
    @Ignore
    public Recipe(String recipeName, String category, String prepTime, String directions, String imageUri, boolean isFavorite, List<String> ingredients, int timerDuration) {
        this.recipeName = recipeName;
        this.category = category;
        this.prepTime = prepTime;
        this.directions = directions;
        this.imageUri = imageUri;
        this.isFavorite = isFavorite;
        this.ingredients = ingredients;
        this.timerDuration = timerDuration;
    }

    // פעולה בונה ריקה - זו שתהיה בשימוש על ידי Room
    public Recipe() {
        this.recipeName = "";
        this.category = "";
        this.prepTime = "";
        this.directions = "";
        this.imageUri = "";
        this.isFavorite = false;
        this.ingredients = null;
        this.timerDuration = 0;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public int getTimerDuration() {
        return timerDuration;
    }

    public void setTimerDuration(int timerDuration) {
        this.timerDuration = timerDuration;
    }
}