package com.example.movies_app_refaite_avant_exam;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Button btnSearch;
    private EditText searchText;
    private  MyMovieAdapter   myMovieAdapter;
    private static  final  String TMDB_API_KEY="3c7d18569e2a55195297db2ba34efe01";
    private static  final  String BASE_URL="https://api.themoviedb.org/3/movie/popular";
    private static  final  String TAG="MainActivity";



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
        searchText=findViewById(R.id.editTxtSearch);

//        resRecyclerView.setLayoutManager();
        String url= BASE_URL+"?api_key="+TMDB_API_KEY;
        RequestQueue queue= Volley.newRequestQueue(this);

        JsonObjectRequest  request=new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results= response.getJSONArray("results");
                            MyMovieData[] movies=new MyMovieData[results.length()];
                            for(int i=0;i<movies.length;i++){
                                JSONObject movie=results.getJSONObject(i);
                                movies[i]=new MyMovieData(movie.getInt("id"),movie.getString("title"),movie.getString("release_date"),movie.getString("poster_path"));




                            }
                            myMovieAdapter=new MyMovieAdapter(movies, MainActivity.this);
                            recyclerView.setAdapter(myMovieAdapter);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }

                }, error -> {
//                    Gestion erreurs
            Log.d(TAG,"erreur reasaeu"+ error.getMessage());
        }
        );

        queue.add(request);//envoiyer req en ariere plan
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(myMovieAdapter!=null){
                    myMovieAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

    }
//        MyMovieData[] myMovies=new MyMovieData[]{
//                new MyMovieData("Wedneswdau","2029 film", R.drawable.avatar),
//                new MyMovieData("twilight","2020 film", R.drawable.ava),
//                new MyMovieData("soso","2002 film", R.drawable.av),
//
//        };
//        MyMovieAdapter  adapter=new MyMovieAdapter(myMovies,this);
//        recyclerView.setAdapter(adapter);

//        adapter.
//        adapter.notifyDataSetChanged();

//    }
}