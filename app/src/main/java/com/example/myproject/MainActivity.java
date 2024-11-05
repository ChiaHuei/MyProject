package com.example.myproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.myproject.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseUser user;
    FirebaseAuth auth;
    private DB db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d("@@@", "user.getEmail() = " + user.getEmail());
        }

        db = new DB(this); // 初始化 db



        // 檢查是否有指定目標 Fragment
        String targetFragment = getIntent().getStringExtra("targetFragment");
        if ("SettingsFragment".equals(targetFragment)) {
            // 設定 BottomNavigationView 選擇 Settings 的項目
            binding.bottomNav.setSelectedItemId(R.id.settings);
            replaceFragment(new SettingsFragment()); // 顯示 SettingsFragment
        } else {
            // 預設顯示 BillsFragment
            replaceFragment(new BillsFragment());
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bills){
                replaceFragment(new BillsFragment());
            } else if (id == R.id.chart) {
                replaceFragment(new ChartFragment());
            } else if (id == R.id.settings) {
                replaceFragment(new SettingsFragment());
            }
            return true;
        });



    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}