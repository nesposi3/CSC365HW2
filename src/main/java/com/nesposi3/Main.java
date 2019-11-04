package com.nesposi3;

import java.io.*;

public class Main {
    public static void  main(String[] args){
        try{
            BTree b = new BTree();
            Node newRoot = new Node(0,-1,new long[]{-1,-1,-1,-1,-1},new long[]{0,100,300,500});
            b.writeNodeToFile(newRoot);
            b.insert(200);
            System.out.println(b.readNodeFromFile(0));
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
