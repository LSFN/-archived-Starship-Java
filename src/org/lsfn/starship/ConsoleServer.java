package org.lsfn.starship;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lsfn.starship.STS.STSup.Join.JoinType;
import org.lsfn.starship.STS.*;

/**
 * This class acts as a server for the Console clients to the Nebula.
 * It runs as a separate thread that listens for new connections
 * and processes received messages. It presents different clients
 * through its methods and *not* different connections. Rejoining clients
 * will show up under the same ID.
 * @author Lukeus_Maximus
 *
 */
public class ConsoleServer extends Thread {
    // TODO throw a whole bunch of synchronize keywords at this. 
    
    private static final Integer tickInterval = 50;
    
    private ServerSocket consoleServer;
    private Map<UUID, ConsoleListener> clients;
    private Map<UUID, List<STSup>> buffers;
    private List<UUID> connectedConsoles;
    private List<UUID> disconnectedConsoles;
    private boolean open;
    
    public ConsoleServer() {
        this.consoleServer = null;
        this.clients = new HashMap<UUID, ConsoleListener>();
        this.buffers = new HashMap<UUID, List<STSup>>();
        this.connectedConsoles = new ArrayList<UUID>();
        this.disconnectedConsoles = new ArrayList<UUID>();
        this.open = false;
    }
    
    public boolean listen(Integer port) {
        try {
            this.consoleServer = new ServerSocket(port);
            this.consoleServer.setSoTimeout(tickInterval);
            this.open = true;
            System.out.println("Listening on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
            this.open = false;
        }
        return this.open;
    }
    
    public List<UUID> getConnectedConsoles() {
        List<UUID> result = this.connectedConsoles;
        this.connectedConsoles = new ArrayList<UUID>();
        return result;
    }
    
    public List<UUID> getDisconnectedConsoles() {
        List<UUID> result = this.disconnectedConsoles;
        this.disconnectedConsoles = new ArrayList<UUID>();
        return result;
    }
    
    public boolean isOpen() {
        return this.open;
    }
    
    public void sendMessageToConsole(UUID id, STSdown downMessage) {
        ConsoleListener listener = this.clients.get(id);
        if(listener != null) {
            listener.sendMessageToConsole(downMessage);
        }
    }
    
    public void sendMessageToAllConsoles(STSdown downMessage) {
        for(UUID clientID : this.clients.keySet()) {
            sendMessageToConsole(clientID, downMessage);
        }
    }
    
    public synchronized Map<UUID, List<STSup>> receiveMessagesFromConsoles() {
        Map<UUID, List<STSup>> result = buffers;
        buffers = new HashMap<UUID, List<STSup>>();
        return result;
    }
    
    public synchronized void addMessageToBuffers(UUID clientID, STSup message) {
        if(!buffers.containsKey(clientID)) {
            buffers.put(clientID, new ArrayList<STSup>());
        }
        buffers.get(clientID).add(message);
    }
    
    
    // TODO sort synchronisation issues
    @Override
    public void run() {
        while(this.open) {
            // Process messages from listeners.
            Iterator<UUID> clientIterator = this.clients.keySet().iterator();
            while(clientIterator.hasNext()) {
                UUID clientID = clientIterator.next();
                ConsoleListener listener = this.clients.get(clientID);
                if(listener.isConnected()) {
                    List<STSup> messages = listener.receiveMessagesFromConsole();
                    for(STSup message : messages) {
                        this.addMessageToBuffers(clientID, message);
                    }
                } else {
                    // The client has disconnected
                    clientIterator.remove();
                    this.disconnectedConsoles.add(clientID);              
                }
            }
            
            // Accept new connections
            Socket consoleSocket = null;
            try {
                consoleSocket = this.consoleServer.accept();
            } catch (SocketTimeoutException e) {
                // Timeouts are normal, do nothing
            } catch (SocketException e) {
                // If the server is closed, it closed because we asked it to.
                if(this.open) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // Shutdown if anything else goes wrong
                this.open = false;
            }
            
            if(consoleSocket != null) {
                try {
                    ConsoleListener listener = new ConsoleListener(consoleSocket);
                    UUID newID = UUID.randomUUID();
                    this.clients.put(newID, listener);
                    this.connectedConsoles.add(newID);
                } catch (IOException e) {
                    // Listener creation failed
                    // Nothing to do here
                }
            }
        }
    }
}
