package com.nesposi3;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void  main(String[] args){
        try{
            BTree b = new BTree();
            Scanner sc = new Scanner(System.in);
            while (true){
                String s = sc.nextLine();
                if(s.equals("exit")){
                    break;
                }else{
                    int a = Integer.parseInt(s);
                    b.insert(a);
                    b.printAll();
                }
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
