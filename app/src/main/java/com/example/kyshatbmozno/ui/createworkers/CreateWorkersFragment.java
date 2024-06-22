package com.example.kyshatbmozno.ui.createworkers;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyshatbmozno.MainActivity;
import com.example.kyshatbmozno.Models.Restaurant;
import com.example.kyshatbmozno.Models.User;
import com.example.kyshatbmozno.R;
import com.example.kyshatbmozno.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class CreateWorkersFragment extends Fragment {

    Button btnCreateAdmin, btnCreateDeliverer;
    FirebaseAuth auth;
    FirebaseAuth auth2;
    FirebaseDatabase db;
    DatabaseReference userRef, restRef;
    ArrayAdapter<String> adapter;
    List<Restaurant> restaurants = new ArrayList<>();
    List<String> namesRests = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_workers, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        userRef = db.getReference("Users");
        restRef = db.getReference("Restaurant");

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://kyshatbmozno-default-rtdb.europe-west1.firebasedatabase.app/")
                .setApiKey("AIzaSyDGgv3mVOIS3htVAWZuuBfeoPMyK1h5Nmk")
                .setApplicationId("kyshatbmozno").build();

        try { FirebaseApp myApp = FirebaseApp.initializeApp(getActivity().getApplicationContext(), firebaseOptions, "AnyAppName");
            auth2 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e){
            auth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("AnyAppName"));
        }

        btnCreateDeliverer = view.findViewById(R.id.btnCreateDeliverer);
        btnCreateAdmin = view.findViewById(R.id.btnCreateAdmin);
        
        btnCreateAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAdminWindow();
            }
        });
        btnCreateDeliverer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDelivererWindow();
            }
        });



    }

    private void createDelivererWindow() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Добавление админа ресторана");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createAdminWindow = inflater.inflate(R.layout.create_workers, null);
        dialog.setView(createAdminWindow);

        final TextView nameRestWorker = createAdminWindow.findViewById(R.id.nameRestWorker);
        final Spinner spinRestsWorker = createAdminWindow.findViewById(R.id.spinRestsWorker);
        nameRestWorker.setVisibility(View.GONE);
        spinRestsWorker.setVisibility(View.GONE);
        final EditText emailRegisterWorker = createAdminWindow.findViewById(R.id.emailRegisterWorker);
        final EditText nameRegisterWorker = createAdminWindow.findViewById(R.id.nameRegisterWorker);
        final EditText phoneRegisterWorker = createAdminWindow.findViewById(R.id.phoneRegisterWorker);
        final EditText passwordRegisterWorker = createAdminWindow.findViewById(R.id.passwordRegisterWorker);
        final EditText passwordAgainRegisterWorker = createAdminWindow.findViewById(R.id.passwordAgainRegisterWorker);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(emailRegisterWorker.getText().toString())){
                    Toast.makeText(getActivity(), "Введите вашу почту", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(nameRegisterWorker.getText().toString())){
                    Toast.makeText(getActivity(), "Введите ваше имя", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(phoneRegisterWorker.getText().toString())||phoneRegisterWorker.getText().toString().length()<11||phoneRegisterWorker.getText().toString().length()>12) {
                    Toast.makeText(getActivity(), "Введите ваш телефон корректно", Toast.LENGTH_SHORT).show();
                }
                else if(passwordRegisterWorker.getText().toString().length() < 8){
                    Toast.makeText(getActivity(), "Введите пароль, имеющий больше 8 символов", Toast.LENGTH_SHORT).show();
                }
                else if (!passwordRegisterWorker.getText().toString().equals(passwordAgainRegisterWorker.getText().toString())) {
                    Toast.makeText(getActivity(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                }
                else{
                    auth2.createUserWithEmailAndPassword(emailRegisterWorker.getText().toString(), passwordRegisterWorker.getText().toString())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        String userId = auth2.getCurrentUser().getUid();

                                        String name, phoneNumber, email, role;
                                        name = nameRegisterWorker.getText().toString();
                                        phoneNumber = phoneRegisterWorker.getText().toString();
                                        email = emailRegisterWorker.getText().toString();
                                        role = "Deliverer";

                                        User newUser = new User();
                                        newUser.setId(userId);
                                        newUser.setPhoneNumber(phoneNumber);
                                        newUser.setName(name);
                                        newUser.setEmail(email);
                                        newUser.setRole(role);
                                        newUser.setOnOrder(false);

                                        userRef.child(userId).setValue(newUser);
                                        auth2.signOut();

                                        Toast.makeText(getActivity(), "Доставщик зарегистрирован!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }
        });

        dialog.show();

    }

    private void createAdminWindow() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Добавление админа ресторана");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View createAdminWindow = inflater.inflate(R.layout.create_workers, null);
        dialog.setView(createAdminWindow);

        final TextView nameRestWorker = createAdminWindow.findViewById(R.id.nameRestWorker);
        final Spinner spinRestsWorker = createAdminWindow.findViewById(R.id.spinRestsWorker);
        final EditText emailRegisterWorker = createAdminWindow.findViewById(R.id.emailRegisterWorker);
        final EditText nameRegisterWorker = createAdminWindow.findViewById(R.id.nameRegisterWorker);
        final EditText phoneRegisterWorker = createAdminWindow.findViewById(R.id.phoneRegisterWorker);
        final EditText passwordRegisterWorker = createAdminWindow.findViewById(R.id.passwordRegisterWorker);
        final EditText passwordAgainRegisterWorker = createAdminWindow.findViewById(R.id.passwordAgainRegisterWorker);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, namesRests);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRestsWorker.setAdapter(adapter);

        restRef.addValueEventListener(new ValueEventListener() {
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
        });

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(emailRegisterWorker.getText().toString())){
                    Toast.makeText(getActivity(), "Введите вашу почту", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(nameRegisterWorker.getText().toString())){
                    Toast.makeText(getActivity(), "Введите ваше имя", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(phoneRegisterWorker.getText().toString())||phoneRegisterWorker.getText().toString().length()<11||phoneRegisterWorker.getText().toString().length()>12) {
                    Toast.makeText(getActivity(), "Введите ваш телефон корректно", Toast.LENGTH_SHORT).show();
                }
                else if(passwordRegisterWorker.getText().toString().length() < 8){
                    Toast.makeText(getActivity(), "Введите пароль, имеющий больше 8 символов", Toast.LENGTH_SHORT).show();
                }
                else if (!passwordRegisterWorker.getText().toString().equals(passwordAgainRegisterWorker.getText().toString())) {
                    Toast.makeText(getActivity(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                }
                else{
                    auth2.createUserWithEmailAndPassword(emailRegisterWorker.getText().toString(), passwordRegisterWorker.getText().toString())
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        String userId = auth2.getCurrentUser().getUid();

                                        String name, phoneNumber, email, role, idRest;
                                        name = nameRegisterWorker.getText().toString();
                                        phoneNumber = phoneRegisterWorker.getText().toString();
                                        email = emailRegisterWorker.getText().toString();
                                        role = "AdminRestaurant";
                                        idRest = restaurants.get((int) spinRestsWorker.getSelectedItemId()).getId();

                                        User newUser = new User();
                                        newUser.setId(userId);
                                        newUser.setPhoneNumber(phoneNumber);
                                        newUser.setName(name);
                                        newUser.setEmail(email);
                                        newUser.setRole(role);
                                        newUser.setIdRest(idRest);

                                        userRef.child(userId).setValue(newUser);
                                        auth2.signOut();

                                        Toast.makeText(getActivity(), "Админ ресторана зарегистрирован!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }
        });

        dialog.show();

    }

}