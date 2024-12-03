package com.android.example.finalprojectapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.example.finalprojectapp.R;
import com.android.example.finalprojectapp.ToDoListActivity;
import com.android.example.finalprojectapp.ToDoListModel.ToDoModel;


import java.util.List;
import com.android.example.finalprojectapp.DataBaseHelper.ToDoDataBaseHelper;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private ToDoDataBaseHelper dataBaseHelper;
    private ToDoListActivity activity;

    public ToDoAdapter(ToDoDataBaseHelper dataBaseHelper, ToDoListActivity activity, List<ToDoModel> todoList) {
        this.dataBaseHelper = dataBaseHelper;
        this.activity = activity;
        this.todoList = todoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        dataBaseHelper.openDatabase();

        ToDoModel task = todoList.get(position);
        holder.checkBox.setText(task.getTask());
        holder.checkBox.setChecked(task.getStatus());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked);
            dataBaseHelper.updateTaskStatus(task.getId(), isChecked);
        });
    }


    @Override
    public int getItemCount() {
        return todoList.size();
    }


    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        dataBaseHelper.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.check_box);
        }
    }
}