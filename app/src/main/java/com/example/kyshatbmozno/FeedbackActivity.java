package com.example.kyshatbmozno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.kyshatbmozno.Adapter.FeedbackAdapter;
import com.example.kyshatbmozno.Models.Feedback;
import com.example.kyshatbmozno.Models.Restaurant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    String idRest;
    TextView tvRestOfFeedbacks, noFeedbacks;
    FirebaseDatabase db;
    DatabaseReference feedRef;
    DatabaseReference restRef;
    List<Feedback> feedbackList = new ArrayList<>();
    RecyclerView rvFeedbacks;
    FeedbackAdapter feedbackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        idRest = getIntent().getStringExtra("idRest");

        rvFeedbacks = findViewById(R.id.rvFeedbacks);
        tvRestOfFeedbacks = findViewById(R.id.tvRestOfFeedbacks);
        noFeedbacks = findViewById(R.id.noFeedbacks);

        rvFeedbacks.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new FeedbackAdapter(this, feedbackList);
        rvFeedbacks.setAdapter(feedbackAdapter);

        db = FirebaseDatabase.getInstance();
        feedRef = db.getReference("Feedbacks");
        feedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (feedbackList.size()>0) feedbackList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Feedback feedback = ds.getValue(Feedback.class);
                    assert feedback != null;
                    if (feedback.getIdRest().equals(idRest)) feedbackList.add(feedback);
                }
                feedbackAdapter.notifyDataSetChanged();
                if (feedbackAdapter.getItemCount()==0) noFeedbacks.setVisibility(View.VISIBLE);
                else noFeedbacks.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        restRef = db.getReference("Restaurant");
        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    if (restaurant.getId().equals(idRest)) tvRestOfFeedbacks.setText(restaurant.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}