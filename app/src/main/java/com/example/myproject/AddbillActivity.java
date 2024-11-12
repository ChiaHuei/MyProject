package com.example.myproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddbillActivity extends AppCompatActivity {
    private Button add_bt_income, add_bt_outcome, add_bt_new;
    private Spinner add_spinner_add_type;
    private LinearLayout add_bt_back;
    private EditText add_et_cost, add_et_content;
    private TextView add_tv_date;
    private String comeType = "支出", costType = "食";
    private DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbill);
        db = new DB(this);  // 確保使用當前的 Context 來初始化 DB
        findViews();
        getCurrentDate();
        btSet();
        spinnerIncomeSet();
        getData();
    }

    private void showDatePickerDialog() {
        // 獲取當前日期
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 創建 DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // 當用戶選擇日期後，更新 add_tv_date 的文本
                        String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay; // 月份從0開始
                        add_tv_date.setText(selectedDate);
                    }
                }, year, month, day);

        // 顯示對話框
        datePickerDialog.show();
    }

    private void getCurrentDate() {
        // 取得當前日期
        Calendar calendar = Calendar.getInstance();
        // 設定日期格式為 "yyyy/MM/dd"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        // 將日期格式化為字串
        Log.d("@@@", dateFormat.format(calendar.getTime()));
        String currentDate = dateFormat.format(calendar.getTime());
        add_tv_date.setText(currentDate);
    }

    private void getData() {
        Intent intent = getIntent();
        long id = intent.getLongExtra("_id", -1);

        if (id != -1) {
            // 如果 id 有效，則從資料庫獲取資料
            add_bt_new.setText("修改");
            Cursor cursor = db.getItemById(String.valueOf(id));
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String come = cursor.getString(cursor.getColumnIndex("come"));
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                @SuppressLint("Range") int money = cursor.getInt(cursor.getColumnIndex("money"));
                @SuppressLint("Range") String note = cursor.getString(cursor.getColumnIndex("note"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));

                // 填充資料到相應的視圖
                add_et_cost.setText(String.valueOf(money));
                add_et_content.setText(note);
                add_tv_date.setText(date);

                if ("收入".equals(come)) {
                    comeType = "收入";
                    add_bt_income.setBackgroundTintList(getResources().getColorStateList(R.color.selected_item_color));
                    add_bt_outcome.setBackgroundTintList(getResources().getColorStateList(R.color.unselected_item_color));
                    spinnerIncomeSet();
                } else {
                    comeType = "支出";
                    add_bt_income.setBackgroundTintList(getResources().getColorStateList(R.color.unselected_item_color));
                    add_bt_outcome.setBackgroundTintList(getResources().getColorStateList(R.color.selected_item_color));
                    spinnerOutcomeSet();
                }

                // 設定 Spinner 的選項
                if (type != null && !type.isEmpty()) {
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) add_spinner_add_type.getAdapter();
                    int position = adapter.getPosition(type);
                    if (position >= 0) {
                        add_spinner_add_type.setSelection(position);
                    }
                }
            }
        } else {
            // id 為 -1，表示是新增操作，這裡可以根據需要設置默認值
            add_et_cost.setText("");
            add_et_content.setText("");
            add_tv_date.setText("");  // 或者使用 getCurrentDate() 方法來設置當前日期
            getCurrentDate();  // 設定為當前日期
            spinnerOutcomeSet();
        }
    }

    private void spinnerIncomeSet() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,                               // 對應的 context
                R.array.add_type_income,                          // 資料選項內容，在 string.xml 內定義
                android.R.layout.simple_spinner_item       // 預設 Spinner 未展開時的 View
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        add_spinner_add_type.setAdapter(adapter);

        add_spinner_add_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 選項選取時動作
                String select = String.valueOf(i);                                      // i 代表第幾項，從 0 開始
                String selectInfo = adapterView.getItemAtPosition(i).toString();        // getItemAtPosition(i) 代表取值
                costType = selectInfo;
//                Toast.makeText(AddbillActivity.this, "select:" + select + "\nselectInfo:" + selectInfo, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 選項未選取時動作
            }
        });
    }

    private void spinnerOutcomeSet() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,                               // 對應的 context
                R.array.add_type_outcome,                  // 資料選項內容，在 string.xml 內定義
                android.R.layout.simple_spinner_item       // 預設 Spinner 未展開時的 View
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        add_spinner_add_type.setAdapter(adapter);

        add_spinner_add_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 選項選取時動作
                String select = String.valueOf(i);                                      // i 代表第幾項，從 0 開始
                String selectInfo = adapterView.getItemAtPosition(i).toString();        // getItemAtPosition(i) 代表取值
                costType = selectInfo;
//                Toast.makeText(AddbillActivity.this, "select:" + select + "\nselectInfo:" + selectInfo, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // 選項未選取時動作
            }
        });
    }

    private void btSet() {
        add_bt_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comeType = "收入";
                add_bt_outcome.setBackgroundTintList(getResources().getColorStateList(R.color.unselected_item_color));
                add_bt_income.setBackgroundTintList(getResources().getColorStateList(R.color.selected_item_color));
                spinnerIncomeSet();

            }
        });

        add_bt_outcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comeType = "支出";
                add_bt_outcome.setBackgroundTintList(getResources().getColorStateList(R.color.selected_item_color));
                add_bt_income.setBackgroundTintList(getResources().getColorStateList(R.color.unselected_item_color));
                spinnerOutcomeSet();
            }
        });

        add_bt_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String come = (comeType != null) ? comeType : "";  // 檢查 comeType 是否為 null，若為 null 則設置為空字串
                String type = (costType != null) ? costType : "";  // 檢查 costType 是否為 null，若為 null 則設置為空字串
                String costText = add_et_cost.getText().toString();
                int money = 0;  // 初始化
                if (!costText.isEmpty()) {
                    money = Integer.parseInt(costText);  // 只有在非空的情況下才進行解析
                }

                String note = add_et_content.getText().toString();
                String date = add_tv_date.getText().toString();

                // 檢查 money、come 和 note 是否為空
                if (come.isEmpty() || type.isEmpty() || note.isEmpty() || costText.isEmpty()) {
                    Toast.makeText(AddbillActivity.this, "所有欄位不能為空！", Toast.LENGTH_SHORT).show();
                    return; // 如果有欄位為空，則返回，不執行後續操作
                }

                // 確保日期格式正確
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                try {
                    date = dateFormat.format(dateFormat.parse(date));  // 確保日期為 yyyy/MM/dd 格式
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent getIntent = getIntent();
                long id = getIntent.getLongExtra("_id", -1);

                Intent intent = new Intent(AddbillActivity.this, MainActivity.class);

                if (come.isEmpty() && type.isEmpty() && costText.isEmpty() && note.isEmpty()) {
                    startActivity(intent);  // 如果所有欄位皆為空，則返回主畫面
                } else {
                    if (id == -1) {
                        db.create(come, type, money, note, date);  // 新增資料
                    } else {
                        db.update(id, come, type, money, note, date);  // 更新資料
                    }
                    startActivity(intent);  // 完成後返回主畫面
                }
            }
        });



        add_bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        add_tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }

    private void findViews() {
        add_bt_income = findViewById(R.id.add_bt_income);
        add_bt_outcome = findViewById(R.id.add_bt_outcome);
        add_bt_new = findViewById(R.id.add_bt_new);
        add_bt_back = findViewById(R.id.add_bt_back);

        add_et_cost = findViewById(R.id.add_et_cost);
        add_et_content = findViewById(R.id.add_et_content);

        add_tv_date = findViewById(R.id.add_tv_date);

        add_spinner_add_type = findViewById(R.id.add_spinner_add_type);
    }
}