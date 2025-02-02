package com.example.recipebook;

import java.util.List;

public class Recipe
{
    private int recipeId; //המספר זיהוי של המתכון
    private String recipeName; //שם המתכון
    private String category; //קטגוריה של המתכון
    private int prepTime; //זמן הכנה בדקות
    private String instructions; //הוראות הכנה
    private String image; //תמונה של המתכון
    private int isFavorite; // האם המתכון במועדפים (0 או 1)
    private List<Ingredient> ingredients; // רשימה של רכיבים במתכון

    // פעולה בונה
    public Recipe(int recipeId, String recipeName, String category, int prepTime, String instructions, String image, int isFavorite, List<Ingredient> ingredients) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.category = category;
        this.prepTime = prepTime;
        this.instructions = instructions;
        this.image = image;
        this.isFavorite = isFavorite;
        this.ingredients = ingredients;
    }

    //  פעולה בונה היוצרת מתכון ריק
    public Recipe() {}

    // פעולות מאחזרות ומשנות לכל תכונה

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

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    // Getter ו-Setter לרשימת רכיבים
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
                ", instructions='" + instructions + '\'' +
                ", image='" + image + '\'' +
                ", isFavorite=" + isFavorite +
                ", ingredients=" + ingredients +
                '}';
    }
}