/*
 * This class writes the metadata of an item to a plain text file for digestion.
 *
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DatasetToSystemToDataCenterParser extends DefaultHandler {
	
	private StringBuffer temp;
	
	String shortName;
	boolean isTarget;
	
	//int[] myIntArray = new int[3];
	String [] sysCenterPackage;
	
	public DatasetToSystemToDataCenterParser (String shortName) {
		this.shortName = shortName;
		this.isTarget = false;
		
		this.sysCenterPackage = new String[2];	//0 is system, 1 is dataCenter
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
	}
    
    /*
     * When the parser encounters the end of an element, it calls this method
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
   	 
		if (qName.equalsIgnoreCase("name"))	{
			String name = temp.toString();
			if (name.equalsIgnoreCase(shortName))
				isTarget = true;
	   
		} else if (qName.equalsIgnoreCase("system")) {		//the 1-1 case
			if (isTarget == true)
				this.sysCenterPackage[0] = temp.toString();	//store system
		
		} else if (qName.equalsIgnoreCase("dataCenter")){			// the multiple case
			if (isTarget == true)
				this.sysCenterPackage[1] = temp.toString();	//store data center
				this.isTarget = false;		//reset isTarget to avoid finding others
			
		}
	}
    
}