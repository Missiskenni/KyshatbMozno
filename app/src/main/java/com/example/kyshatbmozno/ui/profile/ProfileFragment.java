package com.example.kyshatbmozno.ui.profile;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kyshatbmozno.Models.Card;
import com.example.kyshatbmozno.Models.Food;
import com.example.kyshatbmozno.Models.User;
import com.example.kyshatbmozno.OrdersActivity;
import com.example.kyshatbmozno.R;
import com.example.kyshatbmozno.RegisterActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    FirebaseDatabase db;
    DatabaseReference user_ref;
    FirebaseAuth auth;
    FirebaseUser fUser;

    Button changeInfo, changePass, addAddress, addCard, btnViewOrders;
    TextView yourEmail, yourName, yourPhone, yourAddress, yourCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        yourEmail = view.findViewById(R.id.yourEmail);
        yourName = view.findViewById(R.id.yourName);
        yourPhone = view.findViewById(R.id.yourPhone);
        yourAddress = view.findViewById(R.id.yourAddress);
        yourCard = view.findViewById(R.id.yourCard);
        changeInfo = view.findViewById(R.id.btnChangeInfo);
        changePass = view.findViewById(R.id.btnChangePass);
        addAddress = view.findViewById(R.id.btnAddAddress);
        addCard = view.findViewById(R.id.btnAddCard);
        btnViewOrders = view.findViewById(R.id.btnViewOrders);

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        user_ref = db.getReference("Users").child(fUser.getUid());

        btnViewOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OrdersActivity.class);
                startActivity(intent);
            }
        });
        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAddress();
            }
        });
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCard();
            }
        });
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePass();
            }
        });
        changeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeWindow();
            }
        });

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                Card card = snapshot.child("card").getValue(Card.class);

                yourEmail.setText(email);
                yourName.setText(name);
                yourPhone.setText(phoneNumber);
                if (address == null || address.isEmpty()) yourAddress.setText("добавьте свой адрес!");
                else {
                    yourAddress.setText(address);
                    addAddress.setVisibility(View.GONE);
                }
                if (card == null || card.getNumber().isEmpty() || card.getData().isEmpty()) yourCard.setText("добавьте данные вашей карты");
                else {
                    yourCard.setText(card.getNumber()+" "+card.getData());
                    addCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showChangeWindow(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Изменение данных");
        dialog.setMessage("Введите данные");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View changeInfoWindow = inflater.inflate(R.layout.change_userdata, null);
        dialog.setView(changeInfoWindow);

        final EditText changeName = changeInfoWindow.findViewById(R.id.changeName);
        final EditText changePhone = changeInfoWindow.findViewById(R.id.changePhone);
        final EditText changeAddress = changeInfoWindow.findViewById(R.id.changeAddress);
        final EditText changeCardNumber = changeInfoWindow.findViewById(R.id.changeCardNumber);
        final EditText changeCardData = changeInfoWindow.findViewById(R.id.changeCardData);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        changeCardData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog;
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String date;
                        String strYear = String.valueOf(year);
                        String goodYear = strYear.substring(strYear.length()-2);
                        if (month<10) date = "0" + month + "/" + goodYear;
                        else date = month + "/" + goodYear;
                        changeCardData.setText(date);
                    }
                };
                datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth,
                        listener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                Card card = snapshot.child("card").getValue(Card.class);

                changeName.setText(name);
                changePhone.setText(phoneNumber);
                changeAddress.setText(address);
                changeCardNumber.setText(card.getNumber());
                changeCardData.setText(card.getData());
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
        dialog.setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = changeName.getText().toString();
                String phoneNumber = changePhone.getText().toString();
                String address = changeAddress.getText().toString();
                String number = changeCardNumber.getText().toString();
                String data = changeCardData.getText().toString();

                Card card = new Card();
                card.setData(data);
                card.setNumber(number);

                if (name.isEmpty()){
                    Toast.makeText(getActivity(), "Введите ваше имя", Toast.LENGTH_SHORT).show();
                }
                else if(phoneNumber.isEmpty() || phoneNumber.length() < 11 || phoneNumber.length() > 12){
                    Toast.makeText(getActivity(), "Введите телефон корректно", Toast.LENGTH_SHORT).show();
                }
                else if(address.isEmpty()){
                    Toast.makeText(getActivity(), "Введите ваш адрес", Toast.LENGTH_SHORT).show();
                }
                else if(number.length() != 16){
                    Toast.makeText(getActivity(), "Введите номер карты корректно", Toast.LENGTH_SHORT).show();
                }
                else if(data.length() != 5){
                    Toast.makeText(getActivity(), "Введите дату карты корректно", Toast.LENGTH_SHORT).show();
                }
                else {
                    user_ref.child("name").setValue(name);
                    user_ref.child("phoneNumber").setValue(phoneNumber);
                    user_ref.child("address").setValue(address);
                    user_ref.child("card").setValue(card);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void showChangePass(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Изменение пароля");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View changePassWindow = inflater.inflate(R.layout.change_password, null);
        dialog.setView(changePassWindow);

        final EditText enterOldPassword = changePassWindow.findViewById(R.id.enterOldPassword);
        final EditText enterNewPassword = changePassWindow.findViewById(R.id.enterNewPassword);
        final EditText commitNewPassword = changePassWindow.findViewById(R.id.commitNewPassword);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Сохранить пароль", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPass = enterOldPassword.getText().toString();
                String newPass = enterNewPassword.getText().toString();
                String comNewPass = commitNewPassword.getText().toString();

                if (TextUtils.isEmpty(oldPass)){
                    Toast.makeText(getActivity(), "Введите ваш старый пароль...", Toast.LENGTH_SHORT).show();
                }
                else if (newPass.length()<6){
                    Toast.makeText(getActivity(), "Новый пароль должен иметь минимум 6 символов...", Toast.LENGTH_SHORT).show();
                }
                else if (!newPass.equals(comNewPass)){
                    Toast.makeText(getActivity(), "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
                }else {
                    dialog.dismiss();
                    updatePassword(oldPass, newPass);
                }
            }
        });

        dialog.show();
    }

    private void showAddAddress(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Добавление адреса");
        dialog.setMessage("Введите адрес");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View addAddressWindow = inflater.inflate(R.layout.add_address, null);
        dialog.setView(addAddressWindow);

        final EditText addAddress = addAddressWindow.findViewById(R.id.addAddress);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = addAddress.getText().toString();

                if (address.isEmpty()){
                    Toast.makeText(getActivity(), "Введите ваш адрес", Toast.LENGTH_SHORT).show();
                }
                else {
                    user_ref.child("address").setValue(address);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void showAddCard(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Добавление карты");
        dialog.setMessage("Введите данные карты");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View addCardWindow = inflater.inflate(R.layout.add_card, null);
        dialog.setView(addCardWindow);

        final EditText cardNumber = addCardWindow.findViewById(R.id.cardNumber);
        final EditText cardData = addCardWindow.findViewById(R.id.cardData);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        cardData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog;
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String date;
                        String strYear = String.valueOf(year);
                        String goodYear = strYear.substring(strYear.length()-2);
                        if (month<10) date = "0" + month + "/" + goodYear;
                        else date = month + "/" + goodYear;
                        cardData.setText(date);
                    }
                };
                datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth,
                        listener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("Добавить карту", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Card card = new Card();
                String number = cardNumber.getText().toString();
                String data = cardData.getText().toString();

                if(number.length() != 16){
                    Toast.makeText(getActivity(), "Введите номер карты корректно", Toast.LENGTH_SHORT).show();
                }
                else if(data.length() != 5){
                    Toast.makeText(getActivity(), "Введите дату карты корректно", Toast.LENGTH_SHORT).show();
                }
                else {
                    card.setNumber(number);
                    card.setData(data);

                    user_ref.child("card").setValue(card);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void updatePassword(String oldPass, String newPass) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = auth.getCurrentUser().getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = firebaseDatabase.getReference("Users").child(userId);

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        myRef.child("password").setValue(newPass);
                        Toast.makeText(getActivity(), "Пароль изменен!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}