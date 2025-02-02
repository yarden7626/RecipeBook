package com.example.recipebook;

public class Ingredient
{
    private int ingredientId;      // מספר זיהוי של הרכיב
    private int recipeId;          // מספר זיהוי של המתכון אליו שייך הרכיב
    private String ingredientName; // שם הרכיב
    private String quantity;       // כמות של הרכיב
    private String unit;           // יחידת מידה

    // פעולה בונה
    public Ingredient(int ingredientId, int recipeId, String ingredientName, String quantity, String unit) {
        this.ingredientId = ingredientId;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    // פעולות מאחזרות ומשנות
    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // הדפסת רכיב
    @Override
    public String toString() {
        return "Ingredient{" +
                "ingredientId=" + ingredientId +
                ", recipeId=" + recipeId +
                ", ingredientName='" + ingredientName + '\'' +
                ", quantity='" + quantity + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}

