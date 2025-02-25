package com.example.recipebook;

import java.util.List;

public class Recipe {
    private String recipeId; //המספר זיהוי של המתכון
    private String recipeName; //שם המתכון
    private String category; //קטגוריה של המתכון
    private int prepTime; //זמן הכנה בדקות
    private String directions; //הוראות הכנה
    private String imageUri; //תמונה של המתכון
    private int isFavorite; // האם המתכון במועדפים
    private List<Ingredient> ingredients; // רשימה של רכיבים במתכון

    // פעולה בונה
    public Recipe(String recipeId, String recipeName, String category, int prepTime, String instructions, String image, int isFavorite, List<Ingredient> ingredients) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.category = category;
        this.prepTime = prepTime;
        this.directions = instructions;
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

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
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

    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }


    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
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