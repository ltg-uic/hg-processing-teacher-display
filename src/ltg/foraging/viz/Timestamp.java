package ltg.foraging.viz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ltg.foraging.model.RFIDTag;

public class Timestamp {
	
	private List<RFIDTag> tags = null;
	
	public Timestamp(List<RFIDTag>tags) {
		this.tags = tags;
	}
	
	
	public Map<String, Integer> getTagTimes(String id) {
		for (RFIDTag t: tags) {
			if (t.id.equals(id)) {
				return t.getTimes();
			}
		}
		return null;
	}


	public Map<String, Integer> getCollectiveTimes() {
		Map<String, Integer> times = new HashMap<String, Integer>();
		for (RFIDTag t: tags) {
			for (String s: t.getTimes().keySet()) {
				if (times.get(s)==null)
					times.put(s, t.getTimes().get(s));
				else
					times.put(s, new Integer(times.get(s).intValue() + t.getTimes().get(s)));
			}
		}
		return times;
	}

}
