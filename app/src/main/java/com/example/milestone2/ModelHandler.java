package com.example.milestone2;

import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milestone2.ml.MobileModelV2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ModelHandler extends AppCompatActivity {
    int imageSize = 224;
    TextView result;
    TextView confidence;
    ImageView imageView;
    Button picture;

    public ModelHandler(TextView result, TextView confidence, ImageView imageView, Button picture) {
        this.result = result;
        this.confidence = confidence;
        this.imageView = imageView;
        this.picture = picture;
    }

    public void classifySound(Bitmap image, MobileModelV2 model){
        TensorBuffer inputFeature0 = preProcess(image);

        MobileModelV2.Outputs outputs = model.process(inputFeature0);

        postProcess(outputs.getOutputFeature0AsTensorBuffer());

        model.close();
    }

    private TensorBuffer preProcess(Bitmap image){
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int [] intValues = new int[imageSize * imageSize];
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
        int pixel = 0;
        for(int i = 0; i < imageSize; i++){
            for(int j = 0; j < imageSize; j++){
                int val = intValues[pixel++]; // RGB
                byteBuffer.putFloat(((val >> 16) & 0xFF));
                byteBuffer.putFloat(((val >> 8) & 0xFF));
                byteBuffer.putFloat((val & 0xFF));
            }
        }

        inputFeature0.loadBuffer(byteBuffer);
        return inputFeature0;
    }

    private void postProcess(TensorBuffer outputFeature0){
        String[] classes = {"0", "1", "2", "3"};
        float[] confidences = outputFeature0.getFloatArray();
        // find the index of the class with the biggest confidence.
        int maxPos = 0;
        float maxConfidence = 0;
        for(int i = 0; i < confidences.length; i++){
            if(confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }

        result.setText(classes[maxPos]);

        String s = "";
        s += String.format("%s: %.1f%%\n", classes[0], confidences[0] * 100);
        s += String.format("%s: %.1f%%\n", classes[1], confidences[1] * 100);
        s += String.format("%s: %.1f%%\n", classes[2], confidences[2] * 100);
        s += String.format("%s: %.1f%%\n", classes[3], confidences[3] * 100);
        confidence.setText(s);
    }
}