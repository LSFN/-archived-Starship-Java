package org.lsfn.starship;

import java.util.UUID;

import org.lsfn.starship.NebulaConnection.ConnectionStatus;

public class NebulaManager {

    private NebulaConnection nebulaConnection;
    private UUID rejoinToken;
    
    public NebulaManager() {
        this.nebulaConnection = new NebulaConnection();
        this.rejoinToken = null;
    }
    
    public void attemptToJoin() {
        if(this.nebulaConnection.connect() == ConnectionStatus.CONNECTED) {
            System.out.println("Connected to Nebula.");
            
        } else {
            System.out.println("Failed to join Nebula.");
        }
    }
}
