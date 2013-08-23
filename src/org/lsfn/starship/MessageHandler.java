package org.lsfn.starship;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

public class MessageHandler extends Thread {

    private static final String defaultNebulaHost = "localhost";
    private static final Integer defaultNebulaPort = 39461;
    private static final Integer defaultConsolePort = 39460;
    private static final int tickInterval = 50;
    
    private ConsoleServer consoleServer;
    private NebulaConnection nebulaConnection;
    private Lobby lobby;
    private VisualSensors visualSensors;
    private boolean running;
    
    public MessageHandler() {
        this.consoleServer = new ConsoleServer();
        this.nebulaConnection = new NebulaConnection();
        this.lobby = new Lobby();
        this.visualSensors = new VisualSensors();
        this.running = false;
    }
    
    @Override
    public void run() {
        // TODO combine listen and start
        this.consoleServer.listen(defaultConsolePort);
        this.consoleServer.start();
        
        this.running = true;
        while(this.running) {
            // The only reason we need to handle connections (in A0.2) is so that
            // the client can be filled in about the Starship's status 
            for(UUID id : this.consoleServer.getConnectedConsoles()) {
                STSdown.Builder stsDown = STSdown.newBuilder();
                STSdown.Connection.Builder stsDownConnection = STSdown.Connection.newBuilder();
                stsDownConnection.setConnected(this.nebulaConnection.isJoined());
                stsDown.setConnection(stsDownConnection);
                stsDown.setLobby(this.lobby.makeConsoleLobbyInfo(id));
                this.consoleServer.sendMessageToConsole(id, stsDown.build());
            }
            
            // Take input from Nebula, store it and pass it on.
            if(this.nebulaConnection.isJoined()) {
                List<STSdown> downMessages = nebulaConnection.receiveMessagesFromNebula();
                for(STSdown downMessage : downMessages) {
                    if(downMessage.hasLobby()) {
                        lobby.processLobby(downMessage.getLobby());
                    }
                    if(downMessage.hasVisualSensors()) {
                        visualSensors.processVisualSensors(downMessage.getVisualSensors());
                    }
                    this.consoleServer.sendMessageToAllConsoles(downMessage);
                }
            }
                
            // Take input from Consoles and pass it on.
            // Perform no processing on the input.
            Map<UUID, List<STSup>> upMessages = consoleServer.receiveMessagesFromConsoles();
            for(UUID id : upMessages.keySet()) {
                List<STSup> consoleUpMessages = upMessages.get(id);
                for(STSup upMessage : consoleUpMessages) {
                    if(upMessage.hasConnection()) {
                        STSup.Connection connection = upMessage.getConnection();
                        if(connection.getConnectionCommand() == STSup.Connection.ConnectionCommand.CONNECT
                                && !this.nebulaConnection.isJoined()) {
                            boolean joined = false;
                            if(connection.hasHost() && connection.hasPort()) {
                                joined = this.nebulaConnection.join(connection.getHost(), connection.getPort());
                            } else {
                                joined = this.nebulaConnection.join(defaultNebulaHost, defaultNebulaPort);
                            }
                            if(joined) {
                                System.out.println("Joined Nebula succeessfully.");
                                sendConnectedMessages();
                            } else {
                                System.out.println("Failed to join Nebula.");
                            }
                        } else if(connection.getConnectionCommand() == STSup.Connection.ConnectionCommand.DISCONNECT
                                && this.nebulaConnection.isJoined()) {
                            this.nebulaConnection.disconnect();
                            sendDisonnectedMessages();
                        }
                    }
                    if(this.nebulaConnection.isJoined()) {
                        this.nebulaConnection.sendMessageToNebula(upMessage);
                    }
                }
            }
            
            try {
                Thread.sleep(tickInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendConnectedMessages() {
        STSdown.Builder stsDown = STSdown.newBuilder();
        STSdown.Connection.Builder stsDownConnection = STSdown.Connection.newBuilder();
        stsDownConnection.setConnected(true);
        stsDown.setConnection(stsDownConnection);
        System.out.println("Sending connected message.");
        this.consoleServer.sendMessageToAllConsoles(stsDown.build());
    }
    
    private void sendDisonnectedMessages() {
        STSdown.Builder stsDown = STSdown.newBuilder();
        STSdown.Connection.Builder stsDownConnection = STSdown.Connection.newBuilder();
        stsDownConnection.setConnected(false);
        stsDown.setConnection(stsDownConnection);
        System.out.println("Sending disconnected message.");
        this.consoleServer.sendMessageToAllConsoles(stsDown.build());
    }
}
