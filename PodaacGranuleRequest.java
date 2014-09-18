/*
 * 
 *
 */


import java.io.InputStream;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class PodaacGranuleRequest {
	
	int cursor;		//keep track of the page number of the feed, used when page through the feed, we'll increment it (that's why to keep it an int). 
	String timeFrame;	//one timeFrame per request
	
	
	String shortName;
	String itemsPerPage;
	String mappingPath;
	
	int numPageRemaining;	//this is for paging through the response.
	
	
	/* Constructor */
    public PodaacGranuleRequest(String shortName, String itemsPerPage, String mappingPath) throws Exception {
    	
		/* EchoGranuleRequest Constructor */
		cursor = 0;			//TODO: PO.DAAC index starts from 0.
		String cursorStr = Integer.toString(cursor);
		
		this.shortName = shortName;
		this.itemsPerPage = itemsPerPage;
		this.mappingPath = mappingPath;
		
		String serverAddress = "http://podaac.jpl.nasa.gov/ws/search/granule/?startIndex=".concat(cursorStr).concat("&itemsPerPage=").concat(itemsPerPage).concat("&format=atom").concat("&shortName=");
		
		//construct the time frame: 24 hours ago until now
		this.timeFrame = getTimeFrame();				//&startTime=2013-06-30T12:15:01Z&endTime=2013-07-01T12:15:01Z		//TODO: uncomment this line and comment the line below it!!
		//this.timeFrame = "&startTime=2013-07-18T07:00:00Z";		//This line can be used for testing the output against the operational feed.
		
		
		String requestStr = serverAddress.concat(shortName).concat(timeFrame);
		// http://podaac.jpl.nasa.gov/ws/search/granule/?startIndex=0&itemsPerPage=20&startTime=2013-07-23T12:21:00Z&format=atom&shortName=JPL-L2P-MODIS_T
		//System.out.println(requestStr);
		
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(requestStr);
        
        //send out request once, then check how many times it needs to call.
        String totalResults = executeRequest(httpclient, httpGet);
        
        if (!totalResults.equalsIgnoreCase("Connection Error!")) {		//the number returned
        	int numResults = Integer.parseInt(totalResults);
        	int entriesPerPage = Integer.parseInt(this.itemsPerPage);
        	
        	double divide = (double) numResults/entriesPerPage;
        	int numPage =  (int) Math.ceil(divide);
        	
        	this.numPageRemaining = numPage - 1;		//we have requested once
        	
        	//paging through the response, send more requests
        	String newCursorStr;
        	String addr;
        	DefaultHttpClient newHttpclient;
        	HttpGet newHttpGet;
        	for (int i = 1; i <= numPageRemaining; i++){		//automatically handles the 0 entries case
        		this.cursor = this.cursor + 1;		//turn the page
        		newCursorStr = Integer.toString(cursor);
        		
        		//	   "http://podaac.jpl.nasa.gov/ws/search/granule/?startIndex=".concat(cursorStr).concat("&itemsPerPage=").concat(itemsPerPage).concat("&format=atom").concat("&shortName=");
        		addr = "http://podaac.jpl.nasa.gov/ws/search/granule/?startIndex=".concat(newCursorStr).concat("&itemsPerPage=").concat(itemsPerPage).concat("&format=atom").concat("&shortName=").concat(shortName).concat(timeFrame);
        	
        		newHttpclient = new DefaultHttpClient();
                newHttpGet = new HttpGet(addr);
                
                executeRequest(newHttpclient, newHttpGet);		//sends out request; the subsequent return values are ignored.
        	}
        }
	}


	public String executeRequest(DefaultHttpClient httpclient, HttpGet httpGet) throws Exception {
    
        HttpResponse response = httpclient.execute(httpGet);

        // The underlying HTTP connection is still held by the response object 
        // to allow the response content to be streamed directly from the network socket. 
        // In order to ensure correct deallocation of system resources 
        // the user MUST either fully consume the response content  or abort request 
        // execution by calling HttpGet#releaseConnection().

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            
            //parse the response
            
            if (statusCode == 200 ){			//The request has succeeded.
                HttpEntity entity = response.getEntity();
                InputStream XMLcontent = entity.getContent();
                //System.out.println(XMLcontent);
                //parse the response
                
                //Create a "parser factory" for creating SAX parsers
                SAXParserFactory spfac = SAXParserFactory.newInstance();

                //Now use the parser factory to create a SAXParser object
                SAXParser sp = spfac.newSAXParser();

                //Create an instance of ListOfGranulesParser (I created this).
                PodaacListOfGranulesParser handler = new PodaacListOfGranulesParser(this.shortName, this.itemsPerPage, this.mappingPath);

                //Finally, tell the parser to parse the input and notify the handler
                sp.parse(XMLcontent, handler);
               
             
                // ensure it is fully consumed
                EntityUtils.consume(entity);
                
              //return the number of page thru here
                return handler.totalResults;
            }
            
            else {
            	return "Connection Error!";
            }
          
        } finally {
            httpGet.releaseConnection();
        }
    }
    
    
    public String getTimeFrame() {
    /* this function gets the time now and the time 24 hours ago,
     * the output is a string "&startTime=2013-06-30T12:15:01Z&endTime=2013-07-01T12:15:01Z"
     */
    	//get today
        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        String HMS = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());		//hour minute second
        String today = date.concat("T").concat(HMS).concat("Z");
        
        //get 24 hours ago:
        Date todate = new Date();
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(todate);
	    cal.add(Calendar.DAY_OF_MONTH, -1);
        
	    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	    String yesterdayYMD = format1.format(cal.getTime());
	    
	    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
	    String yesterdayHMS = format2.format(cal.getTime());
	    
	    String yesterday = yesterdayYMD.concat("T").concat(yesterdayHMS).concat("Z");
	    
	    String timeFrame = "&startTime=".concat(yesterday).concat("&endTime=").concat(today);
	    return timeFrame;		//&startTime=2013-06-30T12:15:01Z&endTime=2013-07-01T12:15:01Z
    }

}
