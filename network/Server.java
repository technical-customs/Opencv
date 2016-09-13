package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Server{
    final private List<SocketChannel> userList;
    private String ipAddress;
    private int portNumber;
    private boolean connected = false;
    private ServerSocketChannel techServer;
    private Selector sSelector;

    
    public Server(){
        userList = new ArrayList();
    }
    
    //Server
    public synchronized void serverConnect(int portnumber){
        
        try{
            ipAddress = "127.0.0.1";
            
            //ipAddress = Inet4Address.getLocalHost().getHostAddress(); 
            
            sSelector = Selector.open();
            
            techServer = ServerSocketChannel.open();
            techServer.configureBlocking(false);
            
            if(!techServer.socket().isBound()){
                techServer.socket().bind(new InetSocketAddress(ipAddress, portnumber));
            }
            
            SelectionKey socketServerSelectionKey = techServer.register(this.sSelector, SelectionKey.OP_ACCEPT);
            
            
            if(!techServer.isOpen()){
                System.out.println("ERROR CONNECTING TO SERVER");
                return;
            }
            
            System.out.println("SERVER SETUP SUCCESSFUL!!!");
            System.out.println("Listening on " + ipAddress + " Port: " + portnumber);
            
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
                                }
                            }
                            if(key.isReadable()){
                                try {
                                    read(key);
                                } catch (Exception ex) {
                                    System.out.println("Read Acception: " + ex);
                                    
                                    try {
                                        key.channel().close();
                                    } catch (IOException ex1) {
                                        System.out.println("Read Key Close Acception: " + ex);
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
            serverDisconnect();
        }
    }
    public synchronized void serverAccept(SelectionKey key) throws IOException{
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(this.sSelector, SelectionKey.OP_READ);
        
        
        try{
            //System.out.println("Accepted " + channel.toString());
            
            //write(key,);
            addUserToList(channel);
        }catch(Exception ex){
            System.out.println("Accepting Ex: " + ex);
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
            
        }catch(Exception ex){
            System.out.println("Close Client ex: " + ex);
        }
        try{
            
            techServer.socket().close();
            techServer.close();
            
            System.out.println("Disconnected Server");
        }
        catch(Exception ex){
            System.out.println("Server Disconnect Exception: " + ex);
            
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
    
    //Clients
    private void read(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        
        
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = channel.read(buffer);
        
        if(numRead == -1){
            channel.close();
            key.cancel();
            System.out.println("Read Key Closed: " + channel.toString());
            
            return;
        }
        
        //System.out.println("Key read: " + channel.toString());
        
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
                return;
            }
        }
        channel.register(this.sSelector, SelectionKey.OP_READ);
    }
    
    //UserList
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
    }
    private void searchForUsers(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(connected){
                    
                    try{
                        Thread.sleep(1000);
                        
                        if(userList.isEmpty()){
                            continue;
                        }
                        
                        Iterator<SocketChannel> userIter = userList.iterator();
                        
                        System.out.println("Users: ");

                        while(userIter.hasNext()){
                            SocketChannel sc = userIter.next();
                            
                            if(sc.socket().isClosed()){
                                userIter.remove();
                            }else{
                                System.out.println(sc.toString());
                            }
                        }
                    }catch(Exception ex){
                        System.out.println("Search ex: " + ex);
                    }
                }
            }
        }).start();
    }
    
    public static void main(String[] args){
        Server server = new Server();
        
        try{
            server.serverConnect(3803);
            
            if(server.isServerConnected()){
                System.out.println("Connected");
                
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        int x = 0;
                        while(true){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            x++;
                            
                            //System.out.println(x);
                            if(x == 30){
                                System.out.println("Timeout");
                                server.serverDisconnect();
                                System.exit(0);
                            }
                        }
                    }
                }).start();
            }
        }catch(Exception ex){}
        
        
    }
}