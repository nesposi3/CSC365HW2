package com.nesposi3;


import com.nesposi3.Utils.CacheUtils;
import com.nesposi3.Utils.ClusteringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

public class Loader {



    public static void  main(String[] args){
        try {
            //CacheUtils.initialize();
            ClusteringUtils.kMedioids();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
