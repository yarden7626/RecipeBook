package com.example.recipebook;

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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private final Context context;
    private final OnRecipeClickListener listener;
    private final int currentUserId;
    private final AppDatabase database;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, OnRecipeClickListener listener, int currentUserId) {
        this.context = context;
        this.listener = listener;
        this.currentUserId = currentUserId;
        this.recipes = new ArrayList<>();
        this.database = AppDatabase.getInstance(context);
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeName;
        private final ImageButton favoriteButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.RecipeName);
            favoriteButton = itemView.findViewById(R.id.favoriteIcon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = recipes.get(position);
                    listener.onRecipeClick(recipe);
                }
            });

            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = recipes.get(position);
                    toggleFavorite(recipe);
                }
            });
        }

        public void bind(Recipe recipe) {
            recipeName.setText(recipe.getRecipeName());
            updateFavoriteIcon(recipe.isFavorite());
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_star_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_star_empty);
            }
        }

        private void toggleFavorite(Recipe recipe) {
            new Thread(() -> {
                boolean isFavorite = recipe.isFavorite();
                if (isFavorite) {
                    // הסרת המתכון מהמועדפים
                    database.favoriteRecipeDao().deleteFavorite(currentUserId, recipe.getRecipeId());
                } else {
                    // הוספת המתכון למועדפים
                    FavoriteRecipe favorite = new FavoriteRecipe(currentUserId, recipe.getRecipeId());
                    database.favoriteRecipeDao().insert(favorite);
                }
                
                // עדכון סטטוס המועדפים במתכון
                recipe.setFavorite(!isFavorite);
                
                // עדכון הממשק
                runOnUiThread(() -> {
                    updateFavoriteIcon(!isFavorite);
                    notifyItemChanged(getAdapterPosition());
                });
            }).start();
        }

        private void runOnUiThread(Runnable action) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).runOnUiThread(action);
            }
        }
    }
}