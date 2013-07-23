package org.lsfn.starship;

import org.lsfn.starship.STS.STSdown;

public class VisualSensors {
    
    private STSdown.VisualSensors visualSensors;
    
    public VisualSensors() {
        this.visualSensors = null;
    }
    
    public void processVisualSensors(STSdown.VisualSensors visualSensors) {
        this.visualSensors = visualSensors;
    }
    
}
