package org.lsfn.starship;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.lsfn.starship.ConsoleListener.ListenerStatus;
import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

/**
 * Creates a listener for each connection.
 * Each connection assigned unique ID.
 * ID can be used to receive messages from and to send to a specific console.
 * @author Lukeus_Maximus
 *
 */
public class ConsoleServer extends Thread {
    
    private static final Integer defaultPort = 39460;
    private static final Integer pollWait = 50;
    
    private ServerSocket consoleServer;
    private Map<UUID, ConsoleListener> listeners;
    private Map<UUID, List<STSup>> buffers;
    private List<UUID> connectedConsoles;
    private List<UUID> disconnectedConsoles;
    
    public enum ServerStatus {
        CLOSED,
        OPEN
    }
    private ServerStatus serverStatus;
    
    public ConsoleServer() {
        clearServer();
        serverStatus = ServerStatus.CLOSED;
    }
    
    public void clearServer() {
        consoleServer = null;
        listeners = null;
        buffers = null;
        connectedConsoles = null;
        disconnectedConsoles = null;
    }
    
    public ServerStatus getListenStatus() {
        return serverStatus;
    }
    
    public ServerStatus listen() {
        if(this.serverStatus != ServerStatus.OPEN) {
            try {
                this.consoleServer = new ServerSocket(defaultPort);
                this.consoleServer.setSoTimeout(pollWait);
                this.listeners = new HashMap<UUID, ConsoleListener>();
                this.connectedConsoles = new ArrayList<UUID>();
                this.disconnectedConsoles = new ArrayList<UUID>();
            } catch (IOException e) {
                e.printStackTrace();
                clearServer();
            }
        }
        return this.serverStatus;
    }
    
    /**
     * Returns a list of the consoles that have connected since last polled.
     * @return List of consoles that have connected.
     */
    public synchronized List<UUID> getConnectedConsoles() {
        List<UUID> result = new ArrayList<UUID>(this.connectedConsoles);
        this.connectedConsoles.clear();
        return result;
    }
    
    private synchronized void addConnectedConsole(UUID id) {
        this.connectedConsoles.add(id);
    }
    
    /**
     * Returns a list of the consoles that have disconnected since last polled.
     * @return List of consoles that have disconnected.
     */
    public synchronized List<UUID> getDisconnectedConsoles() {
        List<UUID> result = new ArrayList<UUID>(this.disconnectedConsoles);
        this.disconnectedConsoles.clear();
        return result;
    }
    
    private synchronized void addDisconnectedConsole(UUID id) {
        this.disconnectedConsoles.add(id);
    }
    
    public synchronized Map<UUID, List<STSup>> receiveMessagesFromConsoles() {
        Map<UUID, List<STSup>> result = new HashMap<UUID, List<STSup>>();
        for(UUID id : this.buffers.keySet()) {
            List<STSup> buffer = this.buffers.get(id);
            result.put(id, new ArrayList<STSup>(buffer));
        }
        return result;
    }
    
    private synchronized void addMessagesToBuffer(UUID id, List<STSup> upMessages) {
        if(!this.buffers.containsKey(id)) {
            this.buffers.put(id, new ArrayList<STSup>());
        }
        this.buffers.get(id).addAll(upMessages);
    }
    
    public ServerStatus shutDown() {
        try {
            this.consoleServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(UUID id : getListenerIDs()) {
            getListener(id).disconnect();
        }
        clearServer();
        this.serverStatus = ServerStatus.CLOSED;
        return this.serverStatus;
    }
    
    public void disconnectConsole(UUID id) {
        if(this.listeners.containsKey(id)) {
            this.listeners.get(id).disconnect();
        }
    }
    
    public void sendMessageToConsole(UUID id, STSdown downMessage) {
        ConsoleListener listener = getListener(id);
        if(listener != null) {
            listener.sendMessageToConsole(downMessage);
        }
    }
    
    private synchronized void addListener(UUID id, ConsoleListener listener) {
        this.listeners.put(id, listener);
    }
    
    private synchronized void removeListener(UUID id) {
        this.listeners.remove(id);
    }
    
    private synchronized ConsoleListener getListener(UUID id) {
        return this.listeners.get(id);
    }
    
    private synchronized Set<UUID> getListenerIDs() {
        return new HashSet<UUID>(this.listeners.keySet());
    }
    
    @Override
    public void run() {
        while(this.serverStatus == ServerStatus.OPEN) {
            for(UUID id : getListenerIDs()) {
                ConsoleListener listener = this.listeners.get(id);
                List<STSup> upMessages = listener.receiveMessagesFromConsole();
                addMessagesToBuffer(id, upMessages);
                if(listener.getListenerStatus() == ListenerStatus.DISCONNECTED) {
                    removeListener(id);
                    addDisconnectedConsole(id);
                }
            }
            try {
                UUID id = UUID.randomUUID();
                addListener(id, new ConsoleListener(this.consoleServer.accept()));
                addConnectedConsole(id);
            } catch (SocketTimeoutException e) {
                // Timeouts are normal, do nothing
            } catch (IOException e) {
                // Shutdown if anything else goes wrong
                e.printStackTrace();
                shutDown();
            }
        }
    }
}
