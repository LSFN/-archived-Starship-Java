package org.lsfn.starship;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSdown.Join.Response;
import org.lsfn.starship.STS.STSup;

public class MessageHandler extends Thread {

    private static final String defaultNebulaHost = "localhost";
    private static final Integer defaultNebulaPort = 39461;
    private static final int pollWait = 50;
    
    private ConsoleServer consoleServer;
    private NebulaConnection nebulaConnection;
    private UUID rejoinToken;
    private Lobby lobby;
    private VisualSensors visualSensors;
    private boolean running;
    
    public MessageHandler(ConsoleServer consoleServer) {
        this.consoleServer = consoleServer;
        this.nebulaConnection = new NebulaConnection();
        this.rejoinToken = null;
        this.lobby = new Lobby();
        this.visualSensors = new VisualSensors();
        this.running = false;
    }
    
    @Override
    public void run() {
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
                    if(downMessage.hasJoin()) {
                        handleJoin(downMessage.getJoin());
                    }
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
                                this.nebulaConnection.start();
                                System.out.println("Joined Nebula succeessfully.");
                                sendJoinRequest();
                            } else {
                                System.out.println("Failed to join Nebula.");
                            }
                        }
                    }
                    if(this.nebulaConnection.isJoined()) {
                        this.nebulaConnection.sendMessageToNebula(upMessage);
                    }
                }
            }
            
            try {
                Thread.sleep(pollWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleJoin(STSdown.Join join) {
        if(join.getResponse() == Response.JOIN_ACCEPTED) {
            this.rejoinToken = UUID.fromString(join.getRejoinToken());
            sendConnectedMessages();
        } else if(join.getResponse() == Response.JOIN_REJECTED) {
            // No notifications for connection failure at the moment.
        } else if(join.getResponse() == Response.REJOIN_ACCEPTED) {
            sendConnectedMessages();
        }
    }

    private void sendJoinRequest() {
        STSup.Builder stsUp = STSup.newBuilder();
        STSup.Join.Builder stsUpJoin = STSup.Join.newBuilder();
        if(this.rejoinToken != null) {
            stsUpJoin.setType(STSup.Join.JoinType.REJOIN);
            stsUpJoin.setRejoinToken(this.rejoinToken.toString());
        } else {
            stsUpJoin.setType(STSup.Join.JoinType.JOIN);
        }
        stsUp.setJoin(stsUpJoin);
        this.nebulaConnection.sendMessageToNebula(stsUp.build());
    }
    
    private void sendConnectedMessages() {
        STSdown.Builder stsDown = STSdown.newBuilder();
        STSdown.Connection.Builder stsDownConnection = STSdown.Connection.newBuilder();
        stsDownConnection.setConnected(true);
        stsDown.setConnection(stsDownConnection);
        System.out.println("Sending connected message.");
        this.consoleServer.sendMessageToAllConsoles(stsDown.build());
    }
}
