package com.example.kyshatbmozno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kyshatbmozno.Adapter.FoodAdapter;
import com.example.kyshatbmozno.Models.Cart;
import com.example.kyshatbmozno.Models.Feedback;
import com.example.kyshatbmozno.Models.Food;
import com.example.kyshatbmozno.Models.Restaurant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoodActivity extends AppCompatActivity {

    ImageView headImgRest;
    TextView nameRest, tvClickRating;
    String idRest, nameOfRest, photoRest;
    RatingBar ratingRestFood;
    List<Food> listFood = new ArrayList<>();
    FirebaseDatabase db;
    FloatingActionButton fab;
    DatabaseReference food_ref;
    DatabaseReference carts_ref;
    DatabaseReference feed_ref;
    FirebaseAuth auth;
    FirebaseUser curUser;
    FoodAdapter foodAdapter;
    RecyclerView rvFood;
    List<Float> rating = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        idRest = getIntent().getStringExtra("restId");
        nameOfRest = getIntent().getStringExtra("restName");
        photoRest = getIntent().getStringExtra("restPhoto");

        nameRest = findViewById(R.id.nameRest);
        ratingRestFood = findViewById(R.id.ratingRestFood);
        headImgRest = findViewById(R.id.headImgRest);
        tvClickRating = findViewById(R.id.tvClickRating);
        rvFood = findViewById(R.id.rvFood);

        db = FirebaseDatabase.getInstance();
        food_ref = db.getReference("Food");
        carts_ref = db.getReference("Carts");
        feed_ref = db.getReference("Feedbacks");
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();

        fab = findViewById(R.id.fab_food);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        feed_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float rt = 0;
                if (rating.size()>0) rating.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Feedback feedback = ds.getValue(Feedback.class);
                    assert feedback!=null;
                    if (feedback.getIdRest().equals(idRest)) rating.add(Float.valueOf(feedback.getRating()));
                }
                rt = countAverage(rating);
                ratingRestFood.setRating(rt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        tvClickRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodActivity.this, FeedbackActivity.class);
                intent.putExtra("idRest", idRest);
                startActivity(intent);
            }
        });

        nameRest.setText(nameOfRest);
        Picasso.get().load(photoRest).into(headImgRest);

        rvFood.setLayoutManager(new GridLayoutManager(this, 2));
        foodAdapter = new FoodAdapter(this, listFood);
        rvFood.setAdapter(foodAdapter);

        getDataFromDB();
        getCartFromDB();
    }

    private void getDataFromDB(){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listFood.size() > 0) listFood.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Food food = ds.getValue(Food.class);
                    assert food != null;
                    if (food.getIdRest().equals(idRest)) listFood.add(food);
                }
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        food_ref.addValueEventListener(vListener);
    }

    private void getCartFromDB(){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cart cart = snapshot.getValue(Cart.class);
                assert cart != null;
                if (cart.getCartList() == null || cart.getCartList().size() == 0) fab.setVisibility(View.GONE);
                else fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        carts_ref.child(curUser.getUid()).addValueEventListener(vListener);
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