package com.nesposi3;

import com.nesposi3.Utils.StaticUtils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.nesposi3.Utils.StaticUtils.*;

/**
 * Node objects are the Nodes of the Persistent B-Tree.
 * In this B-tree order is 5
 * In this case, Nodes represent websites and store word information
 *
 * Nodes have a maximum size of 4096 bytes, so addresses must be at least 4096 apart
 *
 * Block Format in bytes:
 *          8       8           40      32
 *    | address | parent |   children | keys |
 */
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
        this.children = new long[]{NULL,NULL,NULL,NULL,NULL};
        this.keys = new long[]{NULL,NULL,NULL,NULL};
        this.address = 0;
        this.parent = NULL;
    }

    /**
     * Constructor to initialize empty node at specified position
     * @param pos
     */
    public Node(long pos){
        this.children = new long[]{NULL,NULL,NULL,NULL};
        this.keys = new long[]{NULL,NULL,NULL,NULL,NULL};
        this.address = pos;
        this.parent = NULL;
    }

    /**
     * Create a Node object from a Byte object array
     * @param objectArr Byte object array representing a Node
     */
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

    /**
     * Create a Node object from a byte array
     * @param arr byte primitive array representing a node
     */
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

    /**
     * Serializes this Node object into a byte representation
     * @return a byte[] representing the node
     */
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

    @Override
    public int hashCode() {
        int addCode = Long.hashCode(address);
        int parentCode = Long.hashCode(parent);
        int code = addCode ^ parentCode;
        for (int i = 0; i <NUM_CHILDREN ; i++) {
            code ^= Long.hashCode(children[i]);
        }
        for (int i = 0; i <K ; i++) {
            code ^= Long.hashCode(keys[i]);
        }
        return code;
    }
    public boolean leafStatus(){
        boolean isLeaf = true;
        for (int i = 0; i <NUM_CHILDREN ; i++) {
            if(children[i]!=-1){
                isLeaf = false;
            }
        }
        return isLeaf;
    }
    public int numKeys(){
        int i = 0;
        for (int j = 0; j < K ; j++) {
            if(keys[j] !=-1){
                i++;
            }
        }
        return i;
    }
    public boolean isFull(){
        return numKeys() == K;
    }

    /**
     * Returns a new Node representing the first half of a split
     * @param newParent the new Parent address
     * @param i the index
     * @param newAddr The address this node exists at
     * @return
     */
    public Node getLeftSplitNode(long newParent,long newAddr, int i){
        long[] newChildren = getNullLongArray(NUM_CHILDREN);
        long[] newKeys = getNullLongArray(K);
        for (int j = 0; j <i ; j++) {
            newChildren[j] = this.children[j];
            newKeys[j] = this.keys[j];
        }
        return new Node(newAddr,newParent,newChildren,newKeys);

    }

    /**
     * Returns a new Node representing the second half of a split
     * @param newParent the new partent adress
     * @param i the index
     * @param newAddr The address the new node exists at
     * @return
     */
    public Node getRightSplitNode(long newParent,long newAddr,int i){
        long[] newChildren = getNullLongArray(NUM_CHILDREN);
        long[] newKeys = getNullLongArray(K);
        Node out = new Node(newAddr,newParent,newChildren,newKeys);
        for (int j = i; j <NUM_CHILDREN ; j++) {
            out.addChild(this.children[j]);
        }
        for (int j = i; j <K ; j++) {
            out.addKey(this.keys[j]);
        }
        return out;


    }
    private long[] getNullLongArray(int size){
        long[] out = new long[size];
        for (int i = 0; i <size ; i++) {
            out[i] = NULL;
        }
        return out;
    }
    public void addChild(long addr){
        for (int i = 0; i <NUM_CHILDREN ; i++) {
            // If the key has found something bigger than it, shift up and add
            if(children[i]==NULL){
                children[i] = addr;
                return;
            }
        }
    }
    public void addKey(long key){
        long[] oldKeys = Arrays.copyOf(this.keys,K);
        for (int i = 0; i <K ; i++) {
            if(keys[i]==NULL){
                // No more to compare with, set key and break;
                keys[i] = key;
                return;
            }
            // If the key has found something bigger than it, shift up and add
            if(key <= keys[i]){
                for (int j = i; j <K-1 ; j++) {
                    this.keys[j+1] = oldKeys[j];
                }
                this.keys[i] = key;
                return;
            }
        }
    }
}
