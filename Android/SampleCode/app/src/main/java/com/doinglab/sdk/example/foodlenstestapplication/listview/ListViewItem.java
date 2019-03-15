package com.doinglab.sdk.example.foodlenstestapplication.listview;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    private Drawable iconDrawable ;
    private String titleStr;
    private String foodPosition;
    private String foodNutrition;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }


    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }

    public void setFoodNutrition(String foodNutrition) {
        this.foodNutrition = foodNutrition;
    }

    public void setFoodPosition(String foodPosition) {
        this.foodPosition = foodPosition;
    }

    public String getFoodNutrition() {
        return foodNutrition;
    }

    public String getFoodPosition() {
        return foodPosition;
    }
}
