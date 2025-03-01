package com.example.recipebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private ArrayList<Recipe> recipeList;
    private Context context;
    private DatabaseReference mDatabase;

    public RecipeAdapter(ArrayList<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
        this.mDatabase = FirebaseDatabase.getInstance().getReference("recipes"); // הפנייה לבסיס הנתונים
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
        if (recipe.getIsFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled); // כוכב מלא אם במועדפים
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty); // כוכב ריק אם לא במועדפים
        }

        // הוספת OnClickListener לכוכב
        holder.favoriteIcon.setOnClickListener(v -> {
            // אם המתכון במועדפים, הסר אותו מהמועדפים, אחרת הוסף אותו
            if (recipe.getIsFavorite()) {
                recipe.setIsFavorite(false); // הסר מהמועדפים
                holder.favoriteIcon.setImageResource(R.drawable.ic_star_empty); // עדכון אייקון לכוכב ריק
            } else {
                recipe.setIsFavorite(true); // הוסף למועדפים
                holder.favoriteIcon.setImageResource(R.drawable.ic_star_filled); // עדכון אייקון לכוכב מלא
            }

            // עדכון המידע ב-Firebase
            mDatabase.child(String.valueOf(recipe.getRecipeId())).child("isFavorite").setValue(recipe.getIsFavorite());
            recipeList.set(position, recipe); // עדכון הפריט ברשימה המקומית
            notifyItemChanged(position); // עדכון ה-RecyclerView להצגת השינוי
        });
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