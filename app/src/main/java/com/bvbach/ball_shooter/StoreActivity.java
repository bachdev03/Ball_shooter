package com.bvbach.ball_shooter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;


public class StoreActivity extends AppCompatActivity {
    private Button item1;
    private Button item2;
    private Button item3;
    private Button item4;
    private Button item5;
    private Button item6;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_layout);

        item1 = findViewById(R.id.buyButton1);
        item2 = findViewById(R.id.buyButton2);
        item3 = findViewById(R.id.buyButton3);
        item4 = findViewById(R.id.buyButton4);
        item5 = findViewById(R.id.buyButton5);
        item6 = findViewById(R.id.buyButton6);

        findViewById(R.id.buyButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item1.setText("Đã mua");
                saveSelectedGun(R.drawable.item1);
                Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();


            }
        });


        findViewById(R.id.buyButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item2.setText("Đã mua");
                saveSelectedGun(R.drawable.item2);
                Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();



            }
        });

        findViewById(R.id.buyButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item3.setText("Đã mua");
                saveSelectedGun(R.drawable.item3);
                Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();


            }
        });

        findViewById(R.id.buyButton4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item4.setText("Đã mua");
                saveSelectedGun(R.drawable.item4);
                Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();


            }
        });
        findViewById(R.id.buyButton5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item5.setText("Đã mua");
                saveSelectedGun(R.drawable.item5);
                Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();


            }
        });
        findViewById(R.id.buyButton6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item6.setText("Đã mua");
                saveSelectedGun(R.drawable.sung);
                Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();


            }
        });

    }
    public void saveSelectedGun(int gunImageResource) {
        SharedPreferences preferences = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selectedGunImage", gunImageResource);
        editor.apply();
    }

}


