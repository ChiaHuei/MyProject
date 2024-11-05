package com.example.myproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RateConverterActivity extends AppCompatActivity {

    private EditText inputAmount;
    private TextView resultAmount;
    private Button convertButton;
    private Spinner sourceCurrency, targetCurrency;
    private OkHttpClient client = new OkHttpClient();
    private List<String> currenciesList = new ArrayList<>();
    private Map<String, String> currenciesMap = new HashMap<>(); // 保存貨幣與國家的對應關係
    private LinearLayout rate_bt_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_converter);

        findViews();
        // 請求所有幣值
        fetchAllCurrencies();
        btSet();
    }

    private void btSet() {
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = inputAmount.getText().toString();
                String fromCurrency = getCurrencyCodeFromSelectedItem(sourceCurrency.getSelectedItem().toString());
                String toCurrency = getCurrencyCodeFromSelectedItem(targetCurrency.getSelectedItem().toString());

                if (!amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);
                    fetchExchangeRate(amount, fromCurrency, toCurrency);
                } else {
                    Toast.makeText(RateConverterActivity.this, "請輸入金額", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rate_bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RateConverterActivity.this, MainActivity.class);
                intent.putExtra("targetFragment", "SettingsFragment"); // 添加目標 Fragment 的訊息
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchAllCurrencies() {
        String url = "https://api.exchangerate-api.com/v4/latest/USD"; // 可以選擇任意貨幣作為基準
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(RateConverterActivity.this, "無法獲取幣值資料", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> populateCurrencySpinners(responseData));
                }
            }
        });
    }

    // 動態填充 Spinner，並在幣值後面添加國籍
    private void populateCurrencySpinners(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject rates = jsonObject.getJSONObject("rates");

            // 提取所有的貨幣代碼
            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String currencyCode = keys.next();
                String displayText = currencyCode + " - " + getChineseCurrencyName(currencyCode); // 顯示格式為「幣值 - 中文幣名 - 國籍」
                currenciesList.add(displayText);
            }

            // 將貨幣代碼設置到 Spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currenciesList);
            sourceCurrency.setAdapter(adapter);
            targetCurrency.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 獲取中文幣名的方法
    private String getChineseCurrencyName(String currencyCode) {
        switch (currencyCode) {
            case "AED":
                return "阿聯酋迪拉姆";
            case "AFN":
                return "阿富汗尼";
            case "ALL":
                return "阿爾巴尼亞列克";
            case "AMD":
                return "亞美尼亞德拉姆";
            case "ANG":
                return "荷蘭安地卡";
            case "AOA":
                return "安哥拉庫瓦賓達";
            case "ARS":
                return "阿根廷比索";
            case "AUD":
                return "澳元";
            case "AWG":
                return "阿魯巴弗羅林";
            case "AZN":
                return "阿塞拜疆馬納特";
            case "BAM":
                return "波士尼亞可 convert";
            case "BBD":
                return "巴巴多斯元";
            case "BDT":
                return "孟加拉塔卡";
            case "BGN":
                return "保加利亞列弗";
            case "BHD":
                return "巴林第納爾";
            case "BIF":
                return "布隆迪法郎";
            case "BMD":
                return "百慕達元";
            case "BND":
                return "文萊元";
            case "BOB":
                return "玻利維亞諾";
            case "BRL":
                return "巴西雷亞爾";
            case "BSD":
                return "巴哈馬元";
            case "BTN":
                return "不丹努爾";
            case "BWP":
                return "博茨瓦納普拉";
            case "BYN":
                return "白俄羅斯盧布";
            case "BZD":
                return "貝里斯元";
            case "CAD":
                return "加元";
            case "CDF":
                return "剛果法郎";
            case "CHF":
                return "瑞士法郎";
            case "CLP":
                return "智利比索";
            case "CNY":
                return "人民幣";
            case "COP":
                return "哥倫比亞比索";
            case "CRC":
                return "哥斯大黎加科朗";
            case "CUP":
                return "古巴比索";
            case "CVE":
                return "維德角埃斯庫多";
            case "CZK":
                return "捷克克朗";
            case "DJF":
                return "吉布地法郎";
            case "DKK":
                return "丹麥克朗";
            case "DOP":
                return "多明尼加比索";
            case "DZD":
                return "阿爾及利亞第納爾";
            case "EGP":
                return "埃及鎊";
            case "ERN":
                return "厄立特里亞納克法";
            case "ETB":
                return "衣索比亞比爾";
            case "EUR":
                return "歐元";
            case "FJD":
                return "斐濟元";
            case "FKP":
                return "福克蘭鎊";
            case "FOK":
                return "法羅群島克朗";
            case "GBP":
                return "英鎊";
            case "GEL":
                return "喬治亞拉里";
            case "GGP":
                return "根西鎊";
            case "GHS":
                return "迦納塞地";
            case "GIP":
                return "直布羅陀鎊";
            case "GMD":
                return "甘比亞達拉西";
            case "GNF":
                return "幾內亞法郎";
            case "GTQ":
                return "瓜地馬拉格查";
            case "GYD":
                return "蓋亞那元";
            case "HKD":
                return "港元";
            case "HNL":
                return "洪都拉斯倫皮拉";
            case "HRK":
                return "克羅埃西亞庫納";
            case "HTG":
                return "海地古德";
            case "HUF":
                return "匈牙利福林";
            case "IDR":
                return "印尼盾";
            case "ILS":
                return "以色列新謝克爾";
            case "IMP":
                return "馬恩島鎊";
            case "INR":
                return "印度盧比";
            case "IQD":
                return "伊拉克第納爾";
            case "IRR":
                return "伊朗里亞爾";
            case "ISK":
                return "冰島克朗";
            case "JEP":
                return "澤西鎊";
            case "JMD":
                return "牙買加元";
            case "JOD":
                return "約旦第納爾";
            case "JPY":
                return "日元";
            case "KES":
                return "肯尼亞先令";
            case "KGS":
                return "吉爾吉斯索姆";
            case "KHR":
                return "柬埔寨瑞爾";
            case "KID":
                return "基里巴斯元";
            case "KMF":
                return "科摩羅法郎";
            case "KRW":
                return "韓元";
            case "KWD":
                return "科威特第納爾";
            case "KYD":
                return "開曼群島元";
            case "KZT":
                return "哈薩克堪特";
            case "LAK":
                return "寮國基普";
            case "LBP":
                return "黎巴嫩鎊";
            case "LKR":
                return "斯里蘭卡盧比";
            case "LRD":
                return "賴比瑞亞元";
            case "LSL":
                return "賴索托洛提";
            case "LYD":
                return "利比亞第納爾";
            case "MAD":
                return "摩洛哥迪拉姆";
            case "MDL":
                return "摩爾多瓦列伊";
            case "MGA":
                return "馬達加斯加阿里亞里";
            case "MKD":
                return "北馬其頓第納爾";
            case "MMK":
                return "緬甸元";
            case "MNT":
                return "蒙古圖格里克";
            case "MOP":
                return "澳門元";
            case "MRU":
                return "茅利塔尼亞烏吉亞";
            case "MUR":
                return "模里西斯盧比";
            case "MVR":
                return "馬爾代夫盧比";
            case "MWK":
                return "馬拉威克瓦查";
            case "MXN":
                return "墨西哥比索";
            case "MYR":
                return "馬來西亞令吉";
            case "MZN":
                return "莫三比克梅提卡爾";
            case "NAD":
                return "納米比亞元";
            case "NGN":
                return "奈及利亞奈拉";
            case "NIO":
                return "尼加拉瓜科多巴";
            case "NOK":
                return "挪威克朗";
            case "NPR":
                return "尼泊爾盧比";
            case "NZD":
                return "紐西蘭元";
            case "OMR":
                return "阿曼里亞爾";
            case "PAB":
                return "巴拿馬巴波亞";
            case "PEN":
                return "秘魯新索爾";
            case "PGK":
                return "巴布亞新幾內亞基那";
            case "PHP":
                return "菲律賓比索";
            case "PKR":
                return "巴基斯坦盧比";
            case "PLN":
                return "波蘭茲羅提";
            case "PYG":
                return "巴拉圭瓜拉尼";
            case "QAR":
                return "卡塔爾里亞爾";
            case "RON":
                return "羅馬尼亞列伊";
            case "RSD":
                return "塞爾維亞第納爾";
            case "RUB":
                return "俄羅斯盧布";
            case "RWF":
                return "盧旺達法郎";
            case "SAR":
                return "沙烏地阿拉伯里亞爾";
            case "SBD":
                return "所羅門群島元";
            case "SCR":
                return "塞舌爾盧比";
            case "SDG":
                return "蘇丹鎊";
            case "SEK":
                return "瑞典克朗";
            case "SGD":
                return "新加坡元";
            case "SHP":
                return "聖赫勒拿鎊";
            case "SLL":
                return "塞拉利昂利昂";
            case "SOS":
                return "索馬利亞先令";
            case "SRD":
                return "蘇利南元";
            case "SSP":
                return "南蘇丹鎊";
            case "STN":
                return "聖多美和普林西比多布拉";
            case "SYP":
                return "敘利亞鎊";
            case "SZL":
                return "斯威士蘭里蘇提";
            case "THB":
                return "泰銖";
            case "TJS":
                return "塔吉克斯坦索莫尼";
            case "TMT":
                return "土庫曼斯坦馬納特";
            case "TND":
                return "突尼斯第納爾";
            case "TOP":
                return "湯加帕安加";
            case "TRY":
                return "土耳其里拉";
            case "TTD":
                return "特立尼達和多巴哥元";
            case "TWD":
                return "新台幣";
            case "TZS":
                return "坦尚尼亞先令";
            case "UAH":
                return "烏克蘭赫夫尼亞";
            case "UGX":
                return "烏干達先令";
            case "USD":
                return "美金";
            case "UYU":
                return "烏拉圭比索";
            case "UZS":
                return "烏茲別克索姆";
            case "VES":
                return "委內瑞拉玻利瓦爾";
            case "VND":
                return "越南盾";
            case "VUV":
                return "瓦努阿圖瓦圖";
            case "WST":
                return "薩摩亞塔拉";
            case "XAF":
                return "中非金融合作法郎";
            case "XAG":
                return "白銀（盎司）";
            case "XAU":
                return "黃金（盎司）";
            case "XCD":
                return "東加勒比元";
            case "XDR":
                return "特別提款權（IMF）";
            case "XOF":
                return "西非金融合作法郎";
            case "XPF":
                return "法屬太平洋法郎";
            case "YER":
                return "也門里亞爾";
            case "ZAR":
                return "南非蘭特";
            case "ZMW":
                return "贊比亞克瓦查";
            case "ZWL":
                return "津巴布韋元";
            default:
                return "未知幣別";
        }
    }

    // 從 Spinner 中選擇的顯示文本中提取貨幣代碼
    private String getCurrencyCodeFromSelectedItem(String selectedItem) {
        return selectedItem.split(" - ")[0]; // 分割字串並取出幣值
    }

    private void fetchExchangeRate(final double amount, String fromCurrency, String toCurrency) {
        String url = "https://api.exchangerate-api.com/v4/latest/" + fromCurrency; // 使用來源貨幣進行請求
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(RateConverterActivity.this, "無法獲取匯率", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> parseJson(responseData, amount, toCurrency));
                }
            }
        });
    }

    private void parseJson(String jsonData, double amount, String toCurrency) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject rates = jsonObject.getJSONObject("rates");
            double exchangeRate = rates.getDouble(toCurrency); // 根據目標貨幣取得匯率
            double convertedAmount = amount * exchangeRate;
            Log.d("@@@", resultAmount+"");
            resultAmount.setText(String.format("結果：%.2f %s", convertedAmount, toCurrency));
        } catch (Exception e) {
            Log.d("@@@", "null");

            e.printStackTrace();
        }
    }

    private void findViews() {
        inputAmount = findViewById(R.id.inputAmount);
        resultAmount = findViewById(R.id.resultAmount);
        convertButton = findViewById(R.id.convertButton);
        sourceCurrency = findViewById(R.id.sourceCurrency);
        targetCurrency = findViewById(R.id.targetCurrency);
        rate_bt_back = findViewById(R.id.rate_bt_back);
    }
}