package com.example.recipebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

// מחלקה שאחראית להציג את המתכונים ברשימה (RecyclerView)
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes; // רשימת המתכונים שתוצג ברשימה
    private final Context context; // הקשר (Context) לפעולה מול משאבים של האפליקציה
    private final OnRecipeClickListener listener; // מאזין ללחיצה על מתכון
    private final int currentUserId; // מזהה המשתמש הנוכחי (בשביל מועדפים)
    private final AppDatabase database; // גישה לבסיס הנתונים

    // ממשק שמאפשר לטפל בלחיצה על פריט ברשימת המתכונים
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe); // פעולה שמתבצעת כשמשתמש לוחץ על מתכון
    }

    // פעולה בונה של האדפטר
    public RecipeAdapter(Context context, OnRecipeClickListener listener, int currentUserId) {
        this.context = context; // שמירת הקונטקסט
        this.listener = listener; // שמירת המאזין
        this.currentUserId = currentUserId; // שמירת מזהה המשתמש
        this.recipes = new ArrayList<>(); // אתחול רשימת המתכונים כרשימה ריקה
        this.database = AppDatabase.getInstance(context); // קבלת מופע של בסיס הנתונים
    }

    // עדכון רשימת המתכונים באדפטר
    @SuppressLint("NotifyDataSetChanged")
    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged(); // מודיע ל-RecyclerView שהנתונים השתנו וצריך לרנדר מחדש
    }

    // מחזיר את רשימת המתכונים
    public List<Recipe> getRecipes() {
        return recipes;
    }

    // יוצר ViewHolder (תצוגה של פריט אחד ברשימה)
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false); // מנפח את התצוגה של הפריט
        return new RecipeViewHolder(view); // מחזיר את התצוגה כמחזיק ViewHolder
    }

    // מקשר בין מתכון מסוים לתצוגה שלו
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position); // מקבל את המתכון לפי מיקום
        holder.bind(recipe); // קורא לפונקציית bind להצגת הפרטים בתצוגה
    }

    // מחזיר את מספר הפריטים ברשימה
    @Override
    public int getItemCount() {
        return recipes.size();
    }

    // מחלקה פנימית שמייצגת כל שורה ברשימה
    class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView recipeName; // תצוגת שם המתכון
        private final ImageButton favoriteButton; // כפתור מועדפים

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.RecipeName); // קישור לרכיב תצוגה של שם המתכון
            favoriteButton = itemView.findViewById(R.id.favoriteIcon); // קישור לכפתור המועדפים

            // מאזין ללחיצה על כל הפריט ברשימה
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = recipes.get(position); // מקבל את המתכון שנלחץ עליו
                    listener.onRecipeClick(recipe); // מפעיל את המאזין
                }
            });

            // מאזין ללחיצה על כפתור המועדפים
            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = recipes.get(position); // מקבל את המתכון שנלחץ עליו
                    toggleFavorite(recipe); // משנה את מצב המועדפים
                }
            });
        }

        // מציג את פרטי המתכון בתצוגה
        public void bind(Recipe recipe) {
            recipeName.setText(recipe.getRecipeName()); // מציג את שם המתכון
            updateFavoriteIcon(recipe.isFavorite()); // מציג את האייקון המתאים לפי אם הוא מועדף
        }

        // מעדכן את האייקון של המועדפים לפי האם הוא מועדף או לא
        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_star_filled); // כוכב מלא אם מועדף
            } else {
                favoriteButton.setImageResource(R.drawable.ic_star_empty); // כוכב ריק אם לא
            }
        }

        // משנה את מצב המועדפים של המתכון
        private void toggleFavorite(Recipe recipe) {
            new Thread(() -> { // פעולה ברקע (ולא על ה-UI Thread)
                boolean isFavorite = recipe.isFavorite();
                if (isFavorite) {
                    // הסרת המתכון מהמועדפים בבסיס הנתונים
                    database.favoriteRecipeDao().deleteFavorite(currentUserId, recipe.getRecipeId());
                } else {
                    // הוספת המתכון למועדפים בבסיס הנתונים
                    FavoriteRecipe favorite = new FavoriteRecipe(currentUserId, recipe.getRecipeId());
                    database.favoriteRecipeDao().insert(favorite);
                }

                // עדכון מצב המועדפים של המתכון בזיכרון
                recipe.setFavorite(!isFavorite);

                // עדכון התצוגה דרך ה-UI Thread
                runOnUiThread(() -> {
                    updateFavoriteIcon(!isFavorite); // שינוי האייקון
                    notifyItemChanged(getAdapterPosition()); // רענון השורה ברשימה
                });
            }).start();
        }

        // פעולה לעדכון על גבי ה-UI Thread (נחוץ כי אי אפשר לעדכן תצוגה מתוך Thread אחר)
        private void runOnUiThread(Runnable action) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).runOnUiThread(action);
            }
        }
    }
}