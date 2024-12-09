package com.bvbach.ball_shooter;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PlayerMovementPredictor {

    private Interpreter tflite;

    public PlayerMovementPredictor(AssetManager assetManager) throws IOException {
        tflite = new Interpreter(loadModelFile(assetManager));
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd("player_prediction_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Hàm dự đoán hành vi tiếp theo của người chơi
    public int predictNextAction(float[] playerPosition) {
        float[][] output = new float[1][3];  // 3 hành động: trái, phải, đứng yên
        tflite.run(playerPosition, output);

        // Trả về hành động có xác suất cao nhất
        return argMax(output[0]);
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}

