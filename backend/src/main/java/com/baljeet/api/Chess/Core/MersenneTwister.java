package com.baljeet.api.Chess.Core;

//MT19937
public class MersenneTwister {
    private static final int w = 32;
    private static final int n = 624;
    private static final int m = 397;
    private static final int r = 31;

    private static final int a = 0x9908B0DF;
    private static final int u = 11;
    private static final int d = 0xFFFFFFFF;

    private static final int s = 7;
    private static final int b = 0x9D2C5680;
    private static final int t = 15;
    private static final int c = 0xEFC60000;

    private static final int l = 18;
    private final int[] initial;
    private int index;

    public MersenneTwister(int seed) {
        initial = new int[n];
        initial[0] = seed;

        for (int i = 1; i < n; i++) {
            initial[i] = 1812433253 * (initial[i - 1] ^ (initial[i - 1] >>> (w - 2))) + i;
        }
        index = 0;
    }

    public int nextInt() {
        int k = index;

        int j = k - (n - 1);
        if (j < 0) j = j + n;

        int x = ((initial[k] & 0xFFFFFFFF << u) | (initial[j] & 0xFFFFFFFF >>> (w - r)));
        int xA = x >> 1;
        if ((x & 1) != 1) xA = xA ^ a;

        j = k - (n - m);
        j = (j < 0) ? j + n : j;

        x = initial[j] ^ xA;

        initial[k++] = x;
        index = (k == n) ? 0 : k;

        int y = x ^ ((x >>> u) & d);
        y = y ^ ((y << s) & b);
        y = y ^ ((y << t) & c);
        return y ^ (y >>> l);

    }

    public long nextLong() {
        return nextInt() | ((long) nextInt() << 32);
    }

}
