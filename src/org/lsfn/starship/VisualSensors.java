package org.lsfn.starship;

import java.util.ArrayList;
import java.util.List;

import org.lsfn.starship.FF.FFdown;
import org.lsfn.starship.STS.STSdown;
import org.lsfn.starship.STS.STSdown.VisualSensors.SpaceObject.Type;
import org.lsfn.starship.SpaceObject.SpaceObjectType;

public class VisualSensors {
    
    private List<SpaceObject> spaceObjects;
    
    public VisualSensors() {
        this.spaceObjects = new ArrayList<SpaceObject>();
    }
    
    public STSdown.VisualSensors processVisualSensors(FFdown.VisualSensors visualSensors) {
        STSdown.VisualSensors.Builder stsDownVis = STSdown.VisualSensors.newBuilder();
        this.spaceObjects.clear();
        for(FFdown.VisualSensors.SpaceObject spaceObject : visualSensors.getSpaceObjectsList()) {
            SpaceObject.SpaceObjectType type = SpaceObject.SpaceObjectType.ASTEROID;
            if(spaceObject.getType() == FFdown.VisualSensors.SpaceObject.Type.SHIP_VALUE) {
                type = SpaceObject.SpaceObjectType.SHIP;
            } else if(spaceObject.getType() == FFdown.VisualSensors.SpaceObject.Type.ASTEROID_VALUE) {
                type = SpaceObject.SpaceObjectType.ASTEROID;
            }
            Position p = new Position(spaceObject.getPosition().getX(), spaceObject.getPosition().getY());
            SpaceObject so = new SpaceObject(type, p, spaceObject.getOrientation());
            this.spaceObjects.add(so);
            
            STSdown.VisualSensors.SpaceObject.Builder stsDownVisObj = STSdown.VisualSensors.SpaceObject.newBuilder();
            stsDownVisObj.setOrientation(so.getOrientation());
            STSdown.VisualSensors.SpaceObject.Point.Builder stsDownVisObjPoint = STSdown.VisualSensors.SpaceObject.Point.newBuilder();
            stsDownVisObjPoint.setX(so.getPosition().getX());
            stsDownVisObjPoint.setY(so.getPosition().getY());
            stsDownVisObj.setPosition(stsDownVisObjPoint);
            Type objType = STSdown.VisualSensors.SpaceObject.Type.ASTEROID;
            if(so.getType() == SpaceObjectType.SHIP) {
                objType = STSdown.VisualSensors.SpaceObject.Type.SHIP;
            } else if(so.getType() == SpaceObjectType.ASTEROID) {
                objType = STSdown.VisualSensors.SpaceObject.Type.ASTEROID;
            }
            stsDownVisObj.setType(objType);
            
            stsDownVis.addSpaceObjects(stsDownVisObj.build());
        }
        return stsDownVis.build();
    }
    
}
