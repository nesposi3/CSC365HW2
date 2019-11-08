package com.nesposi3;

import com.nesposi3.Utils.BoundedLinkedHashMap;
import com.nesposi3.Utils.BTreeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.function.Consumer;

import static com.nesposi3.Utils.BTreeUtils.*;

/**
 * A persistent, file-based BTree with IOCache
 */
public class BTree {
    private Node root;
    private BoundedLinkedHashMap<Long, Byte[]> cache;
    private String fileName;
    /**
     * This method checks if the requested node is in cache, or if not, on disk.
     *
     * @param address The address to look at
     * @return The node at said address, or null if it does not exist
     * @throws IOException IOException represents a fatal error during execution, should lead to program shutdown
     */
    private Node readNodeFromFile(long address) {
        if (cache.containsValue(address)) {
            // This node is cached
            return new Node(cache.get(address));
        }
        File f = new File(this.fileName);
        try {
            if (f.exists()) {
                RandomAccessFile btreeFile = new RandomAccessFile(f, "rw");
                btreeFile.seek(address);
                byte[] nodeBytes = new byte[BLOCK_SIZE];
                btreeFile.read(nodeBytes);
                Node n = new Node(nodeBytes);
                cache.put(n.address, BTreeUtils.fromPrimitiveBytes(nodeBytes));
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
    private void writeNodeToFile(Node n) throws IOException {
        long address = n.address;
        byte[] nodeBytes = n.toBytes();
        if (cache.containsKey(address)) {
            // Need to update cache
            cache.remove(address);
            cache.put(address, BTreeUtils.fromPrimitiveBytes(nodeBytes));
        }
        File f = new File(this.fileName);
        RandomAccessFile btreeFile = new RandomAccessFile(f, "rw");
        btreeFile.seek(address);
        btreeFile.write(nodeBytes);
    }

    public BTree(String fileName) throws IOException {
        this.fileName = fileName;
        this.cache = new BoundedLinkedHashMap<>(CACHE_MAX_SIZE);
        File f = new File(this.fileName);
        if (f.exists()) {
            RandomAccessFile btreeFile = new RandomAccessFile(this.fileName, "rw");
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

    private long getNewAddress() {
        try {
            File f = new File(this.fileName);
            RandomAccessFile rFile = new RandomAccessFile(f, "r");
            return rFile.length();
        } catch (FileNotFoundException fnf) {
            return 0L;
        } catch (IOException ioe) {
            return 0L;
        }
    }

    private void splitChild(Node x, int index) {
        Node z = new Node();
        z.address = getNewAddress();
        Node y = readNodeFromFile(x.children[index]);
        y.parent = x.address;
        z.parent = x.address;
        for (int i = 0; i < T - 1; i++) {
            z.keys[i] = y.keys[i + T];
            z.frequencies[i] = y.frequencies[i+T];
        }
        if (!y.leafStatus()) {
            for (int i = 0; i < T; i++) {
                z.children[i] = y.children[i + T];
            }
        }
        for (int i = x.numKeys(); i > index; i--) {
            x.children[i + 1] = x.children[i];
        }
        x.children[index + 1] = z.address;
        for (int i = x.numKeys() - 1; i >= index; i--) {
            x.keys[i + 1] = x.keys[i];
            x.frequencies[i+1] = x.frequencies[i];
        }
        x.keys[index] = y.keys[T - 1];
        x.frequencies[index] = y.frequencies[T-1];
        y.setNumKeys(T-1);
        try {
            writeNodeToFile(z);
            writeNodeToFile(x);
            writeNodeToFile(y);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void insert(long k,int freq) {
        try {
            Node r = readNodeFromFile(0);
            if (r.isFull()) {
                r.address = getNewAddress();
                r.parent = 0;
                writeNodeToFile(r);
                Node s = new Node();
                s.children[0] = r.address;
                s.address = 0;
                writeNodeToFile(s);
                splitChild(s, 0);
                s = readNodeFromFile(0);
                insertNonFull(s, k,freq);
            } else {
                insertNonFull(r, k,freq);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void insertNonFull(Node x, long k, int freq) {
        try {
            int i = x.numKeys() - 1;
            if (x.leafStatus()) {
                while (i >= 0 && k < x.keys[i]) {
                    x.keys[i + 1] = x.keys[i];
                    x.frequencies[i+1] = x.frequencies[i];
                    i--;
                }
                x.keys[i + 1] = k;
                x.frequencies[i+1] = freq;
                writeNodeToFile(x);
            } else {
                while (i >= 0 && k < x.keys[i]) {
                    i--;
                }
                i++;
                Node node = readNodeFromFile(x.children[i]);
                if (node.isFull()) {
                    splitChild(x, i);
                    x = readNodeFromFile(x.address);
                    if (k > x.keys[i]) i++;
                    node = readNodeFromFile(x.children[i]);
                }
                insertNonFull(node, k,freq);

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public int search(long key) {
        return bTreeSearch(this.root, key);
    }

    private int bTreeSearch(Node node, long key) {
        int i = 0;
        while (i < K && key > node.keys[i]) {
            if(node.keys[i]==NULL){
                // Need this in here as NULL==-1, which could be cnsidered
                break;
            }else{
                i++;
            }

        }
        if (i<K && key == node.keys[i]) {
            return node.frequencies[i];
        } else if (node.leafStatus()) {
            return 0;
        } else {
            Node next = readNodeFromFile(node.children[i]);
            if (next == null) return 0;
            return bTreeSearch(next, key);
        }
    }
    public void printAll(){
        printAllNodes(readNodeFromFile(0));
    }
    private void printAllNodes(Node n) {
        if (n.leafStatus()) {
            System.out.println(n.toString());
        } else {
            System.out.println(n.toString());
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (n.children[i] != NULL) {
                    Node x = readNodeFromFile(n.children[i]);
                    printAllNodes(x);
                }
            }
        }

    }
    public void forEach(Consumer<Node> consumer){
        Node node = readNodeFromFile(0);
        if(node != null){
            forEach(consumer,node);
        }
    }
    private void forEach(Consumer<Node> consumer, Node node){
        if(node.leafStatus()){
            consumer.accept(node);
        }else {
            consumer.accept(node);
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (node.children[i] != NULL) {
                    Node x = readNodeFromFile(node.children[i]);
                    forEach(consumer,x);
                }
            }
        }
    }
    public double computeEuclideanDistance(BTree other){
        Node n = readNodeFromFile(0);
        return Math.sqrt(computeEuclideanDistance(n,other,0));
    }
    private double computeEuclideanDistance(Node node,BTree other,double total){
        if(node.leafStatus()){
            for (int i = 0; i <node.keys.length ; i++) {
                long wordA = node.keys[i];
                int freqA = node.frequencies[i];
                int freqB = other.search(wordA);
                double x = (freqA - freqB)*(freqA-freqB);
                total = total + x;
            }
            return total;
        }else {
            for (int i = 0; i <node.keys.length ; i++) {
                long wordA = node.keys[i];
                int freqA = node.frequencies[i];
                int freqB = other.search(wordA);
                double x = (freqA - freqB)*(freqA-freqB);
                total = total + x;
            }
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (node.children[i] != NULL) {
                    Node x = readNodeFromFile(node.children[i]);
                    return computeEuclideanDistance(x,other,total);
                }
            }
            return total;
        }
    }
    public int totalWordCount(){
        Node node = readNodeFromFile(0);
        return this.totalWordCount(node,0);
    }
    private int totalWordCount(Node n,int total) {
        if (n.leafStatus()) {
            for (int i = 0; i <K ; i++) {
                if(n.keys[i]!=NULL){
                    total += n.frequencies[i];
                }
            }
            return total;
        } else {
            for (int i = 0; i <K ; i++) {
                if(n.keys[i]!=NULL){
                    total += n.frequencies[i];
                }
            }            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (n.children[i] != NULL) {
                    Node x = readNodeFromFile(n.children[i]);
                    return totalWordCount(x,total);
                }
            }
            return total;
        }
    }
}
