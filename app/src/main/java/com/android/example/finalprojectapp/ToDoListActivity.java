package com.android.example.finalprojectapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.finalprojectapp.Adapter.ToDoAdapter;
import com.android.example.finalprojectapp.DataBaseHelper.ToDoDataBaseHelper;
import com.android.example.finalprojectapp.ToDoListModel.ToDoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
public class ToDoListActivity extends AppCompatActivity {

    private RecyclerView tasksRecyclerView;
    private FloatingActionButton addButton;
    private List<ToDoModel> taskList;
    private ToDoAdapter taskAdapter;
    private ToDoDataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tasks");
        }

        dataBaseHelper = new ToDoDataBaseHelper(this);
        dataBaseHelper.openDatabase();

        tasksRecyclerView = findViewById(R.id.tasks);
        addButton = findViewById(R.id.add_button);

        taskList = dataBaseHelper.getAllTasks();
        taskAdapter = new ToDoAdapter(dataBaseHelper,this, taskList);
        taskAdapter.notifyDataSetChanged();
        tasksRecyclerView.setAdapter(taskAdapter);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        addButton.setOnClickListener(v -> showAddTaskDialog());

        //swipe function for deleting task
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                taskAdapter.deleteItem(position);
            }

        });

        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_new_task_layout, null);
        builder.setView(dialogView);

        EditText newTaskEditText = dialogView.findViewById(R.id.newTask);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        AlertDialog alertDialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String taskDescription = newTaskEditText.getText().toString();
            if (!taskDescription.isEmpty()) {
                ToDoModel newTask = new ToDoModel();
                newTask.setTask(taskDescription);
                newTask.setStatus(false);
                dataBaseHelper.addTask(newTask);
                taskList.add(newTask);
                taskAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            } else {
                newTaskEditText.setError("Task description cannot be empty");
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataBaseHelper.close();
    }



}
