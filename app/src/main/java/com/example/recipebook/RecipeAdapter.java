package com.example.recipebook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // טוען את ה-layout של הפריט
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        // מקבל את המתכון לפי המיקום ברשימה
        Recipe recipe = recipeList.get(position);

        // מציג את שם המתכון
        holder.recipeName.setText(recipe.getRecipeName());

        // מציג את הכוכב לפי אם המתכון במועדפים
        if (recipe.getIsFavorite() == 1) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled); // כוכב מלא אם במועדפים
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty); // כוכב ריק אם לא במועדפים
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size(); // מחזיר את מספר המתכונים ברשימה
    }

    // מחלקת ViewHolder, אחראית לשמור את כל ה-views שמופיעים בכל פריט
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView recipeName;
        ImageView favoriteIcon;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            // מחברים כל view עם ה-id ב-xml
            recipeName = itemView.findViewById(R.id.editRecipeName);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
    }