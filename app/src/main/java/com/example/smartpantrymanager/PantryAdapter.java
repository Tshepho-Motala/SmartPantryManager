package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Ingredient ingredient);

        void onEditClick(Ingredient selectedIngredient);

        void onDeleteClick(Ingredient selectedIngredient);
    }

    private List<Ingredient> ingredientList;
    private OnItemClickListener listener;

    public PantryAdapter(List<Ingredient> ingredientList, OnItemClickListener listener) {
        this.ingredientList = ingredientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.bind(ingredient, listener);
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, quantityTextView;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvIngredientName);
            quantityTextView = itemView.findViewById(R.id.tvIngredientQuantity);
        }

        public void bind(final Ingredient ingredient, final OnItemClickListener listener) {
            nameTextView.setText(ingredient.getName());
            quantityTextView.setText(ingredient.getQuantity() + " " + ingredient.getUnit());
            itemView.setOnClickListener(v -> listener.onItemClick(ingredient));

            itemView.findViewById(R.id.btnEdit).setOnClickListener(v -> listener.onEditClick(ingredient));
            itemView.findViewById(R.id.btnDelete).setOnClickListener(v -> listener.onDeleteClick(ingredient));
        }
    }
}
