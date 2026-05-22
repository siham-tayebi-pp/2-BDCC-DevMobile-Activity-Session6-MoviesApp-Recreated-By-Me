package com.example.movies_app_refaite_avant_exam;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView=findViewById(R.id.recycView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        resRecyclerView.setLayoutManager();
        MyMovieData[] myMovies=new MyMovieData[]{
                new MyMovieData("Wedneswdau","2029 film", R.drawable.avatar),
                new MyMovieData("twilight","2020 film", R.drawable.ava),
                new MyMovieData("soso","2002 film", R.drawable.av),

        };
        MyMovieAdapter  adapter=new MyMovieAdapter(myMovies,this);
        recyclerView.setAdapter(adapter);
//        adapter.
//        adapter.notifyDataSetChanged();

    }
}