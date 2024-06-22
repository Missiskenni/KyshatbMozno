package com.example.kyshatbmozno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyshatbmozno.Adapter.CartAdapter;
import com.example.kyshatbmozno.Models.Card;
import com.example.kyshatbmozno.Models.Cart;
import com.example.kyshatbmozno.Models.FoodInCart;
import com.example.kyshatbmozno.Models.Order;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CartActivity extends AppCompatActivity {

    Button clearCart, btnBuyOrder;
    RecyclerView rvCart;
    TextView cartNameOfRest;
    EditText commentForOrder, addressForOrder, entranceForOrder, floorForOrder, flatForOrder, numberCardForOrder;

    FirebaseAuth auth;
    FirebaseUser curUser;
    FirebaseDatabase db;
    DatabaseReference cartRef;
    DatabaseReference restRef;
    DatabaseReference userRef;
    DatabaseReference orderRef;
    String idRest;
    int total = 0;
    List<FoodInCart> listFood = new ArrayList<>();
    List<Restaurant> restaurants = new ArrayList<>();

    CartAdapter cartAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        clearCart = findViewById(R.id.clearCart);
        btnBuyOrder = findViewById(R.id.btnBuyOrder);
        rvCart = findViewById(R.id.rvCart);
        cartNameOfRest = findViewById(R.id.cartNameOfRest);
        commentForOrder = findViewById(R.id.commentForOrder);
        addressForOrder = findViewById(R.id.addressForOrder);
        entranceForOrder = findViewById(R.id.entranceForOrder);
        floorForOrder = findViewById(R.id.floorForOrder);
        flatForOrder = findViewById(R.id.flatForOrder);
        numberCardForOrder = findViewById(R.id.numberCardForOrder);

        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        orderRef = db.getReference("Orders");

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, listFood);
        rvCart.setAdapter(cartAdapter);

        cartRef = db.getReference("Carts").child(curUser.getUid());
        cartRef.child("cartList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listFood.size()>0) listFood.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    FoodInCart foodInCart = ds.getValue(FoodInCart.class);
                    assert foodInCart != null;
                    listFood.add(foodInCart);
                    idRest = foodInCart.getIdRest();
                }
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        restRef = db.getReference("Restaurant");


        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (restaurants.size()>0) restaurants.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    assert restaurant != null;
                    if (restaurant.getId().equals(idRest)) cartNameOfRest.setText(restaurant.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userRef = db.getReference("Users").child(curUser.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child("address").getValue(String.class);
                Card card = snapshot.child("card").getValue(Card.class);

                addressForOrder.setText(address);
                if (card!=null) numberCardForOrder.setText(card.getNumber());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        clearCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearCart();
            }
        });

        btnBuyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartAdapter.getItemCount()==0){
                    Toast.makeText(CartActivity.this, "Корзина не должна быть пуста", Toast.LENGTH_SHORT).show();
                }
                else if (addressForOrder.getText().toString().isEmpty()) {
                    Toast.makeText(CartActivity.this, "Укажите адрес", Toast.LENGTH_SHORT).show();
                }
                else if (entranceForOrder.getText().toString().isEmpty()){
                    Toast.makeText(CartActivity.this, "Укажите подъезд", Toast.LENGTH_SHORT).show();
                }
                else if (floorForOrder.getText().toString().isEmpty()) {
                    Toast.makeText(CartActivity.this, "Укажите этаж", Toast.LENGTH_SHORT).show();
                }
                else if (flatForOrder.getText().toString().isEmpty()){
                    Toast.makeText(CartActivity.this, "Укажите квартиру", Toast.LENGTH_SHORT).show();
                }
                else if (numberCardForOrder.getText().toString().isEmpty()
                        ||numberCardForOrder.getText().toString().length()!=16){
                    Toast.makeText(CartActivity.this, "Укажите номер карты корректно", Toast.LENGTH_SHORT).show();
                }
                else {
                    showBuyOrder();
                }
            }
        });
    }

    private String countTotal(){
        if (total!= 0) total=0;
        String totalPr = null;
        for(int i=0; i < cartAdapter.getItemCount(); i++){
            if (rvCart.findViewHolderForAdapterPosition(i)!=null){
                TextView textView = rvCart.findViewHolderForAdapterPosition(i)
                        .itemView.findViewById(R.id.cartOnePrice);
                total = total +  Integer.parseInt(textView.getText().toString());
                totalPr = String.valueOf(total);
            }
        }
        return totalPr;
    }

    private void showBuyOrder(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Оформить заказ");

        LayoutInflater inflater = LayoutInflater.from(this);
        View buyOrder = inflater.inflate(R.layout.buy_order_window, null);
        dialog.setView(buyOrder);

        final TextView totalPriceOrderWin = buyOrder.findViewById(R.id.totalPriceOrder);
        String ttl = countTotal();
        totalPriceOrderWin.setText(ttl);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Заказать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String idOrder, idRestForOrder, status, idUser, comment, address, entrance, floor, flat;
                idOrder = orderRef.push().getKey();
                idRestForOrder = idRest;
                idUser = curUser.getUid();
                comment = commentForOrder.getText().toString();
                address = addressForOrder.getText().toString();
                entrance = entranceForOrder.getText().toString();
                floor = floorForOrder.getText().toString();
                flat = flatForOrder.getText().toString();
                status = "Принят";


                Date curTime = Calendar.getInstance().getTime();

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
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                String h;
                if (hour<10) h = "0"+String.valueOf(hour);
                else h = String.valueOf(hour);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                String min;
                if (minute<10){
                    min = "0"+String.valueOf(minute);
                }
                else min = String.valueOf(minute);

                String date = d+"/"+m+"/"+y+" "+h+":"+min;

                Order order = new Order();
                order.setId(idOrder);
                order.setIdRest(idRestForOrder);
                order.setIdUser(idUser);
                order.setComment(comment);
                order.setAddress(address);
                order.setEntrance(entrance);
                order.setFloor(floor);
                order.setFlat(flat);
                order.setStatus(status);
                order.setOrderList(listFood);
                order.setDate(date);
                order.setPrice(ttl);

                orderRef.child(idOrder).setValue(order);
                Toast.makeText(CartActivity.this, "Заказ сделан!", Toast.LENGTH_SHORT).show();

                List<FoodInCart> empList = new ArrayList<>();
                cartRef.child("cartList").setValue(empList);

                Intent intent = new Intent(CartActivity.this, MainMenuActivity.class);
                startActivity(intent);
            }
        });

        dialog.show();
    }

    private void showClearCart(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Очистить корзину");

        LayoutInflater inflater = LayoutInflater.from(this);
        View clearCart = inflater.inflate(R.layout.clear_cart, null);
        dialog.setView(clearCart);

        dialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("Очистить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<FoodInCart> empList = new ArrayList<>();
                cartRef.child("cartList").setValue(empList);
            }
        });

        dialog.show();
    }

}