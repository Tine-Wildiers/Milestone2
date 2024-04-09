package com.example.milestone2.types;

public final class ShortValues {
    private short[] a;
    private int b;

    public ShortValues() {
        this.a = new short[0];
    }

    public ShortValues(int capacity) {
        if (capacity >= 0) {
            this.a = new short[capacity];
        } else {
            throw new IllegalArgumentException("capacity < 0");
        }
    }

    public ShortValues(short[] items) {
        this.a = items;
        this.b = items.length;
    }

    private void b(int var1) {
        short[] var2;
        if ((var2 = this.a).length < var1) {
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
        if (var1 >= (var2 = this.b)) {
            if (var1 != var2) {
                if (var1 > 0) {
                    short[] var3 = new short[var1];
                    if (var2 > 0) {
                        System.arraycopy(this.a, 0, var3, 0, var2);
                    }

                    this.a = var3;
                    return;
                }

                this.a = new short[0];
            }

        } else {
            throw new IllegalArgumentException("capacity");
        }
    }

    public short[] getItemsArray() {
        return this.a;
    }

    public void add(short value) {
        this.b(this.b + 1);
        short[] var10000 = this.a;
        int var2;
        this.b = (var2 = this.b) + 1;
        var10000[var2] = value;
    }

    public void add(int location, short value) {
        int var3;
        if (location <= (var3 = this.b)) {
            this.b(var3 + 1);
            if (location < (var3 = this.b)) {
                short[] var10000 = this.a;
                int var10002 = var3;
                var3 = location + 1;
                int var4 = var10002 - location;
                System.arraycopy(var10000, location, var10000, var3, var4);
            }

            this.a[location] = value;
            int var10001 = this.b++;
        } else {
            throw new ArrayIndexOutOfBoundsException("location");
        }
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
        this.b(this.b + count);
        short[] var4 = this.a;
        System.arraycopy(values, startIndex, var4, var10004.b, count);
        var10000.b = var10001.b + count;
    }

    public void set(int location, short value) {
        if (location < this.b) {
            this.a[location] = value;
        } else {
            throw new ArrayIndexOutOfBoundsException("location");
        }
    }

    public short get(int index) {
        if (index < this.b) {
            return this.a[index];
        } else {
            throw new ArrayIndexOutOfBoundsException("index");
        }
    }

    public int size() {
        return this.b;
    }

    public void setSize(int size) {
        this.b(size);
        this.b = size;
    }

    public void remove(int location) {
        int var2;
        if (location < (var2 = this.b)) {
            ShortValues var10000 = this;
            int var3;
            this.b = var3 = var2 - 1;
            short[] var4 = var10000.a;
            int var10002 = var3;
            var3 = location + 1;
            var2 = var10002 - location;
            System.arraycopy(var4, var3, var4, location, var2);
        } else {
            throw new ArrayIndexOutOfBoundsException("location");
        }
    }

    public void clear() {
        this.b = 0;
    }

    public void disposeItems() {
        this.clear();
        this.a = new short[0];
    }

    public Class<Short> getValuesType() {
        return Short.class;
    }
}
