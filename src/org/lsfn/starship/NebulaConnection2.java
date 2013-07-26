package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

public class NebulaConnection2 extends Thread {

    private static final Integer tickInterval = 50;
    private static final long timeout = 5000;
    private static final long timeBetweenPings = 3000;
    
    private Socket nebulaSocket;
    private BufferedInputStream nebulaInput;
    private BufferedOutputStream nebulaOutput;
    private List<STSdown> nebulaMessages;
    private long timeLastMessageReceived;
    private boolean connected;
    private STSup pingRequest;
    private UUID rejoinToken;
    
    public NebulaConnection2() {
        this.nebulaSocket = null;
        this.nebulaInput = null;
        this.nebulaOutput = null;
        this.nebulaMessages = new ArrayList<STSdown>();
        this.timeLastMessageReceived = System.currentTimeMillis();
        this.connected = false;
        this.pingRequest = STSup.newBuilder().setPing(STSup.Ping.newBuilder()).build();
        this.rejoinToken = null;
    }
    
    public boolean connect(String host, Integer port) {
        try {
            this.nebulaSocket = new Socket(host, port);
            this.nebulaInput = new BufferedInputStream(nebulaSocket.getInputStream());
            this.nebulaOutput = new BufferedOutputStream(nebulaSocket.getOutputStream());
            this.connected = true;
        } catch(IOException e) {
            this.connected = false;
        }
        return this.connected;
    }
    
    public boolean isConnected() {
        if(System.currentTimeMillis() >= this.timeLastMessageReceived + timeout) {
            this.disconnect();
        }
        return this.connected;
    }
    
    public void disconnect() {
        try {
            this.nebulaSocket.close();
        } catch (IOException e) {
            // We don't care
            e.printStackTrace();
        }
        this.connected = false;
    }
    
    public void sendMessageToNebula(STSup upMessage) {
        if(this.connected) {
            try {
                upMessage.writeDelimitedTo(this.nebulaOutput);
                this.nebulaOutput.flush();
                System.out.println("FF\n" + upMessage);
            } catch (IOException e) {
                e.printStackTrace();
                this.connected = false;
            }
        }
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
        while(this.connected) {
            try {
                if(this.nebulaInput.available() > 0) {
                    STSdown downMessage = STSdown.parseDelimitedFrom(this.nebulaInput);
                    addMessageToBuffer(downMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.connected = false;
            }
            
            if(System.currentTimeMillis() >= this.timeLastMessageReceived + timeBetweenPings) {
                sendMessageToNebula(pingRequest);
            }
            
            try {
                Thread.sleep(tickInterval);
            } catch (InterruptedException e) {
                // An interrupt indicates that something has happened to the connection
                // This loop will now probably terminate.
            }
        }
    }
    
}
