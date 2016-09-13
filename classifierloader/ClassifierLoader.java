package classifierloader;

import org.opencv.objdetect.CascadeClassifier;

public final class ClassifierLoader{
    private CascadeClassifier faceDetector = new CascadeClassifier(ClassifierLoader.class.getResource("/haarcascades/haarcascade_frontalface_alt.xml").getPath().substring(1));
    private CascadeClassifier eyeDetector = new CascadeClassifier(ClassifierLoader.class.getResource("/haarcascades/haarcascade_eye.xml").getPath().substring(1));
    
    
    public ClassifierLoader(){
        
    }
    
    public CascadeClassifier getFaceDetector(){
        return faceDetector;
    }
    public CascadeClassifier getEyeDetector(){
        return eyeDetector;
    }
}