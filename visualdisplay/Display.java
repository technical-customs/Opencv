package visualdisplay;

import classifierloader.ClassifierLoader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.opencv.core.Core;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;

public class Display extends JPanel{
    public final String picExt = ".jpg";
    public final String picLocation = "C:/Users/" + System.getProperty("user.name") + "/Desktop/OpenCvPicFolder";
    private final Path picLocationPath = Paths.get(picLocation);
    private final int width = 420;
    private final int height = width / 16 * 9;
    private final Dimension size = new Dimension(width,height);
    
    private final int fontSize = 18;
    private final Font infoPanelFont = new Font("TimesRoman", Font.PLAIN, fontSize);
    private Graphics2D g2;
    private boolean imageProcessMenu;
    //JFrame
    private JFrame frame;
    private JPanel southPanel;
    private JPanel buttonPanel;
    private JButton saveButton;
    private JButton quitButton;
    private JTextField folderSaveField;
    
    //Camera
    private boolean timer = false;
    private volatile int ttime = 0;
    private final FrameGrabber frameGrabber;
    private final ClassifierLoader cl;
    private final boolean detectFaces = false;
    private final MatOfRect faceDetections;
    private final boolean detectEyes = true;
    private final MatOfRect eyeDetections;
    private final MatOfByte mem;
    private final List circles;
    
    public Display() throws IOException{
        if(Files.notExists(picLocationPath)){
            System.out.println("Making folder");
            Files.createDirectories(picLocationPath);
        }
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        frameGrabber = new FrameGrabber();
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
        
        if(frameGrabber == null){
            return;
        }
        
        if(frameGrabber.getStreamOn()){
            try {
                //Thread.sleep(33);
            }catch(Exception ex){}
            
            frame.setSize(frameGrabber.getSize());
            //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(frameGrabber.getImage(), null, 0, 0);
        }else{
            frame.setSize(width,height);
        }
        //Draw options in top left corner
        g2.setColor(Color.yellow);
        if(frameGrabber.getStreamOn()){
            g2.drawString("Options: P - Process, S - Save, T - Timer, O - Open Stream, E - Exit Stream, Q - Quit",0,fontSize);
            
            if(imageProcessMenu){
                g2.drawString("Process Options: B - Blur, Gray - G",0,(fontSize*2)+2 );
            }
            
            if(timer){
                g2.setFont(new Font("TimesRoman", Font.PLAIN, width/4));
                if(ttime > 0){
                    g2.drawString(ttime + "", frame.getWidth()/2, frame.getHeight()/2);
                }
            }
            
        }else{
            g2.drawString("Options: O - Open Stream, Q - Quit",0,fontSize);
        }
        repaint();
        
        
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
                frameGrabber.stop();
                System.exit(0);
            }
        });
        frame.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent ke){
                //options: S-save, E-exit stream, O-open stream, Q-quit
                
                int keyCode = Character.toUpperCase(ke.getKeyChar());
                
                if(keyCode == (KeyEvent.VK_S)){
                    savePic();
                }
                if(keyCode == (KeyEvent.VK_T)){
                    String tm = JOptionPane.showInputDialog(null,"Enter Time 1-10:",null,1);
                    
                    if(tm == null || tm.isEmpty() || tm.contains(" ")){
                        return;
                    }
                    
                    int t;
                    
                    try{
                        t = Integer.parseInt(tm);
                        
                        if(t < 0 && t > 10){
                            return;
                        }
                        timerSave(t);
                        
                    }catch(Exception ex){
                        System.out.println("Time Input Error");
                        return;
                    }
                    
                    
                }
                if(keyCode == (KeyEvent.VK_O)){ //option to open a stream on specific device
                    if(frameGrabber.getStreamOn()){
                        System.out.println("Stream Still Open");
                        //return;
                    }
                    
                    String dev = JOptionPane.showInputDialog(null,"Enter Device ID:",null,1);
                    
                    if(dev == null || dev.isEmpty() || dev.contains(" ")){
                        return;
                    }
                    
                    int streamdev;
                    
                    try{
                        streamdev = Integer.parseInt(dev);
                        
                        if(streamdev < 0){
                            return;
                        }
                        System.out.println("STARTING STREAM " + streamdev);
                        frameGrabber.start(streamdev);
                        
                    }catch(Exception ex){
                        System.out.println("Stream Device Not Available");
                        return;
                    }
                    
                }
                if(keyCode == (KeyEvent.VK_E)){
                    if(frameGrabber.getStreamOn()){
                        frameGrabber.stop();
                    }
                }
                if(keyCode == (KeyEvent.VK_Q)){
                    if(frameGrabber.getStreamOn()){
                        frameGrabber.stop();
                    }
                    System.exit(0);
                }
                if(keyCode == (KeyEvent.VK_P)){
                    if(frameGrabber.getStreamOn()){
                        frameGrabber.setImageProcess(!frameGrabber.getImageProcess());
                        imageProcessMenu = frameGrabber.getImageProcess();
                    }
                }
                
                //image process keys: B - Blur, G - Gray, E - Eye detect, going to add slide scale
                if(keyCode == (KeyEvent.VK_B)){
                    if(frameGrabber.getStreamOn()){
                        if(imageProcessMenu){
                            frameGrabber.setBlur(!frameGrabber.getBlur());
                        }
                    }
                }
                if(keyCode == (KeyEvent.VK_G)){
                    if(frameGrabber.getStreamOn()){
                        if(imageProcessMenu){
                            frameGrabber.setGrayScale(!frameGrabber.getGrayScale());
                        }
                    }
                }
                if(keyCode == (KeyEvent.VK_E)){
                    if(frameGrabber.getStreamOn()){
                        if(imageProcessMenu){
                            
                        }
                    }
                }
                if(keyCode == (KeyEvent.VK_F)){
                    if(frameGrabber.getStreamOn()){
                        if(imageProcessMenu){
                            
                        }
                    }
                }
            }
        });
        frame.setFocusable(true);
        frame.requestFocus();
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public void runCamera(int device){
        frameGrabber.start(device);
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
    public String getSavePath(String picName, String ext){
        return picLocationPath + "/" + picName + ext;
    }
    
    private void timerSave(int time){
        if(!frameGrabber.getStreamOn()){
            return;
        }
        new Thread(new Runnable(){
            @Override
            public void run(){
               timer = true;
               ttime = time;
                try{
                    while(ttime > 0){
                        //print number on screen
                        System.out.println("Time: " + ttime);
                        
                        Thread.sleep(1000);
                        ttime--;
                    }
                    if(ttime == 0){
                        System.out.println("Time Save");
                        savePic();
                        timer = false;
                    }
                }catch(Exception ex){

                } 
            }
        }).start();
        
        
    }
    private void savePic(){
        if(frameGrabber.getStreamOn()){
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            System.out.println(timeStamp + "");
            Imgcodecs.imwrite(getSavePath(timeStamp, picExt), frameGrabber.getMat());
        }
    }
    private void savePic(String picName){
        if(frameGrabber.getStreamOn()){
            Imgcodecs.imwrite(getSavePath(picName, picExt), frameGrabber.getMat());
        }
    }
    public static void main(String[] args){
        Display display;
        try {
            display = new Display();
            //display.runCamera(0);
        } catch (IOException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        }
        //display.loadPicture(testPicLoc);
        
    }
}