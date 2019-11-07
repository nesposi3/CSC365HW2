package com.nesposi3.Utils;

import java.nio.ByteBuffer;

import static org.apache.commons.codec.digest.DigestUtils.md5;

public class ClusteringUtils {
    /**
     * Convert a string into a long (64 bit) hash code
     * Takes the lower 64 bits of a md5 hash
     * @param input
     * @return
     */
    public static long stringHash64(String input){
        byte[] totalMd5 = md5(input);
        byte[] longMd5 = new byte[8];
        for (int i = 0; i <8 ; i++) {
            longMd5[i] = totalMd5[i];
        }
        ByteBuffer buffer = ByteBuffer.wrap(longMd5);
        return buffer.getLong();
    }

}
