/* This class parse the MetadataLink of an granule to determine
 * if the layer is day/night mode. 
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetadataLinkParser extends DefaultHandler {
	private StringBuffer temp;
	public String dayNightFlag;
	
	/*
     * When the parser encounters plain text (not XML elements),
     * it calls(this method, which accumulates them in a string buffer
     */
    public void characters(char[] buffer, int start, int length) {
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
		if (qName.equalsIgnoreCase("DayNightFlag"))	{
			this.dayNightFlag = temp.toString();
		}
	}
}