package visualdisplay;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;


/*
    returns the data from webcam

*/
class FrameGrabber implements Runnable{
    private int DEVICENUMBER;
    private volatile boolean streamon; 
    private VideoCapture stream; //camera
    private Mat mat; //mat of stream
    private Dimension streamDim; //dimension of mat
    private boolean processImage = false;
    private boolean blur = false, grayScale = false;
    private volatile BufferedImage bi;
    
    public FrameGrabber(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        stream = new VideoCapture();
    }
    
    public FrameGrabber(int devicenumber){
        if(devicenumber < 0){
            return;
        }
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        this.DEVICENUMBER = devicenumber;
        stream = new VideoCapture();
    }
    
    @Override 
    public String toString(){
        if(streamon){
            return "Stream: " + DEVICENUMBER + '\n' +
                    "Mat: " + mat.toString() + '\n' + 
                    "Dimension: " + streamDim + '\n';
        }else{
            return "Stream Off";
        }
        
    }
    @Override
    public void run(){
        while(streamon){
            try{
                stream.retrieve(mat); // retrieve cam info
                streamDim.setSize(mat.cols(),mat.rows()); //get resolution
                Core.flip(mat, mat,1); //flip for true mirror view
                
                //image processing stuff goes here
                
                if(processImage){
                    imageProcessing();
                }
                
                bi = matToBufImg(mat); //convert mat to image and store
                
            }catch(Exception ex){
                System.err.println("Cam Run Exception: " + ex);
            }
        }
    }
    public void start(){
        if(streamon){ //returns because stream is started
            return;
        }
        
        stream.open(DEVICENUMBER);
        if(!stream.isOpened()){
            System.out.println("Could not open stream");
            return;
        }
        
        System.out.println("Opened stream");

        mat = new Mat();
        streamDim = new Dimension(mat.cols(),mat.rows());
        streamon = true;
        
        new Thread(this).start();
    }
    public void start(int devicenumber){
        if(streamon){
            return;
        }
        this.DEVICENUMBER = devicenumber;
        start();
    }
    public void stop(){
        if(!streamon){
            return;
        }
        streamon = false;
        stream.release();
    }
    
   
    
    //getters and setters
    public VideoCapture getStream(){
        return stream;
    }
    public Mat getMat(){
        return mat;
    }
    public BufferedImage getImage(){
        return bi;
    }
    public Dimension getSize(){
        return streamDim;
    }
    public boolean getStreamOn(){
        return streamon;
    }
    public boolean getImageProcess(){
        return processImage;
    }
    public void setImageProcess(boolean processImage){
        this.processImage = processImage;
    }
   
    private BufferedImage matToBufImg(Mat m){
        try {
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", m, mob);
            byte[] bytes = mob.toArray();
            int type = BufferedImage.TYPE_BYTE_GRAY;
            
            if (m.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            return ImageIO.read(new ByteArrayInputStream(bytes));
            
        } catch (Exception ex) {
            return null;
        }
    }
    public Mat bufImgToMat(BufferedImage bi) {
        Mat m = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        
        m.put(0, 0, data);
        
        return m;
    }
    
    public void loadPicture(BufferedImage bi){
        try{
            if(bi == null){
                return;
            }
            loadPicture(bufImgToMat(bi));
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    public void loadPicture(Mat m){
        try{
            if(m == null){
                return;
            }
            this.bi = matToBufImg(m);
            streamDim.setSize(m.cols(),m.rows());
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    public void loadPicture(String url){
        try{
            if(url == null || url.length() <=0){
                return;
            }
            loadPicture(Imgcodecs.imread(url));
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    
    
    private void imageProcessing(){
        //test image processing
        Mat processed = new Mat();
        
        if(blur){
            Imgproc.medianBlur(mat, mat, 15); //median blur to remove noise and retain shapes
        }
        if(grayScale){
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY); //convert cam mat to gray scale
        }
        
        
    }
    public boolean getBlur(){
        return blur;
    }
    public void setBlur(boolean blur){
        this.blur = blur;
    }
    public boolean getGrayScale(){
        return grayScale;
    }
    public void setGrayScale(boolean grayScale){
        this.grayScale = grayScale;
    }
}
