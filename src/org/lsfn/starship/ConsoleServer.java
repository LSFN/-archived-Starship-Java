package org.lsfn.starship;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lsfn.starship.STS.*;

/**
 * Creates a listener thread for each connection.
 * Each connection assigned unique ID.
 * ID can be used to receive messages from and to send to a specific console.
 * @author Lukeus_Maximus
 *
 */
public class ConsoleServer extends Thread {
    
    private ServerSocket consoleServer;
    private Map<Integer, ConsoleListener> listeners;
    private Random rand;
    
    public ConsoleServer() {
        consoleServer = null;
        listeners = null;
        rand = new Random();
    }
    
    public Map<Integer, List<STSup>> readMessages() {
        Map<Integer, List<STSup>> inputs = new HashMap<Integer, List<STSup>>();
        for(Integer i : listeners.keySet()) {
            inputs.put(i, listeners.get(i).readMessages());
        }
        return inputs;
    }
    
    public void writeMessage(Integer i, STSdown downMessage) {
        ConsoleListener listener = listeners.get(i);
        if(listener != null) {
            listener.writeMessage(downMessage);
        }
    }
    
    public void disconnect() {
        try {
            this.consoleServer.close();
        } catch (IOException e) {
            e.printStackTrace();
            this.consoleServer = null;
        }
        destroyListeners();
    }
    
    private void destroyListeners() {
        if(listeners != null) {
            for(Integer i : listeners.keySet()) {
                ConsoleListener listener = listeners.get(i);
                listener.disconnect();
                try {
                    listener.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        listeners.clear();
    }
    
    @Override
    public void run() {
        try {
            consoleServer = new ServerSocket(39460);
            listeners = new HashMap<Integer, ConsoleListener>();
            System.out.println("Listening on port " + consoleServer.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(consoleServer != null && !consoleServer.isClosed()) {
            try {
                Socket newConsole = consoleServer.accept();
                ConsoleListener listener = new ConsoleListener(newConsole);
                Integer randomVal = rand.nextInt();
                while(listeners.containsKey(randomVal)) {
                    randomVal = rand.nextInt();
                }
                listeners.put(randomVal, listener);
                System.out.println("New connection from " + newConsole.getInetAddress().getHostAddress() + " assigned ID " + randomVal.toString());
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    consoleServer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    consoleServer = null;
                }
            }
        }
        destroyListeners();
        System.out.println("ConsoleServer stopped");
    }
}
