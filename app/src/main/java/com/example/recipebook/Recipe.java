package com.example.recipebook;

import java.util.List;

public class Recipe {
    private String recipeId; //המספר זיהוי של המתכון
    private String recipeName; //שם המתכון
    private String category; //קטגוריה של המתכון
    private String prepTime; //זמן הכנה בדקות
    private String directions; //הוראות הכנה
    private String imageUri; //תמונה של המתכון
    private boolean isFavorite; //האם המתכון במועדפים
    private String ingredients; // רשימה של רכיבים במתכון

    // פעולה בונה
    public Recipe(String recipeId, String recipeName, String category, String prepTime, String directions, String image, boolean isFavorite, String ingredients) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.category = category;
        this.prepTime = prepTime;
        this.directions = directions;
        this.imageUri = image;
        this.isFavorite = isFavorite;
        this.ingredients = ingredients;
    }

    //  פעולה בונה היוצרת מתכון ריק
    public Recipe() {}

    public Recipe(String recipeName) {
        this.recipeName = recipeName;
    }

    // פעולות מאחזרות ומשנות לכל תכונה
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
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

    public boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    // הדפסת המתכון עם רכיבים
    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId=" + recipeId +
                ", recipeName='" + recipeName + '\'' +
                ", category='" + category + '\'' +
                ", prepTime=" + prepTime +
                ", instructions='" + directions + '\'' +
                ", image='" + imageUri + '\'' +
                ", isFavorite=" + isFavorite +
                ", ingredients=" + ingredients +
                '}';
    }
}