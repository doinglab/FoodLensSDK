package com.doinglab.sdk.example.foodlenstestapplication.listview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.doinglab.sdk.example.foodlenstestapplication.R;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    public ListViewAdapter() {

    }

    public void clearItems()
    {
        listViewItemList.clear();
    }

    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.img_food) ;
        TextView tv_foodname = (TextView) convertView.findViewById(R.id.tv_foodname) ;
        TextView tv_food_nutrition_info = (TextView) convertView.findViewById(R.id.tv_food_nutrition_info) ;
        TextView tv_food_position = (TextView) convertView.findViewById(R.id.tv_food_position) ;


        ListViewItem listViewItem = listViewItemList.get(position);

        iconImageView.setImageDrawable(listViewItem.getIcon());
        tv_foodname.setText(listViewItem.getTitle());
        tv_food_nutrition_info.setText(listViewItem.getFoodNutrition());
        tv_food_position.setText(listViewItem.getFoodPosition());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(Drawable icon, String title, String nutritonInfo, String boxLoc) {
        ListViewItem item = new ListViewItem();

        if(icon != null)
            item.setIcon(icon);


        if(title != "")
            item.setTitle(title);
        else
            item.setTitle("");

        if(nutritonInfo != "")
            item.setFoodNutrition(nutritonInfo);
        else
            item.setFoodNutrition("");

        if(title != "")
            item.setFoodPosition(boxLoc);
        else
            item.setFoodPosition("");

        listViewItemList.add(item);
    }
}
