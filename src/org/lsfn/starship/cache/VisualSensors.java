package org.lsfn.starship.cache;

import org.lsfn.common.STS.STSdown;

public class VisualSensors {
    
    private STSdown.VisualSensors visualSensors;
    
    public VisualSensors() {
        this.visualSensors = null;
    }
    
    public void processVisualSensors(STSdown.VisualSensors visualSensors) {
        this.visualSensors = visualSensors;
    }
    
}
