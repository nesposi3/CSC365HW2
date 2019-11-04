package com.nesposi3.Utils;

public class StaticUtils {
    public static final String BTREE_FILE_NAME ="storage/b_tree.txt";
    public static final long NULL = -1;
    public static final int K = 4;
    public static final int NUM_CHILDREN = K+1;
    public static final int ADDRESS_SIZE = 8;
    public static final int BLOCK_SIZE = 4096;
    public static final int CACHE_MAX_SIZE = 100;

    /**
     * Converts a Byte object array to a primitive byte array
     * @param objectBytes Byte object array to convert
     * @return the primitve byte array representing the object Byte array
     */
    public static byte[] toPrimitiveBytes(Byte[] objectBytes){
        byte[] bytes = new byte[objectBytes.length];
        for (int i = 0; i <objectBytes.length ; i++) {
            bytes[i] = objectBytes[i];
        }
        return bytes;
    }

    /**
     * Converts a byte primitive array to a Byte object array
     * @param primBytes byte primitive array to voncert
     * @return the Byte object array representing the primitive byte array
     */
    public static Byte[] fromPrimitiveBytes(byte[] primBytes){
        Byte[] bytes = new Byte[primBytes.length];
        for (int i = 0; i < primBytes.length ; i++) {
            bytes[i] = primBytes[i];

        }
        return bytes;
    }
}
