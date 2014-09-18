/*
 * This class writes the metadata of an item to a plain text file for digestion.
 *
 */

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DatasetToLayerParser extends DefaultHandler {
	
	private StringBuffer temp;
	
	String shortName;
	boolean isTarget;
	
	
	
	Granule gran;
	String dayNightFlag;
	
	public DatasetToLayerParser (Granule gran, String shortName) {
		this.shortName = shortName;
		this.isTarget = false;
		
		this.gran = gran;		
		dayNightFlag = "DAY";	//default to be day to avoid null case when doing optimization
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
    public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		temp = new StringBuffer();		//flush out the previous content
		
		if (this.isTarget == true) {					//only do things if this is the target dataset (matched shortName)
			if (qName.equalsIgnoreCase("dayNight"))	{	//the dataset maps to day/night layer.
				
				//Optimization: if the granule has been ingested (the item xml is on disk), then we do not query the metadata link again!
	    		
	    		if(this.gran.isSeenBefore == false){			//if folder not exists or file not exists, we query metadataLink to get dayNightFlag. 
	    														//Otherwise, dayNightFlag is DAY (doesn't matter if it's actually night, we won't write item metadata for seenbefore granule anyway).
	    			// make request to the metadataLink and parse it for <DayNightFlag>
	    			DefaultHttpClient httpclient = new DefaultHttpClient();
	    			HttpGet httpGet = new HttpGet(this.gran.metadataLink);
	    			HttpResponse response = null;
	    			try {
	    				response = httpclient.execute(httpGet);
	    			} catch (ClientProtocolException e) {
	    				e.printStackTrace();
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
				
	    			try {
	    				int statusCode = response.getStatusLine().getStatusCode();
		            
	    				//parse the response
		            
	    				if (statusCode == 200 ){			//The request has succeeded.
	    					HttpEntity entity = response.getEntity();
	    					InputStream metadataContent = entity.getContent();

	    					//parse the response
		                
	    					//Create a "parser factory" for creating SAX parsers
	    					SAXParserFactory spfactory = SAXParserFactory.newInstance();

	    					//Now use the parser factory to create a SAXParser object
	    					SAXParser sParser = spfactory.newSAXParser();

	    					//Create an instance of MetadataLinkParser (I created this).
	    					MetadataLinkParser MLhandler = new MetadataLinkParser();

	    					//Finally, tell the parser to parse the input and notify the handler
	    					sParser.parse(metadataContent, MLhandler);
		             
	    					// ensure it is fully consumed
	    					EntityUtils.consume(entity);
		                
		              
	    					this.dayNightFlag = MLhandler.dayNightFlag;		//store the <DayNightFlag>
	    				}
		          
	    			} catch (IllegalStateException e) {
	    				e.printStackTrace();
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			} catch (ParserConfigurationException e) {
	    				e.printStackTrace();
	    			} finally {
	    				httpGet.releaseConnection();
	    			}
	    		}
			}
		}
		
	}
    
    /*
     * When the parser encounters the end of an element, it calls this method
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
   	 
		if (qName.equalsIgnoreCase("name"))	{
			String name = temp.toString();
			if (name.equalsIgnoreCase(shortName))
				isTarget = true;
	   
		} else if (qName.equalsIgnoreCase("singleLayer")) {		//the 1-1 case
			if (isTarget == true)
				this.gran.layerArray.add(temp.toString());
		
		} else if (qName.equalsIgnoreCase("layer")){			// the multiple case
			if (isTarget == true)
				this.gran.layerArray.add(temp.toString());		//will append two or more layers, create custom element for this case.
		} else if (qName.equalsIgnoreCase("Floods")){			// the multiple case
			if (isTarget == true)
				this.gran.floodLayers = temp.toString();
		} else if (qName.equalsIgnoreCase("Fires")){			// the multiple case
			if (isTarget == true)
				this.gran.fireLayers = temp.toString();
		} else if (qName.equalsIgnoreCase("DustStorms")){			// the multiple case
			if (isTarget == true)
				this.gran.dustStormLayers = temp.toString();
		} else if (qName.equalsIgnoreCase("SevereStorms")){			// the multiple case
			if (isTarget == true)
				this.gran.severeStormLayers = temp.toString();
			
		} else if (qName.equalsIgnoreCase("dayLayer")) {
			if (isTarget==true && (dayNightFlag.equalsIgnoreCase("DAY") || dayNightFlag.equalsIgnoreCase("BOTH")))
				this.gran.layerArray.add(temp.toString());
		} else if (qName.equalsIgnoreCase("nightLayer")) {
			if (isTarget==true && dayNightFlag.equalsIgnoreCase("NIGHT"))
				this.gran.layerArray.add(temp.toString());
			
		} else if (qName.equalsIgnoreCase("imageFormat")) {		//valid for all cases like 1-1, multiple and day/night layer
			if (isTarget == true) {
				this.gran.imageFormat = temp.toString();
				this.isTarget = false;		//reset isTarget to avoid finding others
			}
		} else if (qName.equalsIgnoreCase("dataset")) {
			//TODO: need to change in the future. Traverse all the data still can't find a match. Just use true color.
			if (this.gran.layerArray.size()==0 && this.gran.imageFormat==null) {	//can't find a match for the dataset
				this.gran.layerArray.add("MODIS_Terra_CorrectedReflectance_TrueColor");
				this.gran.imageFormat = "image%2Fpng";
			}
			
			//finish finding the corresponding layer(s) and imageFormat. Now write to Item Metadata file using this.gran
			ItemMetadataGenerator.generateItemMetadata(this.gran, this.shortName);
        }
	}
    
}	