package org.lsfn.starship;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lsfn.starship.FF.FFdown;
import org.lsfn.starship.FF.FFup;
import org.lsfn.starship.NebulaConnection.ConnectionStatus;
import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

public class MessageHandler extends Thread {

    private static final int pollWait = 50;
    
    private ConsoleServer consoleServer;
    private NebulaConnection nebulaConnection;
    private Lobby lobby;
    private VisualSensors visualSensors;
    private String shipName;
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
         // Take input from Nebula, store it and pass it on.
            if(this.nebulaConnection.getConnectionStatus() == NebulaConnection.ConnectionStatus.CONNECTED) {
                List<FFdown> downMessages = nebulaConnection.receiveMessagesFromStarship();
                for(FFdown downMessage : downMessages) {
                    STSdown.Builder stsDown = STSdown.newBuilder();
                    if(downMessage.hasLobby()) {
                        stsDown.setLobby(lobby.processLobby(downMessage.getLobby()));
                    }
                    if(downMessage.hasVisualSensors()) {
                        stsDown.setVisualSensors(visualSensors.processVisualSensors(downMessage.getVisualSensors()));
                    }
                    this.consoleServer.sendMessageToAllConsoles(stsDown.build());
                }
            }
                
            // Take input from Consoles and pass it on.
            // Perform no processing on the input.
            Map<UUID, List<STSup>> upMessages = consoleServer.receiveMessagesFromConsoles();
            for(UUID id : upMessages.keySet()) {
                List<STSup> consoleUpMessages = upMessages.get(id);
                for(STSup upMessage : consoleUpMessages) {
                    if(upMessage.hasConnection()) {
                        // Make the connection
                    }
                    if(this.nebulaConnection.getConnectionStatus() == NebulaConnection.ConnectionStatus.CONNECTED) {
                        FFup.Builder ffUp = FFup.newBuilder();
                        if(upMessage.hasLobby()) {
                            ffUp.setLobby(Lobby.processLobby(upMessage.getLobby()));
                        }
                        if(upMessage.hasPiloting()) {
                            ffUp.setPiloting(Piloting.processPiloting(upMessage.getPiloting()));
                        }
                        this.nebulaConnection.sendMessageToStarship(ffUp.build());
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
}
