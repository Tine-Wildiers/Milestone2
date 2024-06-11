package com.example.milestone2;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.milestone2.helpers.MelSpectrogram;
import com.example.milestone2.ml.MobileModelV2;
import com.example.milestone2.readers.WavFileReader;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class ModelHandler extends AppCompatActivity {
    int imageSize = 224;
    TextView result;
    TextView confidence;
    ImageView imageView;
    ColorMapper cM;
    Context context;

    public ModelHandler(TextView result, TextView confidence, ImageView imageView, ColorMapper colorMapper, Context context) {
        this.result = result;
        this.confidence = confidence;
        this.imageView = imageView;
        this.cM = colorMapper;
        this.context = context;
    }

    public float[] classifySound(Bitmap image, MobileModelV2 model){
        TensorBuffer inputFeature0 = preProcess(image);

        MobileModelV2.Outputs outputs = model.process(inputFeature0);

        float[] confidences = postProcess(outputs.getOutputFeature0AsTensorBuffer());

        model.close();
        return confidences;
    }

    public void preProcessImage(float[] audioData){
        MelSpectrogram melSpectrogram = new MelSpectrogram();
        float[][] melspec = melSpectrogram.process(audioData);

        int height = melspec.length; // Width of the image
        int width = melspec[0].length; // Height of the image

        int[] values = {195, 193, 109, 71, 53, 43, 35, 30, 27, 24, 21, 19, 18, 16, 15, 14, 14, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

        int sum = 0;
        for (int num : values) {
            sum += num;
        }

        // Create a blank bitmap with the specified width and height
        Bitmap image = Bitmap.createBitmap(width, sum, Bitmap.Config.ARGB_8888);

        int bigindex;

        // Loop through each pixel in the float array and set the corresponding pixel in the bitmap
        for (int y = 0; y < width; y++) {
            bigindex = sum-1;
            for (int x = 0; x < height; x++) {
                //TODO: deze berekening correcter maken
                int index = (int) (melspec[x][y]+100)* cM.getColorMapperSize()/100;

                int color = cM.getColor(index);

                for(int z = 0; z < values[x]; z++){
                    image.setPixel(y, bigindex, color);// Set pixel color in the bitmap
                    bigindex -= 1;
                }
            }
        }

        image = scaleBitmap(image);

        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);

        // Now, you can use the bitmap in your ImageView
        imageView.setImageBitmap(image);


        try {
            classifySound(image, MobileModelV2.newInstance(context));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Measurement preProcessImage(InputStream inputStream) throws IOException {
        float[] audioData = WavFileReader.readWavFile(inputStream);

        MelSpectrogram melSpectrogram = new MelSpectrogram();
        float[][] melspec = melSpectrogram.process(audioData);

        int height = melspec.length; // Width of the image
        int width = melspec[0].length; // Height of the image

        int[] values = {195, 193, 109, 71, 53, 43, 35, 30, 27, 24, 21, 19, 18, 16, 15, 14, 14, 12, 12, 11, 11, 10, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

        int sum = 0;
        for (int num : values) {
            sum += num;
        }

        // Create a blank bitmap with the specified width and height
        Bitmap image = Bitmap.createBitmap(width, sum, Bitmap.Config.ARGB_8888);

        int bigindex;

        // Loop through each pixel in the float array and set the corresponding pixel in the bitmap
        for (int y = 0; y < width; y++) {
            bigindex = sum-1;
            for (int x = 0; x < height; x++) {
                int index = (int) (melspec[x][y]+100)* cM.getColorMapperSize()/100;

                int color = cM.getColor(index);

                for(int z = 0; z < values[x]; z++){
                    image.setPixel(y, bigindex, color);// Set pixel color in the bitmap
                    bigindex -= 1;
                }
            }
        }

        image = scaleBitmap(image);

        int dimension = Math.min(image.getWidth(), image.getHeight());
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, true);

        try {
            float[] confidences = classifySound(image, MobileModelV2.newInstance(context));
            return new Measurement(image, confidences);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Bitmap scaleBitmap(Bitmap image){
        int newWidth = 310;  //image.getWidth();
        int newHeight = 308; //image.getHeight();

        Bitmap stretchedBitmap = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
        return stretchedBitmap;
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

    private float[] postProcess(TensorBuffer outputFeature0){
        String[] classes = {"0", "1", "2", "3"};
        float[] confidences = outputFeature0.getFloatArray();

        int maxPos = 0;
        float maxConfidence = 0;
        for(int i = 0; i < confidences.length; i++){
            if(confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }

        result.setText(classes[maxPos]);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; i++) {
            sb.append(String.format(Locale.US, "%s: %.1f%%\n", classes[i], confidences[i] * 100));
        }
        confidence.setText(sb.toString());
        return confidences;
    }
}