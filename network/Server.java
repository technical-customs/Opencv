package network;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import visualdisplay.Display;
class Server{
    //display
    private Display display;
    //logfile
    final private String logfileDir = System.getProperty("user.home")+ "/Desktop/log/";
    final private String logfileName = logfileDir + "log.txt";
    final private File logfile;
    
    private volatile Map<SocketChannel, String> userMap;
    private volatile List<SocketChannel> userList;
    private String ipAddress;
    private int portNumber;
    private boolean useLocalHost = true;
    private boolean connected = false;
    private ServerSocketChannel server;
    private Selector sSelector;
    private Mat mat;
    
    public Server() throws IOException{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        userList = new ArrayList<>();
        userMap = new HashMap<>();
        
        //create display
        mat = new Mat();
        display = new Display();
        display.setTitle("SERVER DISPLAY");
        
        //create log file
        logfile = new File(logfileDir);
        logfile.mkdir();
    }
    
    //Server
    public void setLocalHost(boolean activate){
        useLocalHost = activate;
    }
    public synchronized void serverConnect(int portnumber){
        try{
            if(useLocalHost){
                ipAddress = "127.0.0.1";
            }else{
                ipAddress = Inet4Address.getLocalHost().getHostAddress();
            }
            
            log("IP ADDRESS: " + ipAddress);
            log("PORT NUMBER: " + portnumber);
            
            sSelector = Selector.open();
            
            server = ServerSocketChannel.open();
            
            
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(ipAddress, portnumber));
            
            
            SelectionKey socketServerSelectionKey = server.register(this.sSelector, SelectionKey.OP_ACCEPT);
            
            if(!server.isOpen()){
                System.out.println("ERROR CONNECTING TO SERVER");
                log("ERROR CONNECTING TO SERVER");
                return;
            }
            
            System.out.println("SERVER SETUP SUCCESSFUL!!!");
            System.out.println("Listening on " + ipAddress + " Port: " + portnumber);
            log("SERVER SETUP SUCCESSFUL!!!");
            log("Listening on " + ipAddress + " Port: " + portnumber);
            
            connected = true;
            keyCheck();
            searchForUsers();
            
        }catch(Exception ex){
            System.out.println("Server Connect Exception: " + ex);
            log("Server Connect Exception: " + ex);
            serverDisconnect();
            System.exit(0);
        }
    }
    
    public synchronized void serverAccept(SelectionKey key){
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            channel.register(this.sSelector, SelectionKey.OP_READ);
            
            try{
                //String username = getAcceptanceString(channel);
                String username = "" + (new Random().nextInt(100)+1);

                addUserToMap(channel, username);
                initReadBuffer(channel);
                //write(channel,"Welcome " + username);

            }catch(Exception ex){
                System.err.println("Accepting Ex: " + ex);
                log("Accepting Ex: " + ex);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void serverDisconnect(){
        if(connected == false){
            return;
        }
        connected = false;
        
        try{
            closeAllUsers();
            System.out.println("Closed Clients");
            log("Closed Clients");
            
        }catch(Exception ex){
            System.out.println("Close Client ex: " + ex);
            log("Close Client ex: " + ex);
        }
        try{
            server.socket().close();
            server.close();
            
            System.out.println("Disconnected Server");
            log("Disconnected Server");
        }
        catch(Exception ex){
            System.out.println("Server Disconnect Exception: " + ex);
            log("Server Disconnect Exception: " + ex);
        }
    }
    public synchronized boolean isServerConnected(){
        return server.isOpen();
    }
    public synchronized boolean isServerClosed(){
        return !server.isOpen();
    }
    public synchronized boolean getConnected(){
        return connected;
    }
    public synchronized String getServerAddress(){
        return ipAddress;
    }
    
    //Clients
    private String getAcceptanceString(SocketChannel channel){
        try {
            String username;
            int alloc = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(alloc);
            int numRead = channel.read(buffer);
            buffer.flip();
            
            if(numRead == -1){
                channel.close();
                System.out.println("Acceptance String Closed: " + channel.toString());
                return null;
            }
            if(numRead == 0){
                
            }
            
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(),0,data,0,numRead);
            String string = new String(data);
            
           
            if(string.startsWith("USERNAME=")){
                username = string.substring("USERNAME=".length());
                //write(channel,"USERNAME=" + username);
            }else{
                username = "USER" + (new Random().nextInt(900)+100);
            }
            
            return username;
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void keyCheck(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(connected){
                    try {
                        sSelector.select();
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    Iterator keys = sSelector.selectedKeys().iterator();

                    while(keys.hasNext()){
                        SelectionKey key = (SelectionKey) keys.next();
                        keys.remove();

                        if(!key.isValid()){
                            continue;
                        }
                        if(key.isAcceptable()){
                            serverAccept(key);
                            
                        }
                        if(key.isReadable()){
                            try {
                                readImage(key);
                                //read(key);
                            } catch (Exception ex) {
                                System.out.println("Read Acception: " + ex);
                                log("Read Key Close Acception: " + ex);

                                try {
                                    key.channel().close();
                                } catch (IOException ex1) {
                                    System.out.println("Read Key Close Acception: " + ex);
                                    log("Read Key Close Acception: " + ex);
                                }
                                key.cancel();
                            }
                        }

                        /*
                        if(key.isWritable()){
                            try{
                                //write(key, null);
                            }catch(Exception ex){
                                System.out.println("Write Key Close Acception: " + ex);
                                try {
                                    key.channel().close();
                                } catch (IOException ex1) {
                                    System.out.println("Write Key Close Acception: " + ex);
                                }
                                key.cancel();

                            }
                        }
                        */
                    }
                }
            }
        }).start();
    }
    
    private void read(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        
        int alloc = 8192;
        ByteBuffer buffer = ByteBuffer.allocate(alloc);
        int numRead = channel.read(buffer);
        
        if(numRead == -1){
            channel.close();
            key.cancel();
            System.out.println("Read Key Closed: " + channel.toString());
            log("Read Key Closed: " + channel.toString());
            return;
        }
        
        if(numRead > 0){
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(),0,data,0,numRead);
            String string = Arrays.toString(data);
            System.out.println(string);
            //System.out.println("FROM " + userMap.get(channel) + ": " + string);
            //log("FROM " + userMap.get(channel) + ": " + string);
            
            
        }
    }
    
    ByteBuffer target;
    
    private void initReadBuffer(SocketChannel channel){
        if(target != null){
            target.clear();
        }
        
        int alloc = 100;
        ByteBuffer buffer = ByteBuffer.allocate(alloc);
        int numRead = -1;
        try {
            numRead = channel.read(buffer);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(numRead == -1){
            try {
                channel.close();
                //key.cancel();
                System.out.println("Closed: " + channel.toString());
                log("Closed: " + channel.toString());
                return;
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(numRead > 0){
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(),0,data,0,numRead);
            
            String string = new String(data);
            String[] strings = string.split(" ");
            int size = 0, width = 0, height = 0, type = 0;
            System.out.println(string);
            
            for(String st: strings){
                System.out.println(st);
                
                if(st.startsWith("SIZE=")){
                    String s = st.substring("SIZE=".length()).trim();
                    size = Integer.parseInt(s);
                    System.out.println("CREATE ARRAY SIZED: " + size);
                    target = ByteBuffer.wrap(new byte[size]);
                    
                }
                if(st.startsWith("WIDTH=")){
                    String s = st.substring("WIDTH=".length()).trim();
                    width = Integer.parseInt(s);
                    System.out.println("MAT WIDTH: " + width);
                }
                if(st.startsWith("HEIGHT=")){
                    String s = st.substring("HEIGHT=".length()).trim();
                    height = Integer.parseInt(s);
                    System.out.println("MAT HEIGHT: " + height);
                }
                if(st.startsWith("TYPE=")){
                    String s = st.substring("TYPE=".length()).trim();
                    type = Integer.parseInt(s);
                    System.out.println("MAT TYPE: " + type);
                }
                
            }     
            mat = new Mat(width, height, type);
            System.out.println(mat.toString());
            
            System.out.println(display.getFrameGrabber().toString());
            display.getFrameGrabber().loadPicture(mat);
            
        }
        
    }
    private void readImage(SelectionKey key){
        SocketChannel channel = (SocketChannel) key.channel();
        
        try {
            int alloc = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(alloc);
            int numRead = channel.read(buffer);
            
            if(numRead == -1){
                System.out.println("Read Key Closed: " + channel.toString());
                log("Read Key Closed: " + channel.toString());
                channel.close();
                key.cancel();
                return;
            }
            
            if(numRead > 0){
                byte[] data = new byte[numRead];
                System.arraycopy(buffer.array(),0,data,0,numRead);
                
                if(new String(data).equalsIgnoreCase("SENT")){
                    
                    System.out.println("Image Sent");
                    //System.out.println("TARGET SIZE: " + target.array().length);
                    
                    target.rewind();
                    
                    
                    System.out.println("TARGET: " + target.toString());
                    System.out.println("TARGET: " + Arrays.toString(target.array()));
                    System.out.println("TARGET: " + target.get());
                    
                    target.clear();
                    return;
                }
                if(target.hasRemaining()){
                    target.put(data);
                }
                
                //System.out.println("TARGET SIZE: " + target.position());
                
                //System.out.println(Arrays.toString(target.array()));
                //display.getFrameGrabber().loadPicture(mat);
                //System.out.println("FROM " + userMap.get(channel) + ": " + string);
                //log("FROM " + userMap.get(channel) + ": " + string);
            }
        } catch (IOException ex) {
            System.err.println("Read Image Exception: " + ex);
            log("Read Image Exception: " + ex);
        }
        
    }
    
    private void write(SocketChannel channel, String string) throws IOException{
        
        if(string == null){
            //determine what to do with null object
            string = "HIIIIII";
        }
        channel.register(this.sSelector, SelectionKey.OP_WRITE);
        
        ByteBuffer buf = ByteBuffer.wrap(string.getBytes());
        buf.put(string.getBytes());
        buf.flip();

        while(buf.hasRemaining()) {
            try {
                channel.write(buf);
            } catch (IOException ex) {
                System.out.println("Write to key ex: " + ex);
                log("Write to key ex: " + ex);
                return;
            }
        }
        channel.register(this.sSelector, SelectionKey.OP_READ);
    }
    protected void broadcastMessage(String string){
        if(userMap == null || userMap.isEmpty()){
            return;
        }
        
        try{
            Iterator<SocketChannel> uli = userMap.keySet().iterator();
            while(uli.hasNext()){
                SocketChannel u = uli.next();
                System.out.println("SERVER TO " + userMap.get(u) + ": " + string);
                log("SERVER TO " + userMap.get(u) + ": " + string);
                write(u,"SERVER: " + string);
            }
        }catch(Exception ex){
            System.err.println("Broadcast exception: " + ex);
            log("Broadcast exception: " + ex);
        }
    }
    
    //UserList
    public synchronized void closeUser(SocketChannel s) throws IOException{
        
        Iterator<SocketChannel> userIter = userMap.keySet().iterator();

        while(userIter.hasNext()){
            SocketChannel sc = userIter.next();
            if(sc == null){
                continue;
            }
            if(s.equals(sc)){
                sc.close();
            }
        } 
    }
    private void closeAllUsers() throws IOException{
        Iterator<SocketChannel> userIter = userMap.keySet().iterator();
        
        while(userIter.hasNext()){
            SocketChannel sc = userIter.next();
            sc.close();
        }
    }
    public Map <SocketChannel, String> getUserMap(){
        return this.userMap;
    }
    private void addUserToMap(SocketChannel channel, String username){
        if(channel == null || username == null || username.isEmpty()){
            return;
        }
        if(userMap.containsKey(channel)){
            return;
        }
        userMap.put(channel, username);
        log("CHANNEL ADDED TO MAP: " + channel.toString() + " USERNAME: " + username);
    }
    private void searchForUsers(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(connected){
                    
                    try{
                        if(userMap.isEmpty()){
                            continue;
                        }
                        
                        Iterator<SocketChannel> userIter = userMap.keySet().iterator();
                        
                        while(userIter.hasNext()){
                            SocketChannel sc = userIter.next();
                            
                            if(!sc.isOpen()){
                                userIter.remove();
                            }else{}
                        }
                    }catch(Exception ex){
                        System.err.println("Search ex: " + ex);
                    }
                }
            }
        }).start();
    }
    private void displayUsers(int time){
        new Thread(new Runnable(){
            @Override
            public void run(){
                int x = 0;
                while(connected){
                    Iterator<SocketChannel> userIter = userMap.keySet().iterator();
                    if(!userMap.isEmpty()){
                        try {
                            if(userIter.hasNext()){
                                SocketChannel sc = userIter.next();
                                System.out.println("User: " + sc.toString() + "Username: " + userMap.get(sc));
                            }
                            Thread.sleep(time*1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }).start();
    }
    
    public void log(String string){
       
        try(BufferedWriter buff = new BufferedWriter(new FileWriter(logfileName, true));) {
            LocalDateTime now = LocalDateTime.now();
            String stamp = now.format(DateTimeFormatter.ofPattern("MM/dd/yyy h:mm:ss.SSS a"));
            
            buff.append(stamp);
            buff.append(": ");
            buff.append(string);
            buff.newLine();
            buff.flush();  
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public File getLogFile(){
        if(Files.exists(Paths.get(logfileName))){
            return new File(logfileName);
        }
        return null;
    }
    private void timeout(int time){
        if(time < 0){
            return;
        }
        if(time == 0){
            System.out.println("Disconnect");
            log("DISCONNECT");
            serverDisconnect();
            System.exit(0);
        }
        new Thread(new Runnable(){
            @Override
            public void run(){
                int x = 0;
                while(connected){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    x++;

                    if(x == time){
                        System.out.println("Timeout");
                        log("TIMEOUT");
                        serverDisconnect();
                        System.exit(0);
                    }
                }
            }
        }).start();
    }
    private void sInput(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Scanner scanner = new Scanner(System.in);
                    
                    while(scanner.hasNextLine()){
                        String line = scanner.nextLine();
                        System.out.println("SERVER: " + line);
                        log("SERVER: " + line);
                        broadcastMessage(line);
                    }
                }catch(Exception ex){System.err.println(ex);}
            } 
        }).start();
    }
    
    public static void main(String[] args) throws IOException{
        //Display display = new Display();
        Server server = new Server();
        
        try{
            server.serverConnect(3803);
            
            if(server.isServerConnected()){
                System.out.println("Connected");
                server.sInput();
                //server.displayUsers(2);
                server.timeout(45);
            }
        }catch(Exception ex){}
        
        
    }
}