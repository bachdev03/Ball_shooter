package com.bvbach.ball_shooter;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {
    String DB_PATH_SUFFIX = "/databases/";
    String DATABASE_NAME = "ball_shooter.db";
    SQLiteDatabase database = null;

    ListView listViewLeaderboard;
    ArrayList<String> leaderboardList;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Ánh xạ ListView và Button
        listViewLeaderboard = findViewById(R.id.listViewLeaderboard);
        Button buttonBack = findViewById(R.id.buttonBack);


        // Sao chép database từ assets nếu cần
        processCopy();
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        // Hiển thị dữ liệu
        leaderboardList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, leaderboardList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);

                textView.setTextColor(Color.BLACK);

                return textView;
            }
        };
        listViewLeaderboard.setAdapter(adapter);

        loadData();


        buttonBack.setOnClickListener(view -> {
            Intent intent = new Intent(LeaderboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        Button buttonResetLeaderboard = findViewById(R.id.buttonResetLeaderboard);


        buttonResetLeaderboard.setOnClickListener(view -> {
            resetLeaderboard();
        });

    }
    private void resetLeaderboard() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn reset bảng xếp hạng không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    try {
                        database.execSQL("DELETE FROM bang_diem");
                        Toast.makeText(this, "Bảng xếp hạng đã được reset!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi khi reset bảng xếp hạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> {

                    dialog.dismiss();
                })
                .show();
    }


    private void loadData() {
        leaderboardList.clear();

        Cursor c = database.rawQuery("SELECT diem_so FROM bang_diem ORDER BY diem_so DESC LIMIT 10", null);
        if (c.moveToFirst()) {
            do {
                String data = c.getString(0); // Lấy điểm số từ cột đầu tiên
                leaderboardList.add(data);
            } while (c.moveToNext());
        }
        c.close();
        adapter.notifyDataSetChanged(); // Cập nhật lại ListView
    }


    private void processCopy() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                copyDatabaseFromAssets();
                Toast.makeText(this, "Sao chép cơ sở dữ liệu thành công", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void copyDatabaseFromAssets() throws IOException {
        InputStream input = getAssets().open(DATABASE_NAME);
        String outFileName = getDatabasePath(DATABASE_NAME).getPath();
        File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!f.exists()) f.mkdir();

        OutputStream output = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }
}
