package network;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
class Server{
    //logfile
    
    final private String logfileDir = System.getProperty("user.home")+ "/Desktop/log/";
    final private String logfileName = logfileDir + "log.txt";
    final private File logfile;
    
    private volatile List<SocketChannel> userList;
    private String ipAddress;
    private int portNumber;
    private boolean connected = false;
    private ServerSocketChannel techServer;
    private Selector sSelector;
    
    public Server(){
        
        
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        userList = new ArrayList<>();
        
        //create log file
        logfile = new File(logfileDir);
        logfile.mkdir();
        
        
        
        //logfile.mkdir();
    }
    
    //Server
    public synchronized void serverConnect(int portnumber){
        
        try{
            //ipAddress = Inet4Address.getLocalHost().getHostAddress(); 
            ipAddress = "127.0.0.1";
            log("IP ADDRESS: " + ipAddress);
            log("PORT NUMBER: " + portnumber);
            
           
            
            sSelector = Selector.open();
            
            techServer = ServerSocketChannel.open();
            techServer.configureBlocking(false);
            
            if(!techServer.socket().isBound()){
                techServer.socket().bind(new InetSocketAddress(ipAddress, portnumber));
            }
            
            SelectionKey socketServerSelectionKey = techServer.register(this.sSelector, SelectionKey.OP_ACCEPT);
            
            
            if(!techServer.isOpen()){
                System.out.println("ERROR CONNECTING TO SERVER");
                log("ERROR CONNECTING TO SERVER");
                return;
            }
            
            System.out.println("SERVER SETUP SUCCESSFUL!!!");
            System.out.println("Listening on " + ipAddress + " Port: " + portnumber);
            log("SERVER SETUP SUCCESSFUL!!!");
            log("Listening on " + ipAddress + " Port: " + portnumber);
            
            
            connected = true;
            
            
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
                                try {
                                    serverAccept(key);
                                } catch (IOException ex) {
                                    System.out.println("Accept Acception: " + ex);
                                    log("Accept Acception: " + ex);
                                }
                            }
                            if(key.isReadable()){
                                try {
                                    read(key);
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
            searchForUsers();
            
        }catch(IOException ex){
            System.out.println("Server Connect Exception: " + ex);
            log("Server Connect Exception: " + ex);
            serverDisconnect();
            System.exit(0);
        }
    }
    public synchronized void serverAccept(SelectionKey key) throws IOException{
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(this.sSelector, SelectionKey.OP_READ);
        
        
        try{
            write(channel,"Welcome");
            addUserToList(channel);
            log("SOCKET CONNECTED: " + channel.toString());
        }catch(Exception ex){
            System.out.println("Accepting Ex: " + ex);
            log("Accepting Ex: " + ex);
        }
        
    }
    public synchronized void serverDisconnect(){
        //System.out.println("SHUTTING DOWN THE SERVER!!!");
        if(connected == false){
            return;
        }
        connected = false;
        
        try{
            //check for clients
            closeAllUsers();
            System.out.println("Closed Clients");
            log("Closed Clients");
            
        }catch(Exception ex){
            System.out.println("Close Client ex: " + ex);
            log("Close Client ex: " + ex);
        }
        try{
            
            techServer.socket().close();
            techServer.close();
            
            System.out.println("Disconnected Server");
            log("Disconnected Server");
        }
        catch(Exception ex){
            System.out.println("Server Disconnect Exception: " + ex);
            log("Server Disconnect Exception: " + ex);
            
        }
    }
    public synchronized boolean isServerConnected(){
        return techServer.isOpen();
    }
    public synchronized boolean isServerClosed(){
        return !techServer.isOpen();
    }
    public synchronized boolean getConnected(){
        return connected;
    }
    public synchronized String getServerAddress(){
        return ipAddress;
    }
    
    //Clients
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
            System.out.println("FROM " + channel.toString() + ": " + new String(data));
            log("FROM " + channel.toString() + ": " + new String(data));
        }
    }
    private void write(SocketChannel channel, String string) throws IOException{
        
        if(string == null){
            //determine what to do with null object
            string = "HIIIIII";
        }
        //SocketChannel channel = (SocketChannel) key.channel();
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
    
    //UserList
    public void closeUser(SocketChannel s){
        synchronized(getUserList()){
            Iterator<SocketChannel> userIter = userList.iterator();
        
            while(userIter.hasNext()){
                SocketChannel sc = userIter.next();
                if(s.equals(sc)){
                    try {
                        sc.close();
                    } catch (IOException ex) {
                        System.err.println("User Close Exception: " + ex);
                        log("User Close Exception: " + ex);
                    }
                }

            }
        }
    }
    private void closeAllUsers() throws IOException{
        Iterator<SocketChannel> userIter = userList.iterator();
        
        while(userIter.hasNext()){
            SocketChannel sc = userIter.next();
            sc.close();
        }
    }
    public List<SocketChannel> getUserList(){
        return userList;
    }
    private void addUserToList(SocketChannel channel){
        if(userList.contains(channel)){
            return;
        }
        userList.add(channel);
        log("CHANNEL ADDED TO LIST: " + channel.toString());
    }
    private void searchForUsers(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(connected){
                    
                    try{
                        //Thread.sleep();
                        
                        if(userList.isEmpty()){
                            continue;
                        }
                        
                        Iterator<SocketChannel> userIter = userList.iterator();
                        
                        //System.out.println("Users: ");

                        while(userIter.hasNext()){
                            SocketChannel sc = userIter.next();
                            
                            if(!sc.isOpen()){
                                userIter.remove();
                            }else{
                                //write(sc,"Test");
                                //System.out.println(sc.toString());
                            }
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
                //check 

                int x = 0;
                while(isServerConnected()){
                    Iterator<SocketChannel> userlistIter = getUserList().iterator();
                    if(!getUserList().isEmpty()){
                        try {
                            if(userlistIter.hasNext()){
                                System.out.println("User: " + userlistIter.next().toString());
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
    
    protected void broadcastMessage(String string){
        if(userList.size() <= 0){
            return;
        }
        synchronized(getUserList()){
            try{
                Iterator<SocketChannel> uli = getUserList().iterator();
                while(uli.hasNext()){
                    SocketChannel u = uli.next();
                    System.out.println("SERVER TO " + u.toString() + ": " + string);
                    log("SERVER TO " + u.toString() + ": " + string);
                    write(u,string);

                }
            }catch(Exception ex){
                System.err.println("Broadcast exception: " + ex);
                log("Broadcast exception: " + ex);
                
            }
        }
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

                    //System.out.println(x);
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