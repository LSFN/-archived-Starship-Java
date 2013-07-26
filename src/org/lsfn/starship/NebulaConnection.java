package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

public class NebulaConnection extends Thread {

    private static final String defaultHost = "localhost";
    private static final Integer defaultPort = 39461;
    private static final Integer pollWait = 50;
    private static final long timeout = 5000;
    
    private Socket nebulaSocket;
    private BufferedInputStream nebulaInput;
    private BufferedOutputStream nebulaOutput;
    private ArrayList<STSdown> nebulaMessages;
    private long timeLastMessageSent;
    private long timeLastMessageReceived;
    
    private String host;
    private Integer port;
    
    public enum ConnectionStatus {
        CONNECTED,
        DISCONNECTED
    }
    private ConnectionStatus connectionStatus;
    
    public NebulaConnection() {
        super();
        this.nebulaSocket = null;
        this.nebulaInput = null;
        this.nebulaOutput = null;
        this.nebulaMessages = null;
        this.timeLastMessageSent = System.currentTimeMillis();
        this.timeLastMessageReceived = System.currentTimeMillis();
        this.host = null;
        this.port = null;
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
    }
    
    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
    
    public ConnectionStatus getConnectionStatus() {
        if(System.currentTimeMillis() >= this.timeLastMessageReceived + timeout) {
            return this.disconnect();
        } else {
            if(System.currentTimeMillis() >= this.timeLastMessageSent + timeout - 1000) {
                STSup.Builder stsUp = STSup.newBuilder();
                sendMessageToNebula(stsUp.build());
            }
        }
        return connectionStatus;
    }
    
    private void clearConnection() {
        this.nebulaSocket = null;
        this.nebulaInput = null;
        this.nebulaOutput = null;
        this.nebulaMessages = null;
    }
    
    /**
     * Closes the connection to the remote host.
     * Designed for cases where we don't care if closing the connection fails,
     * we are getting out of this connection one way or the other...
     * ...like a bad relationship.
     */
    private void closeConnection() {
        try {
            this.nebulaOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Connects to the given host on the given port.
     * Will not connect to another host whilst connected.
     * Will not change the connection status from previous disconnected status if connection fails.
     * @param host The host to connect to.
     * @param port The port to connect on.
     * @return The new connection status of the connection.
     */
    public ConnectionStatus connect(String host, Integer port) {
        if(this.connectionStatus != ConnectionStatus.CONNECTED) {
            this.host = host;
            this.port = port;
            try {
                this.nebulaSocket = new Socket(this.host, this.port);
                this.nebulaInput = new BufferedInputStream(nebulaSocket.getInputStream());
                this.nebulaOutput = new BufferedOutputStream(nebulaSocket.getOutputStream());
                this.nebulaMessages = new ArrayList<STSdown>();
                this.connectionStatus = ConnectionStatus.CONNECTED;
            } catch (IOException e) {
                e.printStackTrace();
                clearConnection();
            }
        }
        return this.connectionStatus;
    }
    
    public ConnectionStatus connect() {
        return this.connect(defaultHost, defaultPort);
    }
    
    /**
     * Disconnects from the remote host.
     * @return The new connection status of the connection.
     */
    public ConnectionStatus disconnect() {
        closeConnection();
        clearConnection();
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
        return this.connectionStatus;
    }
    
    /**
     * Sends a message to the Nebula that this class is connected to.
     * Won't sent a message when disconnected
     * @param upMessage The message to be sent.
     * @return The new connection status of the connection.
     */
    public ConnectionStatus sendMessageToNebula(STSup upMessage) {
        if(this.connectionStatus == ConnectionStatus.CONNECTED) {
            try {
                upMessage.writeDelimitedTo(this.nebulaOutput);
                this.nebulaOutput.flush();
                System.out.println("FF\n" + upMessage);
            } catch (IOException e) {
                e.printStackTrace();
                closeConnection();
                clearConnection();
                this.connectionStatus = ConnectionStatus.DISCONNECTED;
            }
        }
        return this.connectionStatus;
    }
    
    public synchronized List<STSdown> receiveMessagesFromNebula() {
        List<STSdown> result = new ArrayList<STSdown>(this.nebulaMessages);
        this.nebulaMessages.clear();
        return result;
    }

    private synchronized void addMessageToBuffer(STSdown downMessage) {
        this.nebulaMessages.add(downMessage);
    }
    
    @Override
    public void run() {
        while(this.connectionStatus == ConnectionStatus.CONNECTED) {
            try {
                if(this.nebulaInput.available() > 0) {
                    STSdown downMessage = STSdown.parseDelimitedFrom(this.nebulaInput);
                    addMessageToBuffer(downMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeConnection();
                clearConnection();
            }
            try {
                Thread.sleep(pollWait);
            } catch (InterruptedException e) {
                // An interrupt indicates that something has happened to the connection
                // This loop will now probably terminate.
            }
        }
    }
}
