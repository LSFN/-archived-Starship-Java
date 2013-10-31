package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lsfn.common.STS.STSdown;
import org.lsfn.common.STS.STSdown.JoinResponse.Type;
import org.lsfn.common.STS.STSup;

public class NebulaConnection implements Runnable {

    private static final Integer tickInterval = 50;
    private static final long timeout = 5000;
    private static final long timeBetweenPings = 3000;
    private static final STSup pingMessage = STSup.newBuilder().build();
    
    private Thread nebulaConnectionThread;
    private Socket nebulaSocket;
    private BufferedInputStream nebulaInput;
    private BufferedOutputStream nebulaOutput;
    private List<STSdown> nebulaMessages;
    private long timeLastMessageReceived;
    private long timeLastMessageSent;
    private boolean joined;
    private UUID rejoinToken;
    private UUID gameToken;
    
    public NebulaConnection() {
        this.nebulaConnectionThread = null;
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
        
        // Receive the joining info
        if(waitForMessageOrTimeout()) {
        	STSdown downMessage;
			try {
				downMessage = STSdown.parseDelimitedFrom(this.nebulaInput);
			} catch (IOException e) {
				return this.joined;
			}
        	if(downMessage.hasJoinInfo()) {
        		if(downMessage.getJoinInfo().getAllowJoin()) {
        			UUID tokenReceived = UUID.fromString(downMessage.getJoinInfo().getGameIDtoken());
        			if(this.gameToken == null) {
        				this.gameToken = tokenReceived;
        				this.rejoinToken = null;
        			} else if(!this.gameToken.equals(tokenReceived)) {
    					this.rejoinToken = null;
        			}
        		} else {
        			return this.joined;
        		}
        	} else {
            	return this.joined;
            }
        } else {
        	return this.joined;
        }
        
        // Send a joining message
        STSup.Builder stsUp = STSup.newBuilder();
        STSup.JoinRequest.Builder stsUpJoinRequest = STSup.JoinRequest.newBuilder();
        if(rejoinToken == null) {
        	stsUpJoinRequest.setType(STSup.JoinRequest.JoinType.JOIN);
        } else {
        	stsUpJoinRequest.setType(STSup.JoinRequest.JoinType.REJOIN);
        	stsUpJoinRequest.setRejoinToken(this.rejoinToken.toString());
        }
        stsUp.setJoinRequest(stsUpJoinRequest);
        STSup upMessage = stsUp.build();
        try {
            upMessage.writeDelimitedTo(this.nebulaOutput);
            this.nebulaOutput.flush();
            System.out.println("UP\n" + upMessage);
            this.timeLastMessageSent = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            return this.joined;
        }
        
        // We wait for a response to the join request and return appropriately
        if(waitForMessageOrTimeout()) {
	        STSdown downMessage;
			try {
				downMessage = STSdown.parseDelimitedFrom(this.nebulaInput);
			} catch (IOException e) {
				return this.joined;
			}
	        if(downMessage.hasJoinResponse()) {
	            STSdown.JoinResponse joinResponse = downMessage.getJoinResponse();
	            if(joinResponse.getType() == Type.JOIN_ACCEPTED) {
	                this.joined = true;
	                this.rejoinToken = UUID.fromString(joinResponse.getRejoinToken());
	            } else if(joinResponse.getType() == Type.REJOIN_ACCEPTED) {
	                this.joined = true;
	            } else if(joinResponse.getType() == Type.JOIN_REJECTED) {
	            	// Do nothing, this.joined is already false;
	            }
	        } else {
	        	return this.joined;
	        }
        } else {
        	return this.joined;
        }
        
        if(this.joined) {
            this.nebulaConnectionThread = new Thread(this);
            this.nebulaConnectionThread.start();
        }
        
        return this.joined;
    }

	private boolean waitForMessageOrTimeout() {
		long timeInitiated = System.currentTimeMillis();
        for(;;) {
            try {
                if(this.nebulaInput.available() > 0) {
                    this.timeLastMessageReceived = System.currentTimeMillis();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                Thread.sleep(tickInterval);
            } catch (InterruptedException e) {
                return false;
            }
            if(System.currentTimeMillis() >= timeInitiated + timeout) {
            	return false;
            }
        }
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
        try {
            this.nebulaConnectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessageToNebula(STSup upMessage) {
        if(this.joined) {
            try {
                upMessage.writeDelimitedTo(this.nebulaOutput);
                this.nebulaOutput.flush();
                this.timeLastMessageSent = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
                disconnect();
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
                disconnect();
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
