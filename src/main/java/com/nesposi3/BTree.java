package com.nesposi3;

import com.nesposi3.Utils.BoundedLinkedHashMap;
import com.nesposi3.Utils.StaticUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.nesposi3.Utils.StaticUtils.*;

public class BTree {
    private Node root;
    private BoundedLinkedHashMap<Long, Byte[]> cache;

    public Node readNodeFromFile(long address) throws IOException {
        if (cache.containsValue(address)) {
            // This node is cached
            return new Node(cache.get(address));
        }

        File f = new File(BTREE_FILE_NAME);
        if (f.exists()) {
            RandomAccessFile btreeFile = new RandomAccessFile(f, "rw");
            try {
                btreeFile.seek(address);
                byte[] nodeBytes = new byte[BLOCK_SIZE];
                btreeFile.read(nodeBytes);
                Node n = new Node(nodeBytes);
                cache.put(n.address, StaticUtils.fromPrimitiveBytes(nodeBytes));
                return n;
            } catch (IOException ioe) {
                // If that address does not exist
                return null;
            }

        } else {
            //If you try to read an empty file
            return null;
        }
    }

    public void writeNodeToFile(Node n) throws IOException {
        long address = n.address;
        byte[] nodeBytes = n.toBytes();
        if(cache.containsKey(address)){
            // Need to update cache
            cache.remove(address);
            cache.put(address,StaticUtils.fromPrimitiveBytes(nodeBytes));
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
}
