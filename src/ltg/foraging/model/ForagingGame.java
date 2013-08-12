package ltg.foraging.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ForagingGame {
	
	private List<RFIDTag> tags = null;
	private Map<String, FoodPatch> patches = null;
	
	
	public ForagingGame() {
	}
	
	
	public synchronized void resetGame(Map<String, FoodPatch> patches, List<RFIDTag> tags) {
		this.patches = patches;
		this.tags = tags;
	}
	
	
	public synchronized FoodPatch getPatch(String patchId) {
		return patches.get(patchId);
	}
	
	
	public synchronized Collection<FoodPatch> getAllPatches() {
		return patches.values();
	}
	
	
	public synchronized List<RFIDTag> getAllTags() {
		return tags;
	}
	

	public synchronized void setLocation(String tag, String dest) {
		for (RFIDTag t : tags) {
			if (t.id.equals(tag))
				t.currentLocation = dest;
		}
	}


	public boolean isInitialized() {
		if (tags!=null && patches!= null)
			return true;
		return false;
	}


	public void updateTimes() {
		for (RFIDTag t : tags) {
			t.updateTimeAtLocation();
		}
	}

}
