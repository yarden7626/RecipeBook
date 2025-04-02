package com.example.recipebook;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipeList;
    private List<FavoriteRecipe> favoriteRecipes;
    private Context context;
    private OnRecipeClickListener listener;
    private String currentUserId;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, OnRecipeClickListener listener, String currentUserId) {
        this.context = context;
        this.listener = listener;
        this.currentUserId = currentUserId;
        this.recipeList = new ArrayList<>();
        this.favoriteRecipes = new ArrayList<>();
    }

    // פונקציה לעדכון רשימת המתכונים
    public void setRecipes(List<Recipe> recipes) {
        this.recipeList = recipes;
        notifyDataSetChanged();
    }

    public void setFavoriteRecipes(List<FavoriteRecipe> favorites) {
        this.favoriteRecipes = favorites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.recipeName.setText(recipe.getRecipeName());
        holder.recipeCategory.setText(recipe.getCategory());
        holder.recipeTime.setText(recipe.getPrepTime());

        // בדיקה האם המתכון נמצא במועדפים של המשתמש הנוכחי
        boolean isFavorite = false;
        for (FavoriteRecipe favorite : favoriteRecipes) {
            if (favorite.getRecipeId() == recipe.getRecipeId() && 
                favorite.getUserId().equals(currentUserId)) {
                isFavorite = true;
                break;
            }
        }

        // עדכון אייקון המועדפים
        holder.favoriteIcon.setImageResource(isFavorite ? 
            R.drawable.ic_favorite_active : R.drawable.ic_favorite_inactive);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName, recipeCategory, recipeTime;
        ImageView favoriteIcon;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
}