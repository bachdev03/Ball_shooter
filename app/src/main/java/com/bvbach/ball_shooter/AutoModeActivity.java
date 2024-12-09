package com.bvbach.ball_shooter;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutoModeActivity extends AppCompatActivity {

    private ImageView playerCircle, aiCircle;
    private Handler handler = new Handler();
    private int screenHeight, screenWidth;
    private boolean isGameOver = false;
    private double[][] qTable;
    private static final int ACTION_LEFT = 0;
    private static final int ACTION_RIGHT = 1;
    private static final int ACTION_SHOOT = 2;
    private static final int STATE_PLAYER_LEFT = 0;
    private static final int STATE_PLAYER_RIGHT = 1;
    private static final int STATE_PLAYER_CENTER = 2;
    private PlayerMovementPredictor predictor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_mode);

        try {
            predictor = new PlayerMovementPredictor(getAssets());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Khởi tạo Q-table với giá trị ngẫu nhiên
        qTable = new double[3][3]; // 3 trạng thái, 3 hành động
        initializeQTable();

        playerCircle = findViewById(R.id.player_circle);
        aiCircle = findViewById(R.id.ai_circle);

        // Lấy kích thước màn hình
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Khởi tạo AI bắn đạn liên tục
        startAIBullets();



        // Di chuyển hình tròn của người chơi bằng cách kéo ngón tay trên màn hình
        playerCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getRawX() - playerCircle.getWidth() / 2;
                        float y = event.getRawY() - playerCircle.getHeight() / 2;

                        // Giới hạn di chuyển của hình tròn người chơi trong màn hình
                        if (x >= 0 && x <= screenWidth - playerCircle.getWidth() &&
                                y >= screenHeight / 2 && y <= screenHeight - playerCircle.getHeight()) {
                            playerCircle.setX(x);
                            playerCircle.setY(y);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void makeAIPrediction() {
        // Lấy vị trí hiện tại của người chơi (X, Y)
        float[] currentPlayerPosition = new float[]{playerCircle.getX(), playerCircle.getY()};

        // Dự đoán hành vi tiếp theo của người chơi
        int predictedAction = predictor.predictNextAction(currentPlayerPosition);

        // Dựa trên dự đoán, điều chỉnh AI
        switch (predictedAction) {
            case 0: // Người chơi sẽ di chuyển sang trái
                aiCircle.setX(aiCircle.getX() - 20);
                break;
            case 1: // Người chơi sẽ di chuyển sang phải
                aiCircle.setX(aiCircle.getX() + 20);
                break;
            case 2: // Người chơi sẽ đứng yên
                // AI có thể bắn đạn hoặc không làm gì
                fireBulletFromAI();
                break;
        }
    }



    private List<float[]> playerMovementData = new ArrayList<>();

    private void trackPlayerMovement() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    // Lưu vị trí X, Y của người chơi
                    float[] playerPosition = new float[]{playerCircle.getX(), playerCircle.getY()};
                    playerMovementData.add(playerPosition);
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
    }



    private void initializeQTable() {
        for (int i = 0; i < qTable.length; i++) {
            for (int j = 0; j < qTable[i].length; j++) {
                qTable[i][j] = Math.random(); // Giá trị ngẫu nhiên ban đầu
            }
        }
    }

    private int getPlayerState() {
        float playerX = playerCircle.getX();
        float aiX = aiCircle.getX();

        if (playerX < aiX) {
            return STATE_PLAYER_LEFT;
        } else if (playerX > aiX) {
            return STATE_PLAYER_RIGHT;
        } else {
            return STATE_PLAYER_CENTER;
        }
    }

    private int chooseAction(int state) {
        double[] actions = qTable[state];
        int bestAction = ACTION_LEFT;

        // Tìm hành động với giá trị cao nhất trong Q-table
        for (int i = 1; i < actions.length; i++) {
            if (actions[i] > actions[bestAction]) {
                bestAction = i;
            }
        }
        return bestAction;
    }

    private void executeAction(int action) {
        switch (action) {
            case ACTION_LEFT:
                // Di chuyển AI sang trái
                aiCircle.setX(aiCircle.getX() - 20);
                break;
            case ACTION_RIGHT:
                // Di chuyển AI sang phải
                aiCircle.setX(aiCircle.getX() + 20);
                break;
            case ACTION_SHOOT:
                // Bắn đạn
                fireBulletFromAI();
                break;
        }
    }

    private void updateQTable(int state, int action, int reward) {
        double learningRate = 0.1;
        double discountFactor = 0.9;

        double bestNextActionValue = getMaxQValue(state);
        qTable[state][action] = qTable[state][action] + learningRate * (reward + discountFactor * bestNextActionValue - qTable[state][action]);
    }

    private double getMaxQValue(int state) {
        double max = qTable[state][0];
        for (int i = 1; i < qTable[state].length; i++) {
            if (qTable[state][i] > max) {
                max = qTable[state][i];
            }
        }
        return max;
    }
;



    // Hàm để AI bắn đạn
    private void startAIBullets() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    // Bắn đạn từ AI như bình thường
                    fireBulletFromAI();
                    // Dự đoán hành vi người chơi và điều chỉnh hành động của AI
                    makeAIPrediction();  // Hàm dự đoán và hành động của AI
                    // Gọi lại mỗi giây
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }


    // Hàm để tạo và di chuyển viên đạn từ AI
    private void fireBulletFromAI() {
        final ImageView bullet = new ImageView(this);
        bullet.setImageResource(R.drawable.dan_phao);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        bullet.setLayoutParams(params);

        // Đặt viên đạn ở vị trí của AI
        bullet.setX(aiCircle.getX() + aiCircle.getWidth() / 2 - 10);
        bullet.setY(aiCircle.getY() + aiCircle.getHeight());

        ((RelativeLayout) findViewById(R.id.auto_mode_layout)).addView(bullet);

        // Lấy tọa độ của người chơi và AI khi viên đạn được bắn ra
        final float playerX = playerCircle.getX();
        final float playerY = playerCircle.getY();
        final float aiX = aiCircle.getX();
        final float aiY = aiCircle.getY();

        // Tính khoảng cách theo trục X và Y giữa AI và người chơi
        final float deltaX = playerX - aiX;
        final float deltaY = playerY - aiY;

        // Tính độ dài đoạn đường di chuyển
        final float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Tính thời gian di chuyển viên đạn dựa trên khoảng cách (điều chỉnh cho tốc độ phù hợp)
        final int duration = (int) (3000 * (distance / screenHeight));

        // Tính toán tốc độ di chuyển của viên đạn theo tỷ lệ của deltaX và deltaY
        final float speedX = deltaX / distance;
        final float speedY = deltaY / distance;

        // Di chuyển viên đạn theo hướng đã tính toán
        handler.post(new Runnable() {
            @Override
            public void run() {
                bullet.setX(bullet.getX() + speedX * 10);  // Điều chỉnh tốc độ X
                bullet.setY(bullet.getY() + speedY * 10);  // Điều chỉnh tốc độ Y

                // Kiểm tra va chạm với người chơi
                if (checkCollision(bullet, playerCircle)) {
                    isGameOver = true;
                    updateQTable(getPlayerState(), ACTION_SHOOT, 10); // Thưởng cho AI nếu trúng
                    gameOver();
                } else if (bullet.getY() > screenHeight || bullet.getX() < 0 || bullet.getX() > screenWidth) {
                    // Xóa viên đạn nếu ra khỏi màn hình
                    ((RelativeLayout) findViewById(R.id.auto_mode_layout)).removeView(bullet);
                } else if (!isGameOver) {
                    // Tiếp tục di chuyển nếu game chưa kết thúc
                    handler.postDelayed(this, 16);  // Cập nhật mỗi 16ms (~60fps)
                }
            }
        });
    }


    // Hàm kiểm tra va chạm giữa viên đạn và người chơi
    private boolean checkCollision(View bullet, View player) {
        return bullet.getX() < player.getX() + player.getWidth() &&
                bullet.getX() + bullet.getWidth() > player.getX() &&
                bullet.getY() < player.getY() + player.getHeight() &&
                bullet.getY() + bullet.getHeight() > player.getY();
    }

    // Hàm kết thúc trò chơi khi người chơi bị bắn trúng
    private void gameOver() {
        // Hiển thị hộp thoại khi trò chơi kết thúc
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(AutoModeActivity.this)
                        .setTitle("Game Over")
                        .setMessage("Bạn đã thua !")
                        .setPositiveButton("Quay lại", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Quay lại MainActivity khi nhấn "Yes"
                                Intent intent = new Intent(AutoModeActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setCancelable(false) // Không cho phép đóng hộp thoại bằng cách nhấn ra ngoài
                        .show();
            }
        });
    }
}
