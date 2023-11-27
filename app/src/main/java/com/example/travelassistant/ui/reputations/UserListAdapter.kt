package com.example.travelassistant.ui.reputations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travelassistant.R
import com.example.travelassistant.models.user.User

class UserListAdapter(
    private val users: List<User>,
    private val onClick: (User) -> Unit
) :
    RecyclerView.Adapter<UserListAdapter.ExerciseEntryViewHolder>() {

    class ExerciseEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.nameListItemText)
        val textViewPoint: TextView = itemView.findViewById(R.id.pointListItemText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_list_item, parent, false)
        return ExerciseEntryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExerciseEntryViewHolder, position: Int) {
        val user = users[position]

        holder.textViewName.text = "${position+1}. ${user.displayName}"
        holder.textViewPoint.text = user.points.toString()
        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = users.size

}