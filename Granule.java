import java.util.ArrayList;
import java.util.Hashtable;

/* This is the Granule Class.
 * 
 * It maintains a CLASS Hashtable (synchronized) to store the granule ID for the granules that has been ingested. 
 * The reason is that we do not want to parse the same set of granules over and over again.
 * 
 * Granule Class also maintains the info for each granule. This info will be written to the corresponding item metadata file.
 */
 

public class Granule {
	
	//The CLASS variable granuleSeenBefore store the granule ID as key and an int as value (the value is dummy, we don't really need it).
	private static Hashtable<String, Integer> granuleSeenBefore = new Hashtable<String, Integer>();

	//create the necessary info to be written to item metadata file.
	
	private String title;
	private String description;
	private String pubDate;
	private String enclosure;				//The link to the actual data
	private String guid;
	private String acquisitionStartDate;
	private String acquisitionEndDate;
	private String extentsPoly;
	private String preview;					//This is the link to the image associated with this granule
	
	private String extents; 					//bounding box, this maybe given or may be derived from extentsPoly
	
	public String metadataLink;										//Product metadata link, this is where the <dayNightFlag> stores.
	public ArrayList<String> layerArray = new ArrayList<String>();	//when layerArray has 1 elm, then it's singleLayer or day/night layer, in this case we do not include any custom elements. 
	public String imageFormat;
	//public boolean isOneLayer;										//dataset and layer is 1-1 mapping
	//public boolean isMultiple;										//dataset maps to multiple layers
	//public boolean isDayNight;										//dataset maps to day/night layer.
	public boolean isSeenBefore;				//a flag if this granule has been already ingested in items-xml folder.
	
	//if the following remains null, then there's no corresponding layer for fire, flood, etc
	public String fireLayers = null;
	public String floodLayers = null;
	public String dustStormLayers = null;
	public String severeStormLayers = null;
	
	//the following are used for generating preview
	private String time;
	private String height; 
	private String width;
	//private String layers;
	
	
	//if isBox and isPoly both are false, then it means the data granule doesn't have geo:rss location info.
	public boolean isBox = false;					//signifies if this data granule contains info about geo box: <georss:box>
	public boolean isPoly = false;					//contains <georss:polygon>
	
	//CLASS method: already seen checker
	public static boolean alreadySeen(String granuleID){
		if (Granule.granuleSeenBefore.containsKey(granuleID))
			return true;					//seen before, don't parse it again
		else {
			Granule.granuleSeenBefore.put(granuleID, 1);	//1 does not have a meaning just random number; store the key in the Hashtable
			return false;					//not seen before, parse it
		}
	}
	
	
	//setters
	public void setTitle(String aTitle) {
		this.title = aTitle;
	}
	
	public void setDescription(String aDescription) {
		this.description = aDescription;
	}
	
	public void setPubDate(String aPubDate) {
		this.pubDate = aPubDate;
	}
	
	public void setEnclosure(String aEnclosure) {
		this.enclosure = aEnclosure;
	}
	
	public void setGuid(String aGUID) {
		this.guid = aGUID;
	}
	
	public void setAcquisitionStartDate(String aAcquisitionStartDate) {
		this.acquisitionStartDate = aAcquisitionStartDate;
	}
	
	public void setAcquisitionEndDate(String aAcquisitionEndDate) {
		this.acquisitionEndDate = aAcquisitionEndDate;
	}
	
	public void setExtentsPoly(String aExtentsPoly) {
		this.extentsPoly = aExtentsPoly;
	}
	
	public void setPreview(String aPreview){
		this.preview = aPreview;
	}
	
	public void setExtents(String aBbox){
		this.extents = aBbox;
	}
	
	public void setTime(String aTime){
		this.time = aTime;
	}
	
	public void setHeight(String aHeight){
		this.height = aHeight;
	}
	
	public void setWidth(String aWidth){
		this.width = aWidth;
	}
	
	//public void setLayers(String aLayer){
		//this.layers = aLayer;
	//}
	
	
	//getters
	public String getTitle() {
		return this.title;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getPubDate() {
		return this.pubDate;
	}
	
	public String getEnclosure() {
		return this.enclosure;
	}
	
	public String getGuid() {
		return this.guid;
	}
	
	public String getAcquisitionStartDate() {
		return this.acquisitionStartDate;
	}
	
	public String getAcquisitionEndDate() {
		return this.acquisitionEndDate;
	}
	
	public String getExtentsPoly() {
		return this.extentsPoly;
	}
	
	public String getPreview(){
		return this.preview;
	}
	
	public String getExtents() {
		return this.extents;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String getHeight() {
		return this.height;
	}
	
	public String getWidth() {
		return this.width;
	}
	
	//public String getLayers() {
		//return this.layers;
	//}
}