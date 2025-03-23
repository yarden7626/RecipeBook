package com.example.recipebook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes; // רשימת המתכונים
    private Context context; // הקונטקסט של האפליקציה

    // בנאי
    public RecipeAdapter(Context context) {
        this.context = context;
        this.recipes = new ArrayList<>();
    }

    // פונקציה לעדכון רשימת המתכונים
    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת תצוגה חדשה לכל פריט ברשימה
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // מילוי הנתונים בתצוגה
        Recipe recipe = recipes.get(position);
        holder.recipeName.setText(recipe.getName());

        // הגדרת כוכב המועדפים
        if (recipe.isFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled); //כוכב מלא אם במועדפים
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty); //כוכב ריק אם לא במועדפים
        }

        // הגדרת מאזין לחיצה על הכוכב
        holder.favoriteIcon.setOnClickListener(v -> {
            recipe.setFavorite(!recipe.isFavorite());
            notifyItemChanged(position);
            // שמירת השינוי בבסיס הנתונים
            new Thread(() -> {
                AppDatabase.getInstance(context).recipeDao().update(recipe);
            }).start();
        });

        // הגדרת מאזין לחיצה על הפריט
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    // מחלקה פנימית להחזקת התצוגה של כל פריט ברשימה
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName; // שם המתכון
        ImageButton favoriteIcon; // כפתור כוכב מועדפים

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.RecipeName);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
}