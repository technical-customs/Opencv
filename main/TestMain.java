package main;

import org.opencv.core.Core;

public class TestMain{
    
   
    public static void displayVersion(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("Opencv: " + Core.VERSION);
    }
    
    public static void main(String[] args){
        TestMain.displayVersion();
    }
}