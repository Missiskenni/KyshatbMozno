package com.example.kyshatbmozno.ui.addrest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.kyshatbmozno.R;
import com.example.kyshatbmozno.databinding.FragmentAddrestBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import com.example.kyshatbmozno.Models.Restaurant;

public class AddRestFragment extends Fragment {

    private FragmentAddrestBinding binding;
    EditText nameOfRest, descOfRest;
    Button btnPhotoOfRest, btnCreateRest;
    ImageView photoRest;

    FirebaseDatabase db;
    DatabaseReference rest_ref;

    Uri photoUriRest;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_addrest, container, false);

        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameOfRest = view.findViewById(R.id.nameOfRest);
        descOfRest = view.findViewById(R.id.descOfRest);
        photoRest = view.findViewById(R.id.photoRest);
        btnPhotoOfRest = view.findViewById(R.id.btnPhotoOfRest);
        btnPhotoOfRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        photoUriRest = Uri.parse("android.resource://com.example.kyshatbmozno/" + R.drawable.rest_def);

        btnCreateRest = view.findViewById(R.id.btnCreateRest);
        btnCreateRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRest();
            }
        });

        db = FirebaseDatabase.getInstance();
        rest_ref = db.getReference("Restaurant");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null){
            photoUriRest = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUriRest);
                photoRest.setImageBitmap(bitmap);
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

    public void createRest(){
        Restaurant restaurant = new Restaurant();

        String id, name, description;
        id = rest_ref.push().getKey();

        name = nameOfRest.getText().toString();
        description = descOfRest.getText().toString();

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child("Restaurants/"+id+"/photoOfRest.jpg");
        storageReference.putFile(photoUriRest).addOnCompleteListener(command -> {
           storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
               restaurant.setId(id);
               restaurant.setName(name);
               restaurant.setDescription(description);
               restaurant.setPhotoUriRest(uri.toString());

               rest_ref.child(id).setValue(restaurant);
               Toast.makeText(getView().getContext(), "Ресторан добавлен!", Toast.LENGTH_SHORT).show();
               clearFields();
           });
        });


    }

    private void clearFields(){
        nameOfRest.setText("");
        descOfRest.setText("");
        photoRest.setImageResource(R.drawable.rest_def);
    }
}