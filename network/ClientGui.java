package network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.text.DefaultCaret;

class ClientGui extends JFrame{
    private final int GUIWIDTH = 520;
    private final int GUIHEIGHT = 400;
    
    private JPanel mainPanel;
    
    //main middle
    private JPanel mainMiddlePanel;
    
    //main middle north
    private JPanel mainMiddleNorthPanel;
    private JTextArea displayedTextArea;
    private JScrollPane displayedTextScroll;
    
    //main middle south
    private JPanel mainMiddleSouthPanel;
    private JTextArea enteredTextArea;
    private JScrollPane enteredTextScroll;
    
    //main east
    private JPanel mainEastPanel;
    //main east north
    private JPanel mainEastNorthPanel;
    
    private JPanel usernamePanel;
    private JLabel usernameLabel;
    private JTextField usernameText;
    
    private JPanel powerButtonPanel;
    private JButton clientOnButton;
    private JButton clientOffButton;
    
    private JPanel ipPanel;
    private JLabel ipLabel;
    private JTextField ipField;
    
    private JPanel portPanel;
    private JLabel portLabel;
    private JTextField portField;
    
    private JPanel connectionPanel;
    private JButton connectButton;
    private JButton disconnectButton;
    
    
    //main east middle
    
    //main east south
    private JPanel mainEastSouthPanel;
    
    private JPanel enteredTextButtonPanel;
    private JButton sendButton;
    private JButton clearButton;
    private JButton clearScreenButton;
    
    public ClientGui(){
        super();
        try {
            SwingUtilities.invokeAndWait(new Runnable(){
                @Override
                public void run(){
                    setupGUI();
                    disableAll();
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {}
    }
    
    private void setupGUI(){
        this.setTitle("TechClient");
        this.setSize(GUIWIDTH, GUIHEIGHT);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                System.exit(0);
            }
        });
        
        this.setLocationRelativeTo(null);
        mainPanel = new JPanel(new BorderLayout());
         
        mainMiddlePanel = new JPanel(new BorderLayout());
        mainPanel.add(mainMiddlePanel, BorderLayout.CENTER);
        
        //mmn
        displayedTextArea = new JTextArea();
        displayedTextArea.setLineWrap(true);
        displayedTextArea.setEditable(false);
        displayedTextScroll = new JScrollPane(displayedTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainMiddlePanel.add(displayedTextScroll, BorderLayout.CENTER);
        
        
        //mms
        enteredTextArea = new JTextArea(0,5);
        enteredTextArea.setLineWrap(true);
        enteredTextScroll = new JScrollPane(enteredTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainMiddlePanel.add(enteredTextScroll, BorderLayout.SOUTH);
        
        mainEastPanel = new JPanel(new BorderLayout());
        mainPanel.add(mainEastPanel, BorderLayout.EAST);
        
        //en
        mainEastNorthPanel = new JPanel(new GridLayout(5,1));
        mainEastPanel.add(mainEastNorthPanel, BorderLayout.NORTH);
        
        usernamePanel = new JPanel(new GridLayout(1,2));
        usernameLabel = new JLabel("Username:");
        usernameText = new JTextField();
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameText);
        mainEastNorthPanel.add(usernamePanel);
        
        powerButtonPanel = new JPanel(new GridLayout(1,2));
        clientOnButton = new JButton("ON");
        clientOffButton = new JButton("OFF");
        powerButtonPanel.add(clientOnButton);
        powerButtonPanel.add(clientOffButton);
        mainEastNorthPanel.add(powerButtonPanel);

        ipPanel = new JPanel(new GridLayout(1,2));
        ipLabel = new JLabel("IP:");
        ipField = new JTextField(10);
        ipField.setText("127.0.0.1");
        ipPanel.add(ipLabel);
        ipPanel.add(ipField);
        mainEastNorthPanel.add(ipPanel);

        portPanel = new JPanel(new GridLayout(1,2));
        portLabel = new JLabel("PORT:");
        portField = new JTextField(5);
        portPanel.add(portLabel);
        portPanel.add(portField);
        mainEastNorthPanel.add(portPanel);

        connectionPanel = new JPanel(new GridLayout(1,2));
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);
        mainEastNorthPanel.add(connectionPanel);
        
        
        //es
        mainEastSouthPanel = new JPanel();
        mainEastPanel.add(mainEastSouthPanel, BorderLayout.SOUTH);
        
        enteredTextButtonPanel = new JPanel(new GridLayout(1,3));
        sendButton = new JButton("SEND");
        clearButton = new JButton("CLEAR");
        clearScreenButton = new JButton("CLS");
        enteredTextButtonPanel.add(sendButton);
        enteredTextButtonPanel.add(clearButton);
        enteredTextButtonPanel.add(clearScreenButton);
        mainEastSouthPanel.add(enteredTextButtonPanel);
        
        
        this.getContentPane().add(mainPanel);
        this.setVisible(true);
    }
     //******************SERVER GUI INITIATE********************//
    protected void setFrameTitle(String string){
        this.setTitle(string);
    }
    protected String getUsername(){
        if(!usernameText.getText().isEmpty()){
            return usernameText.getText();
        }
        return null;
    }
    protected int getPortNumber(){
        if(portField.getText().length() > 0){
            return Integer.parseInt(portField.getText());
        }
        return 0;
    }
    protected String getIpAddress(){
        if(ipField.getText().length() > 0 && ipField.getText()!= null){
            return ipField.getText();
        }
        return null;
    }
    
    protected void enableNameEdit(boolean activate){
        usernameText.setEditable(activate);
    }
    protected void enablePower(boolean activate){
        enableClientOnButton(!activate);
        enableClientOffButton(activate);
        enableNameEdit(!activate);
    }
    protected void enableConnectionEditing(boolean activate){
        enableIpField(activate);
        enablePortField(activate);
        enableConnectButton(activate);
        enableDisconnectButton(!activate);
        enableTextEditing(!activate);
    }
    protected void enableTextEditing(boolean activate){
        enableSendButton(activate);
        enableClearButton(activate);
        enableEnteredTextArea(activate);
    }
    protected void enableAll(){
        enableNameEdit(false);
        enablePower(true);
        enableConnectionEditing(true);
        enableTextEditing(false);
    }
    protected void disableAll(){
        clientOnButton.setEnabled(true);
        clientOffButton.setEnabled(false);
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        sendButton.setEnabled(false);
        clearButton.setEnabled(false);
        clearScreenButton.setEnabled(false);
        enableNameEdit(true);
        enableIpField(false);
        enablePortField(false);
        enableEnteredTextArea(false);
    }
    //******************END SERVER GUI INITIATE********************//
    
    
    //****************BUTTON ENABLING******************//
    private void enableClientOnButton(boolean activate){
        clientOnButton.setEnabled(activate);
    }
    private void enableClientOffButton(boolean activate){
        clientOffButton.setEnabled(activate);
    }
    private void enableConnectButton(boolean activate){
        connectButton.setEnabled(activate);
    }
    private void enableDisconnectButton(boolean activate){
        disconnectButton.setEnabled(activate);
    }
    private void enableSendButton(boolean activate){
        sendButton.setEnabled(activate);
    }
    private void enableClearButton(boolean activate){
        clearButton.setEnabled(activate);
    }
    protected void enableClearScreenButton(boolean activate){
        clearScreenButton.setEnabled(activate);
    }
    //*****************END BUTTON ENABLING*****************//
    
    //*************FIELD DISABLING*************************//
    private void enableIpField(boolean activate){
        ipField.setEditable(activate);
    }
    private void enablePortField(boolean activate){
        portField.setEditable(activate);
    }
    private void enableEnteredTextArea(boolean activate){
        enteredTextArea.setEditable(activate);
    }
    //************* ENDFIELD DISABLING********************//
    
    
    //*****************DISPLAY INFO********************//
    public void clearDisplay(){
        displayedTextArea.setText(null);
    }
    public void writeToDisplay(String string){
        try{
            DefaultCaret caret = (DefaultCaret) displayedTextArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            displayedTextArea.append(string + "\n");
        }catch(NullPointerException ex){
            System.exit(0);
        }
    }
    public void addToDisplay(String string){
        DefaultCaret caret = (DefaultCaret) displayedTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        displayedTextArea.append(string);
    }
    public boolean isDisplayEmpty(){
        return displayedTextArea.getText().isEmpty();
    }
    //*****************END DISPLAY INFO********************//
    
    //*****************ENTERED INFO********************//
    public void clearEnteredTextArea(){
        if(enteredTextArea.getText().length() > 0 && enteredTextArea.getText() != null){
            enteredTextArea.setText(null);
        }
    }
    public String getEnteredText(){
        if(enteredTextArea.getText().length() > 0 && enteredTextArea.getText() != null){
            return enteredTextArea.getText();
        }
        return null;
    }
    //*****************END ENTERED INFO********************//
    
    
    //*****************ACTION LISTENERS********************//
    protected void clientOnButtonListener(ActionListener al){
        clientOnButton.addActionListener(al);
    }
    protected void clientOffButtonListener(ActionListener al){
        clientOffButton.addActionListener(al);
    }
    protected void connectButtonListener(ActionListener al){
        connectButton.addActionListener(al);
    }
    protected void disconnectButtonListener(ActionListener al){
        disconnectButton.addActionListener(al);
    }
    protected void sendButtonListener(ActionListener al){
        sendButton.addActionListener(al);
    }
    protected void clearButtonListener(ActionListener al){
        clearButton.addActionListener(al);
    }
    protected void clearScreenButtonListener(ActionListener al){
        clearScreenButton.addActionListener(al);
    }
    //*************END ACTION LISTENERS********************//
    public static void main(String[] args){
        ClientGui gui = new ClientGui();
    }
}