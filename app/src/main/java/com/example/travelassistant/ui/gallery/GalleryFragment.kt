package com.example.travelassistant.ui.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.travelassistant.R
import com.example.travelassistant.databinding.FragmentGalleryBinding
import com.google.android.material.internal.ViewUtils.hideKeyboard

class GalleryFragment : Fragment() {
    private lateinit var view: View
    private lateinit var inflater: LayoutInflater
    private lateinit var viewModel: GalleryViewModel

    private lateinit var todoListView : RecyclerView
    //temporary array store
    private var tempItems : ArrayList<String> = arrayListOf()
    private var tempItemsFinished : ArrayList<Boolean> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        this.inflater = inflater
        view = inflater.inflate(R.layout.fragment_gallery, container, false)
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSuggestions()

        todoListView = view.findViewById(R.id.todo_created_container)
        setupTodoList()
        setupButtons()
    }

    private fun setupSuggestions() {
        val container = view.findViewById<LinearLayout>(R.id.todo_suggestions_container)
        val loadingOrFail = TextView(requireContext())
        loadingOrFail.text = "Loading..."
        container.addView(loadingOrFail)

        viewModel.suggestedTodoList.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isEmpty()) {
                loadingOrFail.text = "Sorry, cannot give any suggestions at the moment"
                return@observe
            }
            container.removeAllViews()
            suggestions.forEach { suggestion ->
                val todoView = inflater.inflate(R.layout.todo_item, container, false)
                todoView.findViewById<TextView>(R.id.textview_todo).text = suggestion
                container.addView(todoView)
            }
        }
        viewModel.generateSuggestions()
    }

    @SuppressLint("RestrictedApi", "NotifyDataSetChanged")
    private fun setupButtons() {
        val addButton : TextView = view.findViewById(R.id.todo_add_button)
        addButton.setOnClickListener{
            var addItem : TextView = view.findViewById<TextView?>(R.id.todo_add_text)
            tempItems.add(addItem.text.toString())
            tempItemsFinished.add(false)
            addItem.text = ""
            addItem.clearFocus()
            todoListView.adapter?.notifyDataSetChanged()
            hideKeyboard(view)
            Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
        }
}

//Temporary load filler text, change when database implemented
    private fun setupTodoList() {
        tempItems.add("one")
        tempItems.add("two")
        tempItems.add("three")
        tempItems.add("four")

        tempItemsFinished.add(false)
        tempItemsFinished.add(false)
        tempItemsFinished.add(false)
        tempItemsFinished.add(false)

        todoListView.adapter = GalleryRecyclerAdapter(tempItems,tempItemsFinished)
    }
}