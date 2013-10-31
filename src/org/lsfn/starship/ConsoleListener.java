package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.lsfn.common.STS.STSdown;
import org.lsfn.common.STS.STSup;

/**
 * The idea here is that if the connection fails for some reason,
 * the send / receive methods will not produce exceptions.
 * If a disconnection occurs, the enclosing class should check for this
 * by periodically querying getConnectionStatus().
 * @author Lukeus_Maximus
 *
 */
public class ConsoleListener {

    private static final long timeout = 5000;
    private static final STSdown pongMessage = STSdown.newBuilder().build();
    
    private Socket consoleSocket;
    private BufferedInputStream consoleInput;
    private BufferedOutputStream consoleOutput;
    private Long timeLastMessageReceived;
    private boolean connected;
    
    /**
     * If this throws and error, the incoming connection should be discarded immediately.
     * @param consoleSocket
     * @throws IOException
     */
    public ConsoleListener(Socket consoleSocket) throws IOException {
        this.consoleSocket = consoleSocket;
        this.consoleInput = new BufferedInputStream(this.consoleSocket.getInputStream());
        this.consoleOutput = new BufferedOutputStream(this.consoleSocket.getOutputStream());
        this.timeLastMessageReceived = System.currentTimeMillis();
        this.connected = true;
    }
    
    /**
     * It is expected that this is called periodically by enclosing classes
     * to check that this listener is still useful.
     * @return true if still connected to client, false otherwise 
     */
    public boolean isConnected() {
        // Check for a timeout
        if(this.connected && System.currentTimeMillis() >= this.timeLastMessageReceived + timeout) {
            disconnect();
        }
        return connected;
    }
    
    /**
     * Disconnects the socket that this listener was listening to.
     */
    public void disconnect() {
        try {
            this.consoleSocket.close();
        } catch (IOException e) {
            // We don't care
            // This object will soon be garbage collected anyway
        }
        this.connected = false;
    }
    
    public void sendMessageToConsole(STSdown downMessage) {
        if(this.connected) {
            try {
                downMessage.writeDelimitedTo(this.consoleOutput);
                this.consoleOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
                this.connected = false;
            }
        }
    }
    
    public List<STSup> receiveMessagesFromConsole() {
        List<STSup> upMessages = new ArrayList<STSup>();
        if(this.connected) {
            try {
                while(this.consoleInput.available() > 0) {
                    this.timeLastMessageReceived = System.currentTimeMillis();
                    STSup upMessage = STSup.parseDelimitedFrom(this.consoleInput);
                    // Messages of size 0 are keep alives
                    if(upMessage.getSerializedSize() == 0) {
                        sendMessageToConsole(pongMessage);
                    } else {
                        upMessages.add(upMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.connected = false;
            }
        }
        return upMessages;
    }
    
}
