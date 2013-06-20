package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.lsfn.starship.STS.*;

public class ConsoleListener extends Thread {

    private Socket consoleSocket;
    private BufferedInputStream consoleInput;
    private BufferedOutputStream consoleOutput;
    private ArrayList<STSup> consoleMessages;
    private boolean running = true;
    
    public ConsoleListener(Socket socket) {
        consoleSocket = socket;
        consoleInput = null;
        consoleOutput = null;
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gets all the messages received from the Console in the buffer.
     * Clears the buffer after this call.
     * @return A list of messages from the Console.
     */
    public synchronized List<STSup> readMessages() {
        ArrayList<STSup> result = new ArrayList<STSup>(this.consoleMessages);
        this.consoleMessages.clear();
        return result;
    }
    
    /**
     * Adds a message received from the Console to the buffer.
     * @param upMessage The message received.
     */
    private synchronized void addMessageToBuffer(STSup upMessage) {
        this.consoleMessages.add(upMessage);
    }
    
    /**
     * Sends a message to the Console if connected.
     * @param downMessage The message to be sent.
     */
    public void writeMessage(STSdown downMessage) {
        try {
            downMessage.writeDelimitedTo(this.consoleOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void disconnect() {
        try {
            this.consoleSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            consoleInput = new BufferedInputStream(consoleSocket.getInputStream());
            consoleOutput = new BufferedOutputStream(consoleSocket.getOutputStream());
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(running) {
            try {
                STSup nextMessage = STSup.parseDelimitedFrom(consoleInput);
                addMessageToBuffer(nextMessage);
            } catch (IOException e) {
                e.printStackTrace();
                // Disconnects in the case that the exception doesn't cause a disconnection.
                if(consoleSocket.isConnected()) {
                    try {
                        this.consoleSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                consoleInput = null;
                consoleOutput = null;
                consoleMessages = null;
                running = false;
            }
        }
    }
}
