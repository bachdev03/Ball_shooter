package com.bvbach.ball_shooter;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
        Button buttonBackst = findViewById(R.id.buttonBack);


        findViewById(R.id.buyButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalScore = calculateTotalScore();
                int itemCost = 5;

                if (totalScore >= itemCost) {
                    totalScore -= itemCost;
                    item1.setText("Đã mua");
                    saveSelectedGun(R.drawable.item1);
                    Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();
                    updateScoreDisplay(totalScore);
                } else {
                    Toast.makeText(StoreActivity.this, "Bạn không đủ điểm để mua vật phẩm này", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.buyButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalScore = calculateTotalScore();
                int itemCost = 5;

                if (totalScore >= itemCost) {
                    totalScore -= itemCost;
                    item2.setText("Đã mua");
                    saveSelectedGun(R.drawable.item2);
                    Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();
                    updateScoreDisplay(totalScore);
                } else {
                    Toast.makeText(StoreActivity.this, "Bạn không đủ điểm để mua vật phẩm này", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.buyButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalScore = calculateTotalScore();
                int itemCost = 5;

                if (totalScore >= itemCost) {
                    totalScore -= itemCost;
                    item3.setText("Đã mua");
                    saveSelectedGun(R.drawable.item3);
                    Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();
                    updateScoreDisplay(totalScore);
                } else {
                    Toast.makeText(StoreActivity.this, "Bạn không đủ điểm để mua vật phẩm này", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.buyButton4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalScore = calculateTotalScore();
                int itemCost = 5;

                if (totalScore >= itemCost) {
                    totalScore -= itemCost;
                    item4.setText("Đã mua");
                    saveSelectedGun(R.drawable.item4);
                    Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();
                    updateScoreDisplay(totalScore);
                } else {
                    Toast.makeText(StoreActivity.this, "Bạn không đủ điểm để mua vật phẩm này", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.buyButton5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalScore = calculateTotalScore();
                int itemCost = 5;

                if (totalScore >= itemCost) {
                    totalScore -= itemCost;
                    item5.setText("Đã mua");
                    saveSelectedGun(R.drawable.item5);
                    Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();
                    updateScoreDisplay(totalScore);
                } else {
                    Toast.makeText(StoreActivity.this, "Bạn không đủ điểm để mua vật phẩm này", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.buyButton6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalScore = calculateTotalScore();
                int itemCost = 5;

                if (totalScore >= itemCost) {
                    totalScore -= itemCost;
                    item6.setText("Đã mua");
                    saveSelectedGun(R.drawable.sung);
                    Toast.makeText(StoreActivity.this, "Đã mua thành công", Toast.LENGTH_SHORT).show();
                    updateScoreDisplay(totalScore);
                } else {
                    Toast.makeText(StoreActivity.this, "Bạn không đủ điểm để mua vật phẩm này", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonBackst.setOnClickListener(view -> {
            Intent intent = new Intent(StoreActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        TextView textViewTotalScore = findViewById(R.id.textViewTotalScore);

        int totalScore = calculateTotalScore();

        textViewTotalScore.setText("                  " + totalScore);

    }

    private void updateScoreDisplay(int totalScore) {
        TextView scoreTextView = findViewById(R.id.textViewTotalScore);  // Giả sử TextView có ID là totalScoreTextView
        scoreTextView.setText("                  " + totalScore);  // Hiển thị tổng điểm
    }

    private int calculateTotalScore() {
        SQLiteDatabase database = null;
        int totalScore = 0;
        try {

            String dbPath = this.getDatabasePath("ball_shooter.db").getPath();
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

            Cursor cursor = database.rawQuery("SELECT SUM(diem_so) FROM bang_diem", null);
            if (cursor.moveToFirst()) {
                totalScore = cursor.getInt(0); // Lấy tổng điểm từ cột đầu tiên
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.close();
            }
        }
        return totalScore;
    }

    public void saveSelectedGun(int gunImageResource) {
        SharedPreferences preferences = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("selectedGunImage", gunImageResource);
        editor.apply();
    }

}


