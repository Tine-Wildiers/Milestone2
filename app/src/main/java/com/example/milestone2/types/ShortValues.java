package com.example.milestone2.types;

public final class ShortValues {
    private short[] itemsArray;
    private int arraySize;

    public ShortValues(int capacity) {
        if (capacity >= 0) {
            this.itemsArray = new short[capacity];
        } else {
            throw new IllegalArgumentException("capacity < 0");
        }
    }

    public ShortValues(short[] items) {
        this.itemsArray = items;
        this.arraySize = items.length;
    }

    private void b(int var1) {
        short[] var2;
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
                    short[] var3 = new short[var1];
                    if (var2 > 0) {
                        System.arraycopy(this.itemsArray, 0, var3, 0, var2);
                    }

                    this.itemsArray = var3;
                    return;
                }

                this.itemsArray = new short[0];
            }

        } else {
            throw new IllegalArgumentException("capacity");
        }
    }

    public short[] getItemsArray() {
        return this.itemsArray;
    }

    public void add(short[] values) {
        ShortValues var10000 = this;
        int var2 = values.length;
        var10000.add(values, 0, var2);
    }

    public void add(short[] values, int startIndex, int count) {
        ShortValues var10000 = this;
        ShortValues var10001 = this;
        ShortValues var10004 = this;
        this.b(this.arraySize + count);
        short[] var4 = this.itemsArray;
        System.arraycopy(values, startIndex, var4, var10004.arraySize, count);
        var10000.arraySize = var10001.arraySize + count;
    }

    public short get(int index) {
        if (index < this.arraySize) {
            return this.itemsArray[index];
        } else {
            throw new ArrayIndexOutOfBoundsException("index");
        }
    }

    public int size() {
        return this.arraySize;
    }

    public void setSize(int size) {
        this.b(size);
        this.arraySize = size;
    }

    public void clear() {
        this.arraySize = 0;
    }

    public ShortValues[] splitIntoThree() {
        int partSize = arraySize / 3;
        int remainder = arraySize % 3;

        ShortValues[] result = new ShortValues[3];

        int startIndex = 0;
        for (int i = 0; i < 3; i++) {
            int currentPartSize = partSize + (i < remainder ? 1 : 0);
            short[] partArray = new short[currentPartSize];
            System.arraycopy(this.getItemsArray(), startIndex, partArray, 0, currentPartSize);
            result[i] = new ShortValues(partArray);
            startIndex += currentPartSize;
        }

        return result;
    }
}
