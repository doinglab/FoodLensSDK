package com.doinglab.sdk.example.foodlenstestapplication.listview

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.doinglab.sdk.example.foodlenstestapplication.R

class ListViewAdapter : BaseAdapter() {
    val listViewItemList = ArrayList<ListViewItem>()

    fun clearItems() {
        listViewItemList.clear()
    }

    override fun getCount(): Int {
        return listViewItemList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val pos = position
        val context = parent.context

        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.list_item, parent, false)
        }

        val iconImageView = convertView!!.findViewById<View>(R.id.img_food) as ImageView
        val tv_foodname = convertView!!.findViewById<View>(R.id.tv_foodname) as TextView
        val tv_food_nutrition_info =
            convertView!!.findViewById<View>(R.id.tv_food_nutrition_info) as TextView
        val tv_food_position = convertView!!.findViewById<View>(R.id.tv_food_position) as TextView
        val listViewItem = listViewItemList[position]

        iconImageView.setImageDrawable(listViewItem.iconDrawable)
        tv_foodname.text = listViewItem.titleStr
        tv_food_nutrition_info.text = listViewItem.foodNutrition
        tv_food_position.text = listViewItem.foodPosition

        return convertView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any? {
        return listViewItemList[position]
    }

    fun addItem(icon: Drawable?, title: String, nutritonInfo: String, boxLoc: String?) {
        val item = ListViewItem()
        if (icon != null)
            item.iconDrawable = icon

        if (title !== "")
            item.titleStr = title
        else
            item.titleStr = ""

        if (nutritonInfo !== "")
            item.foodNutrition = nutritonInfo
        else
            item.foodNutrition = ""

        if (title !== "")
            item.foodPosition = boxLoc
        else
            item.foodPosition = ""

        listViewItemList.add(item)
    }
}