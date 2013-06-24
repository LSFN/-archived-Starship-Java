package org.lsfn.starship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lsfn.starship.STS.STSup;

public class Lobby extends Thread {
    
    private static final Integer pollWait = 50; 
    
    private ConsoleServer consoleServer;
    private Map<UUID, ConsoleInfo> consoles;
    
    public Lobby(ConsoleServer consoleServer) {
        this.consoleServer = consoleServer;
        this.consoles = new HashMap<UUID, ConsoleInfo>();
    }
        
    @Override
    public void run() {
        while(true) {
            // There is no need to remember consoles between connections. 
            for(UUID id : consoleServer.getDisconnectedConsoles()) {
                consoles.remove(id);
            }
            for(UUID id : consoleServer.getConnectedConsoles()) {
                if(!consoles.containsKey(id)) {
                    consoles.put(id, new ConsoleInfo(id));
                }
            }
            Map<UUID, List<STSup>> messages = consoleServer.receiveMessagesFromConsoles();
            for(UUID id : messages.keySet()) {
                for(STSup upMessage : messages.get(id)) {
                    
                }
            }
            
            try {
                Thread.sleep(pollWait);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
