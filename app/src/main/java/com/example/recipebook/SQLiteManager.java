package com.example.recipebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManager;

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Recipes";

    private static final String ID_FIELD = "id";
    private static final String RECIPENAME_FIELD = "recipeName";
    private static final String PREPARATIONTIME_FIELD = "preparationTime";
    private static final String INGREDIENTS_FIELD = "Ingredients";
    private static final String DIRECTIONS_FIELD = "Directions";
    private static final String CATEGORY_FIELD = "category";
    private static final String ISFAVORITE_FIELD = "isFavorite";
    private static final String IMAGEURI_FIELD = "ImageUri";

    private static final String USERID_FIELD = "userID";


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
                .append(ID_FIELD)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(USERID_FIELD)
                .append(" INTEGER, ")
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
                .append(" INTEGER) ")
                .append(")");
        sqLiteDatabase.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }



    public void updateRecipeInDB(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RECIPENAME_FIELD, recipe.getRecipeName());
        values.put(PREPARATIONTIME_FIELD, recipe.getPrepTime());
        values.put(INGREDIENTS_FIELD, recipe.getIngredients());
        values.put(DIRECTIONS_FIELD, recipe.getDirections());
        values.put(CATEGORY_FIELD, recipe.getCategory());
        values.put(ISFAVORITE_FIELD, recipe.getIsFavorite());
        values.put(IMAGEURI_FIELD, recipe.getImageUri());
        values.put(USERID_FIELD, recipe.getUserId());

        db.update(TABLE_NAME, values, ID_FIELD + " = ?", new String[]{recipe.getRecipeId()});
        Log.d("DB_SQLITE", "Updated recipe with ID: " + recipe.getRecipeId());
    }

    public void addRecipeToDB(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RECIPENAME_FIELD, recipe.getRecipeName());
        values.put(PREPARATIONTIME_FIELD, recipe.getPrepTime());
        values.put(INGREDIENTS_FIELD, recipe.getIngredients());
        values.put(DIRECTIONS_FIELD, recipe.getDirections());
        values.put(CATEGORY_FIELD, recipe.getCategory());
        values.put(ISFAVORITE_FIELD, recipe.getIsFavorite());
        values.put(IMAGEURI_FIELD, recipe.getImageUri());
        values.put(USERID_FIELD, recipe.getUserId());


        db.insert(TABLE_NAME, null, values);
        Log.d("DB_SQLITE","db.insert("+TABLE_NAME+", null, "+values+");" );
    }

    public ArrayList<Recipe> getListRecipesFromDB() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Recipe> recipeList = new ArrayList<>();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            while (cursor.moveToNext()) {

                String id = cursor.getString(cursor.getColumnIndexOrThrow(ID_FIELD));
                String recipeName = cursor.getString(cursor.getColumnIndexOrThrow(RECIPENAME_FIELD));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_FIELD));
                String prepTime = cursor.getString(cursor.getColumnIndexOrThrow(PREPARATIONTIME_FIELD)); // prepTime as String
                String directions = cursor.getString(cursor.getColumnIndexOrThrow(DIRECTIONS_FIELD));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(IMAGEURI_FIELD));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(ISFAVORITE_FIELD)) == 1;
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(INGREDIENTS_FIELD));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID_FIELD));


                Recipe recipe = new Recipe(id, recipeName, category, prepTime, directions, imageUri, isFavorite, ingredients, userId);
                recipeList.add(recipe);
            }
        } catch (Exception e) {
            Log.e("SQLiteManager", "Error while reading database", e);
        }

        return recipeList;
    }

    public Recipe getRecipeById(String recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = ?", new String[]{recipeId})) {
            if (cursor != null && cursor.moveToFirst()) {

                String recipeName = cursor.getString(cursor.getColumnIndexOrThrow(RECIPENAME_FIELD));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_FIELD));
                String prepTime = cursor.getString(cursor.getColumnIndexOrThrow(PREPARATIONTIME_FIELD));
                String directions = cursor.getString(cursor.getColumnIndexOrThrow(DIRECTIONS_FIELD));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(IMAGEURI_FIELD));
                boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(ISFAVORITE_FIELD)) == 1;
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(INGREDIENTS_FIELD));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID_FIELD));


                return new Recipe(recipeId, recipeName, category, prepTime, directions, imageUri, isFavorite, ingredients, userId);
            }
        } catch (Exception e) {
            Log.e("SQLiteManager", "Error while reading recipe by ID", e);
        }

        return null;
    }
}