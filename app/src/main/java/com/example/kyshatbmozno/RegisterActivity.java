package com.example.kyshatbmozno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.kyshatbmozno.Models.User;

public class RegisterActivity extends AppCompatActivity {

    TextView lnkSignIn;
    EditText emailRegister, nameRegister, passwordRegister, passwordAgainRegister, phoneRegister;
    Button btnRegister;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users_ref = db.getReference("Users");

        emailRegister = (EditText)findViewById(R.id.emailRegister);
        nameRegister = (EditText)findViewById(R.id.nameRegister);
        passwordRegister = (EditText)findViewById(R.id.passwordRegister);
        passwordAgainRegister = (EditText)findViewById(R.id.passwordAgainRegister);
        phoneRegister = (EditText)findViewById(R.id.phoneRegister);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        lnkSignIn = (TextView) findViewById(R.id.lnkSignIn);
        lnkSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerNewUser(){
        if (TextUtils.isEmpty(emailRegister.getText().toString())){
            Toast.makeText(RegisterActivity.this, "Введите вашу почту", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(nameRegister.getText().toString())){
            Toast.makeText(RegisterActivity.this, "Введите ваше имя", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phoneRegister.getText().toString())||phoneRegister.getText().toString().length()<11||phoneRegister.getText().toString().length()>12) {
            Toast.makeText(RegisterActivity.this, "Введите ваш телефон корректно", Toast.LENGTH_SHORT).show();
        }
        else if(passwordRegister.getText().toString().length() < 8){
            Toast.makeText(RegisterActivity.this, "Введите пароль, имеющий больше 8 символов", Toast.LENGTH_SHORT).show();
        }
        else if (!passwordRegister.getText().toString().equals(passwordAgainRegister.getText().toString())) {
            Toast.makeText(RegisterActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
        }
        else {
            auth.createUserWithEmailAndPassword(emailRegister.getText().toString(), passwordRegister.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            String userId = auth.getCurrentUser().getUid();
                            users_ref = FirebaseDatabase.getInstance().getReference("Users");

                            String id, name, phoneNumber, email, role;
                            id = userId;
                            name = nameRegister.getText().toString();
                            phoneNumber = phoneRegister.getText().toString();
                            email = emailRegister.getText().toString();
                            role = "User";

                            User newUser = new User();
                            newUser.setId(id);
                            newUser.setPhoneNumber(phoneNumber);
                            newUser.setName(name);
                            newUser.setEmail(email);
                            newUser.setRole(role);

                            users_ref.child(userId).setValue(newUser);

                            Toast.makeText(RegisterActivity.this, "Пользователь зарегистрирован!", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification();

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
        }
    }

    private void sendEmailVar(){
        FirebaseUser user = auth.getCurrentUser();

        assert user != null;
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

}