package com.example.kyshatbmozno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyshatbmozno.Adapter.OrdersAdapter;
import com.example.kyshatbmozno.Models.Order;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {

    TextView ordersNow, ordersPast, tvGetDate;
    LinearLayout llord2;
    RecyclerView rvOrders;
    OrdersAdapter ordersAdapter;
    OrdersAdapter orderPastsAdapter;
    OrdersAdapter orderSortedPastsAdapter;
    List<Order> orderList = new ArrayList<>();
    List<Order> pastOrderList = new ArrayList<>();
    List<Order> sortedPastOrderList = new ArrayList<>();

    FirebaseAuth auth;
    FirebaseUser curUser;
    FirebaseDatabase db;
    DatabaseReference ordRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        ordRef = db.getReference("Orders");

        ordersNow = findViewById(R.id.ordersNow);
        ordersPast = findViewById(R.id.ordersPast);
        rvOrders = findViewById(R.id.rvOrders);
        llord2 = findViewById(R.id.llord2);
        tvGetDate = findViewById(R.id.tvGetDate);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrdersAdapter(this, orderList);
        orderPastsAdapter = new OrdersAdapter(this, pastOrderList);
        orderSortedPastsAdapter = new OrdersAdapter(this, sortedPastOrderList);

        rvOrders.setAdapter(ordersAdapter);

        tvGetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog();
            }
        });

        ordRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (orderList.size()>0) orderList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Order order = ds.getValue(Order.class);
                    assert order != null;
                    if (order.getIdUser().equals(curUser.getUid())){
                        if (!order.getStatus().equals("Завершен")) orderList.add(order);
                        else pastOrderList.add(order);
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ordersNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvOrders.setAdapter(ordersAdapter);
                ordersNow.setBackgroundColor(getResources().getColor(R.color.yellow));
                ordersPast.setBackgroundColor(getResources().getColor(R.color.white));
                llord2.setVisibility(View.GONE);
            }
        });
        ordersPast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvOrders.setAdapter(orderPastsAdapter);
                ordersNow.setBackgroundColor(getResources().getColor(R.color.white));
                ordersPast.setBackgroundColor(getResources().getColor(R.color.yellow));
                tvGetDate.setText("Выберите диапозон дат");
                llord2.setVisibility(View.VISIBLE);
            }
        });
    }

    private void datePickerDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Выберите диапозон дат");

        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {

            if (sortedPastOrderList.size()>0) sortedPastOrderList.clear();

            Long startDate = selection.first;
            Long endDate = selection.second;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateString = sdf.format(new Date(startDate));
            String endDateString = sdf.format(new Date(endDate));

            String selectedDateRange = startDateString + " - " + endDateString;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate start = LocalDate.parse(startDateString, formatter);
            LocalDate end = LocalDate.parse(endDateString, formatter);

            for (int i=0;i<pastOrderList.size();i++){
                String orderDate = pastOrderList.get(i).getDate();
                int indEnd = orderDate.indexOf(" ");
                String date = orderDate.substring(0, indEnd);
                LocalDate localDateOrder = LocalDate.parse(date, formatter);
                if (localDateOrder.isAfter(start)&&localDateOrder.isBefore(end)
                        ||localDateOrder.isEqual(start)||localDateOrder.isEqual(end)) sortedPastOrderList.add(pastOrderList.get(i));
            }

            rvOrders.setAdapter(orderSortedPastsAdapter);
            tvGetDate.setText(selectedDateRange);
        });

        // Showing the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

}