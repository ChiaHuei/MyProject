package com.example.myproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BillsFragment extends Fragment {
    private FloatingActionButton fab_add;
    DB db;
    long id;
    ListView lv;
    TextView bills_tv_time, bills_tv_month_income, bills_tv_month_outcome, bills_tv_month_totalcome, bills_tv_day_income, bills_tv_day_outcome, bills_tv_day_totalcome;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bills, container, false);

        db = new DB(getContext());  // 確保使用當前的 Context 來初始化 DB
//        db.deleteAll();
//        importDataFromFirebase();
        findViews(view);
        btSet();
        setCurrentTime(); // 設置當前時間
        lvSet();
        showData(bills_tv_time.getText().toString());
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onResume() {
        super.onResume();
        showData(bills_tv_time.getText().toString());
    }



    private void lvSet() {
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 獲取被長按的項目
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                @SuppressLint("Range") String selectedItem = cursor.getString(cursor.getColumnIndex("_id"));

                // 創建選項對話框
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("選擇操作")
                        .setItems(new CharSequence[]{"編輯", "刪除", "取消"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // 編輯
                                        editItem(selectedItem);
                                        break;
                                    case 1: // 刪除
                                        deleteItem(selectedItem);
                                        break;
                                    case 2: // 取消
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        })
                        .create();
                dialog.show(); // 顯示對話框
                return true; // 返回 true 以表示事件已被處理
            }
        });

    }

    // 編輯項目的方法
    private void editItem(String itemId) {
        // 查詢資料庫以獲取要編輯的項目資料
        Cursor cursor = db.getItemById(itemId); // 假設這個方法會根據ID返回資料
        if (cursor != null && cursor.moveToFirst()) {
            // 獲取所需的數據，例如money、come、date、type、note等
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("_id"));
            @SuppressLint("Range") int money = cursor.getInt(cursor.getColumnIndex("money"));
            @SuppressLint("Range") String come = cursor.getString(cursor.getColumnIndex("come"));
            @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
            @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
            @SuppressLint("Range") String note = cursor.getString(cursor.getColumnIndex("note"));

            // 使用Intent傳遞數據
            Intent intent = new Intent(getActivity(), AddbillActivity.class);
            intent.putExtra("_id", id);
            intent.putExtra("money", money);
            intent.putExtra("come", come);
            intent.putExtra("date", date);
            intent.putExtra("type", type);
            intent.putExtra("note", note);

            startActivity(intent); // 跳轉到AddbillActivity
        } else {
            Toast.makeText(getContext(), "無法找到該項目", Toast.LENGTH_SHORT).show();
        }
    }


    // 刪除項目的方法
// 刪除項目的方法
    private void deleteItem(String itemId) {
        // 創建確認刪除的對話框
        new AlertDialog.Builder(getContext())
                .setTitle("確認刪除")
                .setMessage("您確定要刪除這個項目嗎？")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 查詢資料庫以獲取要刪除的項目資料
                        Cursor cursor = db.getItemById(itemId); // 假設這個方法會根據ID返回資料
                        if (cursor != null && cursor.moveToFirst()) {
                            // 獲取所需的數據，例如money、come、date、type、note等
                            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex("_id"));
                            db.delete(id);
                            showData(bills_tv_time.getText().toString());
                            Toast.makeText(getContext(), "刪除成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            showData(bills_tv_time.getText().toString());
                            Toast.makeText(getContext(), "無法找到該項目", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 取消操作，關閉對話框
                    }
                })
                .show(); // 顯示對話框
    }


    private void setCurrentTime() {
        // 獲取當前的日期時間
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM", Locale.getDefault());
        String currentTime = dateFormat.format(calendar.getTime());

        // 將當前的日期設置到 bills_tv_time
        bills_tv_time.setText(currentTime);
    }

    @SuppressLint("SetTextI18n")
    private void showData(String selectedYearMonth) {
        Cursor c = db.read(selectedYearMonth); // 使用選定的年月讀取資料
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_bill,
                c,
                new String[]{"come", "date", "type", "note", "money"},
                new int[]{R.id.bills_tv_come, R.id.bills_tv_date_all, R.id.bills_tv_type, R.id.bills_tv_note, R.id.bills_tv_money}
        );
        lv.setAdapter(adapter); // 更新 ListView

        // 初始化月收入、月支出和月總計
        int monthIncome = 0;
        int monthOutcome = 0;

        // 初始化日收入、日支出和日總計
        int dayIncome = 0;
        int dayOutcome = 0;

        // 獲取當前日期（用於日統計）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime()); // 當前日期（yyyy/MM/dd）

        // 遍歷Cursor來統計收入和支出
        if (c.moveToFirst()) {
            do {
                @SuppressLint("Range") int money = c.getInt(c.getColumnIndex("money"));
                @SuppressLint("Range") String come = c.getString(c.getColumnIndex("come"));
                @SuppressLint("Range") String date = c.getString(c.getColumnIndex("date")); // 假設"date"是日期欄位 (yyyy/MM/dd)

                // 統計月收入和支出
                if ("收入".equals(come)) {  // 假設"come"欄位標示收入/支出
                    monthIncome += money;
                } else if ("支出".equals(come)) {
                    monthOutcome += money;
                }

                // 統計當天的收入和支出
                if (date.equals(currentDate)) {
                    if ("收入".equals(come)) {
                        dayIncome += money;
                    } else if ("支出".equals(come)) {
                        dayOutcome += money;
                    }
                }

            } while (c.moveToNext());
        }

        // 設置月收入、支出和總計到TextView
        bills_tv_month_income.setText("$"+ monthIncome);
        bills_tv_month_outcome.setText("$"+ monthOutcome);
        bills_tv_month_totalcome.setText("$"+ (monthIncome - monthOutcome)); // 總計 = 收入 - 支出

        // 設置日收入、支出和總計到TextView
        bills_tv_day_income.setText("$"+ dayIncome);
        bills_tv_day_outcome.setText("$"+ dayOutcome);
        bills_tv_day_totalcome.setText("$"+ (dayIncome - dayOutcome)); // 總計 = 收入 - 支出
    }




    private void btSet() {
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddbillActivity.class);
                startActivity(intent);
            }
        });

        bills_tv_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMonthYearPicker();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

    }
    private void findViews(View view) {
        fab_add = view.findViewById(R.id.fab_add);

        bills_tv_time = view.findViewById(R.id.bills_tv_time);

        bills_tv_month_income = view.findViewById(R.id.bills_tv_month_income);
        bills_tv_month_outcome = view.findViewById(R.id.bills_tv_month_outcome);
        bills_tv_month_totalcome = view.findViewById(R.id.bills_tv_month_totalcome);
        bills_tv_day_income = view.findViewById(R.id.bills_tv_day_income);
        bills_tv_day_outcome = view.findViewById(R.id.bills_tv_day_outcome);
        bills_tv_day_totalcome = view.findViewById(R.id.bills_tv_day_totalcome);

        lv = view.findViewById(R.id.lv);

    }

    public void showMonthYearPicker() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;  // 月份是0索引，所以+1

        final Dialog dialog = new Dialog(getContext());
        dialog.setTitle("選擇年月");
        dialog.setContentView(R.layout.month_year_picker_dialog); // 自定義的布局

        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);
        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);

        // 設置年份範圍
        yearPicker.setMinValue(currentYear - 50); // 最早50年
        yearPicker.setMaxValue(currentYear + 50); // 最後50年
        yearPicker.setValue(currentYear); // 當前年

        // 設置月份範圍
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(currentMonth); // 當前月

        // 當點擊確定時，將選擇的年月設置到 TextView，並顯示當前月份資料
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedYear = yearPicker.getValue();
                int selectedMonth = monthPicker.getValue();
                String selectedYearMonth = String.format("%04d/%02d", selectedYear, selectedMonth);
                bills_tv_time.setText(selectedYearMonth); // 更新 TextView 顯示的年月

                // 根據選定的年月顯示相應的資料
                showData(selectedYearMonth);

                dialog.dismiss();
            }
        });

        dialog.show();
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

                        showData(bills_tv_time.getText().toString());

                        Toast.makeText(getActivity(), "同步成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        // Firebase 沒有資料，清空 SQLite 資料庫
                        db.deleteAll();
                        showData(bills_tv_time.getText().toString());
                        Toast.makeText(getActivity(), "Firebase 無資料，已清空本地資料庫！", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "同步失敗：" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "無法取得使用者資訊，請重新登入。", Toast.LENGTH_SHORT).show();
        }
    }




}