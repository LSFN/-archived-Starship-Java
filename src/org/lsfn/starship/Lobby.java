package org.lsfn.starship;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lsfn.starship.FF.FFdown;
import org.lsfn.starship.FF.FFup;
import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSup;

public class Lobby {
    
    private String shipName;
    private List<String> shipNames;
    private boolean ready;
    private boolean gameStarted;
    
    public Lobby() {
        this.shipName = "";
        this.shipNames = new ArrayList<String>();
        this.ready = false;
        this.gameStarted = false;
    }
    
    public STSdown.Lobby processLobby(FFdown.Lobby lobby) {
        STSdown.Lobby.Builder stsDownLobby = STSdown.Lobby.newBuilder();
        if(lobby.hasReadyState()) {
            this.ready = lobby.getReadyState();
            stsDownLobby.setReadyState(this.ready);
        }
        if(lobby.hasShipName()) {
            this.shipName = lobby.getShipName();
            stsDownLobby.setShipName(this.shipName);
        }
        if(lobby.getShipsInGameCount() > 0) {
            this.shipNames = lobby.getShipsInGameList();
            stsDownLobby.addAllShipsInGame(this.shipNames);
        }
        if(lobby.hasGameStarted()) {
            this.gameStarted = lobby.getGameStarted();
            stsDownLobby.setGameStarted(this.gameStarted);
        }
        return stsDownLobby.build();
    }

    public static FFup.Lobby processLobby(STSup.Lobby lobby) {
        FFup.Lobby.Builder ffUpLobby = FFup.Lobby.newBuilder();
        if(lobby.hasReadyState()) {
            ffUpLobby.setReadyState(lobby.getReadyState());
        }
        if(lobby.hasShipName()) {
            ffUpLobby.setShipName(lobby.getShipName());
        }
        return ffUpLobby.build();
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
