package com.example.esp32_cam_artur_diplom;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button Wifi_Turn_On_button, Onovlena_button;
    TextView Settings_textview, Name_Camera_textview, Camera_textview, Connect_textview,  TxtRES_textview, TxtRES1_textview, WiFi_textview, Renewal_textview ;
    ImageView CameraView_imageview;

    private OkHttpClient client = new OkHttpClient();
    private BroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        NetworkRequest request = builder.build();
        connManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onAvailable(Network network) {
                connManager.bindProcessToNetwork(network);
            }
        });//всі мережеві запити, що робить ваш додаток, будуть виконуватися через певну мережу(wi-fi)

        Settings_textview = findViewById(R.id.Settings_textview);
        Name_Camera_textview = findViewById(R.id.Name_Camera_textview);
        Wifi_Turn_On_button = findViewById(R.id.Wifi_Turn_On_button);
        WiFi_textview = findViewById(R.id.WiFi_textview);
        CameraView_imageview = findViewById(R.id.CameraView_imageview);
        TxtRES_textview = findViewById(R.id.TxtRES_textview);
        Connect_textview = findViewById(R.id.Connect_textview);
        Camera_textview = findViewById(R.id.Name_Camera_textview);
        TxtRES1_textview = findViewById(R.id.TxtRES1_textview);
        Renewal_textview = findViewById(R.id.Renewal_textview);
        Onovlena_button = findViewById(R.id.Onovlena_button);
//прописування посилань на змінні

        Wifi_Turn_On_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWifiSettings();
                sendCommand("capture");
            }
        });//включення вайфаю в налаштуваннях

        Onovlena_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshActivity();
            }
        });//кнопка оновлення додатку

        // Initialize the BroadcastReceiver
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.isConnected()) {
                    TxtRES_textview.setText("OK");
                }
            }
        };//текст для підключення вайфаю,якщо вайфай включений

        // Register the BroadcastReceiver to listen for connectivity changes
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiReceiver, filter);
    }//прослуховування змін у підключенні до мережі

    private void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }//метод для переходу з додатку в налаштування вайфаю

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }//метод для перезапуску активності

    public void sendCommand(String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command = "http://192.168.13.24/" + cmd;
                Log.d("Command------------------------------------------", command);
                Request request = new Request.Builder().url(command).build();
                try {
                    Response response = client.newCall(request).execute();
                    // Отримати зображення як байтовий масив
                    byte[] imageData = response.body().bytes();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Перетворення байтового масиву у зображення та відображення його в ImageView
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            CameraView_imageview.setImageBitmap(bitmap);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}