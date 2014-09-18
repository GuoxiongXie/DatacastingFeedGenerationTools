/*
 * This class writes the metadata of an item to a plain text file for digestion.
 *
 */

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class ItemMetadataGenerator {

    public static void generateItemMetadata(Granule gran, String shortName) {
    	// all info is contained in gran
	    try{
	    	String allFeedRootPath = "/home/felixxie/FeedGenerator/datacasts/";		//TODO: This is the path of server. Should manually create datacasts folder.
	    	//String allFeedRootPath = "/Users/felixxie/Documents/workspace/FeedGenerator/src/datacasts/";		//TODO: This is the path of JPL Mac. Should manually create datacasts folder. Change it when deploying to server.
	    	//String allFeedRootPath = "/Users/felixxie/Desktop/SummerProject/FeedGenerator/src/datacasts/";		//TODO: This is the path in my Mac pro.
	    	
	    	String oneFeedRootPath = allFeedRootPath.concat(shortName).concat("-feed/");		// datacasts/MOD02QKM-feed/   I think the config and item-xml and queue are at this level
	    	String itemMetadataPath = oneFeedRootPath.concat("items/"); 						// datacasts/MOD02QKM-feed/items/  itemMetadata files will be stored inside this folder.
	    	
	    	//create folders if not exists.
    		File file4DirCreation = new File(itemMetadataPath);
    		if (!file4DirCreation.exists())
    			file4DirCreation.mkdirs();		//creates the items/ folder
	    	
	    	String title = gran.getTitle().concat(".item");
	    	
	    	String itemPath = itemMetadataPath.concat(title);
 
    		File file = new File(itemPath);
 
    		//let it loop to avoid racing between writing to file and getting layer (should happen first).
    		while (gran.layerArray.size() == 0) {
    			
    		}
    		
    		//if file doesnt exists, then create it
    		if(gran.isSeenBefore==false && !file.exists()){		//only when not ingested before and only not exist in items folder (no repeating) we write to the file.
    			
        		String newline = System.getProperty("line.separator");
        		String cumulation = "title=";
        		cumulation = cumulation.concat(gran.getTitle()).concat(newline);
        		cumulation = cumulation.concat("description=").concat(gran.getDescription()).concat(newline);
        		cumulation = cumulation.concat("pubDate=").concat(gran.getPubDate()).concat(newline);
        		cumulation = cumulation.concat("enclosure=").concat(gran.getEnclosure()).concat(newline);
        		cumulation = cumulation.concat("guid=").concat(gran.getGuid()).concat(newline);
        		cumulation = cumulation.concat("acquisitionStartDate=").concat(gran.getAcquisitionStartDate()).concat(newline);
        		cumulation = cumulation.concat("acquisitionEndDate=").concat(gran.getAcquisitionEndDate()).concat(newline);
        		
        		//Handle the bbox and polygon cases:
        		if (gran.isBox) {
        			cumulation = cumulation.concat("extents=").concat(gran.getExtents()).concat(newline);
        			
        			//in bbox case, the extents in the item metadata file is min lat (min y), min long (min x), max lat(max y), max long(max x) (this is what it returns from ECHO)
        			//but in the preview, the bbox should be min long (min x), min lat (min y), max long (max x), max lat (max y)
        		
        			String extentsField = gran.getExtents();	//min lat (min y), min long (min x), max lat(max y), max long(max x)
        			String delims = "[,]+";
           	    	String[] tokens = extentsField.split(delims);
           	    	String minY = tokens[0];
           	    	String minX = tokens[1];
           	    	String maxY = tokens[2];
           	    	String maxX = tokens[3];
           	    	
        			String bboxInPreview = minX.concat(",").concat(minY).concat(",").concat(maxX).concat(",").concat(maxY);	//long, lat, long, lat
        			gran.setExtents(bboxInPreview);		//be careful, the extents is reset to min long (min x), min lat (min y), max long (max x), max lat (max y)
        		}	
        		if (gran.isPoly)
        			cumulation = cumulation.concat("extentsPoly=").concat(gran.getExtentsPoly()).concat(newline);
        		//end of bbox and polygon cases.
        		
        		
        		
        		//customElement section (may or may not contain one):
        		//1-1 case and day/night case, then no customElement, only preview.
        		if (gran.layerArray.size() == 1) {
        			//construct the preview string:
        			String preview = "preview = http://datacasting.jpl.nasa.gov/wms/service?LAYERS=";
        			preview = preview.concat(gran.layerArray.get(0)).concat("&FORMAT=").concat(gran.imageFormat).concat("&SRS=EPSG%3A4326&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&BBOX=");
        			preview = preview.concat(gran.getExtents()).concat("&WIDTH=");
        			preview = preview.concat(gran.getWidth()).concat("&HEIGHT=");
        			preview = preview.concat(gran.getHeight()).concat("&TIME=");
        			preview = preview.concat(gran.getTime()).concat("&TRANSPARENT=TRUE").concat(newline);
        			
            		//form the entire string:
            		cumulation = cumulation.concat(preview);
        		}
        		
        		//one dataset maps to many layers, then it has customElement
        		else if (gran.layerArray.size() > 1) {
        			//System.out.println("heyyo");
        			int numLayers = gran.layerArray.size();
        			String tempLayer;
        			String preview = ""; //use the first link for preview
        			
        			//construct the links custom elements
        			for (int i=0; i<numLayers; i++) {
        				//System.out.println("say 3 times");
        				tempLayer = gran.layerArray.get(i); 
        				cumulation = cumulation.concat("customElement = ").concat(tempLayer).concat(",");
        				//System.out.println(cumulation);
        				//construct the link
        				String link = "http://datacasting.jpl.nasa.gov/wms/service?LAYERS=".concat(tempLayer).concat("&FORMAT=").concat(gran.imageFormat).concat("&SRS=EPSG%3A4326&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&BBOX=");
        				link = link.concat(gran.getExtents()).concat("&WIDTH=").concat(gran.getWidth()).concat("&HEIGHT=").concat(gran.getHeight()).concat("&TIME=").concat(gran.getTime()).concat("&TRANSPARENT=TRUE").concat(newline);
        			
        				cumulation = cumulation.concat(link);
        				
        				//btw, set preview.
        				if (i == 0)
        					preview = "preview = ".concat(link).concat(newline);
        			}
        			
        			//construct the Floods, Fires, etc customElements.
        			String layerList = "";
        			String floodLayers = gran.floodLayers;
        			String fireLayers = gran.fireLayers;
        			String severeStormLayers = gran.severeStormLayers;
        			String dustStormLayers = gran.dustStormLayers;
        			
        			if (floodLayers != null)
        				layerList = layerList.concat("customElement = Floods,").concat(floodLayers).concat(newline);
        				
        			if (fireLayers != null)
        				layerList = layerList.concat("customElement = Fires,").concat(fireLayers).concat(newline);
        			
        			if (severeStormLayers != null)
        				layerList = layerList.concat("customElement = SevereStorms,").concat(severeStormLayers).concat(newline);
        			
        			if (dustStormLayers != null)
        				layerList = layerList.concat("customElement = DustStorms,").concat(dustStormLayers).concat(newline);
        			
        			cumulation = cumulation.concat(layerList);
            		//form the entire string:
            		cumulation = cumulation.concat(preview);
        		}
        		
    			//finally write to the file.
    			file.createNewFile();
    			FileWriter fileWritter = new FileWriter(file,true);
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        bufferWritter.write(cumulation);
    	        bufferWritter.close();
    		}
 
	        //System.out.println("Done");
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

}	