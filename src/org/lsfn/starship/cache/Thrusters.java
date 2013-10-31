package org.lsfn.starship.cache;

import org.lsfn.common.STS.STSup;

public class Thrusters {

	private double forwardLeft, forwardRight, rearLeft, rearRight;
	
	public Thrusters() {
		forwardLeft = forwardRight = rearLeft = rearRight = 0.0;
	}

	public double getForwardLeft() {
		return forwardLeft;
	}

	public double getForwardRight() {
		return forwardRight;
	}

	public double getRearLeft() {
		return rearLeft;
	}

	public double getRearRight() {
		return rearRight;
	}
	
	public void processThrusters(STSup.Thrusters thrusters) {
		if(thrusters.hasForwardLeft()) this.forwardLeft = thrusters.getForwardLeft();
		if(thrusters.hasForwardRight()) this.forwardRight = thrusters.getForwardRight();
		if(thrusters.hasRearLeft()) this.rearLeft = thrusters.getRearLeft();
		if(thrusters.hasRearRight()) this.rearRight = thrusters.getRearRight();
	}
}
