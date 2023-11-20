package com.example.travelassistant.ui.gallery

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelassistant.R


class GalleryRecyclerAdapter(items : ArrayList<String>, itemsFinish : ArrayList<Boolean>) : RecyclerView.Adapter<GalleryRecyclerAdapter.ViewHolder>() {
    private var todoItemList = items
    private var todoFinishList = itemsFinish
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item : TextView = view.findViewById(R.id.textview_todo)
        var finished : CheckBox = view.findViewById(R.id.checkbox_todo)
        val delButton : Button = view.findViewById(R.id.delete_button)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent,false)

        return ViewHolder(view)
    }

    //Adds strikethrough to text if checked
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.text = todoItemList[position]
        println("debug: $position")
        holder.finished.isChecked = todoFinishList[position]
        holder.finished.setOnCheckedChangeListener { _, isChecked : Boolean ->
            if (isChecked) {
                holder.item.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                holder.delButton.visibility = View.VISIBLE
            }
            else {
                holder.item.paintFlags = Paint.ANTI_ALIAS_FLAG
                holder.delButton.visibility = View.INVISIBLE
            }
        }
        holder.delButton.setOnClickListener {
            todoItemList.removeAt(holder.adapterPosition)
            todoFinishList.removeAt(holder.adapterPosition)
            notifyDataSetChanged()
        }
    }
    override fun getItemCount(): Int {
        return todoItemList.size
    }

}