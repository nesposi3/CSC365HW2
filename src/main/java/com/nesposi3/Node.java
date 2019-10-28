package com.nesposi3;

import com.nesposi3.Utils.StaticUtils;

import java.nio.ByteBuffer;

import static com.nesposi3.Utils.StaticUtils.*;

/**
 * Node objects are the Nodes of the Persistent B-Tree.
 * In this B-tree order is 5
 * In this case, Nodes represent websites and store word information
 *
 * Nodes have a maximum size of 4096 bytes, so addresses must be at least 4096 apart
 *
 * Block Format in bytes:
 *          8       8           32      40
 *    | address | parent |   children | keys |
 */
//TODO int[] keys has to be added. Use hash of url for key. Going
public class Node {
    public long address;
    public long parent;
    public long[] children;
    public long[] keys;

    /**
     * This constructor is used for debugging reasons
     * @param address
     * @param parent
     * @param children
     * @param keys
     */
    public Node(long address, long parent, long[] children, long[] keys ) {
        this.address = address;
        this.parent = parent;
        this.children = children;
        this.keys=keys;
    }

    /**
     * This constructor is used to initialize root Node
     *
     */
    public Node(){
        this.children = new long[]{NULL,NULL,NULL,NULL};
        this.keys = new long[]{NULL,NULL,NULL,NULL,NULL};
        this.address = 0;
        this.parent = NULL;
    }
    public Node(Byte[] objectArr){
        byte[] arr = StaticUtils.toPrimitiveBytes(objectArr);
        this.children = new long[NUM_CHILDREN];
        this.keys = new long[K];
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        this.address = buffer.getLong();
        this.parent = buffer.getLong();
        for (int i = 0; i <NUM_CHILDREN; i++) {
            this.children[i] = buffer.getLong();
        }
        for (int i = 0; i < K; i++) {
            this.keys[i] = buffer.getLong();
        }
    }

    public Node(byte[] arr){
        this.children = new long[NUM_CHILDREN];
        this.keys = new long[K];
        ByteBuffer buffer = ByteBuffer.wrap(arr);
        this.address = buffer.getLong();
        this.parent = buffer.getLong();
        for (int i = 0; i <NUM_CHILDREN; i++) {
            this.children[i] = buffer.getLong();
        }
        for (int i = 0; i < K; i++) {
            this.keys[i] = buffer.getLong();
        }

    }
    public byte[] toBytes(){
        int nodeSize = (6 * ADDRESS_SIZE)+ (K*ADDRESS_SIZE) + (NUM_CHILDREN*ADDRESS_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate(nodeSize);
        buffer.putLong(this.address);
        buffer.putLong(this.parent);
        for (int i = 0; i <NUM_CHILDREN ; i++) {
            buffer.putLong(this.children[i]);
        }
        for (int i = 0; i < K ; i++) {
            buffer.putLong(keys[i]);
        }
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
            boolean keys = true;
            for (int i = 0; i < K ; i++) {
                if(this.keys[i]!=other.keys[i]){
                    keys = false;
                }
            }
            return (address && parent && children && keys);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("\nAddress: " + this.address+ "\nParent " + this.parent);
        for (int i = 0; i < NUM_CHILDREN; i++) {
            s.append("\nChild " + i +": " + this.children[i] );
        }
        for (int i = 0; i < K; i++) {
            s.append("\nKey " + i +": " + this.keys[i] );
        }
        return s.toString();
    }
}