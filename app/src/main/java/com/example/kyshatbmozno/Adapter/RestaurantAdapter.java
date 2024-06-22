package com.example.kyshatbmozno.Adapter;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmozno.FoodActivity;
import com.example.kyshatbmozno.Models.Feedback;
import com.example.kyshatbmozno.R;
import com.example.kyshatbmozno.ui.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.example.kyshatbmozno.Models.Restaurant;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder>{

    Context context;
    List<Restaurant> restaurants;

    public RestaurantAdapter(Context context, List<Restaurant> restaurants) {
        this.context = context;
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RestaurantViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_rest, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.nameOfRest.setText(restaurants.get(position).getName());
        holder.descriptionOfRest.setText(restaurants.get(position).getDescription());
        Picasso.get().load(restaurants.get(position).getPhotoUriRest()).into(holder.photoRestMain);
        holder.ratingRest.setRating(0);

        List<Float> rating = new ArrayList<>();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference feedRef = db.getReference("Feedbacks");
        feedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float rt = 0;
                if (rating.size()>0) rating.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Feedback feedback = ds.getValue(Feedback.class);
                    assert feedback!=null;
                    if (feedback.getIdRest().equals(restaurants.get(position).getId())) rating.add(Float.valueOf(feedback.getRating()));
                }
                rt = countAverage(rating);
                holder.ratingRest.setRating(rt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.cvRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FoodActivity.class);
                intent.putExtra("restId", restaurants.get(position).getId());
                intent.putExtra("restName", restaurants.get(position).getName());
                intent.putExtra("restPhoto", restaurants.get(position).getPhotoUriRest());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    private float countAverage(List<Float> list){
        float average = 0;
        float total = 0;

        for (int i = 0; i<list.size(); i++){
            total = total + list.get(i);
        }

        average = total/list.size();

        return average;
    }
}

class RestaurantViewHolder extends RecyclerView.ViewHolder{

    TextView nameOfRest, descriptionOfRest;
    ImageView photoRestMain;
    CardView cvRest;
    RatingBar ratingRest;

    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        cvRest = itemView.findViewById(R.id.cvRest);
        nameOfRest = itemView.findViewById(R.id.nameOfRest);
        descriptionOfRest = itemView.findViewById(R.id.descriptionOfRest);
        photoRestMain = itemView.findViewById(R.id.photoRestMain);
        ratingRest = itemView.findViewById(R.id.ratingRest);
    }
}
