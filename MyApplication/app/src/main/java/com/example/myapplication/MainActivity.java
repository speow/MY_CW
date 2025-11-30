package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private View emptyView;
    private TextView taskCountText;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        taskCountText = findViewById(R.id.taskCountText);
        FloatingActionButton fab = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();

        taskAdapter = new TaskAdapter(this, taskList, new TaskAdapter.OnTaskListener() {
            @Override
            public void onTaskDelete(Task task) {
                deleteTask(task);
            }

            @Override
            public void onTaskStatusChange(Task task) {
                updateTaskStatus(task);
            }
        });

        recyclerView.setAdapter(taskAdapter);

        loadTasks();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        taskList.clear();
        taskList.addAll(databaseHelper.getAllTasks());
        taskAdapter.notifyDataSetChanged();

        updateTaskCount();

        if (taskList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void updateTaskCount() {
        int totalTasks = taskList.size();
        int completedTasks = 0;

        for (Task task : taskList) {
            if (task.isCompleted()) {
                completedTasks++;
            }
        }

        if (totalTasks == 0) {
            taskCountText.setText("У вас нет активных задач");
        } else {
            taskCountText.setText("Задач: " + totalTasks + " | Выполнено: " + completedTasks);
        }
    }

    private void deleteTask(Task task) {
        databaseHelper.deleteTask(task.getId());
        loadTasks();
        Toast.makeText(this, "✓ Задача удалена", Toast.LENGTH_SHORT).show();
    }

    private void updateTaskStatus(Task task) {
        task.setCompleted(!task.isCompleted());
        databaseHelper.updateTask(task);
        taskAdapter.notifyDataSetChanged();
        updateTaskCount();

        if (task.isCompleted()) {
            Toast.makeText(this, "✓ Задача выполнена!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "○ Задача не выполнена", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all) {
            if (taskList.isEmpty()) {
                Toast.makeText(this, "Нет задач для удаления", Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.deleteAllTasks();
                loadTasks();
                Toast.makeText(this, "✓ Все задачи удалены", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}