package org.lsfn.starship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lsfn.starship.ConsoleServer.ServerStatus;


public class Starship {

    private ConsoleServer consoleServer;
    private MessageHandler messageHandler;
    private boolean keepGoing;
    
    public Starship() {
        // TODO make sure ConsoleServer and NebulaConnection can do multiple runs without needing a new one of them. 
        this.consoleServer = new ConsoleServer();
        this.messageHandler = new MessageHandler(this.consoleServer);
        this.keepGoing = true;
    }
    
    private void startConsoleServer(int port) {
        if(port == -1) {
            this.consoleServer.listen();
        } else {
            this.consoleServer.listen(port);
        }
        this.consoleServer.start();
        this.messageHandler.start();
    }
    
    private void printHelp() {
        System.out.println("Starship commands:");
        System.out.println("\thelp                  : Print this help text.");
        System.out.println("\tlisten                : Listens for Starship connections on the default port.");
        System.out.println("\texit                  : Exit the program.");
    }
    
    private void processCommand(String commandStr) {
        String[] commandParts = commandStr.split(" ");
         
        if(commandParts[0].equals("listen")) {
            if(commandParts.length >= 2) {
                startConsoleServer(Integer.parseInt(commandParts[1]));
            } else {
                startConsoleServer(-1);
            }
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
