package org.lsfn.starship.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lsfn.common.STS.STSdown;

public class Lobby {
    
    private String shipName;
    private List<String> shipNames;
    private boolean ready;
    private boolean gameStarted;
    
    public Lobby() {
        this.shipName = "Mungle Box";
        this.shipNames = new ArrayList<String>();
        this.ready = false;
        this.gameStarted = false;
    }
    
    public void processLobby(STSdown.Lobby lobby) {
        if(lobby.hasReadyState()) {
            this.ready = lobby.getReadyState();
        }
        if(lobby.hasShipName()) {
            this.shipName = lobby.getShipName();
        }
        if(lobby.getShipsInGameCount() > 0) {
            this.shipNames = lobby.getShipsInGameList();
        }
        if(lobby.hasGameStarted()) {
            this.gameStarted = lobby.getGameStarted();
        }
    }
    
    public String getShipName() {
        return shipName;
    }

    public List<String> getShipNames() {
        return shipNames;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public STSdown.Lobby makeConsoleLobbyInfo(UUID id) {
        STSdown.Lobby.Builder stsDownLobby = STSdown.Lobby.newBuilder();
        stsDownLobby.setGameStarted(this.gameStarted)
                .setReadyState(this.ready)
                .setShipName(this.shipName)
                .addAllShipsInGame(this.shipNames);
        return stsDownLobby.build();
    }

}
