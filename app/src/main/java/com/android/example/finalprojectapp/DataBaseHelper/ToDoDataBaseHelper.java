package com.android.example.finalprojectapp.DataBaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.example.finalprojectapp.ToDoListModel.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class ToDoDataBaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name and Columns
    private static final String TABLE_TODO = "todo";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TASK = "task";
    private static final String COLUMN_STATUS = "status";
    private SQLiteDatabase db;

    // Create Table SQL Query
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_TODO + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TASK + " TEXT, "
            + COLUMN_STATUS + " INTEGER"
            + ");";

    // Constructor
    public ToDoDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }


    // Add a new task
    public void addTask(ToDoModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK, task.getTask());
        values.put(COLUMN_STATUS, task.getStatus()? 1 : 0);

        db.insert(TABLE_TODO, null, values);
        db.close();
    }

    // Get all tasks
    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TODO, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                ToDoModel task = new ToDoModel();
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int taskIndex = cursor.getColumnIndex(COLUMN_TASK);
                int statusIndex = cursor.getColumnIndex(COLUMN_STATUS);


                task.setId(cursor.getInt(idIndex));
                task.setTask(cursor.getString(taskIndex));
                task.setStatus(cursor.getInt(statusIndex) == 1);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Update a tasks status
    public void updateTaskStatus(int id, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status?1:0);

        db.update(TABLE_TODO, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }


    // Delete a task
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODO, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}

