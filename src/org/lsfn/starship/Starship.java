package org.lsfn.starship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lsfn.starship.ConsoleServer.ServerStatus;


public class Starship {

    private ConsoleServer consoleServer;
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
    
    private void printHelp() {
        System.out.println("Starship commands:");
        System.out.println("\thelp   : print this help text.");
        System.out.println("\tlisten : opens the console server on the default port.");
        System.out.println("\texit   : end this program.");
    }
    
    private void processCommand(String commandStr) {
        String[] commandParts = commandStr.split(" ");
         
        if(commandParts[0].equals("listen")) {
            startConsoleServer();
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
