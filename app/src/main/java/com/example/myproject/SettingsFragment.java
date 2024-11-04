package com.example.myproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private ListView settings_listView;
    private String[] functions = {"刪除全部資料", "密碼修改", "將資料備份上雲端", "雲端資料匯入", "匯率查詢", "登出"};
    private int[] img = {R.drawable.baseline_delete_24, R.drawable.baseline_edit_24, R.drawable.baseline_content_copy_24
            , R.drawable.baseline_important_devices_24, R.drawable.baseline_monetization_on_24, R.drawable.baseline_delete_24};

    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private DB db;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        db = new DB(getContext());
        listSet(view);
        return view;
    }

    private void listSet(View view) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < functions.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("functions", functions[i]);
            item.put("img", img[i]);
            items.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                getContext(),
                items,
                R.layout.item_settings,
                new String[]{"img", "functions"},
                new int[]{R.id.settings_item_iv, R.id.settings_item_tv}
        );

        settings_listView = view.findViewById(R.id.settings_listView);
        settings_listView.setAdapter(adapter);
        settings_listView.setOnItemClickListener(listViewOnItemClickerListener);
    }

    private AdapterView.OnItemClickListener listViewOnItemClickerListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                // 創建確認刪除全部的對話框
                new AlertDialog.Builder(getContext())
                        .setTitle("確認刪除")
                        .setMessage("您確定要刪除嗎？\n按下確認鍵的話，僅刪除手機本地資料，如果要連雲端一起刪除，請在刪除完按下「將資料備份上雲端」！")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteAll();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // 取消操作，關閉對話框
                            }
                        })
                        // 顯示對話框
                        .show();
            } else if (i == 1) {
                showPasswordChangeDialog();
            } else if (i == 2) {
                uploadDataToFirebase();
            } else if (i == 3) {
                importDataFromFirebase();
            } else if (i == 4) {
                Toast.makeText(getContext(), "點擊第" + (i + 1) + "項\n內容：" + functions[i], Toast.LENGTH_SHORT).show();
            } else if (i == 5) {
                // 創建確認登出的對話框
                new AlertDialog.Builder(getContext())
                        .setTitle("確認登出")
                        .setMessage("您確定要登出嗎？")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth = FirebaseAuth.getInstance();
                                user = auth.getCurrentUser();
                                FirebaseAuth.getInstance().signOut();
//                                db.deleteAll();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // 取消操作，關閉對話框
                            }
                        })
                        // 顯示對話框
                        .show();
            }
        }


    };

    private void uploadDataToFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("notes").child(userId);

            // Remove existing data first
            databaseRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Cursor cursor = db.readAll();

                    // Proceed only if there is data to upload
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
                        Toast.makeText(getContext(), "資料已備份到雲端", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "SQLite 為空，雲端資料已清空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "雲端資料清空失敗", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "用戶未登入，請重新登入", Toast.LENGTH_SHORT).show();
        }
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
                        if (result) {
                            Log.e("SQLiteInsert", "資料插入失敗！");
                        } else {
                            Log.d("SQLiteInsert", "資料插入成功！");
                        }
                    }
                    Toast.makeText(getContext(), "同步成功！", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), "同步失敗：" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "無法取得使用者資訊，請重新登入。", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPasswordChangeDialog() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "用戶未登入，請重新登入後再嘗試。", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_password_change, null);
        builder.setView(dialogView);

        // 取得佈局中的元素
        EditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmNewPasswordInput = dialogView.findViewById(R.id.confirmNewPasswordInput);

        ImageView oldPasswordCheckIcon = dialogView.findViewById(R.id.oldPasswordCheckIcon);
        ImageView newPasswordCheckIcon = dialogView.findViewById(R.id.newPasswordCheckIcon);
        ImageView confirmNewPasswordCheckIcon = dialogView.findViewById(R.id.confirmNewPasswordCheckIcon);

        builder.setPositiveButton("確認", null); // 我們稍後會覆蓋這個按鈕的點擊事件
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // 獲取確認按鈕並設置點擊事件
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();

            boolean valid = true;

            // 清除之前的圖標狀態
            oldPasswordCheckIcon.setVisibility(View.GONE);
            confirmNewPasswordCheckIcon.setVisibility(View.GONE);

            // 驗證新密碼長度
            if (newPassword.length() < 6 || newPassword.length() > 30) {
                Toast.makeText(getContext(), "新密碼長度必須介於6到30位數之間", Toast.LENGTH_SHORT).show();
                newPasswordCheckIcon.setImageResource(R.drawable.ic_red_cross);
                newPasswordCheckIcon.setVisibility(View.VISIBLE);
                valid = false;
            } else {
                newPasswordCheckIcon.setImageResource(R.drawable.ic_green_check);
                newPasswordCheckIcon.setVisibility(View.VISIBLE);
            }

            // 驗證新密碼和確認新密碼是否一致
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(getContext(), "新密碼與確認密碼不一致", Toast.LENGTH_SHORT).show();
                confirmNewPasswordCheckIcon.setImageResource(R.drawable.ic_red_cross);
                confirmNewPasswordCheckIcon.setVisibility(View.VISIBLE);
                valid = false;
            } else {
                confirmNewPasswordCheckIcon.setImageResource(R.drawable.ic_green_check);
                confirmNewPasswordCheckIcon.setVisibility(View.VISIBLE);
            }

            if (!valid) {
                return; // 如果驗證不通過，則不繼續
            }

            if (oldPassword.isEmpty()) {
                Toast.makeText(getContext(), "舊密碼不可為空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 進行重新驗證
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // 驗證成功，顯示綠色勾勾
                    oldPasswordCheckIcon.setImageResource(R.drawable.ic_green_check);
                    oldPasswordCheckIcon.setVisibility(View.VISIBLE);

                    // 更新密碼
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(getContext(), "密碼修改成功！", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // 密碼修改成功，關閉對話框
                        } else {
                            Toast.makeText(getContext(), "密碼修改失敗：" + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 驗證失敗，顯示紅色叉叉
                    oldPasswordCheckIcon.setImageResource(R.drawable.ic_red_cross);
                    oldPasswordCheckIcon.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "舊密碼錯誤，請重新輸入", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 設置監聽器，監測新密碼和確認新密碼的變更情況
        newPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String newPwd = s.toString().trim();
                if (newPwd.length() >= 6 && newPwd.length() <= 30) {
                    newPasswordCheckIcon.setImageResource(R.drawable.ic_green_check);
                } else {
                    newPasswordCheckIcon.setImageResource(R.drawable.ic_red_cross);
                }
                newPasswordCheckIcon.setVisibility(View.VISIBLE);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        confirmNewPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String confirmPwd = s.toString().trim();
                String newPwd = newPasswordInput.getText().toString().trim();
                if (confirmPwd.equals(newPwd) && !confirmPwd.isEmpty()) {
                    confirmNewPasswordCheckIcon.setImageResource(R.drawable.ic_green_check);
                } else {
                    confirmNewPasswordCheckIcon.setImageResource(R.drawable.ic_red_cross);
                }
                confirmNewPasswordCheckIcon.setVisibility(View.VISIBLE);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }


}