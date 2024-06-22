package com.example.kyshatbmozno.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmozno.Models.Cart;
import com.example.kyshatbmozno.Models.FoodInCart;
import com.example.kyshatbmozno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{
    Context context;
    List<FoodInCart> foodInCartList;

    public CartAdapter(Context context, List<FoodInCart> foodInCartList) {
        this.context = context;
        this.foodInCartList = foodInCartList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CartViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser curUser = auth.getCurrentUser();
        DatabaseReference cartRef = db.getReference("Carts").child(curUser.getUid());

        String price = foodInCartList.get(position).getPrice();
        String quantity = foodInCartList.get(position).getQuantity();

        holder.cartQuan.setText(foodInCartList.get(position).getQuantity());
        holder.nameItemRV.setText(foodInCartList.get(position).getName());

        int pr = Integer.parseInt(price);
        int q = Integer.parseInt(quantity);
        pr = pr * q;
        String allprice = String.valueOf(pr);
        holder.cartOnePrice.setText(allprice);


        holder.btnCartAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldQ = foodInCartList.get(position).getQuantity();
                String newQ;
                int newQuan = Integer.parseInt(oldQ) + 1;
                newQ = String.valueOf(newQuan);
                foodInCartList.get(position).setQuantity(newQ);
                List<FoodInCart> newList = foodInCartList;
                cartRef.child("cartList").setValue(newList);
            }
        });

        holder.btnCartRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldQ = foodInCartList.get(position).getQuantity();
                String newQ;
                int newQuan = Integer.parseInt(oldQ) - 1;
                if (newQuan == 0) {
                    foodInCartList.remove(position);
                    List<FoodInCart> newList = foodInCartList;
                    cartRef.child("cartList").setValue(newList);
                }
                else {
                    newQ = String.valueOf(newQuan);
                    foodInCartList.get(position).setQuantity(newQ);
                    List<FoodInCart> newList = foodInCartList;
                    cartRef.child("cartList").setValue(newList);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return foodInCartList.size();
    }
}

class CartViewHolder extends RecyclerView.ViewHolder{
    TextView nameItemRV, cartQuan, cartOnePrice;
    Button btnCartAdd, btnCartRemove;


    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        nameItemRV = itemView.findViewById(R.id.nameItemRV);
        cartQuan = itemView.findViewById(R.id.cartQuan);
        cartOnePrice = itemView.findViewById(R.id.cartOnePrice);
        btnCartAdd = itemView.findViewById(R.id.btnCartAdd);
        btnCartRemove = itemView.findViewById(R.id.btnCartRemove);
    }
}