package org.lsfn.starship.cache;

import org.lsfn.common.STS.STSdown;
import org.lsfn.common.STS.STSup;

public class Reactor {

	private double reactantIntroduction, coolantIntroduction, reactorOutput, reactorTemperature;
	
	public Reactor() {
		reactantIntroduction = coolantIntroduction = reactorOutput = reactorTemperature = 0.0;
	}

	public double getReactantIntroduction() {
		return reactantIntroduction;
	}

	public double getCoolantIntroduction() {
		return coolantIntroduction;
	}

	public double getReactorOutput() {
		return reactorOutput;
	}

	public double getReactorTemperature() {
		return reactorTemperature;
	}
	
	public void processReactor(STSup.Reactor reactor) {
		if(reactor.hasReactantIntroduction()) reactantIntroduction = reactor.getReactantIntroduction();
		if(reactor.hasCoolantIntroduction()) coolantIntroduction = reactor.getCoolantIntroduction();
	}
	
	public void processReactor(STSdown.Reactor reactor) {
		if(reactor.hasReactantIntroduction()) reactantIntroduction = reactor.getReactantIntroduction();
		if(reactor.hasCoolantIntroduction()) coolantIntroduction = reactor.getCoolantIntroduction();
		if(reactor.hasPowerOutput()) reactorOutput = reactor.getPowerOutput();
		if(reactor.hasHeatLevel()) reactorTemperature = reactor.getHeatLevel();		
	}
}
