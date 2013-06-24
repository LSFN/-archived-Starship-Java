package org.lsfn.starship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lsfn.starship.ConsoleServer.ServerStatus;
import org.lsfn.starship.NebulaConnection.ConnectionStatus;


public class Starship {

    private ConsoleServer consoleServer;
    private NebulaConnection nebulaConnection;
    private boolean keepGoing;
    
    public Starship() {
        this.consoleServer = null;
        this.keepGoing = true;
    }
    
    private void startConsoleServer() {
        this.consoleServer = new ConsoleServer();
        this.consoleServer.listen();
        this.consoleServer.start();
    }
    
    private void startNebulaClient(String host, Integer port) {
        this.nebulaConnection = new NebulaConnection();
        System.out.println("Connecting...");
        ConnectionStatus status = ConnectionStatus.DISCONNECTED;
        if(host == null || port == null) {
            status = this.nebulaConnection.connect();
        } else {
            status = this.nebulaConnection.connect(host, port);
        }
        if(status == ConnectionStatus.CONNECTED) {
            this.nebulaConnection.start();
            System.out.println("Connected.");
        } else {
            System.out.println("Connection failed.");
        }
    }

    private void stopNebulaClient() {
        if(this.nebulaConnection != null) {
            if(this.nebulaConnection.getConnectionStatus() == ConnectionStatus.CONNECTED) {
                this.nebulaConnection.disconnect();
            }
        }
        System.out.println("Disconnected.");
    }
    
    private void printHelp() {
        System.out.println("Starship commands:");
        System.out.println("\thelp   : print this help text.");
        System.out.println("\tlisten : opens the console server on the default port.");
        System.out.println("\tconnect <host> <port> : connects to the Nebula on the given host and port.");
        System.out.println("\tconnect               : connects to the Nebula on the default host and port.");
        System.out.println("\texit   : end this program.");
    }
    
    private void processCommand(String commandStr) {
        String[] commandParts = commandStr.split(" ");
         
        if(commandParts[0].equals("listen")) {
            startConsoleServer();
        } else if(commandParts[0].equals("connect")) {
            if(commandParts.length == 3) {
                startNebulaClient(commandParts[1], Integer.parseInt(commandParts[2]));
            } else if(commandParts.length == 1) {
                startNebulaClient(null, null);
            }
        } else if(commandParts[0].equals("disconnect")) {
            stopNebulaClient();
        } else if(commandParts[0].equals("exit")) {
            this.keepGoing = false;
        } else if(commandParts[0].equals("help")) {
            printHelp();
        } else {
            System.out.println("You're spouting gibberish. Please try English.");
        }
    }
    
    public void run(String[] args) {
        printHelp();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(this.keepGoing) {
            try {
                processCommand(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Close up the threads
        if(consoleServer.getListenStatus() == ServerStatus.OPEN) {
            consoleServer.shutDown();
        }
        if(consoleServer.isAlive()) {
            try {
                consoleServer.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        Starship starship = new Starship();
        starship.run(args);
    }

}
