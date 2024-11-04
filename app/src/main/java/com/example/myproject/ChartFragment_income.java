package com.example.myproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChartFragment_income extends Fragment {

    private PieChart pieChart_income;
    private TextView chart_income_tv;
    private DB db;
    private Button pieChart_bt_money, pieChart_bt_percentage;
    private boolean showInMoney = true; // 初始狀態為現金金額顯示

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart_outcome, container, false);
        findViews(view);
        db = new DB(getContext());  // 確保在使用前初始化 db
        setCurrentTime();
        tvSet();
        btSet();
        getData(chart_income_tv.getText().toString());
        return view;
    }

    private void btSet() {
        pieChart_bt_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInMoney = true;
                pieChart_bt_money.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.selected_item_color));
                pieChart_bt_percentage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.unselected_item_color));
                getData(chart_income_tv.getText().toString()); // 重新顯示資料
            }
        });

        pieChart_bt_percentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInMoney = false;
                pieChart_bt_percentage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.selected_item_color));
                pieChart_bt_money.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.unselected_item_color));
                getData(chart_income_tv.getText().toString()); // 重新顯示資料
            }
        });
    }

    private void tvSet() {
        chart_income_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMonthYearPicker();
            }
        });
    }

    private void findViews(View view) {
        chart_income_tv = view.findViewById(R.id.chart_outcome_tv);
        pieChart_income = view.findViewById(R.id.pieChart_outcome);
        pieChart_bt_percentage = view.findViewById(R.id.pieChart_bt_percentage);
        pieChart_bt_money = view.findViewById(R.id.pieChart_bt_money);
    }

    private void setCurrentTime() {
        // 獲取當前的日期時間
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM", Locale.getDefault());
        String currentTime = dateFormat.format(calendar.getTime());

        // 將當前的日期設置到 bills_tv_time
        chart_income_tv.setText(currentTime);
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
                chart_income_tv.setText(selectedYearMonth); // 更新 TextView 顯示的年月

                getData(chart_income_tv.getText().toString());

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getData(String selectedYearMonth) {
        Cursor c = db.read(selectedYearMonth); // 使用選定的年月讀取資料
        Map<String, Integer> expenseMap = new HashMap<>(); // 儲存每個 type 的支出總額

        // 初始化各類型的支出
        expenseMap.put("薪水", 0);
        expenseMap.put("投資", 0);
        expenseMap.put("其它", 0);


        if (c.moveToFirst()) {
            do {
                @SuppressLint("Range") int money = c.getInt(c.getColumnIndex("money"));
                @SuppressLint("Range") String come = c.getString(c.getColumnIndex("come"));
                @SuppressLint("Range") String type = c.getString(c.getColumnIndex("type"));

                if ("收入".equals(come)) {  // 假設"come"欄位標示收入/支出
                    // 將每種類型的支出加總
                    if (expenseMap.containsKey(type)) {
                        expenseMap.put(type, expenseMap.get(type) + money);
                    }
                }

            } while (c.moveToNext());
        }
        c.close();

        displayPieChart(expenseMap);
    }

    private void displayPieChart(Map<String, Integer> expenseMap) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        float totalExpenses = 0;

        for (Map.Entry<String, Integer> entry : expenseMap.entrySet()) {
            int amount = entry.getValue();
            if (amount > 0) {  // 只添加有收入的類別
                entries.add(new PieEntry(amount, entry.getKey()));
                totalExpenses += amount;
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // 設置每個片段的顏色
        dataSet.setColors(new int[]{
                ContextCompat.getColor(getContext(), R.color.salary),
                ContextCompat.getColor(getContext(), R.color.invest),
                ContextCompat.getColor(getContext(), R.color.other)
        });

        // 設置片段的數值顯示樣式
        dataSet.setValueTextColor(Color.BLACK);  // 設置數值字體顏色
        dataSet.setValueTextSize(18f);           // 設置數值字體大小


        float finalTotalExpenses = totalExpenses;
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                if (showInMoney) {
                    return (int) value + ""; // 現金數字顯示
                } else {
                    float percentage = (value / finalTotalExpenses) * 100;
                    return String.format(Locale.getDefault(), "%.1f%%", percentage); // 百分比顯示
                }
            }
        });

        // 設置 PieChart 的圖例 (Legend) 樣式
        Legend legend = pieChart_income.getLegend();
        legend.setTextColor(Color.BLACK);  // 設置圖例文字顏色
        legend.setTextSize(18f);           // 設置圖例文字大小
        legend.setForm(Legend.LegendForm.CIRCLE);  // 設置圖例形狀
        legend.setFormSize(18f);           // 設置圖例的大小

        // 將圖例位置設置在圖表正下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false); // 確保圖例顯示在圖表外部

        pieChart_income.getDescription().setEnabled(true);
        pieChart_income.getDescription().setText("收入分析圖"); // 設定自訂描述文字
        pieChart_income.getDescription().setTextSize(16f);     // 設定文字大小
        pieChart_income.getDescription().setTextColor(Color.BLACK); // 設定文字顏色



        // 設置 PieData 並刷新圖表
        PieData data = new PieData(dataSet);
        pieChart_income.setData(data);
        pieChart_income.setEntryLabelColor(Color.BLACK); // 設置標籤文字顏色
        pieChart_income.setEntryLabelTextSize(12f);      // 設置標籤文字大小

        // 在圓餅圖中心顯示總支出
        pieChart_income.setCenterText("總計\n$ " + (int) totalExpenses); // 設置總支出文字
        pieChart_income.setCenterTextSize(24f); // 設置中心文字大小
        pieChart_income.setCenterTextColor(Color.BLACK); // 設置中心文字顏色

        pieChart_income.invalidate();  // 刷新圖表
    }


}