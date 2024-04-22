package com.example.milestone2;

public class MelSpectrogram {
    private final static float    fMin                 = 0.0F;
    private final static int       n_fft                = 2048;
    private final static int       hop_length           = 512;
    private final static int	   n_mels               = 256;

    private final static float    sampleRate           = 44100;
    private final static float    fMax                 = (float) (sampleRate/2.0);

    FFT fft = new FFT();


    public float[][] process(float[] y) {
        // 1 librosa.load = y

        // 2 librosa.feature.melspectrogram
        float [][] melSpectrogram = melSpectrogram(y); //Klopt

        // 3 librosa.power_to_db
        final float[][] specTroGram = powerToDb(melSpectrogram, 100); //Klopt

        return specTroGram;
    }

    private float[][] melSpectrogram(float[] y){
        // 2.1 spectrogram
        // 2.2 mel_basis
        // 2.3 melspec = melfilters x spectrogram

        float[][] melBasis = melFilter(); //256x1025  = deze is juist
        float[][] spectro = stftMagSpec(y); //1025x403  = deze is juist buiten de randwaarden (maar boeit ni?)
        float[][] melS = new float[melBasis.length][spectro[0].length]; //256x403

        for (int i = 0; i < melBasis.length; i++){
            for (int j = 0; j < spectro[0].length; j++){
                for (int k = 0; k < melBasis[0].length; k++){
                    melS[i][j] += melBasis[i][k]*spectro[k][j];
                }
            }
        }

        //TODO: inlezen in Parallel Colt voor optimalisatie van deze berekening.
        return melS; //Klopt
    }

    private float[][] stftMagSpec(float[] y){
        //Short-time Fourier transform (STFT)
        final float[] fftwin = getWindow(); //Hann Window - zelfde als in python

        float[] ypad = new float[n_fft+y.length];
        for (int i = 0; i < n_fft/2; i++){
            ypad[(n_fft/2)-i-1] = y[i+1];
            ypad[(n_fft/2)+y.length+i] = y[y.length-2-i];
        }
        for (int j = 0; j < y.length; j++){
            ypad[(n_fft/2)+j] = y[j];
        }


        final float[][] frame = yFrame(ypad);
        float[][] fftmagSpec = new float[1+n_fft/2][frame[0].length];
        float[] fftFrame = new float[n_fft];
        for (int k = 0; k < frame[0].length; k++){
            for (int l =0; l < n_fft; l++){
                fftFrame[l] = fftwin[l]*frame[l][k];
            }
            float[] magSpec = magSpectrogram(fftFrame);
            for (int i =0; i < 1+n_fft/2; i++){
                fftmagSpec[i][k] = magSpec[i];
            }
        }
        return fftmagSpec;
    }

    private float[] magSpectrogram(float[] frame){
        float[] magSpec = new float[frame.length];
        fft.process(frame);
        for (int m = 0; m < frame.length; m++) {
            magSpec[m] = (float) (fft.real[m] * fft.real[m] + fft.imag[m] * fft.imag[m]);
        }
        return magSpec;
    }

    private float[] getWindow(){
        //Return a Hann window for even n_fft.
        //The Hann window is a taper formed by using a raised cosine or sine-squared
        //with ends that touch zero.
        float[] win = new float[n_fft];
        for (int i = 0; i < n_fft; i++){
            win[i] = (float) (0.5 - 0.5 * Math.cos(2*Math.PI*i/n_fft));
        }
        return win;
    }

    private float[][] yFrame(float[] ypad){
        final int n_frames = 1 + (ypad.length - n_fft) / hop_length;
        float[][] winFrames = new float[n_fft][n_frames];
        for (int i = 0; i < n_fft; i++){
            for (int j = 0; j < n_frames; j++){
                winFrames[i][j] = ypad[j*hop_length+i];
            }
        }
        return winFrames;
    }

    private float[][] powerToDb(float[][] S, float topDb){
        double amin = 1e-10;
        int numRows = S.length;
        int numCols = S[0].length;
        float[][] result = new float[numRows][numCols];

        double max = Float.NEGATIVE_INFINITY;

        int indi = 0;
        int indj = 0;
        for (int i = 0; i <= S.length -1; i++) {
            for (int j = 0; j <= S[i].length - 10; j++) {
                //TODO: deze manuele -10 uitzoeken, waarom zijn de laatste 10 colums brol?
                if (S[i][j] > max) {
                    indi = i;
                    indj = j;
                    max = S[i][j];
                }
            }
        }

        double ref_value = max;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                float mag = (float) Math.max(amin, S[i][j]);

                float logSpec = (float) (10.0 * Math.log10(mag) - 10.0 * Math.log10(ref_value));


                //logSpec = (float) Math.max(logSpec, ref_value - topDb);
                result[i][j] = logSpec;
            }
        }
        return result;
    }

    private double[][] powerToDbOld(float[][] melS){
        //Convert a power spectrogram (amplitude squared) to decibel (dB) units
        //  This computes the scaling ``10 * log10(S / ref)`` in a numerically
        //  stable way.
        double[][] log_spec = new double[melS.length][melS[0].length];
        double maxValue = -100;
        for (int i = 0; i < melS.length; i++){
            for (int j = 0; j < melS[0].length; j++){
                double magnitude = Math.abs(melS[i][j]);
                if (magnitude > 1e-10){
                    log_spec[i][j]=10.0*log10(magnitude);
                }else{
                    log_spec[i][j]=10.0*(-10);
                }
                if (log_spec[i][j] > maxValue){
                    maxValue = log_spec[i][j];
                }
            }
        }

        for (int i = 0; i < melS.length; i++){
            for (int j = 0; j < melS[0].length; j++){
                if (log_spec[i][j] < maxValue - 100.0){
                    log_spec[i][j] = maxValue - 100.0;
                }
            }
        }
        return log_spec;
    }

    private float[][] melFilter(){
        //Create a Filterbank matrix to combine FFT bins into Mel-frequency bins.
        // Center freqs of each FFT bin
        final double[] fftFreqs = fftFreq();
        //'Center freqs' of mel bands - uniformly spaced between limits
        final double[] melF = melFreq(n_mels+2);

        double[] fdiff = new double[melF.length-1];
        for (int i = 0; i < melF.length-1; i++){
            fdiff[i] = melF[i+1]-melF[i];
        }

        double[][] ramps = new double[melF.length][fftFreqs.length];
        for (int i = 0; i < melF.length; i++){
            for (int j = 0; j < fftFreqs.length; j++){
                ramps[i][j] = melF[i]-fftFreqs[j];
            }
        }

        double[][] weights = new double[n_mels][1+n_fft/2];
        for (int i = 0; i < n_mels; i++){
            for (int j = 0; j < fftFreqs.length; j++){
                double lowerF = -ramps[i][j] / fdiff[i];
                double upperF = ramps[i+2][j] / fdiff[i+1];
                if (lowerF > upperF && upperF>0){
                    weights[i][j] = upperF;
                }else if (lowerF > upperF && upperF<0){
                    weights[i][j] = 0;
                }else if (lowerF < upperF && lowerF>0){
                    weights[i][j] =lowerF;
                }else if (lowerF < upperF && lowerF<0){
                    weights[i][j] = 0;
                }else {}
            }
        }

        double enorm[] = new double[n_mels];
        for (int i = 0; i < n_mels; i++){
            enorm[i] = 2.0 / (melF[i+2]-melF[i]);
            for (int j = 0; j < fftFreqs.length; j++){
                weights[i][j] *= enorm[i];
            }
        }

        int rows = weights.length;
        int cols = weights[0].length;
        float[][] floatArray = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                floatArray[i][j] = (float) weights[i][j];
            }
        }

        return floatArray;
    }

    private double[] fftFreq() {
        //Alternative implementation of np.fft.fftfreqs
        double[] freqs = new double[1+n_fft/2];
        for (int i = 0; i < 1+n_fft/2; i++){
            freqs[i] = 0 + (sampleRate/2)/(n_fft/2) * i;
        }
        return freqs;
    }

    private double[] melFreq(int numMels) {
        //'Center freqs' of mel bands - uniformly spaced between limits
        double[] LowFFreq = new double[1];
        double[] HighFFreq = new double[1];
        LowFFreq[0] = fMin;
        HighFFreq[0] = fMax;
        final double[] melFLow    = freqToMel(LowFFreq);
        final double[] melFHigh   = freqToMel(HighFFreq);
        double[] mels = new double[numMels];
        for (int i = 0; i < numMels; i++) {
            mels[i] = melFLow[0] + (melFHigh[0] - melFLow[0]) / (numMels-1) * i;
        }
        return melToFreq(mels);
    }

    private double[] melToFreq(double[] mels) {
        // Fill in the linear scale
        final double f_min = 0.0;
        final double f_sp = 200.0 / 3;
        double[] freqs = new double[mels.length];

        // And now the nonlinear scale
        final double min_log_hz = 1000.0;                         // beginning of log region (Hz)
        final double min_log_mel = (min_log_hz - f_min) / f_sp;  // same (Mels)
        final double logstep = Math.log(6.4) / 27.0;

        for (int i = 0; i < mels.length; i++) {
            if (mels[i] < min_log_mel){
                freqs[i] =  f_min + f_sp * mels[i];
            }else{
                freqs[i] = min_log_hz * Math.exp(logstep * (mels[i] - min_log_mel));
            }
        }
        return freqs;
    }

    protected double[] freqToMel(double[] freqs) {
        final double f_min = 0.0;
        final double f_sp = 200.0 / 3;
        double[] mels = new double[freqs.length];

        final double min_log_hz = 1000.0;                         // beginning of log region (Hz)
        final double min_log_mel = (min_log_hz - f_min) / f_sp ;  // # same (Mels)
        final double logstep = Math.log(6.4) / 27.0;              // step size for log region

        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] < min_log_hz){
                mels[i] = (freqs[i] - f_min) / f_sp;
            }else{
                mels[i] = min_log_mel + Math.log(freqs[i]/min_log_hz) / logstep;
            }
        }
        return mels;
    }

    // log10
    private double log10(double value) {
        return Math.log(value) / Math.log(10);
    }
}