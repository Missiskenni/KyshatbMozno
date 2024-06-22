package com.example.kyshatbmozno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyshatbmozno.Adapter.OrderCartAdapter;
import com.example.kyshatbmozno.Models.Feedback;
import com.example.kyshatbmozno.Models.FoodInCart;
import com.example.kyshatbmozno.Models.Restaurant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OneOrderActivity extends AppCompatActivity {

    String idOrder;
    String idRest, idUser;
    TextView tvAddress, tvDate, tvStatus, tvFeedback, tvTotalPrice, tvRestNameOrder;
    LinearLayout llFeedback;
    RecyclerView rvCartInOrder;
    FirebaseDatabase db;
    DatabaseReference ordRef;
    DatabaseReference restRef;
    DatabaseReference feedRef;

    FirebaseAuth auth;
    FirebaseUser curUser;
    List<FoodInCart> foodInCartList = new ArrayList<>();
    OrderCartAdapter orderCartAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_order);

        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        idUser = curUser.getUid();

        idOrder = getIntent().getStringExtra("id");

        tvAddress = findViewById(R.id.tvAddress);
        tvRestNameOrder = findViewById(R.id.tvRestNameOrder);
        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvFeedback = findViewById(R.id.tvFeedback);
        rvCartInOrder = findViewById(R.id.rvCartInOrder);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        llFeedback = findViewById(R.id.llFeedback);

        rvCartInOrder.setLayoutManager(new LinearLayoutManager(this));
        orderCartAdapter = new OrderCartAdapter(this, foodInCartList);
        rvCartInOrder.setAdapter(orderCartAdapter);

        db = FirebaseDatabase.getInstance();
        ordRef = db.getReference("Orders").child(idOrder);
        feedRef = db.getReference("Feedbacks");
        restRef = db.getReference("Restaurant");

        ordRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idRest = snapshot.child("idRest").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String date = snapshot.child("date").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                String price = snapshot.child("price").getValue(String.class);

                tvAddress.setText(address);
                tvDate.setText(date);
                tvStatus.setText(status);
                tvTotalPrice.setText(price);

                if (status.equals("Завершен")) llFeedback.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    assert restaurant!=null;
                    if (restaurant.getId().equals(idRest)) tvRestNameOrder.setText(restaurant.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ordRef.child("orderList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (foodInCartList.size()>0) foodInCartList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    FoodInCart foodInCart = ds.getValue(FoodInCart.class);
                    assert foodInCart!=null;
                    foodInCartList.add(foodInCart);
                }
                orderCartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        tvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFeedback();
            }
        });

    }

    private void doFeedback() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Отзыв на ресторан");

        LayoutInflater inflater = LayoutInflater.from(this);
        View doFeedback = inflater.inflate(R.layout.do_feedback, null);
        dialog.setView(doFeedback);

        List<String> listRating = new ArrayList<>();
        listRating.add("1");
        listRating.add("2");
        listRating.add("3");
        listRating.add("4");
        listRating.add("5");

        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String d;
        if (day<10) d = "0"+String.valueOf(day);
        else d = String.valueOf(day);
        int month = Calendar.getInstance().get(Calendar.MONTH)+1;
        String m;
        if (month<10) m = "0"+String.valueOf(month);
        else m = String.valueOf(month);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String y = String.valueOf(year);

        String date = d+":"+m+":"+y;

        final Spinner spinnerRating = doFeedback.findViewById(R.id.spinnerRating);
        final EditText edFeedback = doFeedback.findViewById(R.id.edFeedback);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listRating);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(adapter);
        spinnerRating.setSelection(4);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Оставить отзыв", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (spinnerRating.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(OneOrderActivity.this, " Поставьте оценку", Toast.LENGTH_SHORT).show();
                }
                else {
                    String comment = edFeedback.getText().toString();
                    String rating = spinnerRating.getSelectedItem().toString();
                    String id = feedRef.push().getKey();

                    Feedback feedback = new Feedback();
                    feedback.setId(id);
                    feedback.setIdRest(idRest);
                    feedback.setComment(comment);
                    feedback.setRating(rating);
                    feedback.setIdUser(idUser);
                    feedback.setDate(date);

                    feedRef.child(id).setValue(feedback);
                }

            }
        });

        dialog.show();
    }
}