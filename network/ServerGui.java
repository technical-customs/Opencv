package network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

class ServerGui extends JFrame{
    //basic gui setup
    private final int GUIWIDTH = 800, GUIHEIGHT = 400;
    private JPanel mainPanel;
    
    //panel 1 - connect disconnect button
    private JPanel connectionPanel;
    private JPanel northernConnectionPanel;//grid layout
    
    private JPanel onOffPanel;//grid layout
    private JButton serverOnButton;
    private JButton serverOffButton;
    
    private JPanel searchAndConnectPanel;//gridlayout
    private JPanel portPanel;
    private JTextField portField;
    private JPanel portConnectPanel;
    private JButton portConnectButton;
    private JButton portDisconnectButton;
    
    //panel 2 - log panel
    private JPanel logPanel;
    private JTextArea logArea;
    private JScrollPane logScroll;
    
    //panel 3 - user info and control panel
    private DefaultListModel<String> userListModel;
    private JPanel userPanel;
    private JPanel userInfoGroupPanel;
    private JList<String> userList;
    private JScrollPane userScroll;
    private JButton kickUserButton;
    
   
    public ServerGui(){
        //basic gui setup
        super();
        
        userListModel = new DefaultListModel<>();
        try {
            SwingUtilities.invokeAndWait(new Runnable(){
                @Override
                public void run(){
                    setupGUI();
                    
                    disableAll();
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            System.out.println("UNABLE TO INITIATE GUI");
        }
        
    }
    
    //*******************SETUP INFO*********************//
    private void setupGUI(){
        //setup main panel
        this.setTitle("-");
        this.setSize(GUIWIDTH, GUIHEIGHT);
        this.setPreferredSize(new Dimension(200,400));
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        this.setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new BorderLayout());
        
        //panel 1
        connectionPanel = new JPanel(new BorderLayout());
        connectionPanel.setPreferredSize(new Dimension(200,400));
        
        northernConnectionPanel = new JPanel(new GridLayout(2,1));//grid layout

        onOffPanel = new JPanel(new GridLayout(1,2));//grid layout
        serverOnButton = new JButton("ON");
        serverOffButton = new JButton("OFF");

        searchAndConnectPanel = new JPanel(new GridLayout(2,1));//gridlayout
        portPanel = new JPanel();
        portField = new JTextField(5);
        portField.setText("PORT #");
        portConnectPanel = new JPanel(new GridLayout(1,2));
        portConnectButton = new JButton("Connect");
        portDisconnectButton = new JButton("Disconnect");
        
        onOffPanel.add(serverOnButton);
        onOffPanel.add(serverOffButton);
        portPanel.add(portField);
        portConnectPanel.add(portConnectButton);
        portConnectPanel.add(portDisconnectButton);
        searchAndConnectPanel.add(portPanel);
        searchAndConnectPanel.add(portConnectPanel);
        northernConnectionPanel.add(onOffPanel);
        northernConnectionPanel.add(searchAndConnectPanel);
        connectionPanel.add(northernConnectionPanel, BorderLayout.NORTH);
        mainPanel.add(connectionPanel, BorderLayout.WEST);
        
        //panel 2
        logPanel = new JPanel(new BorderLayout());
        logPanel.setPreferredSize(new Dimension(400,400));
        
        logArea = new JTextArea();
        logArea.setLineWrap(true);
        logArea.setEditable(false);
        
        logScroll = new JScrollPane(logArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logPanel.add(logScroll);
        
        mainPanel.add(logPanel, BorderLayout.CENTER);
        
        //panel 3
        userPanel = new JPanel(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(200,400));
        userInfoGroupPanel = new JPanel(new BorderLayout());
        
        userList = new JList<>();
        userList.setModel(userListModel);
        userScroll = new JScrollPane(userList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        kickUserButton = new JButton("Kick User");
        
        userInfoGroupPanel.add(userScroll, BorderLayout.CENTER);
        userInfoGroupPanel.add(kickUserButton, BorderLayout.SOUTH);
        userPanel.add(userInfoGroupPanel, BorderLayout.NORTH);
        mainPanel.add(userPanel, BorderLayout.EAST);
        
        this.getContentPane().add(mainPanel);
        this.setVisible(true);
    }
    //****************END SETUP INFO********************//
   
    //******************SERVER GUI INITIATE********************//
    protected void setFrameTitle(String string){
        this.setTitle(string);
    }
    protected int getPortNumber(){
        if(portField.getText() != null){
            return Integer.parseInt(portField.getText());
        }
        return 0;
    }
    protected void enablePower(boolean activate){
        enableServerOnButton(!activate);
        enableServerOffButton(activate);
    }
    protected void enablePortEditing(boolean activate){
        enablePortField(activate);
        enablePortConnectButton(activate);
        enablePortDisconnectButton(!activate);
    }
    
    protected void enableAll(){
        enablePower(true);
        enablePortEditing(true);
        //enableUserInfo(true);
    }
    protected void disableAll(){
        serverOnButton.setEnabled(true);
        serverOffButton.setEnabled(false);
        portConnectButton.setEnabled(false);
        portDisconnectButton.setEnabled(false);
        kickUserButton.setEnabled(false);
        enablePortField(false);
        enableUserClick(false);
    }
    //******************END SERVER GUI INITIATE********************//
    
    
    //****************BUTTON ENABLING******************//
    private void enableServerOnButton(boolean activate){
        serverOnButton.setEnabled(activate);
    }
    private void enableServerOffButton(boolean activate){
        serverOffButton.setEnabled(activate);
    }
    private void enablePortConnectButton(boolean activate){
        portConnectButton.setEnabled(activate);
    }
    private void enablePortDisconnectButton(boolean activate){
        portDisconnectButton.setEnabled(activate);
    }
    public void enableKickButton(boolean activate){
        kickUserButton.setEnabled(activate);
    }
    //*****************END BUTTON ENABLING*****************//
    
    //*************FIELD DISABLING*************************//
    private void enablePortField(boolean activate){
        portField.setEditable(activate);
    }
    public void enableUserClick(boolean activate){
        userList.setEnabled(activate);
        
        
    }
    //************* ENDFIELD DISABLING********************//
    
    
    //****************LOGAREA INFO**********************//
    public void clearDisplay(){
        logArea.setText(null);
    }
    public void writeToDisplay(String string){
        try{
            DefaultCaret caret = (DefaultCaret) logArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            logArea.append(string + "\n");
        }catch(NullPointerException ex){
            System.exit(0);
        }
    }
    public void addToDisplay(String string){
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        logArea.append(string);
    }
    //***************END LOGAREA INFO*******************//
    
    //****************USER LIST***************************//
    public void addListModel(DefaultListModel<String> userListModel){
        this.userListModel = userListModel;
    }
    public void addListItem(String string){
        userListModel.addElement(string);
    }
    public void setList(ArrayList list){
        //convert list to list of strings
        
        ArrayList<String> newList = new ArrayList<>();
        
        synchronized(list){
            userList = new JList(list.toArray());
        }
        
        
    }
    public JList<String> getUserList(){
        return userList;
    }
    public DefaultListModel<String> getUserListModel(){
        return userListModel;
    }
    public void clearListModel(){
        userListModel.removeAllElements();
        userList.setModel(userListModel);
    }
    //************* END USER LIST***************************//

    
    //*****************ACTION LISTENERS********************//
    protected void serverOnButtonListener(ActionListener al){
        serverOnButton.addActionListener(al);
    }
    protected void serverOffButtonListener(ActionListener al){
        serverOffButton.addActionListener(al);
    }
    protected void portConnectButtonListener(ActionListener al){
        portConnectButton.addActionListener(al);
    }
    protected void portDisconnectButtonListener(ActionListener al){
        portDisconnectButton.addActionListener(al);
    }
    protected void userSelectionListener(ListSelectionListener ll){
        userList.addListSelectionListener(ll);
    }
    protected void kickUserButtonListener(ActionListener al){
        kickUserButton.addActionListener(al);
    }
    //*************END ACTION LISTENERS********************//
    
    public static void main(String[] args){
        ServerGui tsg = new ServerGui();
    }
}