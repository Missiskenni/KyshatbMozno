package com.example.kyshatbmozno.ui.addfood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kyshatbmozno.Models.Food;
import com.example.kyshatbmozno.Models.Restaurant;
import com.example.kyshatbmozno.R;
import com.example.kyshatbmozno.databinding.FragmentAddfoodBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddFoodFragment extends Fragment {

    Spinner spinRests;
    EditText nameOfFood, compOfFood, priceOfFood, weightOfFood, catOfFood;
    Button btnPhotoOfFood, btnCreateRest;
    ImageView photoFood;
    List<Restaurant> restaurants = new ArrayList<>();
    List<String> namesRests = new ArrayList<>();

    ArrayAdapter<String> adapter;
    FirebaseDatabase db;
    DatabaseReference rest_ref;
    DatabaseReference food_ref;

    Uri photoUriFood;

    private FragmentAddfoodBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_addfood, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoUriFood = Uri.parse("android.resource://com.example.kyshatbmozno/" + R.drawable.food_def);

        db = FirebaseDatabase.getInstance();
        rest_ref = db.getReference("Restaurant");
        food_ref = db.getReference("Food");

        spinRests = view.findViewById(R.id.spinRests);
        nameOfFood = view.findViewById(R.id.nameOfFood);
        compOfFood = view.findViewById(R.id.compOfFood);
        priceOfFood = view.findViewById(R.id.priceOfFood);
        weightOfFood = view.findViewById(R.id.weightOfFood);
        catOfFood = view.findViewById(R.id.catOfFood);
        photoFood = view.findViewById(R.id.photoFood);

        btnPhotoOfFood = view.findViewById(R.id.btnPhotoOfFood);
        btnPhotoOfFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        btnCreateRest = view.findViewById(R.id.btnCreateRest);
        btnCreateRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFood();
            }
        });

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, namesRests);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRests.setAdapter(adapter);

        getDataFromDB();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getDataFromDB(){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (restaurants.size() > 0) restaurants.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Restaurant restaurant = ds.getValue(Restaurant.class);
                    assert restaurant != null;
                    restaurants.add(restaurant);
                    namesRests.add(restaurant.getName());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        rest_ref.addValueEventListener(vListener);
    }

    private void createFood(){
        Food newFood = new Food();
        String id, idRest, name, price, composition, weight, category;


        idRest = restaurants.get((int) spinRests.getSelectedItemId()).getId();
        id = food_ref.push().getKey();
        name = nameOfFood.getText().toString();
        price = priceOfFood.getText().toString();
        composition = compOfFood.getText().toString();
        weight = weightOfFood.getText().toString();
        category = catOfFood.getText().toString();

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child("Food/"+id+"/photoOfFood.jpg");
        storageReference.putFile(photoUriFood).addOnCompleteListener(command -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
               newFood.setId(id);
               newFood.setIdRest(idRest);
               newFood.setName(name);
               newFood.setPrice(price);
               newFood.setComposition(composition);
               newFood.setWeight(weight);
               newFood.setCategory(category);
               newFood.setPhotoUriFood(uri.toString());

               food_ref.child(id).setValue(newFood);
               Toast.makeText(getView().getContext(), "Еда добавлена!", Toast.LENGTH_SHORT).show();
               clearFields();
            });
        });
    }

    private void clearFields(){
        nameOfFood.setText("");
        priceOfFood.setText("");
        compOfFood.setText("");
        weightOfFood.setText("");
        catOfFood.setText("");
        photoFood.setImageResource(R.drawable.food_def);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null){
            photoUriFood = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUriFood);
                photoFood.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(getView().getContext(), "Фото выбрано!", Toast.LENGTH_SHORT).show();
        }
    }
    public void getImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);
    }

}