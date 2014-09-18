import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class ListOfGranulesParser extends DefaultHandler {

       private Granule gran;
       private StringBuffer temp;			//string buffer
       private boolean isItem = false;		//this flag is for identifying the id element inside entry, not the one in the feed level.
       private boolean newItem = false;		//this flag is to signify new item.
       
       
       //The following is for passing on the info
       String shortName;
       String itemsPerPage;
       String mappingPath;
       
       //The following is for paging through the return response
       String totalResults;
       
       /* This is constructor is for passing on the shortName, itemsPerPage and layer */
       public ListOfGranulesParser (String shortName, String itemsPerPage, String mappingPath) {
    	   this.shortName = shortName;
    	   this.itemsPerPage = itemsPerPage;
    	   this.mappingPath = mappingPath;
       }
       
       
       /*
        * When the parser encounters plain text (not XML elements),
        * it calls(this method, which accumulates them in a string buffer
        */
       public void characters(char[] buffer, int start, int length) {
           //temp = new String(buffer, start, length);
    	   temp.append(new String(buffer, start, length));
       }
      

       /*
        * Every time the parser encounters the beginning of a new element,
        * it calls this method, which resets the string buffer
        */ 
       public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	   
    	   /*
    	    * uri - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
			* localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
			* qName - The qualified name (with prefix), or the empty string if qualified names are not available. e.g. echo:datasetId
			* attributes - The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    	    */
    	   
    	   //System.out.println("line 63, in startElement " + qName +", temp is "+ temp);
    	   temp = new StringBuffer();		//flush out the previous content
    	   
           if (qName.equalsIgnoreCase("entry"))	{	//for each <entry> encountered
        	   //System.out.println("Entry found");
        	   this.isItem = true;		//u r currently inside an entry.
        	   gran = new Granule();	//create a new Granule obj to store info before writting to file
        	   
           } else if (qName.equalsIgnoreCase("link")) {	//get enclosure
        	   if (this.isItem && this.newItem)	{	//store info only when inside entry and the item is new
        		  if (attributes.getValue("rel").equalsIgnoreCase("enclosure")) {
        			  String linkToData = attributes.getValue("href");
        			  gran.setGuid(linkToData);
        			  
        			  String type = attributes.getValue("type");
        			  //System.out.print(type);
        			  
        			  /*new code: handles OMI but no type field in the enclosure */
        			  if (type != null) {
        				  String enclosureStr = linkToData.concat(", 0, ").concat(type);
            			  gran.setEnclosure(enclosureStr);
        			  }
        			  else {	//no type field, like OMTO3e
        				  String title = gran.getTitle();
        				  String delims = "[.]+";
        				  String[] tokens = title.split(delims);
        				  int lastIndex = tokens.length - 1;
        				  String extention = tokens[lastIndex];				// extention: e.g. he5
        				  
        				  if (extention.equalsIgnoreCase("he5")) {
        					  String enclosureStr = linkToData.concat(", 0, application/x-hdfeos");
        					  gran.setEnclosure(enclosureStr);
        				  }
        				  
        				  else {	//no type attribute and not .he5; still no type for this kind.
        					  String enclosureStr = linkToData.concat(", 0");
        					  gran.setEnclosure(enclosureStr);
        				  }
            			  
        			  }
        			  //end of new code
        			  
        			  /* the original code: does not handle OMI
        			  String enclosureStr = linkToData.concat(", 0, ").concat(type);
        			  gran.setEnclosure(enclosureStr);
        		  	*/	
        		  } else if (attributes.getValue("title") != null && attributes.getValue("title").equalsIgnoreCase("Product metadata")) {
        			  String linkToMetadata = attributes.getValue("href");
        			  gran.metadataLink = linkToMetadata;						//this is where the <DayNightFlag> stores.
        		  }
        	   }
           }
       }

       /*
        * When the parser encounters the end of an element, it calls this method
        */
       public void endElement(String uri, String localName, String qName)
                     throws SAXException {
    	   	  if (qName.equalsIgnoreCase("os:totalResults"))
    	   		  this.totalResults = temp.toString();
    	   
    	   	  else if (qName.equalsIgnoreCase("entry")) {		//end of an item
            	  
            	try {
					DatasetToLayer.getLayerAndImageFormat(this.gran, this.shortName, this.mappingPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
            	  
                //signify the end of one item:
            	this.isItem = false;
            	this.newItem = false;		//set the flag to false when done with the entry

            	  
              } else if (qName.equalsIgnoreCase("id")) {
            	  
            	  if (this.isItem) {						//u r currently in an entry
            		  String id = temp.toString();
            		  if (Granule.alreadySeen(id) == false) 	// not seen before, store it and write to file later
            			  this.newItem = true;				//new item, process it  
            	  }
            	  
              } else if (qName.equalsIgnoreCase("echo:producerGranuleId")) {
            	  if (this.isItem && this.newItem) {		//store info only when inside entry and the item is new
            		  //System.out.println("Hello, line 107, I'm producerGID: " + temp);
            		  gran.setTitle(temp.toString());		//temp is AIRS.2013.07.15.110.L2.RetSup.v6.0.7.0.G13196094610.hdf
            		  gran.setDescription(temp.toString());
            		  
            		  //set gran.isSeenBefore to do the optimization in day/night and final write to disk cases.
            		  SetSeenBeforeFlag.setFlag(gran, shortName, temp.toString()); 		//this sets gran.isSeenBefore
            	  }
              }	else if (qName.equalsIgnoreCase("updated")) {
            	  if (this.isItem && this.newItem)	{	//store info only when inside entry and the item is new
            		  //System.out.println("In line 112, the updated is " + temp);
            		  String stdFormat = getStdFormat(temp.toString());
            		  gran.setPubDate(stdFormat);
            	  }
              }	else if (qName.equalsIgnoreCase("dc:date")) {
            	  if (this.isItem && this.newItem) {		//store info only when inside entry and the item is new
            		  //temp is of the format: 2013-05-24T23:55:00.000Z/2013-05-25T00:00:00.000Z
            		  //System.out.println("In line 119, the dc:date is " + temp);
            		  String startTime = getStartTime(temp.toString());	//startTime: 2013:06:22:14:10:00
            		  gran.setAcquisitionStartDate(startTime);
            		  
            		  String endTime = getEndTime(temp.toString());		//endTime: 2013:06:22:14:15:00
            		  gran.setAcquisitionEndDate(endTime);
            	  }
              } else if (qName.equalsIgnoreCase("georss:box")) {		//set extents, width and height TODO: need to debug for this case
            	  //In the case when we have bbox, we do not store Granule's extentsPoly field.
            	  if (this.isItem && this.newItem && gran.isBox==false && gran.isPoly==false) {		//isBox and isPoly needs to both false to set box cuz if we have both box and poly, we will use poly instead
            		  gran.isBox = true;
            		  gran.isPoly = false;
            		  
            		  String boxInStdFormat = temp.toString().replace(" ", ",");	//the bbox format is min lat (min y), min long (min x), max lat(max y), max long(max x)
            		  gran.setExtents(boxInStdFormat);		//this order is correct for extents field in item metadata files, wrong in preview. Will transform this when writing to file.
            		  
            		  //set width and height for preview
            		  bBoxSetWidthHeight (boxInStdFormat);
            	  }
              }	else if (qName.equalsIgnoreCase("georss:polygon")) {	//set extentsPoly.
            	  //In the case when we have polygon, we also set Granule's extents field for WMS part
            	  if (this.isItem && this.newItem) {
            		  gran.isBox = false;
            		  gran.isPoly = true;
            		  
            		  String polyInStdFormat = temp.toString().replace(" ", ",");
            		  gran.setExtentsPoly(polyInStdFormat);
            		  
            		  //transform poly to box for WMS use in the future, store bbox, height and width
            		  polygonToBbox(polyInStdFormat);
            	  }
              } else if (qName.equalsIgnoreCase("feed")) {		//all item metadata files created, now ingest and generate feed. TODO: take care of no entry case when XX-feed folder doesn't even created: but we should first have config.cfg file in it, so it has to be exist first. items folder maybe empty tho.
            	  FeedPublisher.publishFeed(this.shortName);
              }
              
       }

       
       //format the startTime
       public String getStartTime(String startAndEnd) {
    	    //System.out.println(startAndEnd);
   	    	String delims = "[/]+";
   	    	String[] tokens = startAndEnd.split(delims);
   	    	String startTime = tokens[0];				// startTime is 2013-05-24T23:55:00.000Z
   	    	
   	    	//btw, set the time parameter in preview.
   	    	String stdFormatTime = getStdFormat4Time(startTime);
   	    	gran.setTime(stdFormatTime);				//This is used for preview: TIME=2013-06-22
   	    	
   	    	// go back to get the acquisition time.
   	    	//System.out.println(startTime);
   	    	String stdStartTime = getStdFormat(startTime);
   	    	return stdStartTime;
       }
       
       public String getEndTime(String startAndEnd) {
    	   String delims = "[/]+";
    	   String[] tokens = startAndEnd.split(delims);
    	   String endTime = tokens[1];
    	   
    	   String stdEndTime = getStdFormat(endTime);
    	   return stdEndTime;
    	   
       }
       
       public String getStdFormat (String time) {		//from 2013-05-24T23:55:00.000Z to 2013:05:24:23:55:00
    	   //System.out.println(time);
           String Tdelims = "[T]+";
           String[] dateTimeTokens = time.split(Tdelims);
           String startDate = dateTimeTokens[0];
           String newStartTime = dateTimeTokens[1];
           
           String newStartDate = startDate.replace("-", ":");	//2013:05:24
   	    
           String dotDelims = "[.]+";
           String[] dotTokens = newStartTime.split(dotDelims);
           String newNewStartTime = dotTokens[0];								//23:55:00
   	    
           String stdFormat = newStartDate.concat(":").concat(newNewStartTime);		//2013:05:24:23:55:00
           
           return stdFormat;
       }
       
       
       public String getStdFormat4Time (String time) {		//from 2013-05-24T23:55:00.000Z to 2013-05-24
           String Tdelims = "[T]+";
           String[] dateTimeTokens = time.split(Tdelims);
           String startDate = dateTimeTokens[0];
           
           return startDate;		//2013-05-24
       }
       
       
       public void polygonToBbox (String polygon){	//arg is comma separated; return result should also be comma separated
    	   String delims = "[,]+";
    	   String[] tokens = polygon.split(delims);
    	   int totalSize = tokens.length;
    	   //int size = totalSize / 2;		//this is the array sizes for longArray and latArray
    	   
    	   //String[] longArray = new String[size];
    	   //String[] latArray = new String[size];
    	   
    	   ArrayList<String> longArray = new ArrayList<String>();
    	   ArrayList<String> latArray = new ArrayList<String>();
    	   
    	   boolean isLong = false;		//used for signifying if the num is longtitude. Set it to false because the first one is lat. It goes like "lat long lat long"
    	   
    	   for (int i=0; i < totalSize; i++) {
    		   if (isLong) {
    			   longArray.add(tokens[i]);
    			   isLong = false;		//flip the flag
    		   }
    		   else {
    			   latArray.add(tokens[i]);
    			   isLong = true;		//flip the flag
    		   }
    	   }
    	   
    	   //----------------------------------------------
    	   
    	   int size = totalSize/2;
    	   Coordinate[] polyCoords = new Coordinate[size];
    	   
    	   for (int i = 0; i < size; i++) {
    		   Double lon = Double.parseDouble((String) longArray.get(i));
    		   Double lat = Double.parseDouble((String) latArray.get(i));
    		   polyCoords[i] = new Coordinate(lon, lat);
    	   }
    	      
    	   String bbox = "";
    	   long imageWidth = -1;
    	   long imageHeight = -1;
    	   if (GeoUtil.datelineCrossoverOccurs(polyCoords)) {
    		   Coordinate[] datelineBBox = GeoUtil.getBBox(polyCoords);
    	       
    	       imageWidth = Math.round(GeoUtil.getBBoxWidth(datelineBBox) * 50);
    	       imageHeight = Math.round(GeoUtil.getBBoxHeight(datelineBBox) * 50);
    	         
    	       bbox = datelineBBox[0].x + "," + datelineBBox[0].y + "," + datelineBBox[1].x + "," + datelineBBox[1].y;	//bbox is comma separated
    	   } else {
    		   GeometryFactory geoFactory = new GeometryFactory();
    	        LinearRing exteriorRing = geoFactory.createLinearRing(polyCoords);
    	        Polygon poly = geoFactory.createPolygon(exteriorRing, null);
    	        Envelope env = poly.getEnvelopeInternal();
    	        imageWidth = Math.round(env.getWidth() * 50);
    	        imageHeight = Math.round(env.getHeight() * 50);
    	         
    	        bbox = env.getMinX() + "," + env.getMinY() + "," + env.getMaxX() + "," + env.getMaxY();		//bbox is comma separated
    	   }
    	   
    	   String newWidth = Long.toString(imageWidth);
    	   String newHeight = Long.toString(imageHeight);
    	   
    	   gran.setExtents(bbox);
    	   gran.setWidth(newWidth);
    	   gran.setHeight(newHeight);
       }
       
       
       //The following fxn is when the response provides bbox, we set width and height for the preview
       public void bBoxSetWidthHeight (String boxInStdFormat) {		//boxInStdFormat is 4 comma separated points.
    	 //the bbox format is now min lat (min y), min long (min x), max lat(max y), max long(max x)
    	   
    	   String delims = "[,]+";
    	   String[] tokens = boxInStdFormat.split(delims);
    	   
    	   String llLat = tokens[0];
    	   String llLong = tokens[1];
    	   String urLat = tokens[2];
    	   String urLong = tokens[3];
    	   
    	   Double doublellLong = Double.parseDouble((String) llLong);
    	   Double doublellLat = Double.parseDouble((String) llLat);
    	   Double doubleurLong = Double.parseDouble((String) urLong);
    	   Double doubleurLat = Double.parseDouble((String) urLat);
    	   
    	   //----------   
    	   
    	   long imageWidth = -1;
    	   long imageHeight = -1;
    	   
    	   Coordinate lowerLeft = new Coordinate(doublellLong, doublellLat);	//it should be long, lat as implied in line 56 of GeoUtil.java
		   Coordinate upperRight = new Coordinate(doubleurLong, doubleurLat);   //TODO: debug: In the original code it says positive and negative
    	   
    	   if (GeoUtil.datelineCrossoverOccurs(doublellLong, doubleurLong)) {	//TODO: this part may be wrong cuz lowerLeft and upperRight are not sure
    		   //Construct a bbox: like the getBBox fxn in GeoUtil.java			//this can be the case when we have global image. See MOD09CMG. I have tested it and adjust the image scaler, and it works fine.
    		   
    		   Coordinate[] bbox = new Coordinate[2];
    		   bbox[0] = lowerLeft;
    		   bbox[1] = upperRight;
    		   
    	       imageWidth = Math.round(GeoUtil.getBBoxWidth(bbox) * 13);		//the original scaler is 50
    	       imageHeight = Math.round(GeoUtil.getBBoxHeight(bbox) * 15);		//the original scaler is 50
    	         
    	       
    	   } else {
    	        Envelope env = new Envelope(lowerLeft, upperRight);		//TODO: debug: in the API, it just says p1 and p2, not ll and ur
    	        imageWidth = Math.round(env.getWidth() * 50);
    	        imageHeight = Math.round(env.getHeight() * 50);
    	         
    	   }
    	   
    	   String newWidth = Long.toString(imageWidth);
    	   String newHeight = Long.toString(imageHeight);
    	   
    	   gran.setWidth(newWidth);
    	   gran.setHeight(newHeight);
       }
       
      
}