package com.example.milestone2;

import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milestone2.ml.MobileModelV2;
import com.example.milestone2.ml.Model;

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

    public void classifyImage(Bitmap image, Model model){
        // Creates inputs for reference.
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        // get 1D array of 224 * 224 pixels in image
        int [] intValues = new int[imageSize * imageSize]; //dit heeft size 224x224
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        inputFeature0 = preProcess(byteBuffer, intValues, inputFeature0);

        // Runs model inference and gets result.
        Model.Outputs outputs = model.process(inputFeature0);

        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        String[] classes = {"Pen", "Marker", "Rose"};
        postProcess(classes, outputFeature0);

        model.close();
    }

    public void classifySound(Bitmap image, MobileModelV2 model){
        // Creates inputs for reference.
        //TODO: deze fixed input size correct instellen
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        // get 1D array of 224 * 224 pixels in image
        int [] intValues = new int[imageSize * imageSize];
        image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        inputFeature0 = preProcess(byteBuffer, intValues, inputFeature0);

        // Runs model inference and gets result.
        MobileModelV2.Outputs outputs = model.process(inputFeature0);

        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        String[] classes = {"0", "1", "2", "3"};
        postProcess(classes, outputFeature0);

        model.close();
    }

    private TensorBuffer preProcess(ByteBuffer byteBuffer, int[] intValues, TensorBuffer inputFeature0){
        // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
        int pixel = 0;
        for(int i = 0; i < imageSize; i++){
            for(int j = 0; j < imageSize; j++){
                int val = intValues[pixel++]; // RGB
                float r = ((val >> 16) & 0xFF);
                float g =((val >> 8) & 0xFF);
                float b = (val & 0xFF);
                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);
            }
        }

        inputFeature0.loadBuffer(byteBuffer);
        return inputFeature0;
    }

    private void postProcess(String[] classes, TensorBuffer outputFeature0){
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
        for(int i = 0; i < classes.length; i++){
            s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
        }
        confidence.setText(s);
    }
}