package ltg.foraging;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ltg.commons.MessageListener;
import ltg.commons.SimpleXMPPClient;
import ltg.foraging.model.FoodPatch;
import ltg.foraging.model.ForagingGame;
import ltg.foraging.model.RFIDTag;
import ltg.foraging.viz.GameHistory;

import org.jivesoftware.smack.packet.Message;

import processing.core.PApplet;
import processing.core.PFont;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.exceptions.JsonParseException;
import com.github.jsonj.tools.JsonParser;

import controlP5.Bang;
import controlP5.ControlEvent;
import controlP5.ControlP5;


public class ForagingTeacherTools extends PApplet {
	private static final long serialVersionUID = 1L;
	private static final String DISPLAY_INIT_MESSAGE = "{\"event\":\"teacher_display_init\",\"payload\":{}}";

	// Data collection flag
	private boolean collectData = false;
	// XMMP client
	private SimpleXMPPClient xmpp = null;
	// JSON parser
	private JsonParser parser = new JsonParser();
	// Foraging game
	private ForagingGame fg = new ForagingGame();
	// Game history
	private GameHistory gh = new GameHistory();
	// Control P5 widget
	private ControlP5 cp5;
	// Timer
	private int previousMillis = 0;
	private int residualMillis = 0;
	// Patches colors
	private Map<String, Integer> patchColor = new HashMap<String, Integer>();
	// Current tab
	private int currentTabId = 0;
	// Current selected KID
	private String currenKid = null;
	// Bangs
	private List<Bang> bangs = new ArrayList<Bang>();


	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "ForagingTeacherTools" });
	}



	////////////////////////
	// Processing methods //
	////////////////////////
	public void setup() {
		// Sketch
		setupSketch();	
		// Logic
		xmpp = new SimpleXMPPClient("fg-teacher-tools@ltg.evl.uic.edu", "fg-teacher-tools", "fg-pilot-oct12@conference.ltg.evl.uic.edu");
		xmpp.registerEventListener(new MessageListener() {
			@Override
			public void processMessage(Message m) {
				processIncomingData(m.getBody());
			}
		});
		println("Connected to chatroom and listening");
	}


	public void draw() {
		// Update times
		int currentMillis = millis();
		residualMillis = residualMillis + currentMillis - previousMillis;
		previousMillis = currentMillis;
		// Return if there is no data
		if(!fg.isInitialized())
			return;
		if (!collectData)
			return;
		// Update data
		if (residualMillis > 1000) {
			residualMillis -=1000;
			fg.updateTimes();
			gh.updateHistory(fg.getAllTags());
			// Draw
			if (currentTabId==0) {
				background(0);
				updateClassPieChart();
			} else if (currentTabId==1) {
				background(0);
				updateIndividualPieChart(currenKid);
			}
			updateControls();

		}
	}



	/////////////////////
	// Drawing methods //
	/////////////////////
	private void setupSketch() {
		// Sketch size & backgroud
		size(displayWidth/2, displayHeight/2);
		background(0);
		// Initialize controls
		cp5 = new ControlP5(this);
		// Tabs
		cp5.getTab("default").setLabel("Collective Pie Chart").setId(0).activateEvent(true);
		cp5.addTab("individual").setLabel("Individual Pie Chart").setId(1).activateEvent(true);
		// Slider
		//cp5.addSlider();
		// Patches colors
		patchColor.put("fg-patch-1", 255);
		patchColor.put("fg-patch-2", 220);
		patchColor.put("fg-patch-3", 185);
		patchColor.put("fg-patch-4", 150);
		patchColor.put("fg-patch-5", 115);
		patchColor.put("fg-patch-6", 80);
		patchColor.put("fg-den", 	 55);
	}


	private void updateControls() {
		// Move bangs
		for (Bang b : bangs) {
			float oldP = b.getPosition().y;
			b.setPosition(.85f*width, oldP);
		}
		//Legend
		PFont f = createFont("Helvetica", 24, true);
		textFont(f);
		float os = (textAscent()+textDescent())/2;
		// Patch 1
		fill(patchColor.get("fg-patch-1"));
		rect(.15f*width-80,70,80,50);
		text("Patch 1", .01f*width, 70+25+os);
		//Patch 2
		fill(patchColor.get("fg-patch-2"));
		rect(.15f*width-80,140,80,50);
		text("Patch 2", .01f*width, 140+25+os);
		//Patch 3
		fill(patchColor.get("fg-patch-3"));
		rect(.15f*width-80,210,80,50);
		text("Patch 3", .01f*width, 210+25+os);
		// Patch 4
		fill(patchColor.get("fg-patch-4"));
		rect(.15f*width-80,280,80,50);
		text("Patch 4", .01f*width, 280+25+os);
		// Patch 5
		fill(patchColor.get("fg-patch-5"));
		rect(.15f*width-80,350,80,50);
		text("Patch 5", .01f*width, 350+25+os);
		// Patch 6
		fill(patchColor.get("fg-patch-6"));
		rect(.15f*width-80,420,80,50);
		text("Patch 6", .01f*width, 420+25+os);
		// Patch den
		fill(patchColor.get("fg-den"));
		rect(.15f*width-80,490,80,50);
		text("Den", .01f*width, 490+25+os);
	}


	private void updateClassPieChart() {
		float lastAngle = 0;
		Map<String, Float> times= gh.getCollectiveNormalizedTimes(gh.getCurrentTs());
		Collection<String> keys = times.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String currentKey = iterator.next();
			fill(patchColor.get(currentKey).intValue());
			float angle = 360f*times.get(currentKey).floatValue();
			arc(width/2, height/2, height*.9f, height*.9f, lastAngle, lastAngle+radians(angle));
			lastAngle += radians(angle);
		}
	}


	private void updateIndividualPieChart(String kid) {
		if (kid == null) 
			return;
		float lastAngle = 0;
		Map<String, Float> times= gh.getIndividualNormalizedTimes(gh.getCurrentTs(), currenKid);
		Collection<String> keys = times.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String currentKey = iterator.next();
			fill(patchColor.get(currentKey).intValue());
			float angle = 360f*times.get(currentKey).floatValue();
			arc(width/2, height/2, height*.8f, height*.8f, lastAngle, lastAngle+radians(angle));
			lastAngle += radians(angle);
		}
	}



	//////////////////////
	// Procesing events //
	//////////////////////

	public void controlEvent(ControlEvent theControlEvent) {
		if (theControlEvent.isTab()) {
			currentTabId = theControlEvent.getTab().getId();
			cp5.getTab(theControlEvent.getTab().getName()).bringToFront();
		} else {
			currenKid = theControlEvent.getController().getName();
		}
	}

	

	////////////////////////////
	// Event handling methods //
	////////////////////////////

	public void processIncomingData(String s) {
		JsonObject json = null;
		JsonElement jsone = null;
		try {
			jsone = parser.parse(s);
			if (!jsone.isObject()) {
				// The element is not an object... bad...
				return;
			}
			json = jsone.asObject();
			// Pick the right JSON handler based on the event type
			if (isTeacherDisplayInit(json)) {
				teacherDisplayInitInfo(json);
			}else if (isRFIDUpdate(json)) {
				updateLocation(json);
			}else if (isGameReset(json)) {
				resetGame();
			}else if (isGameStop(json)) {
				stopGame();
			} 
		} catch (JsonParseException e) {
			// Not JSON... skip
			//System.err.println("Not JSON: " + s);
		}
	}


	private void stopGame() {
		collectData = false;
	}


	private void resetGame() {
		System.out.println("Resetting history...");
		gh.resetHistory();
		xmpp.sendMessage(DISPLAY_INIT_MESSAGE);
	}


	private void updateLocation(JsonObject json) {
		if (!collectData)
			return;
		// Update location
		String dest = json.getString("destination");
		for (JsonElement a : json.getArray("payload", "arrivals")) {
			fg.setLocation(a.asPrimitive().asString(), dest);
		}
		for (JsonElement a : json.getArray("payload", "departures")) {
			fg.setLocation(a.asPrimitive().asString(), null);
		}
	}


	private void teacherDisplayInitInfo(JsonObject json) {
		System.out.println("Init info received");
		// Process patches
		Map <String, FoodPatch> patches = new HashMap<String, FoodPatch>();
		JsonArray jPatches = json.getArray("payload", "patches");
		for (JsonElement jp : jPatches) {
			JsonObject p = ((JsonObject) jp);
			patches.put(p.getString("patch"), new FoodPatch(p.getString("patch"), p.getInt("feed-ratio")));
		}
		// Process tags
		List <RFIDTag> tags = new ArrayList<RFIDTag>();
		JsonArray jTags = json.getArray("payload", "tags");
		for (JsonElement jt: jTags) {
			JsonObject t = ((JsonObject) jt);
			tags.add(new RFIDTag(t.getString("tag"), t.getString("cluster"), t.getString("color"), patches.keySet()));
		}
		// Sort tags according to cluster and tagID
		Collections.sort(tags, new Comparator<RFIDTag>() {
			@Override
			public int compare(RFIDTag t1, RFIDTag t2) {
				int clusterDiff = t1.cluster.compareTo(t2.cluster);
				return clusterDiff == 0 ? 
						t1.id.compareTo(t2.id) :
							clusterDiff;
			}
		});
		// Initialize interface: add a button per tag
		int count = 1;
		int pos = 35;
		for (RFIDTag t: tags) {
			Bang b = cp5.addBang(t.id);
			b.setPosition(.8f*width, pos);
			b.setSize(80, 30);
			b.setLabelVisible(false);
			b.setColorForeground(unhex("FF"+t.color.substring(1)));
			b.moveTo("individual");
			bangs.add(b);
			if (count==4 || count==8 || count==12)
				pos+=70;
			else
				pos+=35;
			count++;
		}
		// Init game
		fg.resetGame(patches, tags);
		// Enable data collection
		collectData = true;
	}


	private boolean isGameStop(JsonObject json) {
		if (json.getString("event")!= null && 
				json.getString("event").equals("game_stop"))
			return true;
		return false;
	}


	private boolean isGameReset(JsonObject json) {
		if (json.getString("event")!= null && 
				json.getString("event").equals("game_reset"))
			return true;
		return false;
	}


	private boolean isRFIDUpdate(JsonObject json) {
		if (json.getString("event")!= null && 
				json.getString("event").equals("rfid_update") && 
				json.getString("destination")!= null && 
				json.getObject("payload") != null)
			return true;
		return false;
	}


	private boolean isTeacherDisplayInit(JsonObject json) {
		if (json.getString("event")!= null && 
				json.getString("event").equals("teacher_display_init_data") &&  
				json.getArray("payload", "patches")!= null &&
				json.getArray("payload", "tags")!= null)
			return true;
		return false;
	}

}
