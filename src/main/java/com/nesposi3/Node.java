package com.nesposi3;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Node objects are the Nodes of the Persistent B-Tree.
 * In this B-tree order is 5
 * In this case, Nodes represent websites and store word information
 *
 * Nodes have a maximum size of 4096 bytes, so addresses must be at least 4096 apart
 *
 * Block Format in bytes:
 *          8       8       32                   4                    m
 *    | address | parent | children | number of bytes in url | m bytes of url |
 */
public class Node {
    public static final long NULL = -1;
    public static final long[] NULL_CHILD_ARRAY = new long[]{NULL,NULL,NULL,NULL};
    public static final int K = 5;
    public static final int NUM_CHILDREN = K-1;
    private static final int ADDRESS_SIZE = 8;
    public long address;
    public long parent;
    public long[] children;
    public String url;

    /**
     * This constructor is used for debugging reasons
     * @param address
     * @param parent
     * @param children
     * @param url
     */
    public Node(long address, long parent, long[] children, String url) {
        this.address = address;
        this.parent = parent;
        this.children = children;
        this.url = url;
    }

    /**
     * This constructor is used to initialize root Node
     * @param url Url of the website
     */
    public Node(String url){
        this.children = NULL_CHILD_ARRAY;
        this.address = 0;
        this.parent = NULL;
        this.url = url;
    }
    public Node(byte[] arr){
        this.children = new long[4];
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        this.address = buffer.getLong();
        this.parent = buffer.getLong();
        for (int i = 0; i <NUM_CHILDREN; i++) {
            this.children[i] = buffer.getLong();
        }
        int urlSize = buffer.getInt();
        byte[] urlBuff = new byte[urlSize];
        buffer.get(urlBuff);
        this.url= new String(urlBuff);
    }
    public byte[] toBytes(){
        byte[] urlBytes = this.url.getBytes();
        int urlSize = urlBytes.length;
        int nodeSize = (6 * ADDRESS_SIZE)+ (K*ADDRESS_SIZE) + 4 + (urlSize);
        ByteBuffer buffer = ByteBuffer.allocate(nodeSize);
        buffer.putLong(this.address);
        buffer.putLong(this.parent);
        for (int i = 0; i <NUM_CHILDREN ; i++) {
            buffer.putLong(this.children[i]);
        }
        buffer.putInt(urlSize);
        buffer.put(urlBytes);
        return buffer.array();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Node)){
            return false;
        }else{
            Node other = (Node) obj;
            boolean address = other.address == this.address;
            boolean parent = other.parent == this.parent;
            boolean children = true;
            for (int i = 0; i < NUM_CHILDREN ; i++) {
                if(this.children[i]!=other.children[i]){
                    children = false;
                }
            }
            boolean url = this.url.equals(other.url);
            return (address && parent && children && url);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("URL: " + this.url + "\nAddress: " + this.address+ "\nParent " + this.parent);
        for (int i = 0; i < NUM_CHILDREN; i++) {
            s.append("\nChild " + i +": " + address );
        }
        return s.toString();
    }
}
