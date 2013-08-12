package ltg.foraging.viz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ltg.foraging.model.RFIDTag;

public class GameHistory {
	
	private int currentTimestamp;
	private List<Timestamp> timestamps;

	public synchronized void resetHistory() {
		currentTimestamp = 0;
		timestamps = new ArrayList<Timestamp>(); 
	}

	
	public synchronized void updateHistory(List<RFIDTag> tags) {
		currentTimestamp++;
		timestamps.add(new Timestamp(tags));
	}
	
	
	public synchronized Map<String, Float> getIndividualNormalizedTimes(int ts, String tag) {
		// Initialization
		int totalTime = 0;
		ts--;
		// Get times
		Map<String, Integer> times = timestamps.get(ts).getTagTimes(tag);
		// Calculate total time
		for (Integer i: times.values())
			totalTime += i.intValue();
		// Normalize
		Map<String, Float> results = new HashMap<String, Float>();
		for (String k: timestamps.get(ts).getTagTimes(tag).keySet()) {
			if (totalTime!=0)
				results.put(k, new Float(((float)times.get(k).intValue())/((float) totalTime)));
			else
				results.put(k, 0f);
		}
		// Return
		return results;
	}
	
	
	public synchronized Map<String, Float> getCollectiveNormalizedTimes(int ts) {
		// Initialization
		int totalTime = 0;
		ts--;
		// Get times
		Map<String, Integer> times = timestamps.get(ts).getCollectiveTimes();
		// Calculate total time
		for (Integer i: times.values())
			totalTime += i.intValue();
		// Normalize
		Map<String, Float> results = new HashMap<String, Float>();
		for (String k: times.keySet()) {
			if (totalTime!=0)
				results.put(k, new Float( ((float)times.get(k).intValue())/((float) totalTime)));
			else 
				results.put(k, 0f);
		}
		// Return
		return results;
	}
	
	
	public synchronized int getCurrentTs() {
		return currentTimestamp;
	}

}
