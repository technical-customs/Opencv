package network;


import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



class ServerController{
    
    private final Server server;
    private final ServerGui gui;
    
    public ServerController(Server server, ServerGui gui){
        this.server = server;
        this.gui = gui;
        this.gui.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                if(shutDownOptionPane((JFrame) gui)){
                    try{
                        disconnectServer();
                    }catch(Exception ex){}
                    System.exit(0);
                }
            }
        });
        
        gui.serverOnButtonListener(new ServerOnAction());
        gui.serverOffButtonListener(new ServerOffAction());
        gui.portConnectButtonListener(new PortConnectAction());
        gui.portDisconnectButtonListener(new PortDisconnectAction());
        
        gui.sendButtonListener(new SendButtonAction());
        gui.clearButtonListener(new ClearButtonAction());
        gui.deleteLogButtonListener(new DeleteLogButtonAction());
        
        gui.userSelectionListener(new UserSelectAction());
        gui.kickUserButtonListener(new KickUserAction());
        gui.kickAllButtonListener(new KickAllAction());
        
        
    }
    private boolean shutDownOptionPane(JFrame frame){
        Object[] serverShutDownOptions = {"CONTINUE...", "ABORT!"};
        int serverShutDownOption =  JOptionPane.showOptionDialog(frame,
                "IT IS ADVISED TO NOT SHUTDOWN THE SERVER!",
                "SERVER SHUTDOWN",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                serverShutDownOptions,
                serverShutDownOptions[1]
        );
        
        if(serverShutDownOption == JOptionPane.YES_OPTION){
            return true;
        }
        if(serverShutDownOption == JOptionPane.NO_OPTION){
            return false;
        }
        if(serverShutDownOption == JOptionPane.CLOSED_OPTION){
            return false;
        }
        return false;
    }
    
    
    //**********GUI TO SERVER****************//
    private int getPortNumber(){
        return gui.getPortNumber();
    }
    protected void writeToDisplay(String string){
        gui.writeToDisplay(string);
    }
    //**********END GUI TO SERVER*************//
    
    
    //***********SERVER TO GUI***************//
    private String getIpAddress(){
        return server.getServerAddress();
    }
    
    private synchronized List<SocketChannel> getuserList(){
        return server.getUserList();
    }
    private synchronized Map<SocketChannel,String> getusermap(){
        return server.getUserMap();
    }
    
    private synchronized boolean isServerClosed(){
        return server.isServerClosed();
    }
    private synchronized void disconnectServer(){
        server.serverDisconnect();
    }
    
    
    private synchronized void checkForUsers(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    synchronized(getuserList()){
                        while(server.isServerConnected()){
                            //gui.enableUserClick(gui.getUserListModel().size() > 0);
                            gui.enableKickAllButton(!gui.getUserListModel().isEmpty());
                            
                            if(getuserList().size() == gui.getUserListModel().size()){
                                //same size
                                
                                continue;
                            }
                            clearUserList();
                            
                            //System.out.println("serverUSERS: " + getuserList().toString());
                            
                            for(SocketChannel sc: getuserList()){
                                gui.addListItem(sc.toString());
                            }
                            
                            //System.out.println("guiUSERS: " + Arrays.toString(gui.getUserListModel().toArray()));
                              
                           
                            Thread.sleep(60);
                        }
                        
                    }
                }catch(Exception ex){
                    System.err.println("User Sync exception: " + ex);
                    server.log("User Sync exception: " + ex);
                }
            }
        }).start();
    }
    private synchronized void checkForUsersMap(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    synchronized(getusermap()){
                        while(server.isServerConnected()){
                            //gui.enableUserClick(gui.getUserListModel().size() > 0);
                            gui.enableKickAllButton(!gui.getUserListModel().isEmpty());
                            
                            if(getusermap().size() == gui.getUserListModel().size()){
                                //same size
                                
                                continue;
                            }
                            clearUserList();
                            
                            //System.out.println("serverUSERS: " + getuserList().toString());
                            
                            for(SocketChannel sc: getusermap().keySet()){
                                gui.addListItem(server.getUserMap().get(sc));
                            }
                            
                            //System.out.println("guiUSERS: " + Arrays.toString(gui.getUserListModel().toArray()));
                              
                           
                            Thread.sleep(60);
                        }
                        
                    }
                }catch(Exception ex){
                    System.err.println("User Sync exception: " + ex);
                    server.log("User Sync exception: " + ex);
                }
            }
        }).start();
    }
    
    private void clearUserList(){
        gui.clearListModel();
        gui.getUserList().setModel(gui.getUserListModel());
        
    }
   
    //**********END SERVER TO GUI************//
    
    //****************CONTROLLER INFO***********//
    private void logGrabber(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                BufferedReader br = null;
                try {
                    //clear Screen
                    gui.clearDisplay();
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(server.getLogFile())));
                    System.out.println("LOG FILE: " + server.getLogFile());
                    
                    while(server.isServerConnected()){
                        String line = br.readLine();
                        if(line != null){
                            gui.writeToDisplay(line);
                        }else{
                            Thread.sleep(1);
                        }
                        //get log file and update every 60 frames
                        
                        
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if(br != null){
                            br.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    } 
    //************END CONTROLLER INFO***********//
    
    
    
    
    //**************ACTION CLASSES****************//
    class ServerOnAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            gui.enableAll();
            gui.writeToDisplay("SERVER INITIATED" + "\n");
            server.log("SERVER INITIATED");
        }
    }
    class ServerOffAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            try{
                if(server.getConnected()){
                    disconnectServer();
                }
            }catch(Exception ex){
                System.err.println("Server off Exception:" + ex);
                server.log("Server off Exception:" + ex);
            }finally{
                gui.writeToDisplay("SERVER CLOSED" + "\n");
                server.log("SERVER CLOSED");
                gui.setTitle("-");
                //clearUserList();
                gui.disableAll();
            }
            
            
        }
    }
    class PortConnectAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try{
                        server.serverConnect(getPortNumber());

                        if(server.getConnected()){
                            gui.setTitle(getIpAddress() + " Port: " + getPortNumber());
                            gui.enablePortEditing(false);
                            gui.writeToDisplay("CONNECTED. LISTENING ON " + getIpAddress() + " Port: " + getPortNumber() + "\n");
                            server.log("CONNECTED. LISTENING ON " + getIpAddress() + " Port: " + getPortNumber());
                            //Init here............
                            checkForUsersMap();
                            gui.enableTextEditing(true);
                            //checkForMessages();
                            logGrabber();
                    }
                    }catch(Exception ex){
                        System.err.println("Server Connect ex: " + ex);
                        server.log("Server Connect ex: " + ex);
                    }
                }
            }).start();
            
        }
    }
    class PortDisconnectAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            try{
                disconnectServer();
            }catch(Exception ex){
                
            }
            
            if(isServerClosed()){
                gui.enablePortEditing(true);
                gui.enableTextEditing(false);
                
                gui.setTitle("-");
                clearUserList();
                
                gui.writeToDisplay("DISCONNECTED" + "\n");
                server.log("DISCONNECTED");
            }
        }
    }
    
    class SendButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            if(!gui.getEnteredText().isEmpty()){
                server.log("SERVER: " + gui.getEnteredText());
                server.broadcastMessageMap(gui.getEnteredText());
                
                
                gui.clearEnteredTextArea();
            }
        }
        
    }
    class ClearButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            if(gui.getEnteredText() != null){
                gui.clearEnteredTextArea();
            }
        }
        
    }
    class DeleteLogButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
        
        }
        
    }
    
    class UserSelectAction implements ListSelectionListener{

        @Override
        public void valueChanged(ListSelectionEvent e) {
           try{
               if(!e.getValueIsAdjusting()){
                   JList source = (JList)e.getSource();
                   String sel = source.getSelectedValue().toString();

                    if(!sel.isEmpty()){
                        
                        gui.enableKickButton(true);
                    }else{
                        gui.enableKickButton(false);
                    }
               }
               
               
               
           }catch(Exception ex){
               System.err.println("USER SELECT ex: " + ex);
               server.log("USER SELECT ex: " + ex);
           }
           
        }
    
}
    class KickUserAction implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae){
            //get selecteed user from list
            //gui.getUserListModel().get
            //check if online, boot
            String su = gui.getUserList().getSelectedValue();
            
            if(su != null){
                //Iterator<SocketChannel> uli = server.getUserList().iterator();
                Iterator<SocketChannel> uli = getusermap().keySet().iterator();
                
                while(uli.hasNext()){
                    SocketChannel sc = uli.next();
                    
                    if(sc.toString().equals(su)){
                        System.out.println("Kick " + sc);
                        server.log("Kick " + sc);
                        server.closeUserMap(sc);
                    }
                }
            }
            
        }
    }
    class KickAllAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            Iterator<SocketChannel> uli = getusermap().keySet().iterator();
                
                while(uli.hasNext()){
                    SocketChannel sc = uli.next();
                    
                    System.out.println("Kick " + sc);
                    server.log("Kick " + sc);
                    server.closeUserMap(sc);
                    
                }
        }
        
    }
    //**************END ACTION CLASSES*************//
    
    
    public static void main(String[] arg){
        
    }
}