package network;

class ClientSetup{
    
    public static void main(String[] args){
        Client client = new Client();
        ClientGui gui = new ClientGui();
        ClientController controller = new ClientController(client, gui);
    }
}