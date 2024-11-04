package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            importDataFromFirebase();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }


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

                                    importDataFromFirebase();


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

    private void importDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("notes").child(userId);

            // 讀取 Firebase 中的 notes 資料
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        db.deleteAll(); // 清除本地 SQLite 資料庫的舊資料
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String come = snapshot.child("come").getValue(String.class);
                            String type = snapshot.child("type").getValue(String.class);
                            Integer money = snapshot.child("money").getValue(Integer.class);
                            String note = snapshot.child("note").getValue(String.class);
                            String date = snapshot.child("date").getValue(String.class);

                            // Log 檢查 Firebase 中的資料
                            Log.d("FirebaseData", "come: " + come + ", type: " + type + ", money: " + money + ", note: " + note + ", date: " + date);

                            // 將從 Firebase 取得的資料插入 SQLite
                            boolean result = db.create(come, type, money, note, date);
                            if (!result) {
                                Log.e("SQLiteInsert", "資料插入失敗！");
                            } else {
                                Log.d("SQLiteInsert", "資料插入成功！");
                            }
                        }
                        Toast.makeText(RegisterActivity.this, "同步成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        // Firebase 沒有資料，清空 SQLite 資料庫
                        db.deleteAll();
                        Toast.makeText(RegisterActivity.this, "Firebase 無資料，已清空本地資料庫！", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(RegisterActivity.this, "同步失敗：" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(RegisterActivity.this, "無法取得使用者資訊，請重新登入。", Toast.LENGTH_SHORT).show();
        }
    }

}