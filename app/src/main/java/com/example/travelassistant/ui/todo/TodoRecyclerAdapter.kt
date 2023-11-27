package com.example.travelassistant.ui.todo

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelassistant.R
import com.example.travelassistant.models.user.TodoItem
import com.example.travelassistant.viewModels.UserViewModel


class TodoRecyclerAdapter(items : List<TodoItem>, userModel : UserViewModel, userId : String) : RecyclerView.Adapter<TodoRecyclerAdapter.ViewHolder>() {
    private var todoItemList = items
    private var myModel = userModel
    private var myUserId = userId

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
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.finished.isChecked = todoItemList[position].completed
        holder.item.text = todoItemList[position].task

        if (todoItemList[position].completed) {
            holder.item.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.delButton.visibility = View.VISIBLE
        }
        else {
            holder.item.paintFlags = Paint.ANTI_ALIAS_FLAG
            holder.delButton.visibility = View.INVISIBLE
        }

        //Delete todoItems
        holder.finished.setOnCheckedChangeListener { _, isChecked : Boolean ->

            if (isChecked) {
                holder.item.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                myModel.checkTodoItem(myUserId,todoItemList[position].id)
                holder.delButton.visibility = View.VISIBLE
            }
            else {
                holder.item.paintFlags = Paint.ANTI_ALIAS_FLAG
                myModel.unCheckTodoItem(myUserId,todoItemList[position].id)
                holder.delButton.visibility = View.INVISIBLE
            }
        }

        holder.delButton.setOnClickListener {
            //todoFinishList.removeAt(holder.adapterPosition)
            myModel.deleteTodoItem(myUserId,todoItemList[position].id)
            //todoItemList.filterNot { it.id == todoItemList[position].id}
            notifyItemRemoved(holder.adapterPosition)
        }
    }
    override fun getItemCount(): Int {
        return todoItemList.size
    }

}