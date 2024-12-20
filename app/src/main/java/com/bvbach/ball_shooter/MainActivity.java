package com.bvbach.ball_shooter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        // Xử lý nút bắt đầu game
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DifficultySelectionActivity.class);
                startActivity(intent);
            }
        });

        Button storeButton = findViewById(R.id.storeButton);
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StoreActivity.class);
                startActivity(intent); // Mở StoreActivity
            }
        });

        Button buttonLeaderboard = findViewById(R.id.buttonLeaderboard);
        buttonLeaderboard.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        VideoView videoView = findViewById(R.id.videoView);

        // Đường dẫn video
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.back_gr_main);
        videoView.setVideoURI(videoUri);

        // Đặt lắng nghe khi video hoàn thành
        videoView.setOnCompletionListener(mp -> {
            // Khi video kết thúc, phát lại video từ đầu
            videoView.start();
        });

        // Bắt đầu video
        videoView.start();

    }
}
