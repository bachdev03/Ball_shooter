package com.bvbach.ball_shooter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {

    private SoundPool soundPool;
    private int shootSoundId;
    private int impactSoundId;
    private int milestoneSoundId; // ID cho âm thanh cột mốc
    private Thread gameThread;
    private boolean isPlaying;
    private boolean gameOver; // Trạng thái kết thúc trò chơi
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Bitmap shooterBitmap; // Biến lưu trữ hình ảnh khẩu súng
    private Bitmap ballBitmap; //
    private Bitmap ballPointBitmap; //
    private float screenX, screenY;
    private float shooterX, shooterY, shooterRadius;
    private int shooterWidth, shooterHeight; // Kích thước của hình ảnh khẩu súng
    private int ballWidth, ballHeight; //
    private int ballPointWidth, ballPointHeight;
    private List<Ball> balls; // Danh sách các viên đạn
    private List<Circle> fallingCircles; //
    private Handler handler; // Handler để quản lý thời gian
    private Runnable addCirclesRunnable; //
    private int circlesToAdd = 1; //
    private int score; // Điểm số
    private Paint scorePaint; //
    private String difficulty;
    private Bitmap backgroundBitmap;
    private int backgroundWidth, backgroundHeight;
    private Bitmap pauseButtonBitmap;
    private int pauseButtonX, pauseButtonY, pauseButtonWidth, pauseButtonHeight;
    private boolean isPaused = false;
    private boolean isPauseButtonVisible = true; // Biến để kiểm tra xem nút pause hay continue đang hiển thị
    private Bitmap continueButtonBitmap;// Trạng thái tạm dừng
    private int maxBullets = 4;
    private int currentBullets = maxBullets;
    private Handler bulletHandler;




    private class Ball {
        float x, y, speedX, speedY;
        boolean isActive;

        Ball(float x, float y, float speedX, float speedY) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.isActive = true;
        }
    }

    private class Circle {
        float x, y, radius, speedY;
        boolean isActive;

        Circle(float x, float y, float radius, float speedY) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speedY = speedY;
            this.isActive = true;
        }
    }


    public GameView(Context context, String difficulty) {
        super(context);
        this.difficulty = difficulty;
        surfaceHolder = getHolder();
        paint = new Paint();
        loadGunImage();




        ballBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.vien_dan);
        ballWidth = ballBitmap.getWidth();
        ballHeight = ballBitmap.getHeight();

        ballPointBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.qua_bong);
        ballPointWidth = ballBitmap.getWidth();
        ballPointHeight = ballBitmap.getHeight();

        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.anh_nen_game);
        backgroundWidth = backgroundBitmap.getWidth();
        backgroundHeight = backgroundBitmap.getHeight();

        pauseButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button); // Sử dụng hình ảnh cho nút
        pauseButtonWidth = pauseButtonBitmap.getWidth();
        pauseButtonHeight = pauseButtonBitmap.getHeight();
        pauseButtonX = 20; // Vị trí x của nút (đáy bên trái)
        pauseButtonY = (int) screenY - pauseButtonHeight - 20; // Vị trí y của nút (đáy bên trái)

        continueButtonBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.continue_button);



        // Khởi tạo danh sách các viên đạn
        balls = new ArrayList<>();
        // Khởi tạo danh sách các hình tròn đang rơi
        fallingCircles = new ArrayList<>();
        // Khởi tạo điểm số và paint cho điểm số
        score = 0;
        scorePaint = new Paint();
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextSize(60);
        scorePaint.setAntiAlias(true);

        // Thêm một số hình tròn đang rơi vào danh sách
        handler = new Handler();
        addCirclesRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    // Thêm hình tròn mới vào danh sách
                    for (int i = 0; i < circlesToAdd; i++) {
                        addNewFallingCircle();
                    }
                    circlesToAdd++;
                    handler.postDelayed(this, 2000);
                }
            }
        };
        // Bắt đầu thêm hình tròn
        handler.postDelayed(addCirclesRunnable, 1000);

        // Khởi tạo SoundPool
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        // Tải âm thanh bắn súng từ thư mục raw
        shootSoundId = soundPool.load(context, R.raw.tieng_sung_ban, 1);
        // Tải âm thanh trúng bóng từ thư mục raw
        impactSoundId = soundPool.load(context, R.raw.tieng_sung_pha_bong, 1);
        // Load the milestone sound
        milestoneSoundId = soundPool.load(context, R.raw.am_thanh_ace, 1);

        bulletHandler = new Handler();// so luong dan

    }

    // Phương thức tải súng từ SharedPreferences
    public void loadGunImage() {
        SharedPreferences preferences = getContext().getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int selectedGunImage = preferences.getInt("selectedGunImage", R.drawable.sung); // Súng mặc định nếu không có
        shooterBitmap = BitmapFactory.decodeResource(getResources(), selectedGunImage);
        shooterWidth = shooterBitmap.getWidth();
        shooterHeight = shooterBitmap.getHeight();
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }
            update();
            draw();
        }
    }

    private void update() {
        // Cập nhật vị trí của tất cả các viên đạn
        for (Ball ball : balls) {
            if (ball.isActive) {
                ball.x += ball.speedX;
                ball.y += ball.speedY;

                // Kiểm tra nếu viên đạn ra khỏi màn hình
                if (ball.x < 0 || ball.x > screenX || ball.y < 0 || ball.y > screenY) {
                    ball.isActive = false;
                    currentBullets++; // Tăng số lượng đạn khi viên đạn ra ngoài
                }
            }
        }

        // Loại bỏ các viên đạn không còn hoạt động
        balls.removeIf(ball -> !ball.isActive);

        // Cập nhật vị trí của tất cả các hình tròn đang rơi
        boolean hasFallen = false;
        for (Circle circle : fallingCircles) {
            if (circle.isActive) {
                circle.y += circle.speedY;

                // Kiểm tra nếu hình tròn ra khỏi màn hình
                if (circle.y > screenY + circle.radius) {
                    circle.isActive = false;
                }

                // Kiểm tra nếu hình tròn chạm đáy
                if (circle.y + circle.radius >= screenY) {
                    hasFallen = true;
                }
            }
        }

        // Kiểm tra va chạm giữa viên đạn và hình tròn
        for (Ball ball : balls) {
            if (ball.isActive) {
                for (Circle circle : fallingCircles) {
                    if (circle.isActive) {
                        float dx = ball.x - circle.x;
                        float dy = ball.y - circle.y;
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);

                        if (distance < circle.radius) {
                            // Va chạm xảy ra
                            ball.isActive = false;
                            circle.isActive = false;
                            score++; // Tăng điểm số

                            // Phát âm thanh khi va chạm
                            if (score % 5 == 0) {
                                soundPool.play(milestoneSoundId, 1, 1, 0, 0, 1);
                            } else {
                                soundPool.play(impactSoundId, 1, 1, 0, 0, 1);
                            }
                            // Khôi phục lại viên đạn ngay lập tức
                            if (currentBullets < maxBullets) {
                                currentBullets++; // Tăng số lượng đạn
                            }
                            break;
                        }
                    }
                }
            }
        }

        // Loại bỏ các hình tròn không còn hoạt động
        fallingCircles.removeIf(circle -> !circle.isActive);

        // Kiểm tra điều kiện kết thúc trò chơi
        if (hasFallen) {
            gameOver = true;
            handler.removeCallbacks(addCirclesRunnable); // Dừng thêm hình tròn
            showGameOverDialog();
        }
    }
    private void addNewFallingCircle() {
        if (fallingCircles.size() >= 4) {
            return;
        }

        float radius = ballPointWidth / 2.0f;
        // Set the speed based on difficulty
        float baseSpeedY;
        switch (difficulty) {
            case "easy":
                baseSpeedY = 3;
                break;
            case "medium":
                baseSpeedY = 7;
                break;
            case "hard":
                baseSpeedY = 13;
                break;
            default:
                baseSpeedY = 3; // Default to easy if something goes wrong
        }

        float x = (float) Math.random() * (screenX - ballPointWidth);
        float y = (float) Math.random() * -screenY; // Bắt đầu từ trên màn hình
        float speedY = baseSpeedY + (float) Math.random() * 5; // Adjust speed based on difficulty
        fallingCircles.add(new Circle(x, y, radius, speedY));
    }



    private void draw() {
        // Vẽ giao diện game
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            // Vẽ hình nền
            canvas.drawBitmap(backgroundBitmap, 0, 0, paint);

            // Vẽ số lượng đạn hiện có
            for (int i = 0; i < maxBullets; i++) {
                if (i < currentBullets) {
                    // Vẽ viên đạn nếu còn đạn
                    canvas.drawBitmap(ballBitmap, screenX - (70 + (i * (ballWidth + 4))), screenY - ballHeight - 25, paint); // Điều chỉnh vị trí
                }
            }

            // Vẽ hình ảnh khẩu súng
            canvas.drawBitmap(shooterBitmap, shooterX - shooterWidth / 2, shooterY - shooterHeight / 2, paint);

            // Vẽ tất cả các viên đạn đang hoạt động
            for (Ball ball : balls) {
                if (ball.isActive) {
                    canvas.drawBitmap(ballBitmap, ball.x - ballWidth / 2, ball.y - ballHeight / 2, paint);
                }
            }

            // Vẽ tất cả các hình tròn đang rơi
            for (Circle circle : fallingCircles) {
                if (circle.isActive) {
                    canvas.drawBitmap(ballPointBitmap, circle.x - ballPointWidth / 2, circle.y - ballPointHeight / 2, paint);
                }
            }

            // Vẽ điểm số
            canvas.drawText("         " + score, 30, 140, scorePaint);

            // Tính toán vị trí của nút (góc trên bên phải)
            pauseButtonX = canvas.getWidth() - pauseButtonBitmap.getWidth() - 30; // Cách mép phải 30 pixels
            pauseButtonY = 50; // Cách mép trên 50 pixels

            // Vẽ nút tạm dừng hoặc tiếp tục dựa trên trạng thái
            if (isPaused) {
                // Vẽ nút "Continue"
                canvas.drawBitmap(continueButtonBitmap, pauseButtonX, pauseButtonY, paint);
            } else {
                // Vẽ nút "Pause"
                canvas.drawBitmap(pauseButtonBitmap, pauseButtonX, pauseButtonY, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void saveScoreToDatabase(int score) {
        SQLiteDatabase database = null;
        try {
            String dbPath = getContext().getDatabasePath("ball_shooter.db").getPath();
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
            database.execSQL("INSERT INTO bang_diem (diem_so) VALUES (?)", new Object[]{score});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }
    private void showGameOverDialog() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                pause();
                // Lưu điểm số vào cơ sở dữ liệu
                saveScoreToDatabase(score);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Game Over");
                builder.setMessage("Bạn đã thua rồi, chơi lại nào !");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), DifficultySelectionActivity.class);
                        getContext().startActivity(intent);
                    }
                });
                builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    public void resume() {

        isPlaying = true;
        gameOver = false; // Reset trạng thái kết thúc trò chơi khi bắt đầu trò chơi
        gameThread = new Thread(this);
        gameThread.start();
        isPauseButtonVisible = true; // Hiển thị lại nút pause khi tiếp tục trò chơi
    }

    public void pause() {

        try {

            isPlaying = false;
            gameThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isPauseButtonVisible = false; // Hiển thị nút continue khi tạm dừng trò chơi
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Kiểm tra nếu người chơi chạm vào nút tạm dừng hoặc nút tiếp tục
            if (touchX >= pauseButtonX && touchX <= pauseButtonX + pauseButtonBitmap.getWidth() &&
                    touchY >= pauseButtonY && touchY <= pauseButtonY + pauseButtonBitmap.getHeight()) {

                // Nếu đang tạm dừng, thì tiếp tục game
                if (isPaused) {
                    resume(); // Tiếp tục trò chơi
                } else {
                    pause(); // Tạm dừng trò chơi
                }

                // Đảo trạng thái giữa tạm dừng và tiếp tục
                isPaused = !isPaused;

                //Ve lai
                draw();

                return true; // Không cần xử lý các logic khác khi ấn nút tạm dừng/tiếp tục
            }

            // Nếu trò chơi không ở trạng thái tạm dừng, thực hiện các logic khác như bắn đạn
            if (!isPaused) {
                // Kiểm tra số lượng đạn hiện tại
                if (currentBullets > 0) {
                    // Tính toán vị trí chạm
                    shooterX = touchX;

                    // Tính toán vector hướng từ khẩu súng đến điểm chạm
                    float directionX = touchX - shooterX;
                    float directionY = touchY - shooterY;

                    // Tính toán độ dài của vector
                    float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);

                    // Chuyển hướng thành tốc độ viên đạn
                    float speedX = (directionX / length) * 20; // 20 là tốc độ viên đạn
                    float speedY = (directionY / length) * 20;

                    // Tạo một viên đạn mới và thêm vào danh sách
                    balls.add(new Ball(shooterX, shooterY - ballHeight / 2, speedX, speedY));

                    // Giảm số lượng đạn hiện tại
                    currentBullets--;

                    // Phát âm thanh khi bắn
                    soundPool.play(shootSoundId, 1, 1, 0, 0, 1);
                }
            }
        }
        return true;
    }


    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenX = w;
        screenY = h;

        // Đặt khẩu súng gần đáy màn hình
        shooterX = screenX / 2;
        shooterY = screenY - shooterHeight / 2 - 80;

        // Điều chỉnh hình nền theo kích thước màn hình
        backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, (int) screenX, (int) screenY, false);

        // Đặt vị trí của nút tạm dừng (sau khi screenX và screenY được khởi tạo)
        pauseButtonX = 20; // Cạnh bên trái
        pauseButtonY = (int) screenY - pauseButtonHeight - 20; // Cạnh dưới cùng, cách 20 dp
    }

}
