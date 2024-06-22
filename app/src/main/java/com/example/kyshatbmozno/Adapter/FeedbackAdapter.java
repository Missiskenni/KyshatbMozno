package com.example.kyshatbmozno.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmozno.Models.Feedback;
import com.example.kyshatbmozno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackViewHolder>{
    Context context;
    List<Feedback> feedbackList;

    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedbackViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_feedback, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference userRef = db.getReference("Users").child(curUser.getUid());

        holder.ratingRestOfFeedback.setRating(Float.parseFloat(feedbackList.get(position).getRating()));
        holder.tvTextOfFeedback.setText(feedbackList.get(position).getComment());
        holder.tvDateOfFeedback.setText(feedbackList.get(position).getDate());

        userRef.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                holder.tvNameOfUser.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }
}

class FeedbackViewHolder extends RecyclerView.ViewHolder{
    TextView tvNameOfUser, tvTextOfFeedback, tvDateOfFeedback;
    RatingBar ratingRestOfFeedback;

    public FeedbackViewHolder(@NonNull View itemView) {
        super(itemView);

        tvNameOfUser = itemView.findViewById(R.id.tvNameOfUser);
        tvTextOfFeedback = itemView.findViewById(R.id.tvTextOfFeedback);
        tvDateOfFeedback = itemView.findViewById(R.id.tvDateOfFeedback);
        ratingRestOfFeedback = itemView.findViewById(R.id.ratingRestOfFeedback);
    }
}