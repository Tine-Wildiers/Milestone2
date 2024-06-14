package com.example.milestone2.types;

public final class LongValues {
    private long[] itemsArray;
    private int arraySize;

    public LongValues(int capacity) {
        if (capacity >= 0) {
            this.itemsArray = new long[capacity];
        } else {
            throw new IllegalArgumentException("capacity < 0");
        }
    }

    private void b(int var1) {
        long[] var2;
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
                    long[] var3 = new long[var1];
                    if (var2 > 0) {
                        System.arraycopy(this.itemsArray, 0, var3, 0, var2);
                    }

                    this.itemsArray = var3;
                    return;
                }

                this.itemsArray = new long[0];
            }

        } else {
            throw new IllegalArgumentException("capacity");
        }
    }

    public int size() {
        return this.arraySize;
    }

    public void setSize(int size) {
        this.b(size);
        this.arraySize = size;
    }
}
