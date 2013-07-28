package org.lsfn.starship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Starship {

    private MessageHandler messageHandler;
    private boolean keepGoing;
    
    public Starship() {
        // TODO make sure ConsoleServer and NebulaConnection can do multiple runs without needing a new one of them. 
        this.messageHandler = null;
        this.keepGoing = true;
    }
    
    private void printHelp() {
        System.out.println("Starship commands:");
        System.out.println("\thelp                  : Print this help text.");
        System.out.println("\tlisten                : Listens for Starship connections on the default port.");
        System.out.println("\texit                  : Exit the program.");
    }
    
    private void processCommand(String commandStr) {
        String[] commandParts = commandStr.split(" ");
         
        if(commandParts[0].equals("exit")) {
            this.keepGoing = false;
        } else if(commandParts[0].equals("help")) {
            printHelp();
        } else {
            System.out.println("You're spouting gibberish. Please try English.");
        }
    }
    
    public void run(String[] args) {
        printHelp();
        
        this.messageHandler = new MessageHandler();
        this.messageHandler.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(this.keepGoing) {
            try {
                processCommand(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // TODO some sensible shut down
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        Starship starship = new Starship();
        starship.run(args);
    }

}
