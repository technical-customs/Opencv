package network;

import java.io.IOException;
import java.io.Serializable;

public class ServerSetup implements Serializable{
    public static void main(String[] args) throws IOException{
        Server server = new Server();
        ServerGui gui = new ServerGui();
        ServerController controller = new ServerController(server, gui);
    }
}