package com.example.myproject;

import static android.content.ContentValues.TAG;

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

public class LoginActivity extends AppCompatActivity {

    TextInputEditText login_et_email, login_et_password;
    Button login_bt_login;
    FirebaseAuth mAuth;
    ProgressBar login_progressBar;
    TextView login_tv_registerNow;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private DB db;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
//            db.deleteAll();
            importDataFromFirebase();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViews();
        mAuth = FirebaseAuth.getInstance();
        tvSet();
        btSet();
        db = new DB(this); // 初始化 db


    }

    private void btSet() {
        login_bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = login_et_email.getText().toString();
                password = login_et_password.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                login_progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Authentication Success.",
                                            Toast.LENGTH_SHORT).show();

                                    importDataFromFirebase();


                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });
    }

    private void tvSet() {
        login_tv_registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void findViews() {
        login_et_email = findViewById(R.id.login_et_email);
        login_et_password = findViewById(R.id.login_et_password);
        login_bt_login = findViewById(R.id.login_bt_login);
        login_progressBar = findViewById(R.id.login_progressBar);
        login_tv_registerNow = findViewById(R.id.login_tv_registerNow);
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

                        goMainActivityPage();


                        Toast.makeText(LoginActivity.this, "同步成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        // Firebase 沒有資料，清空 SQLite 資料庫
                        db.deleteAll();
                        goMainActivityPage();
                        Toast.makeText(LoginActivity.this, "Firebase 無資料，已清空本地資料庫！", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, "同步失敗：" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, "無法取得使用者資訊，請重新登入。", Toast.LENGTH_SHORT).show();
        }
    }

    private void goMainActivityPage() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}