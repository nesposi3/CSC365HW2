package com.nesposi3;

import com.nesposi3.Utils.BoundedLinkedHashMap;
import com.nesposi3.Utils.StaticUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.nesposi3.Utils.StaticUtils.*;

/**
 * A persistent, file-based BTree with IOCache
 */
public class BTree {
    private Node root;
    private BoundedLinkedHashMap<Long, Byte[]> cache;

    /**
     * This method checks if the requested node is in cache, or if not, on disk.
     *
     * @param address The address to look at
     * @return The node at said address, or null if it does not exist
     * @throws IOException IOException represents a fatal error during execution, should lead to program shutdown
     */
    public Node readNodeFromFile(long address) {
        if (cache.containsValue(address)) {
            // This node is cached
            return new Node(cache.get(address));
        }
        File f = new File(BTREE_FILE_NAME);
        try {
            if (f.exists()) {
                RandomAccessFile btreeFile = new RandomAccessFile(f, "rw");
                btreeFile.seek(address);
                byte[] nodeBytes = new byte[BLOCK_SIZE];
                btreeFile.read(nodeBytes);
                Node n = new Node(nodeBytes);
                cache.put(n.address, StaticUtils.fromPrimitiveBytes(nodeBytes));
                return n;

            } else {
                //If you try to read an empty file
                return null;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }

    }

    /**
     * Writes a node to disk and cache
     *
     * @param n The node to write to disk/cache
     * @throws IOException Represents fatal error in execution, should cause shutdown
     */
    public void writeNodeToFile(Node n) throws IOException {
        long address = n.address;
        byte[] nodeBytes = n.toBytes();
        if (cache.containsKey(address)) {
            // Need to update cache
            cache.remove(address);
            cache.put(address, StaticUtils.fromPrimitiveBytes(nodeBytes));
        }
        File f = new File(BTREE_FILE_NAME);
        RandomAccessFile btreeFile = new RandomAccessFile(f, "rw");
        btreeFile.seek(address);
        btreeFile.write(nodeBytes);
    }

    public BTree() throws IOException {
        this.cache = new BoundedLinkedHashMap<>(CACHE_MAX_SIZE);
        File f = new File(BTREE_FILE_NAME);
        if (f.exists()) {
            RandomAccessFile btreeFile = new RandomAccessFile(BTREE_FILE_NAME, "rw");
            //File exists, read root node and set as root
            btreeFile.seek(0);
            byte[] nodeBytes = new byte[BLOCK_SIZE];
            btreeFile.read(nodeBytes);
            btreeFile.close();
            Node initial = new Node(nodeBytes);
            this.root = initial;
        } else {
            //File doesn't already exist, create and initialize root
            f.createNewFile();
            RandomAccessFile btreeFile = new RandomAccessFile(f, "rw");
            btreeFile.seek(0);
            Node initial = new Node();
            byte[] initialBytes = initial.toBytes();
            btreeFile.write(initialBytes);
            btreeFile.close();
            this.root = initial;
        }

    }

    public long getNewAddress() {
        try {
            File f = new File(BTREE_FILE_NAME);
            RandomAccessFile rFile = new RandomAccessFile(f, "r");
            return rFile.length();
        } catch (FileNotFoundException fnf) {
            return 0L;
        } catch (IOException ioe) {
            return 0L;
        }
    }

    public void splitChild(Node x,int index) {
        long zAddr = getNewAddress();
        Node y = readNodeFromFile(x.children[index]);
        int splitIndex = K >>> 1;
        long yMedianKey = y.keys[splitIndex];
        Node z =  y.getRightSplitNode(x.address,zAddr,splitIndex);
        y = y.getLeftSplitNode(y.parent,y.address,splitIndex);
        x.addKey(yMedianKey);
        x.addChild(z.address);
        System.out.println(y);
        System.out.println(z);
        try{
            writeNodeToFile(y);
            writeNodeToFile(x);
            writeNodeToFile(z);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public boolean search(long key) {
        return bTreeSearch(this.root, key);
    }

    private boolean bTreeSearch(Node node, long key) {
        int i = 0;
        while (i < NUM_CHILDREN && key > node.keys[i]) {
            i++;
        }
        if (key == node.keys[i]) {
            return true;
        } else if (node.leafStatus()) {
            return false;
        } else {
            Node next = readNodeFromFile(node.children[i]);
            if(next==null) return false;
            return bTreeSearch(next,key);
        }
    }
    public void insert(int k){
        Node n = this.root;
        if(n.isFull()){
            Node s = new Node();
            this.root = s;
            n.address = getNewAddress();
            s.addChild(n.address);
            System.out.println(n.address);
            splitChild(s,0);
            insertNonFull(s,k);

        }else{
            insertNonFull(n,k);
        }
    }
    private void insertNonFull(Node x, int k){

    }
}
