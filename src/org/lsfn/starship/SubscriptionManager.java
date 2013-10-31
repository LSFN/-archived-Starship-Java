package org.lsfn.starship;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.lsfn.common.STS.STSup;
import org.lsfn.starship.cache.Engines;
import org.lsfn.starship.cache.PowerDistribution;
import org.lsfn.starship.cache.Reactor;
import org.lsfn.starship.cache.Thrusters;
import org.lsfn.starship.cache.VisualSensors;

public class SubscriptionManager {

	private Map<UUID, ConsoleSubscriptions> subscriptions;
	private VisualSensors visualSensors;
	private Reactor reactor;
	private PowerDistribution powerDistribution;
	private Engines engines;
	private Thrusters thrusters;
	
	public SubscriptionManager(VisualSensors visualSensors, Reactor reactor, PowerDistribution powerDistribution, Engines engines, Thrusters thrusters) {
		subscriptions = new HashMap<UUID, ConsoleSubscriptions>();
		this.visualSensors = visualSensors;
		this.reactor = reactor;
		this.powerDistribution = powerDistribution;
		this.engines = engines;
		this.thrusters = thrusters;
	}
	
	public void registerNewSubscription(UUID consoleID, STSup.Subscribe subscribe) {
		subscriptions.put(consoleID, new ConsoleSubscriptions(subscribe));
	}
}
