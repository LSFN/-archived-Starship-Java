package org.lsfn.starship;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lsfn.starship.NebulaConnection.ConnectionStatus;
import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

public class MessageHandler extends Thread {

    private static final int pollWait = 50;
    
    private ConsoleServer consoleServer;
    private NebulaConnection nebulaConnection;
    private Lobby lobby;
    private VisualSensors visualSensors;
    private boolean running;
    
    public MessageHandler(ConsoleServer consoleServer, NebulaConnection nebulaConnection) {
        this.consoleServer = consoleServer;
        this.nebulaConnection = nebulaConnection;
        this.lobby = new Lobby();
        this.visualSensors = new VisualSensors();
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
                stsDownConnection.setConnected(this.nebulaConnection.getConnectionStatus() == NebulaConnection.ConnectionStatus.CONNECTED);
                stsDown.setConnection(stsDownConnection);
                stsDown.setLobby(this.lobby.makeConsoleLobbyInfo(id));
                this.consoleServer.sendMessageToConsole(id, stsDown.build());
            }
            
            // Take input from Nebula, store it and pass it on.
            if(this.nebulaConnection.getConnectionStatus() == NebulaConnection.ConnectionStatus.CONNECTED) {
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
                                && this.nebulaConnection.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
                            ConnectionStatus status = ConnectionStatus.DISCONNECTED;
                            if(connection.hasHost() && connection.hasPort()) {
                                status = this.nebulaConnection.connect(connection.getHost(), connection.getPort());
                            } else {
                                status = this.nebulaConnection.connect();
                            }
                            if(status == ConnectionStatus.CONNECTED) {
                                this.nebulaConnection.start();
                                System.out.println("Connected.");
                                sendConnectedMessages();
                            } else {
                                System.out.println("Connection failed.");
                            }
                        }
                    }
                    if(this.nebulaConnection.getConnectionStatus() == NebulaConnection.ConnectionStatus.CONNECTED) {
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

    private void sendConnectedMessages() {
        STSdown.Builder stsDown = STSdown.newBuilder();
        STSdown.Connection.Builder stsDownConnection = STSdown.Connection.newBuilder();
        stsDownConnection.setConnected(true);
        stsDown.setConnection(stsDownConnection);
        System.out.println("Sending connected message.");
        this.consoleServer.sendMessageToAllConsoles(stsDown.build());
    }
}
