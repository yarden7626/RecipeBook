package com.example.recipebook;

import android.content.Context;

import java.util.ArrayList;

public class DataManager {

    Context context;
    SQLiteManager sqLiteManager;

    public DataManager(Context context) {
        this.context = context;
        sqLiteManager = new SQLiteManager(context);
    }
    public Recipe getInfo(String index) //שולף מידע ממתכון לפי אינדקס המתכון שהתקבל
    {
        return sqLiteManager.getRecipeById(index);

    }

    public ArrayList<Recipe> getRecipeListInfo(String userID) //מחזירה את המידע של כל המתכונים ששייכים למשתמש לפי התז שהתקבל
    {
        return sqLiteManager.getListRecipesFromDB();

    }

    //מעדכן מתכון ואפשר לזהות את המתכון בזכות התכונה של המספר זהות של המתכון
    public void upDateRecipe(Recipe recipe)
    {

        sqLiteManager.updateRecipeInDB(recipe);

    }

    public void AddRecipe(Recipe recipe) //מוסיף לdatabase מתכון חדש
    {

        sqLiteManager.addRecipeToDB(recipe);

    }



}
