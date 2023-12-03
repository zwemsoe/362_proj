package com.example.travelassistant.ui.todo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.travelassistant.R
import com.example.travelassistant.models.user.TodoItem
import com.example.travelassistant.models.user.User
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID


class TodoFragment : Fragment() {
    private lateinit var view: View
    private lateinit var inflater: LayoutInflater
    private lateinit var viewModel: TodoViewModel

    private lateinit var todoListView : RecyclerView
    private lateinit var todoListAdapter : TodoRecyclerAdapter
    private var todoList : List<TodoItem> = listOf()
    private lateinit var myUser : String

    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        this.inflater = inflater
        view = inflater.inflate(R.layout.fragment_todo, container, false)

        viewModel = ViewModelProvider(this)[TodoViewModel::class.java]
        userRepository = UserRepository()
        userViewModel = ViewModelProvider(this, UserViewModelFactory(userRepository))[UserViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todoListView = view.findViewById(R.id.todo_created_container)
        //observeDataStoreChanges()

        userViewModel.getUser(auth.currentUser!!.uid)
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                return@observe
            }
            myUser = user.id
            setupButtons(user)
            getUserTodoList(user)
        }
        setupSuggestions()
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
                val todoView = inflater.inflate(R.layout.suggestion_item, container, false)
                todoView.findViewById<TextView>(R.id.textview_todo).text = suggestion

                //Adds suggestion to user's to do list if checked
                todoView.findViewById<TextView>(R.id.suggestion_add_button).setOnClickListener() {
                    val itemID = UUID.randomUUID().toString()
                    val item = TodoItem(itemID,"", false)

                    item.task = suggestion
                    userViewModel.addTodoItem(myUser,item)
                    todoListView.adapter?.notifyItemInserted(todoList.size)

                    Toast.makeText(requireContext(), "Added suggestion!", Toast.LENGTH_SHORT).show()
                }
                container.addView(todoView)
            }
        }
        viewModel.generateSuggestions()
    }

    //Add todoItem
    @SuppressLint("RestrictedApi", "NotifyDataSetChanged")
    private fun setupButtons(user : User) {
        val addButton : TextView = view.findViewById(R.id.todo_add_button)
        addButton.setOnClickListener{
            val addItem : TextView = view.findViewById<TextView?>(R.id.todo_add_text)
            val itemID = UUID.randomUUID().toString()
            val item = TodoItem(itemID,"", false)

            item.task = addItem.text.toString()
            userViewModel.addTodoItem(user.id,item)

            //Clear text field
            addItem.text = ""
            addItem.clearFocus()
            todoListView.adapter?.notifyItemInserted(todoList.size)

            //Close keyboard
            val kb = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            kb.hideSoftInputFromWindow(view.windowToken, 0)

            Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
        }
    }

    //Initiate todoList
    private fun getUserTodoList(user : User) {
        todoList = user.todoList
        todoListAdapter = TodoRecyclerAdapter(userViewModel, user.id)
        todoListAdapter.setTodoList(todoList)
        todoListView.adapter = todoListAdapter
        todoListAdapter.notifyDataSetChanged()
    }
}