package com.nesposi3;

import java.io.*;

public class Main {
    public static void  main(String[] args){
        try{
            BTree b = new BTree();
            Node newRoot = new Node(0,-1,new long[]{-1,-1,-1,-1},new long[]{55,23,45,67,23133});
            b.writeNodeToFile(newRoot);
            Node hey = new Node(4096,0,new long[]{-1,-1,-1,-1},new long[]{44,99,1020205,32332332,88});
            b.writeNodeToFile(hey);
            System.out.println(b.readNodeFromFile(0));
            System.out.println(b.readNodeFromFile(4096));
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
