package com.nesposi3;


import com.nesposi3.Utils.CacheUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

import static com.nesposi3.Utils.CacheUtils.DIRECTORY_NAME;

public class Loader {



    public static void  main(String[] args){
        try {
            CacheUtils.initialize();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
