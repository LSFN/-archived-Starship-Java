package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSdown.Join.Response;
import org.lsfn.starship.STS.STSup;

public class NebulaConnection extends Thread {

    private static final Integer tickInterval = 50;
    private static final long timeout = 5000;
    private static final long timeBetweenPings = 3000;
    private static final STSup pingMessage = STSup.newBuilder().build();
    
    private Socket nebulaSocket;
    private BufferedInputStream nebulaInput;
    private BufferedOutputStream nebulaOutput;
    private List<STSdown> nebulaMessages;
    private long timeLastMessageReceived;
    private long timeLastMessageSent;
    private boolean joined;
    private UUID rejoinToken;
    
    public NebulaConnection() {
        this.nebulaSocket = null;
        this.nebulaInput = null;
        this.nebulaOutput = null;
        this.nebulaMessages = new ArrayList<STSdown>();
        this.timeLastMessageSent = 0;
        this.timeLastMessageReceived = 0;
        this.joined = false;
        this.rejoinToken = null;
    }
    
    public boolean join(String host, Integer port) {
        this.joined = false;
        try {
            this.nebulaSocket = new Socket(host, port);
            this.nebulaInput = new BufferedInputStream(nebulaSocket.getInputStream());
            this.nebulaOutput = new BufferedOutputStream(nebulaSocket.getOutputStream());
            // If no exception occurs, joining can proceed.
        } catch(IOException e) {
            e.printStackTrace();
            return this.joined;
        }
        
        // Send a joining message
        STSup.Builder stsUp = STSup.newBuilder();
        STSup.Join.Builder stsUpJoin = STSup.Join.newBuilder();
        if(rejoinToken == null) {
            stsUpJoin.setType(STSup.Join.JoinType.JOIN);
        } else {
            stsUpJoin.setType(STSup.Join.JoinType.REJOIN);
            stsUpJoin.setRejoinToken(this.rejoinToken.toString());
        }
        stsUp.setJoin(stsUpJoin);
        STSup upMessage = stsUp.build();
        try {
            upMessage.writeDelimitedTo(this.nebulaOutput);
            this.nebulaOutput.flush();
            System.out.println("FF\n" + upMessage);
            this.timeLastMessageSent = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            return this.joined;
        }
        
        // We wait for a response to the join request and return appropriately
        long timeInitiated = System.currentTimeMillis();
        boolean waiting = true;
        while(waiting) {
            try {
                if(this.nebulaInput.available() > 0) {
                    this.timeLastMessageReceived = System.currentTimeMillis();
                    STSdown downMessage = STSdown.parseDelimitedFrom(this.nebulaInput);
                    if(downMessage.hasJoin()) {
                        STSdown.Join join = downMessage.getJoin();
                        if(join.getResponse() == Response.JOIN_ACCEPTED) {
                            waiting = false;
                            this.joined = true;
                            this.rejoinToken = UUID.fromString(join.getRejoinToken());
                        } else if(join.getResponse() == Response.REJOIN_ACCEPTED) {
                            waiting = false;
                            this.joined = true;
                        } else if(join.getResponse() == Response.JOIN_REJECTED) {
                            waiting = false;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                waiting = false;
            }
            try {
                Thread.sleep(tickInterval);
            } catch (InterruptedException e) {
                waiting = false;
            }
            if(System.currentTimeMillis() >= timeInitiated + timeout) {
                waiting = false;
            }
        }
        
        return this.joined;
    }
    
    public boolean isJoined() {
        if(this.joined && System.currentTimeMillis() >= this.timeLastMessageReceived + timeout) {
            this.disconnect();
        }
        return this.joined;
    }
    
    public void disconnect() {
        try {
            this.nebulaSocket.close();
        } catch (IOException e) {
            // We don't care
            e.printStackTrace();
        }
        this.joined = false;
    }
    
    public void sendMessageToNebula(STSup upMessage) {
        if(this.joined) {
            try {
                upMessage.writeDelimitedTo(this.nebulaOutput);
                this.nebulaOutput.flush();
                this.timeLastMessageSent = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
                this.joined = false;
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
        while(this.joined) {
            try {
                if(this.nebulaInput.available() > 0) {
                    this.timeLastMessageReceived = System.currentTimeMillis();
                    STSdown downMessage = STSdown.parseDelimitedFrom(this.nebulaInput);
                    // Messages of size 0 are keep alives
                    if(downMessage.getSerializedSize() > 0) {
                        addMessageToBuffer(downMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.joined = false;
            }
            
            if(System.currentTimeMillis() >= this.timeLastMessageSent + timeBetweenPings) {
                sendMessageToNebula(pingMessage);
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
