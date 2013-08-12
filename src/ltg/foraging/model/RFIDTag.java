package ltg.foraging.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RFIDTag {

	public String id;
	public String cluster;
	public String color;
	public String currentLocation;
	private Map <String, Integer> timeAtLocation;
	
	
	public RFIDTag(String id, String cluster, String color, Set<String> patches) {
		this.id = id;
		this.cluster = cluster;
		this.color = color;
		currentLocation = null;
		timeAtLocation = new HashMap<String, Integer>();
		for (String s : patches) {
			timeAtLocation.put(s, 0);
		}
	}


	public void updateTimeAtLocation() {
		if (currentLocation!=null) {
			timeAtLocation.put(currentLocation, new Integer(timeAtLocation.get(currentLocation).intValue()+1));
		}
	}


	public Map<String, Integer >getTimes() {
		return this.timeAtLocation;
	}

}
