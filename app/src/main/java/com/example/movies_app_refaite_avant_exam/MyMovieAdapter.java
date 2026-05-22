package com.example.movies_app_refaite_avant_exam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyMovieAdapter extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder> implements Filterable {
    MyMovieData[] myMovieData;
    Context context;

    public MyMovieAdapter(MyMovieData[] myMovieData, MainActivity activity) {
        this.myMovieData = myMovieData;
        this.context = activity;
    }

    @NonNull
    @Override
    public MyMovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //convertir fahter xml  en java
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        // specifier la vue
        View view=inflater.inflate(R.layout.movie_item_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyMovieAdapter.ViewHolder holder, int position) {
        final  MyMovieData myMovieDataElt=myMovieData[position];
        holder.txtViewMovieName.setText(myMovieDataElt.getMovieTitle());
        holder.txtViewMovieData.setText(myMovieDataElt.getMoviData());
        holder.imageViewMovie.setImageResource(myMovieDataElt.getMovieImage());



    }

    @Override
    public int getItemCount() {
        return myMovieData.length;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
    private Filter movieFilter=new Filter() {
        List<MyMovieData> movies= new ArrayList<>();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint==null || constraint.length()==0 || constraint.toString().trim().length()==0)
                movies.addAll(Arrays.asList(myMovieData));
            else{
                for(MyMovieData movie: movies){
                    if(! movie.getMovieTitle().equalsIgnoreCase(constraint.toString())){
                        movies.add(movie);
                    }
                }


            }
            FilterResults filterResults=new FilterResults();
            filterResults.values=movies;
            return  filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            movies.clear();
            movies.addAll((List) results.values);
            notifyDataSetChanged();

        }
    };

    public  class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtViewMovieName;
        TextView txtViewMovieData;
        ImageView imageViewMovie;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            txtViewMovieName=itemView.findViewById(R.id.textViewMovieName);
            txtViewMovieData=itemView.findViewById(R.id.textViewMovieData);
            imageViewMovie=itemView.findViewById(R.id.imageViewMovie);
        }
    }
}
