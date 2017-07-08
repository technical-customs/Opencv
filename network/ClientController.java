package network;


import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
class ClientController{
    
    private final Client client;
    private final ClientGui gui;
    public ClientController(Client client, ClientGui gui){
        this.client = client;
        this.gui = gui;
        
        this.gui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                    try{
                        client.disconnectChannel();
                    }catch(Exception ex){}
                    System.exit(0);
            }
        });
        
        gui.clientOnButtonListener(new ClientOnAction());
        gui.clientOffButtonListener(new ClientOffAction());
        gui.connectButtonListener(new ConnectAction());
        gui.disconnectButtonListener(new DisconnectAction());
        gui.sendButtonListener(new SendAction());
        gui.clearButtonListener(new ClearAction());
        gui.clearScreenButtonListener(new ClearScreenAction());
    }
    
    //**********GUI TO SERVER****************//
    private int getPortNumber(){
        return gui.getPortNumber();
    }
    private String getIpAddress(){
        return gui.getIpAddress();
    }
    protected void writeToDisplay(String string){
        gui.writeToDisplay(string);
    }
    private String readFromConsole(){
        return gui.getEnteredText();
    }
    //**********END GUI TO SERVER*************//
    
    private void checkServerConnection(SocketChannel channel){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    
                    while(client.getConnected()){
                        
                        if(!client.getConnected()){
                            try{
                                disconnectClient();
                            }catch(Exception ex){System.out.println("R " + ex);}

                            if(!client.getConnected()){
                                System.out.println("Running close ");
                                gui.enableConnectionEditing(true);
                                gui.writeToDisplay("DISCONNECTED" + "\n");
                            }
                        }
                        
                       
                    }
                    if(!client.getConnected()){
                        try{
                            disconnectClient();
                        }catch(Exception ex){System.out.println("R " + ex);}

                        if(!client.getConnected()){
                            System.out.println("Running close ");
                            gui.enableConnectionEditing(true);
                            gui.writeToDisplay("DISCONNECTED" + "\n");
                        }
                    }
                    
                    
                }catch(Exception ex){
                    System.out.println("Check server exception: " + ex);
                }
            }
        }).start();
    }
    private void disconnectClient(){
        client.disconnectChannel();
        gui.enableConnectionEditing(true);
        gui.writeToDisplay("DISCONNECTED" + "\n");
    }
    
    protected void writeToClientDisplay(){
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();
        
        
        
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(client.getConnected()){
                    gui.enableClearScreenButton(!gui.isDisplayEmpty());
                    
                    //get the buffer from the server
                    ByteBuffer buf2 = ByteBuffer.allocate(1024);
                    int bytesRead = -1;
                    
                    try {
                        bytesRead = client.getChannel().read(buf2);
                    } catch (IOException ex) {System.err.println("Bytes Read Exception: " + ex);}
                    
                    
                    if(bytesRead > 0){
                        byte[] data = new byte[bytesRead];
                        System.arraycopy(buf2.array(),0,data,0, bytesRead);
                        
                        
                        
                        if(new String(data).startsWith("USERNAME=")){
                            String username = new String(data).substring("USERNAME=".length());
                            client.setUsername(username);
                        } 
                        
                        gui.writeToDisplay(new String(data));
                        
                    }
                    
                }
            }
        }).start();
    }
    
    protected void readFromClient(String string){
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();
        
        //read from socket and display it on the screen
        new Thread(new Runnable(){
            @Override
            public void run(){
                if(client.isChannelConnected()){
                    //when user hits send it sends
                    
                    
                    try{
                        client.write(string);
                    }catch(Exception ex){}
                    
                    //gui.addToDisplay("\n");
                }
            }
        }).start();
    }
    private void read() throws IOException{
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(client.getConnected()){
                    try{
                        if(client.getChannel() != null){
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int numRead = client.getChannel().read(buffer);

                            if(numRead == -1){
                                //disconnect
                                client.disconnectChannel();
                                System.err.println("Read Closed: " + client.getChannel().toString());
                                return;
                            }


                            byte[] data = new byte[numRead];
                            System.arraycopy(buffer.array(),0,data,0, numRead);
                            
                            client.readData.setLength(0);
                            client.readData.append(data);
                            //send string through pipe
                            System.out.println("READ:   " + client.getChannel().toString() + ": " + new String(data));
                            
                            if(new String(data).startsWith("USERNAME=")){
                                String username = new String(data).substring("USERNAME=".length());
                                client.setUsername(username);
                            } 

                            gui.writeToDisplay(new String(data));
                        }
                    }catch(Exception ex){
                        System.err.println("Read Exception: " + ex);
                        client.disconnectChannel();
                        return;
                    }
                    
                }
            }
        }).start();
    }
   
    
    //**************ACTION CLASSES****************//
    class ClientOnAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            System.out.println(gui.getUsername());
            
            if(gui.getUsername() != null){
                if(gui.getUsername().contains(" ")){
                    return;
                }
                client.setUsername(gui.getUsername());
            }else if (gui.getUsername() == null){
                client.setUsername("Anonymous");
            }
            gui.enableAll();
            gui.writeToDisplay("CLIENT INITIATED as (" + client.getUsername() + ")" + "\n");
        }
    }
    class ClientOffAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            if(client.isChannelConnected()){
                try{
                    client.disconnectChannel();
                }catch(Exception ex){}
                
            }
            gui.writeToDisplay("CLIENT CLOSED" + "\n");
            gui.clearDisplay();
            gui.disableAll();
        }
    }
    class ConnectAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            try{
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        gui.writeToDisplay("ATTEMPTING CONNECTION TO " + getIpAddress() + " Port: " + getPortNumber() + "\n");
                        
                        client.connectChannel(getIpAddress(), getPortNumber());
                        
                        if(client.isChannelConnected()){
                            try {
                                client.write("USERNAME="+client.getUsername());
                                
                                gui.enableConnectionEditing(false);
                                gui.writeToDisplay("CONNECTED. LISTENING ON " + getIpAddress() + " Port: " + getPortNumber() + "\n");
                                
                                gui.clearDisplay();
                                
                                read();
                                checkServerConnection(client.getChannel());
                            } catch (IOException ex) {
                                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }).start();
            }catch(Exception ex){System.out.println("Connect ex " + ex);}
        }
    }
    class DisconnectAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            try{
                disconnectClient();
            }catch(Exception ex){}
            
            if(!client.isChannelConnected()){
                gui.enableConnectionEditing(true);
                gui.writeToDisplay("DISCONNECTED" + "\n");
            }
        }
    }
    class SendAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            if(gui.getEnteredText() != null){
                client.write(gui.getEnteredText());
                writeToDisplay("- " + gui.getEnteredText());
                gui.clearEnteredTextArea();
                readFromClient(readFromConsole());
            }
        }
    }
    class ClearAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            if(gui.getEnteredText() != null){
                gui.clearEnteredTextArea();
            }
        }
    }
    class ClearScreenAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            gui.clearDisplay();
        }
    }
    //**************END ACTION CLASSES*************//
    
}