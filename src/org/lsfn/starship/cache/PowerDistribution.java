package org.lsfn.starship.cache;

import org.lsfn.common.STS.STSdown;
import org.lsfn.common.STS.STSup;

public class PowerDistribution {

	private boolean forwardLeftThruster, forwardRightThruster, rearLeftThruster, rearRightThruster, leftEngine, rightEngine;
	
	public PowerDistribution() {
		forwardLeftThruster = forwardRightThruster = rearLeftThruster = rearRightThruster = leftEngine = rightEngine = false;
	}

	public boolean isForwardLeftThruster() {
		return forwardLeftThruster;
	}

	public boolean isForwardRightThruster() {
		return forwardRightThruster;
	}

	public boolean isRearLeftThruster() {
		return rearLeftThruster;
	}

	public boolean isRearRightThruster() {
		return rearRightThruster;
	}

	public boolean isLeftEngine() {
		return leftEngine;
	}

	public boolean isRightEngine() {
		return rightEngine;
	}
	
	public void processPowerDistribution(STSup.PowerDistribution powerDistribution) {
		if(powerDistribution.hasForwardLeftThruster()) forwardLeftThruster = powerDistribution.getForwardLeftThruster();
		if(powerDistribution.hasForwardRightThruster()) forwardRightThruster = powerDistribution.getForwardRightThruster();
		if(powerDistribution.hasRearLeftThruster()) rearLeftThruster = powerDistribution.getRearLeftThruster();
		if(powerDistribution.hasRearRightThruster()) rearRightThruster = powerDistribution.getRearRightThruster();
		if(powerDistribution.hasLeftEngine()) leftEngine = powerDistribution.getLeftEngine();
		if(powerDistribution.hasRightEngine()) rightEngine = powerDistribution.getRightEngine();
	}
	
	public void processPowerDistribution(STSdown.PowerDistribution powerDistribution) {
		if(powerDistribution.hasForwardLeftThruster()) forwardLeftThruster = powerDistribution.getForwardLeftThruster();
		if(powerDistribution.hasForwardRightThruster()) forwardRightThruster = powerDistribution.getForwardRightThruster();
		if(powerDistribution.hasRearLeftThruster()) rearLeftThruster = powerDistribution.getRearLeftThruster();
		if(powerDistribution.hasRearRightThruster()) rearRightThruster = powerDistribution.getRearRightThruster();
		if(powerDistribution.hasLeftEngine()) leftEngine = powerDistribution.getLeftEngine();
		if(powerDistribution.hasRightEngine()) rightEngine = powerDistribution.getRightEngine();
	}
}
