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

    public void splitChild(Node x, int index) {
        Node z = new Node();
        z.address = getNewAddress();
        Node y = readNodeFromFile(x.children[index]);
        y.parent = x.address;
        z.parent = x.address;
        for (int i = 0; i < T - 1; i++) {
            z.keys[i] = y.keys[i + T];
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
        }
        x.keys[index] = y.keys[T - 1];
        y.setNumKeys(T-1);
        try {
            writeNodeToFile(z);
            writeNodeToFile(x);
            writeNodeToFile(y);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void insert(long k) {
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
                insertNonFull(s, k);
            } else {
                insertNonFull(r, k);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void insertNonFull(Node x, long k) {
        try {
            int i = x.numKeys() - 1;
            //System.out.println("{insertNonFull: " + x.toString() + "}");
            if (x.leafStatus()) {
                while (i >= 0 && k < x.keys[i]) {
                    x.keys[i + 1] = x.keys[i];
                    i--;
                }
                x.keys[i + 1] = k;
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
                insertNonFull(node, k);

            }
        } catch (IOException ioe) {
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
            if (next == null) return false;
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
}
