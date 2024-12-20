package com.bvbach.ball_shooter;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private ImageView gunImageView; // ImageView để hiển thị súng
    private int selectedGunImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gunImageView = findViewById(R.id.gunImageView);

        // Lấy thông tin súng từ SharedPreferences
        selectedGunImage = getSelectedGunImage();
        // Cập nhật súng cho game
        gunImageView.setImageResource(selectedGunImage);

        // Lấy độ khó từ Intent
        String difficulty = getIntent().getStringExtra("difficulty");

        // Kiểm tra giá trị của difficulty để đảm bảo nó không null
        if (difficulty == null) {
            difficulty = "easy"; // Gán giá trị mặc định nếu không có giá trị được truyền
        }

        // Khởi tạo GameView với độ khó đã chọn
        gameView = new GameView(this, difficulty);
        setContentView(gameView); // Đặt GameView làm nội dung hiển thị
    }
    // Phương thức lấy thông tin súng đã lưu trong SharedPreferences
    public int getSelectedGunImage() {
        SharedPreferences preferences = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        // Nếu chưa có súng nào được chọn, sử dụng súng mặc định (R.drawable.sung)
        return preferences.getInt("selectedGunImage", R.drawable.sung); // R.drawable.sung là súng mặc định
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}