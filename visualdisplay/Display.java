package visualdisplay;

import classifierloader.ClassifierLoader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import main.TestMain;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

public class Display extends JPanel{
    public static final String testPicLoc = "C:/Users/" + System.getProperty("user.name") + "/Desktop/TestPic.jpg";
    
    private final int width = 720;
    private final int height = width / 16 * 9;
    private final Dimension size = new Dimension(width,height);
    private volatile boolean camRun = false;
    
    private Graphics2D g2;
    //JFrame
    private JFrame frame;
    private JPanel southPanel;
    private JPanel buttonPanel;
    private JButton saveButton;
    private JButton quitButton;
    private JTextField folderSaveField;
    
    //Camera
    private ScheduledExecutorService timer;
    private VideoCapture stream;
    private Mat mat;
    private volatile BufferedImage bi;
    private ClassifierLoader cl;
    private boolean detectFaces = false;
    private MatOfRect faceDetections;
    private boolean detectEyes = true;
    private MatOfRect eyeDetections;
    private MatOfByte mem;
    private List circles;
    
    public Display(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        cl = new ClassifierLoader();
        faceDetections = new MatOfRect();
        eyeDetections = new MatOfRect();
        mem = new MatOfByte();
        circles = new ArrayList();
        
        System.out.println();
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                setupGUI();
            }
        });
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        
        g2 = (Graphics2D)g;
        
        g2.setColor(Color.blue);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        //if camRun is true, draw image to display
        if(camRun){
            if(bi != null){
                g2.drawImage(bi, null, 0, 0);
                
                //create grayscaled mat of bi
                Mat m2 = new Mat();
                Imgproc.cvtColor(bufImgToMat(bi), m2, Imgproc.COLOR_BGR2GRAY);
                
                cl.getFaceDetector().detectMultiScale(m2, faceDetections);
                cl.getEyeDetector().detectMultiScale(m2, eyeDetections);
                
                g2.setColor(Color.yellow);
                
                if(detectFaces){
                    for (Rect rect : faceDetections.toArray()) {
                        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
                    }
                    
                    
                }
                if(detectEyes){
                    for (Rect rect : eyeDetections.toArray()) {
                        
                        //System.out.println("Eye");
                        //Imgproc.rectangle(bufImgToMat(bi), new Point(rect.x, rect.y), new Point(rect.x+rect.width, rect.y+rect.height), new Scalar(0,255,0));
                        g2.drawOval(rect.x, rect.y, rect.width, rect.height);
                        
                    }
                    /*
                    if(!eyeDetections.empty()){
                        Rect eye1 = eyeDetections.toArray()[0];
                        //Rect eye2 = eyeDetections.toArray()[1];
                        //g2.drawRect(eye1.x, eye1.y, eye1.width, eye1.height); //get region of  eye
                        
                        //create mat of eye
                        Rect er = new Rect(eye1.x, eye1.y, eye1.width, eye1.height);
                        //Rect er2 = new Rect(eye2.x, eye2.y, eye2.width, eye2.height);
                        
                        
                        Mat em = new Mat(m2, er); //eye mat 1
                        //Mat em = m2.clone();
                        //Imgproc.cvtColor(em, em, Imgproc.COLOR_GRAY2BGR);
                        
                        //Mat em2 = new Mat(m2, er2);
                        //Imgproc.cvtColor(em2, em2, Imgproc.COLOR_GRAY2BGR);
                        
                        //Mat be = Mat.zeros(em.rows(),em.cols()*2+10,em.type()); //mat to hold both eyes
                        //this.bi = matToBufImg(em); //replace bi with eye mat
                        
                        //Mat mm = bufImgToMat(bi);
                        //System.out.println(mm);
                        
                        Imgproc.resize(em, em, m2.size(),0,0,Imgproc.INTER_LINEAR);
                        
                        
                        
                        Imgproc.medianBlur(em, em, 15);
                        Imgproc.Canny(em, em, 5, 15);
                        
                        //Imgproc.threshold(em, em, 70, 255, Imgproc.THRESH_BINARY);
                        
                        
                        this.bi = matToBufImg(em);
                        
                        
                        return;
                        
                    }
                    */
                }
                
            }
        }
        repaint();
        
        g2.dispose();
        
    }
    private void setupGUI(){
        this.setPreferredSize(size);
        
        frame = new JFrame("Display");
        
        frame.getContentPane().add(this);
        frame.pack();
        
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                //close all resources, save all files
                
                /*
                if(bi != null){
                    //write file
                }
                */
                
                if(camRun){
                    camRun = false;
                }
                if(stream != null && stream.isOpened()){
                    stream.release();
                }
                System.exit(0);
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public void setRunner(boolean camRun){
        this.camRun = camRun;
    }
    public void setTitle(String title){
        if(frame != null){
            frame.setTitle(title);
        }
    }
    public String getFolderSaveLocation(){
        if(folderSaveField != null && folderSaveField.getText() != null && folderSaveField.getText().length() > 0){
            return folderSaveField.getText();
        }else{
            return null;
        }
    }
    
    //Camera
    Runnable frameGrabber = new Runnable() {
        @Override
        public void run(){
            synchronized(Display.this){
                if(camRun){
                    try{
                        stream.retrieve(mat);
            
                        //resize frame
                        setSize(mat.cols(),mat.rows());
                        frame.setSize(getSize());
                        
                        //flip for true mirror view
                        Core.flip(mat, mat,1);
                        
                        //test image processing
                        Mat m1 = new Mat();
                        Imgproc.cvtColor(mat, m1, Imgproc.COLOR_BGR2GRAY);
                        
                        
                        Imgproc.medianBlur(m1, m1, 15);
                        
                        //Imgproc.THRESH_BINARY_INV, 15, 4);
                        mat = m1;
                        
                        //encode mat to matofbyte for memory buffered image
                        Imgcodecs.imencode(".jpg", mat, mem);
                        Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                        
                        bi = (BufferedImage) im;
                        
                    }catch(Exception ex){
                        System.out.println("Exception: " + ex);
                    }
                    
                    
                
                }
            }
            
            
        }
    };
    public void loadPicture(BufferedImage bi){
        try{
            if(bi == null){
                return;
            }
             System.out.println("lp BI");
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
            setSize(m.cols(),m.rows());
            frame.setSize(getSize());
            camRun = true;
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    
    public void loadPicture(String url){
        try{
            if(url == null || url.length() <=0){
                return;
            }
            System.out.println("lp URL");
            Mat m = Imgcodecs.imread(url);
            loadPicture(m);
            
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    
    public void runCamera(int device){
        stream = new VideoCapture(device);
        
        
        if(!stream.isOpened()){
            System.out.println("Could not open stream");
            return;
        }
        camRun = true;
        mat = new Mat();

        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
        
    }
    
    public Mat bufImgToMat(BufferedImage bi) {
        Mat m = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        
        m.put(0, 0, data);
        
        return m;
    }
    
    public BufferedImage matToBufImg(Mat m){
        try {
            
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", m, mob);
            byte[] bytes = mob.toArray();
            
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (m.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            return img;
            
        } catch (IOException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static void main(String[] args){
        Display display = new Display();
        //display.loadPicture(testPicLoc);
        display.runCamera(0);
    }
}