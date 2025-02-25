package com.example.recipebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManager;

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Recipes";
    private static final String COUNTER = "counter";

    private static final String ID_FIELD = "id";
    private static final String RECIPENAME_FIELD = "recipeName";
    private static final String PREPARATIONTIME_FIELD = "preparationTime";
    private static final String INGREDIENTS_FIELD = "Ingredients";
    private static final String DIRECTIONS_FIELD = "Directions";
    private static final String CATEGORY_FIELD = "category";
    private static final String ISFAVORITE_FIELD = "isFavorite";
    private static final String IMAGEURI_FIELD = "ImageUri";

    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteManager instanceOfDatabase(Context context) {
        if (sqLiteManager == null) {
            sqLiteManager = new SQLiteManager(context);
        }
        return sqLiteManager;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append(" (")
                .append(COUNTER)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(ID_FIELD)
                .append(" INT, ")
                .append(RECIPENAME_FIELD)
                .append(" TEXT, ")
                .append(PREPARATIONTIME_FIELD)
                .append(" TEXT, ")
                .append(INGREDIENTS_FIELD)
                .append(" TEXT, ")
                .append(DIRECTIONS_FIELD)
                .append(" TEXT, ")
                .append(CATEGORY_FIELD)
                .append(" TEXT, ")
                .append(IMAGEURI_FIELD)
                .append(" TEXT, ")
                .append(ISFAVORITE_FIELD)
                .append(" INTEGER)")
                .append(")");
        sqLiteDatabase.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addRecipeToDB(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_FIELD, recipe.getRecipeId());
        values.put(RECIPENAME_FIELD, recipe.getRecipeName());
        values.put(PREPARATIONTIME_FIELD, recipe.getPrepTime());
        values.put(INGREDIENTS_FIELD, recipe.getIngredients().toString());
        values.put(DIRECTIONS_FIELD, recipe.getDirections());
        values.put(CATEGORY_FIELD, recipe.getCategory());
        values.put(ISFAVORITE_FIELD, recipe.getIsFavorite());
        values.put(IMAGEURI_FIELD, recipe.getImageUri());

        db.insert(TABLE_NAME, null, values);
    }

    public void updateRecipeToDB(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_FIELD, recipe.getRecipeId());
        values.put(RECIPENAME_FIELD, recipe.getRecipeName());
        values.put(PREPARATIONTIME_FIELD, recipe.getPrepTime());
        values.put(INGREDIENTS_FIELD, recipe.getIngredients().toString());
        values.put(DIRECTIONS_FIELD, recipe.getDirections());
        values.put(CATEGORY_FIELD, recipe.getCategory());
        values.put(ISFAVORITE_FIELD, recipe.getIsFavorite());
        values.put(IMAGEURI_FIELD, recipe.getImageUri());


        db.update(TABLE_NAME, values, "ID=?", new String[]{String.valueOf(recipe.getRecipeId())});
    }

    public void populateRecipesFromDB(Recipe recipe) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {

            while (result.moveToNext()) {
                //The table we setup has 5 columns and this is how we can access each item in the row
                int id = result.getInt(1);
                String RecipeName = result.getString(2);
                String preparationTime = result.getString(3);
                String Ingredients = result.getString(4);
                String Directions = result.getString(5);
                String Category = result.getString(6);
                int IsFavorite = result.getInt(7);

                public Recipe(String recipeId, String recipeName, String category, int prepTime, String instructions, String image, int isFavorite, List<Ingredient> ingredients) {


                    Recipe recipe = new Recipe(id,RecipeName,preparationTime,Ingredients,Directions,Category,IsFavorite);
                Recipe.add(recipe);
            }
        } catch (SQLiteException e) {
            // handle exception, e.g. log error message
            Log.e("SQLiteManager", "Error executing query", e);
        } catch (CursorIndexOutOfBoundsException e) {
            // handle exception, e.g. log error message
            Log.e("SQLiteManager", "There was a problem reading one of the fields", e);
        }
    }
}
