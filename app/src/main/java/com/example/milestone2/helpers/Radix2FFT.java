package com.example.milestone2.helpers;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.example.milestone2.types.DoubleValues;
import com.example.milestone2.types.ShortValues;

public class Radix2FFT {
    private final int inputSize;
    private final int stages; //The number of stages in the FFT computation
    private final int stagesm1;

    public final int fftSize; //Half the size of the FFT result, used for setting the size of the output array.

    private final Complex[] x; //An array of Complex objects representing the input sequence.
    private final Complex[] dft; //Discrete Fourier Transform. An array of Complex objects representing the output of the FFT.
    private final double TwoPi_N;

    private final Complex WN = new Complex();    // Wn is the exponential weighting function in the form a + jb
    private final Complex TEMP = new Complex();  // TEMP is used to save computation in the butterfly calc

    public Radix2FFT(int inputSize) {
        this.inputSize = inputSize;
        this.stages = (int) log(inputSize, 2d);
        // Radix2FFT wordt gebruikt om efficiente berekeningen te doen op inputs met size meervoud van 2
        if (Math.pow(2, stages) != inputSize)
            throw new UnsupportedOperationException("n should be with power of 2");

        this.fftSize = inputSize/2;
        this.TwoPi_N = Math.PI * 2 / inputSize;    // constant to save computational time.  = 2*PI / N
        this.stagesm1 = stages - 1;

        this.x = new Complex[inputSize];
        this.dft = new Complex[inputSize];

        // Lege arrays worden hier al gemaakt, is computationeel efficienter.
        for (int i = 0; i < inputSize; i++) {
            x[i] = new Complex();
            dft[i] = new Complex();
        }
    }

    public void run(ShortValues input, DoubleValues output) {

        if(input.size() != inputSize) throw new UnsupportedOperationException();

        // init input values
        final short[] itemsArray = input.getItemsArray();

        applyHammingWindow(itemsArray);

        for (int i = 0; i < inputSize; i++) {
            final Complex complex = x[i];
            complex.re = itemsArray[i];
            complex.im = 0;
        }

        // perform fft
        rad2FFT(x, dft);

        // set output
        output.setSize(fftSize);
        final double[] outputItems = output.getItemsArray();
        for (int i = 0; i < fftSize; i++) {
            outputItems[i] = calculateOutputValue(dft[i]);
        }
    }

    private void applyHammingWindow(short[] input) {
        final int N = input.length;
        for (int i = 0; i < N; i++) {
            input[i] = (short) (input[i] * (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (N - 1))));
        }
    }

    private double calculateOutputValue(Complex complex) {
        final double magnitude = Math.sqrt(complex.re * complex.re + complex.im * complex.im);

        // convert to magnitude to dB
        return 20 * Math.log10(magnitude / inputSize);
    }

    private void rad2FFT(Complex[] x, Complex[] DFT) {
        int BSep;                  // BSep is memory spacing between butterflies
        int BWidth;                // BWidth is memory spacing of opposite ends of the butterfly
        int P;                     // P is number of similar Wn's to be used in that stage
        int iaddr;                 // bitmask for bit reversal
        int ii;                    // Integer bitfield for bit reversal (Decimation in Time)

        int DFTindex = 0;          // Pointer to first elements in DFT array

        // Decimation In Time - x[n] sample sorting
        for (int i = 0; i < inputSize; i++, DFTindex++) {
            final Complex pX = x[i];        // Calculate current x[n] from index i.
            ii = 0;                         // Reset new address for DFT[n]
            iaddr = i;                      // Copy i for manipulations
            for (int l = 0; l < stages; l++)     
            {
                if ((iaddr & 0x01) != 0)    // Detemine least significant bit
                    ii += (1 << (stagesm1 - l)); // Increment ii by 2^(M-1-l) if lsb was 1
                iaddr >>= 1;                // right shift iaddr to test next bit. Use logical operations for speed increase
                if (iaddr == 0)
                    break;
            }

            final Complex dft = DFT[ii];    // Calculate current DFT[n] from bit reversed index ii
            dft.re = pX.re;                 // Update the complex array with address sorted time domain signal x[n]
            dft.im = pX.im;                 // NB: Imaginary is always zero
        }

        // FFT Computation by butterfly calculation
        for (int stage = 1; stage <= stages; stage++) // Loop for M stages, where 2^M = N
        {
            BSep = (int) (Math.pow(2, stage));  // Separation between butterflies = 2^stage
            P = inputSize / BSep;                       // Similar Wn's in this stage = N/Bsep
            BWidth = BSep / 2;                  // Butterfly width (spacing between opposite points) = Separation / 2.

            for (int j = 0; j < BWidth; j++) // Loop for j calculations per butterfly
            {
                if (j != 0)              // Save on calculation if R = 0, as WN^0 = (1 + j0)
                {
                    WN.re = cos(TwoPi_N * P * j);     // Calculate Wn (Real and Imaginary)
                    WN.im = -sin(TwoPi_N * P * j);
                }

                // HiIndex is the index of the DFT array for the top value of each butterfly calc
                for (int HiIndex = j; HiIndex < inputSize; HiIndex += BSep) // Loop for HiIndex Step BSep butterflies per stage
                {
                    final Complex pHi = DFT[HiIndex];                  // Point to higher value
                    final Complex pLo = DFT[HiIndex + BWidth];         // Point to lower value

                    if (j != 0)                            // If exponential power is not zero...
                    {
                        // Perform complex multiplication of LoValue with Wn
                        TEMP.re = (pLo.re * WN.re) - (pLo.im * WN.im);
                        TEMP.im = (pLo.re * WN.im) + (pLo.im * WN.re);

                        // Find new LoValue (complex subtraction)
                        pLo.re = pHi.re - TEMP.re;
                        pLo.im = pHi.im - TEMP.im;

                        // Find new HiValue (complex addition)
                        pHi.re = (pHi.re + TEMP.re);
                        pHi.im = (pHi.im + TEMP.im);
                    } else {
                        TEMP.re = pLo.re;
                        TEMP.im = pLo.im;

                        // Find new LoValue (complex subtraction)
                        pLo.re = pHi.re - TEMP.re;
                        pLo.im = pHi.im - TEMP.im;

                        // Find new HiValue (complex addition)
                        pHi.re = (pHi.re + TEMP.re);
                        pHi.im = (pHi.im + TEMP.im);
                    }
                }
            }
        }
    }

    public static double log(double value, double base) {
        return Math.log(value) / Math.log(base);
    }

    private static class Complex {
        double re, im;
    }
}
