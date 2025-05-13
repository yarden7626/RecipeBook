package com.example.recipebook;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "favorite_recipes",  // שם הטבלה במסד הנתונים
        foreignKeys = {  // הגדרת המפתחות הזרים (FK) שיש בטבלה
                @ForeignKey(
                        entity = User.class,  // קשר עם טבלת המשתמשים
                        parentColumns = "id",  // העמודה בטבלה הראשית (User) שמפנה אליה
                        childColumns = "userId",  // העמודה בטבלה הנוכחית (FavoriteRecipe) שמפנה
                        onDelete = ForeignKey.CASCADE  // אם המשתמש נמחק, כל המתכונים המועדפים שלו נמחקים גם
                ),
                @ForeignKey(
                        entity = Recipe.class,  // קשר עם טבלת המתכונים
                        parentColumns = "recipeId",  // העמודה בטבלת המתכונים שמפנים אליה
                        childColumns = "recipeId",  // העמודה בטבלה הנוכחית שמפנה
                        onDelete = ForeignKey.CASCADE  // אם המתכון נמחק, הוא יימחק גם מהמועדפים
                )
        },
        indices = {  // הגדרת אינדקסים על העמודות שנעשה בהם שימוש בהשוואות
                @Index("userId"),  // אינדקס על עמודת userId
                @Index("recipeId")  // אינדקס על עמודת recipeId
        }
)
public class FavoriteRecipe {
    @PrimaryKey(autoGenerate = true)  // עמודת מפתח ראשי שתתמוך בהפעלה אוטומטית של ה-ID
    private int id;  // מזהה ייחודי של המתכון המועדף
    private int userId;  // מזהה המשתמש ששם המתכון במועדפים
    private int recipeId;  // מזהה המתכון שנשמר במועדפים

    // קונסטרוקטור עבור יצירת אובייקט FavoriteRecipe עם מזהה משתמש ומזהה מתכון
    public FavoriteRecipe(int userId, int recipeId) {
        this.userId = userId;  // אתחול מזהה המשתמש
        this.recipeId = recipeId;  // אתחול מזהה המתכון
    }

    // פונקציה לקבלת מזהה המתכון המועדף
    public int getId() {
        return id;
    }

    // פונקציה להגדיר את מזהה המתכון המועדף
    public void setId(int id) {
        this.id = id;
    }

    // פונקציה לקבלת מזהה המשתמש
    public int getUserId() {
        return userId;
    }

    // פונקציה להגדיר את מזהה המשתמש
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // פונקציה לקבלת מזהה המתכון המועדף
    public int getRecipeId() {
        return recipeId;
    }

    // פונקציה להגדיר את מזהה המתכון המועדף
    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
}