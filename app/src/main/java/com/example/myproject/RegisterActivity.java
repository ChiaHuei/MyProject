package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText register_et_email, register_et_password;
    Button register_bt_register;
    FirebaseAuth mAuth;
    ProgressBar register_progressBar;
    TextView register_tv_loginNow;
    private FirebaseUser user;
    private DatabaseReference databaseRef;
    private DB db;

    @Override
    public void onStart() {
        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            importDataFromFirebase();
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//            finish();
//        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();
        mAuth = FirebaseAuth.getInstance();
        btSet();
        tvSet();
        db = new DB(this); // 初始化 db

    }

    private void tvSet() {
        register_tv_loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void btSet() {
        register_bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = register_et_email.getText().toString();
                password = register_et_password.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    register_progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Authentication created.", Toast.LENGTH_SHORT).show();
                                    // 註冊完成，將訪客原本 sqlite 資料備份上 Firebase
                                    uploadDataToFirebase();

                                } else {
                                    register_progressBar.setVisibility(View.GONE);
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Toast.makeText(RegisterActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    Log.e("RegisterActivity", "Error: " + errorMessage);
                                }

                            }
                        });
            }
        });


    }

    private void findViews() {
        register_et_email = findViewById(R.id.register_et_email);
        register_et_password = findViewById(R.id.register_et_password);
        register_bt_register = findViewById(R.id.register_bt_register);
        register_progressBar = findViewById(R.id.register_progressBar);
        register_tv_loginNow = findViewById(R.id.register_tv_loginNow);

    }

    private void uploadDataToFirebase() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("notes").child(userId);

            databaseRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Cursor cursor = db.readAll();
                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                            String come = cursor.getString(cursor.getColumnIndexOrThrow("come"));
                            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                            int money = cursor.getInt(cursor.getColumnIndexOrThrow("money"));
                            String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                            Map<String, Object> data = new HashMap<>();
                            data.put("come", come);
                            data.put("type", type);
                            data.put("money", money);
                            data.put("note", note);
                            data.put("date", date);

                            // Upload data to Firebase
                            databaseRef.child(id).setValue(data);
                        }
                        Toast.makeText(RegisterActivity.this, "資料已備份到雲端", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "SQLite 為空，雲端資料已清空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "雲端資料清空失敗", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(RegisterActivity.this, "用戶未登入，請重新登入", Toast.LENGTH_SHORT).show();
        }
    }

}