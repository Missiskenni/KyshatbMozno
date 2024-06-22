package com.example.kyshatbmozno.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kyshatbmozno.CartActivity;
import com.example.kyshatbmozno.Models.Cart;
import com.example.kyshatbmozno.R;
import com.example.kyshatbmozno.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.kyshatbmozno.Adapter.RestaurantAdapter;
import com.example.kyshatbmozno.Models.Restaurant;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    RecyclerView rvRest;
    List<Restaurant> restaurants = new ArrayList<>();
    List<Restaurant> sortedRestaurants = new ArrayList<>();
    SearchView svRest;
    FloatingActionButton fab;
    FirebaseDatabase db;
    DatabaseReference rest_ref;
    DatabaseReference carts_ref;
    FirebaseAuth auth;
    FirebaseUser curUser;
    RestaurantAdapter restAdapter;
    RestaurantAdapter restSortedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseDatabase.getInstance();
        rest_ref = db.getReference("Restaurant");
        carts_ref = db.getReference("Carts");
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();

        fab = view.findViewById(R.id.fab_home);
        svRest = view.findViewById(R.id.svRest);

        rvRest = view.findViewById(R.id.rvRest);
        rvRest.setLayoutManager(new LinearLayoutManager(getContext()));
        restAdapter = new RestaurantAdapter(getContext(), restaurants);
        restSortedAdapter = new RestaurantAdapter(getContext(), sortedRestaurants);
        rvRest.setAdapter(restAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CartActivity.class);
                startActivity(intent);
            }
        });

        getRestFromDB();
        getCartFromDB();

        svRest.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) rvRest.setAdapter(restAdapter);
                else {
                    if (sortedRestaurants.size()>0) sortedRestaurants.clear();
                    for (int i=0; i<restaurants.size();i++){
                        if (restaurants.get(i).getName().toLowerCase().contains(newText.toLowerCase())
                                || restaurants.get(i).getDescription().toLowerCase().contains(newText.toLowerCase())) sortedRestaurants.add(restaurants.get(i));
                    }
                    rvRest.setAdapter(restSortedAdapter);
                }
                return false;
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getRestFromDB(){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (restaurants.size() > 0) restaurants.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    assert restaurant != null;
                    restaurants.add(restaurant);
                }
                rvRest.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        rest_ref.addValueEventListener(vListener);
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

}