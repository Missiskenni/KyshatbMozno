package com.example.kyshatbmozno.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmozno.Models.Order;
import com.example.kyshatbmozno.Models.Restaurant;
import com.example.kyshatbmozno.OneOrderActivity;
import com.example.kyshatbmozno.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrderViewHolder>{

    Context context;
    List<Order> orderList;

    public OrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference restRef = db.getReference("Restaurant");

        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Restaurant restaurant1 = ds.getValue(Restaurant.class);
                    assert restaurant1 != null;
                    if (orderList.get(position).getIdRest().equals(restaurant1.getId())) holder.tvRestOrder.setText(restaurant1.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tvPriceOrder.setText(orderList.get(position).getPrice());
        holder.tvTimeOrder.setText(orderList.get(position).getDate());

        holder.rlOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OneOrderActivity.class);
                intent.putExtra("id",orderList.get(position).getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}

class OrderViewHolder extends RecyclerView.ViewHolder{

    TextView tvRestOrder, tvPriceOrder, tvTimeOrder;
    RelativeLayout rlOrder;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        tvRestOrder = itemView.findViewById(R.id.tvRestOrder);
        tvPriceOrder = itemView.findViewById(R.id.tvPriceOrder);
        tvTimeOrder = itemView.findViewById(R.id.tvTimeOrder);
        rlOrder = itemView.findViewById(R.id.rlOrder);
    }


}