package org.lsfn.starship;

public class SpaceObject {

    private Position position;
    private double orientation;
    
    public enum SpaceObjectType {
        SHIP,
        ASTEROID
    }
    private SpaceObjectType type;
    
    public SpaceObject(SpaceObjectType type, Position position, double orientation) {
        this.type = type;
        this.position = position;
        this.orientation = orientation;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public SpaceObjectType getType() {
        return type;
    }

    public void setType(SpaceObjectType type) {
        this.type = type;
    }
    
    
}
