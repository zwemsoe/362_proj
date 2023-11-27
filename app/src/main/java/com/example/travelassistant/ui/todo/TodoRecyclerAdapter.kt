package com.example.travelassistant.ui.todo

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


class TodoRecyclerAdapter(userModel : UserViewModel, userId : String) : RecyclerView.Adapter<TodoRecyclerAdapter.ViewHolder>() {
    private var todoItemList : ArrayList<TodoItem> = arrayListOf()
    private var myModel = userModel
    private var myUserId = userId

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item : TextView = view.findViewById(R.id.textview_todo)
        val delButton : Button = view.findViewById(R.id.delete_button)

        var finished : CheckBox = view.findViewById<CheckBox?>(R.id.checkbox_todo).apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    item.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    todoItemList[adapterPosition].completed = true
                    myModel.checkTodoItem(myUserId,todoItemList[adapterPosition].id)
                    delButton.visibility = View.VISIBLE
                }
                else {
                    item.paintFlags = Paint.ANTI_ALIAS_FLAG
                    todoItemList[adapterPosition].completed = false
                    myModel.unCheckTodoItem(myUserId,todoItemList[adapterPosition].id)
                    delButton.visibility = View.INVISIBLE
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent,false)
        var holder = ViewHolder(view)

        holder.delButton.setOnClickListener {
            myModel.deleteTodoItem(myUserId,todoItemList[holder.adapterPosition].id)
        }

        return holder
    }

    //Adds strikethrough to text if checked
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
    }
    override fun getItemCount(): Int {
        return todoItemList.size
    }

    fun setTodoList(newList : List<TodoItem>) {
        todoItemList = newList as ArrayList<TodoItem>
    }

}