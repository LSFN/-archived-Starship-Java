package org.lsfn.starship;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPath implements ISubscriptionPath {

	private List<String> pathNodes;
	
	public SubscriptionPath() {
		pathNodes = new ArrayList<String>();
	}
	
	@Override
	public void addTailNode(String node) {
		pathNodes.add(node);
	}

	@Override
	public boolean isPathEquivalent(String path) {
		String assembledPath = "";
		for(String node : pathNodes) {
			if(!assembledPath.equals("")) assembledPath += '/';
			assembledPath += node;
		}
		return path.equals(assembledPath);
	}

}
