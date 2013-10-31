package org.lsfn.starship.cache;

import org.lsfn.common.STS.STSup;

public class Engines {
	
	private double leftEngineThrottle, rightEngineThrottle;
	
	public Engines() {
		leftEngineThrottle = rightEngineThrottle = 0.0;
	}

	public double getLeftEngineThrottle() {
		return leftEngineThrottle;
	}

	public double getRightEngineThrottle() {
		return rightEngineThrottle;
	}
	
	public void processEngines(STSup.Engines engines) {
		if(engines.hasLeftEngineThrottle()) leftEngineThrottle = engines.getLeftEngineThrottle();
		if(engines.hasRightEngineThrottle()) rightEngineThrottle = engines.getRightEngineThrottle();
	}
}
