package com.example.recipebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private ArrayList<Recipe> recipeList;
    private Context context;

    public RecipeAdapter(ArrayList<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        RecipeViewHolder rvh = new RecipeViewHolder(view);
        return  rvh;
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
        ImageButton favoriteIcon;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            // מחברים כל view עם ה-id ב-xml
            recipeName = itemView.findViewById(R.id.RecipeName);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
    }