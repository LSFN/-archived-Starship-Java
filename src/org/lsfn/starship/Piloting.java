package org.lsfn.starship;

import org.lsfn.starship.FF.FFup;
import org.lsfn.starship.STS.STSup;

public class Piloting {
    
    public static FFup.Piloting processPiloting(STSup.Piloting piloting) {
        FFup.Piloting.Builder ffUpPiloting = FFup.Piloting.newBuilder();
        if(piloting.hasTurnAnti()) {
            ffUpPiloting.setTurnAnti(piloting.getTurnAnti());
        }
        if(piloting.hasTurnClock()) {
            ffUpPiloting.setTurnClock(piloting.getTurnClock());
        }
        if(piloting.hasThrustLeft()) {
            ffUpPiloting.setThrustLeft(piloting.getThrustLeft());
        }
        if(piloting.hasThrustRight()) {
            ffUpPiloting.setThrustRight(piloting.getThrustRight());
        }
        if(piloting.hasThrustForward()) {
            ffUpPiloting.setThrustForward(piloting.getThrustForward());
        }
        if(piloting.hasThrustBackward()) {
            ffUpPiloting.setThrustBackward(piloting.getThrustBackward());
        }
        return ffUpPiloting.build();
    }
}
