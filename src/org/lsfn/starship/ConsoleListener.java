package org.lsfn.starship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.lsfn.starship.STS.*;

/**
 * Listens to a single socket.
 * Designed to be discarded when disconnected.
 * @author Lukeus_Maximus
 *
 */
public class ConsoleListener {

    private Socket consoleSocket;
    private BufferedInputStream consoleInput;
    private BufferedOutputStream consoleOutput;
    
    public enum ListenerStatus {
        NOT_SETUP,
        CONNECTED,
        DISCONNECTED
    }
    private ListenerStatus listenerStatus;
    
    public ConsoleListener(Socket consoleSocket) {
        this.consoleSocket = consoleSocket;
        this.consoleInput = null;
        this.consoleOutput = null;
        this.listenerStatus = ListenerStatus.NOT_SETUP;
    }
    
    public ListenerStatus getListenerStatus() {
        return this.listenerStatus;
    }
    
    private boolean setupStreams() {
        try {
            this.consoleInput = new BufferedInputStream(this.consoleSocket.getInputStream());
            this.consoleOutput = new BufferedOutputStream(this.consoleSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public ListenerStatus disconnect() {
        try {
            this.consoleSocket.close();
        } catch (IOException e) {
            // We simply don't care if this fails.
            // This assumes nothing bad comes of a close() failing.
            e.printStackTrace();
        }
        this.listenerStatus = ListenerStatus.DISCONNECTED;
        return this.listenerStatus;
    }
    
    public ListenerStatus sendMessageToConsole(STSdown downMessage) {
        if(this.listenerStatus == ListenerStatus.NOT_SETUP) {
            if(!setupStreams()) {
                this.listenerStatus = ListenerStatus.DISCONNECTED;
            }
        }
        if(this.listenerStatus == ListenerStatus.CONNECTED) {
            try {
                downMessage.writeDelimitedTo(this.consoleOutput);
                this.consoleOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
                this.listenerStatus = ListenerStatus.DISCONNECTED;
            }
        }
        return this.listenerStatus;
    }
    
    public List<STSup> receiveMessagesFromConsole() {
        List<STSup> upMessages = new ArrayList<STSup>();
        if(this.listenerStatus == ListenerStatus.NOT_SETUP) {
            if(setupStreams()) {
                this.listenerStatus = ListenerStatus.CONNECTED;
            } else {
                this.listenerStatus = ListenerStatus.DISCONNECTED;
            }
        }
        if(this.listenerStatus == ListenerStatus.CONNECTED) {
            try {
                while(this.consoleInput.available() > 0) {
                    STSup upMessage = STSup.parseDelimitedFrom(this.consoleInput);
                    upMessages.add(upMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.listenerStatus = ListenerStatus.DISCONNECTED;
            }
        }
        return upMessages;
    }
}
