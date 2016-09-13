package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

class Client{
    private SocketChannel channel;
    private boolean connected;
    
    public Client(){
        
    }
    
    //Connection
    private void openChannel(){
        try{
            channel = SocketChannel.open();
            
            if(channel.isOpen()){
                System.out.println("Opened");
            }
        }catch(Exception ex){System.out.println("error connecting");}
    }
    protected void connectChannel(String ipAddress, int portNumber){
        //new Thread(new Runnable(){
            //@Override
            //public void run(){
        openChannel();
                
        if(channel == null){
            return;
        }
                
        System.out.println("now connecting");
                
                
        try{
            if(channel.isConnected()){
                
            }
            channel.connect(new InetSocketAddress(ipAddress,portNumber));
        }catch(IOException ex) {
            System.out.println("Connect x " + ex);
        }
        try{
            channel.finishConnect();
        }catch(IOException ex) {
            System.out.println("Finish Connect x " + ex);
        }

        try{
            while(!channel.finishConnect()){}
        }catch(IOException ex){
            
        }
                    
                    
        try {
            if(channel.finishConnect()) {
                connected = true;
                checkConnection();
                read();
                
                //Send something to server to server
                String text = "Yes, Im in You";
                write(text);
            }
        } catch (IOException ex) {
        }
        
        
    }
    protected void disconnectChannel(){
        if(connected == false){
            return;
        }
        if(channel != null ){
            connected = false;
            try{
                channel.socket().close();
                channel.close();
                
                if(channel.socket().isClosed()){
                    System.out.println("Closed");
                }
                
            }catch(Exception ex){}
        }
    }
    protected boolean isChannelConnected(){
        if(channel == null){
            return false;
        }
        return channel.isOpen();
    }
    private void checkConnection(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(connected){
                    try{
                        Thread.sleep(1000);
                        //write("Test");
                        
                        String string = "";
                        ByteBuffer buf = ByteBuffer.wrap(string.getBytes());
                        buf.put(string.getBytes());
                        buf.flip();

                        while(buf.hasRemaining()) {
                            try {
                                channel.write(buf);
                            } catch (IOException ex) {
                                System.out.println("Write ex: " + ex);
                                disconnectChannel();
                                return;
                            }
                        }
                    }catch(Exception ex){
                        
                        return;
                    }
                }
            }
        }).start();
    }
    
    //R+W
    public void read() throws IOException{
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(connected){
                    try{
                        if(channel != null){
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int numRead = channel.read(buffer);

                            if(numRead == -1){
                                disconnectChannel();
                                System.out.println("Read Closed: " + channel.toString());
                                return;
                            }


                            byte[] data = new byte[numRead];
                            System.arraycopy(buffer.array(),0,data,0, numRead);
                            System.out.println(channel.toString() + ": " + new String(data));
                        }
                    }catch(Exception ex){
                        System.out.println(ex);
                        disconnectChannel();
                        return;
                    }
                    
                }
            }
        }).start();
    }
    public void write(String string){
        if(connected){
            if(channel != null){
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
            }
        }
    }
    
    //Getters and Setters
    public boolean getConnected(){
        return connected;
    }
    protected SocketChannel getUserChannel(){
        if(channel != null){
            return channel;
        }else{
            return null;
        }
    }
            
    public static void main(String[] args){
        Client client = new Client();
        
        try{
            client.connectChannel("127.0.0.1", 3803);
            
            if(client.isChannelConnected()){
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
                                //server.serverDisconnect();
                                System.exit(0);
                            }
                        }
                    }
                }).start();
            }
        }catch(Exception ex){}
    }
}