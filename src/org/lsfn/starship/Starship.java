package org.lsfn.starship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Starship {

    private ConsoleServer consoleServer;
    private boolean keepGoing;
    
    public Starship() {
        this.consoleServer = null;
        this.keepGoing = true;
    }
    
    private void startConsoleServer() {
        this.consoleServer = new ConsoleServer();
        this.consoleServer.run();
    }
    
    private void processCommand(String commandStr) {
        String[] commandParts = commandStr.split(" ");
         
        if(commandParts[0].equals("listen")) {
            startConsoleServer();
        } else if(commandParts[0].equals("exit")) {
            this.keepGoing = false;
        } else {
            System.out.println("You're spouting gibberish. Please try English.");
        }
    }
    
    public void run(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(this.keepGoing) {
            try {
                processCommand(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Close up the threads
        consoleServer.disconnect();
        try {
            consoleServer.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
