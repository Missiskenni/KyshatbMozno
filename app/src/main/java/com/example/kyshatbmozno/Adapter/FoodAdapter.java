package com.example.kyshatbmozno.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmozno.Models.Cart;
import com.example.kyshatbmozno.Models.Food;
import com.example.kyshatbmozno.Models.FoodInCart;
import com.example.kyshatbmozno.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodViewHolder>{

    Context context;
    List<Food> foodList;

    public FoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FoodViewHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item_food, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser curUser = auth.getCurrentUser();
        DatabaseReference cartRef = db.getReference("Carts");

        holder.foodName.setText(foodList.get(position).getName());
        holder.foodPrice.setText(foodList.get(position).getPrice()+"р.");
        holder.foodWeight.setText(foodList.get(position).getWeight()+"г.");
        Picasso.get().load(foodList.get(position).getPhotoUriFood()).into(holder.photoOfFood);

        List<Cart> carts = new ArrayList<>();


        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (carts.size()>0) carts.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Cart cart = ds.getValue(Cart.class);
                    if (cart.getIdUser().equals(curUser.getUid())) carts.add(cart);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.btnInCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<FoodInCart> listCart = new ArrayList<>();

                FoodInCart fic = new FoodInCart();
                fic.setIdFood(foodList.get(position).getId());
                fic.setIdRest(foodList.get(position).getIdRest());
                fic.setName(foodList.get(position).getName());
                fic.setPrice(foodList.get(position).getPrice());
                fic.setQuantity("1");

                if (carts.size() == 0){
                    listCart.add(fic);
                    Cart cart = new Cart();
                    cart.setIdUser(curUser.getUid());
                    cart.setCartList(listCart);

                    cartRef.child(curUser.getUid()).setValue(cart);
                    Toast.makeText(context,  foodList.get(position).getName() + " был добавлен в корзину", Toast.LENGTH_SHORT).show();
                }
                else {
                    Cart cart = carts.get(0);

                    if (cart.getCartList()==null){
                        listCart.add(fic);
                        cart.setCartList(listCart);

                        cartRef.child(curUser.getUid()).setValue(cart);
                        Toast.makeText(context, foodList.get(position).getName() + " был добавлен в корзину", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        listCart = cart.getCartList();
                        List<FoodInCart> newListOfFood = new ArrayList<>();

                        boolean flag = true;
                        boolean flagForRest = true;
                        if (listCart.size()!=0){
                            for (FoodInCart foodInCart: listCart){
                                if (!foodInCart.getIdRest().equals(fic.getIdRest())) flagForRest = false;
                                if(foodInCart.getIdFood().equals(fic.getIdFood())){
                                    String qun = foodInCart.getQuantity();
                                    int q = Integer.parseInt(qun);
                                    q = q + 1;
                                    foodInCart.setQuantity(String.valueOf(q));
                                    flag = false;
                                }
                                newListOfFood.add(foodInCart);
                            }
                        }

                        if (flag&&flagForRest) newListOfFood.add(fic);
                        cart.setCartList(newListOfFood);
                        cartRef.child(curUser.getUid()).setValue(cart);
                        if (flagForRest) Toast.makeText(context, foodList.get(position).getName() + " был добавлен в корзину", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, "У вас есть товар из другого ресторана", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }
}

class FoodViewHolder extends RecyclerView.ViewHolder{

    TextView foodPrice, foodName, foodWeight;
    Button btnInCart;
    ImageView photoOfFood;

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        foodPrice = itemView.findViewById(R.id.foodPrice);
        foodName = itemView.findViewById(R.id.foodName);
        foodWeight = itemView.findViewById(R.id.foodWeight);
        btnInCart = itemView.findViewById(R.id.btnInCart);
        photoOfFood = itemView.findViewById(R.id.photoOfFood);
    }
}
