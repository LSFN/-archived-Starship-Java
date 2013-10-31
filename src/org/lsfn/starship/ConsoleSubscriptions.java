package org.lsfn.starship;

import java.util.HashSet;
import java.util.Set;

import org.lsfn.common.STS.STSup;

public class ConsoleSubscriptions {

	private boolean visualSensors, reactorReactantIntroduction, reactorCoolantIntroduction, reactorOutput, reactorTemperature, powerForwardLeftThruster, powerForwardRightThruster, powerRearLeftThruster, powerRearRightThruster, powerLeftEngine, powerRightEngine, engineLeftEngineThrottle, engineRightEngineThrottle, thrusterForwardLeft, thrusterForwardRight, thrusterRearLeft, thrusterRearRight;
	
	public ConsoleSubscriptions(STSup.Subscribe subscribe) {
		visualSensors = reactorReactantIntroduction = reactorCoolantIntroduction = reactorOutput = reactorTemperature = powerForwardLeftThruster = powerForwardRightThruster = powerRearLeftThruster = powerRearRightThruster = powerLeftEngine = powerRightEngine = engineLeftEngineThrottle = engineRightEngineThrottle = thrusterForwardLeft = thrusterForwardRight = thrusterRearLeft = thrusterRearRight = false;
		
		Set<ISubscriptionPath> subscriptionPaths = assemblePathListFromSubscribe(new SubscriptionPath(), subscribe);
		for(ISubscriptionPath path : subscriptionPaths) {
			subscribeToPath(path);
		}
	}
	
	private Set<ISubscriptionPath> assemblePathListFromSubscribe(ISubscriptionPath path, STSup.Subscribe subscribe) {
		Set<ISubscriptionPath> results = new HashSet<ISubscriptionPath>();
		path.addTailNode(subscribe.getNodeName());
		if(subscribe.getChildrenCount() == 0) {
			results.add(path);
		} else {
			for(STSup.Subscribe sub : subscribe.getChildrenList()) {
				results.addAll(assemblePathListFromSubscribe(path, sub));
			}
		}
		return results;
	}
	
	private void subscribeToPath(ISubscriptionPath path) {
		visualSensors = path.isPathEquivalent("visualSensors");
		reactorReactantIntroduction = path.isPathEquivalent("reactor/reactantIntroduction");
		reactorCoolantIntroduction = path.isPathEquivalent("reactor/coolantIntroduction");
		reactorOutput = path.isPathEquivalent("reactor/reactorOutput");
		reactorTemperature = path.isPathEquivalent("reactor/reactorTemperature");
		powerForwardLeftThruster = path.isPathEquivalent("powerDistribution/forwardLeftThruster");
		powerForwardRightThruster = path.isPathEquivalent("powerDistribution/forwardRightThruster");
		powerRearLeftThruster = path.isPathEquivalent("powerDistribution/rearLeftThruster");
		powerRearRightThruster = path.isPathEquivalent("powerDistribution/rearRightThruster");
		powerLeftEngine = path.isPathEquivalent("powerDistribution/leftEngine");
		powerRightEngine = path.isPathEquivalent("powerDistribution/rightEngine");
		engineLeftEngineThrottle = path.isPathEquivalent("engine/leftEngineThrottle");
		engineRightEngineThrottle = path.isPathEquivalent("engine/rightEngineThrottle");
		thrusterForwardLeft = path.isPathEquivalent("thrusters/forwardLeft");
		thrusterForwardRight = path.isPathEquivalent("thrusters/forwardRight");
		thrusterRearLeft = path.isPathEquivalent("thrusters/rearLeft");
		thrusterRearRight = path.isPathEquivalent("thrusters/rearRight");
	}
}
