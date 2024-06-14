package com.example.milestone2.types;
public final class DoubleValues {
    private double[] itemsArray;
    private int arraySize;

    public DoubleValues() {
        this.itemsArray = new double[0];
    }

    private void b(int var1) {
        double[] var2;
        if ((var2 = this.itemsArray).length < var1) {
            int var3;
            if (var2.length == 0) {
                var3 = 4;
            } else {
                var3 = var2.length * 2;
            }

            if (var3 >= var1) {
                var1 = var3;
            }

            this.a(var1);
        }

    }

    private void a(int var1) {
        int var2;
        if (var1 >= (var2 = this.arraySize)) {
            if (var1 != var2) {
                if (var1 > 0) {
                    double[] var3 = new double[var1];
                    if (var2 > 0) {
                        System.arraycopy(this.itemsArray, 0, var3, 0, var2);
                    }

                    this.itemsArray = var3;
                    return;
                }

                this.itemsArray = new double[0];
            }

        } else {
            throw new IllegalArgumentException("capacity");
        }
    }

    public double[] getItemsArray() {
        return this.itemsArray;
    }

    public int size() {
        return this.arraySize;
    }

    public void setSize(int size) {
        this.b(size);
        this.arraySize = size;
    }
}
