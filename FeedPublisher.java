/*
 * This class writes the metadata of an item to a plain text file for digestion.
 *
 */

import java.io.File;
import java.io.IOException;

public class FeedPublisher {

    public static void publishFeed (String shortName) {
    	// all info is contained in gran
	    try{
	    	//System.out.println("publisher is called!!");
	    	String allFeedRootPath = "/home/felixxie/FeedGenerator/datacasts";				//TODO: this is the path of server!!
	    	//String allFeedRootPath = "/Users/felixxie/Documents/workspace/FeedGenerator/src/datacasts";		//TODO: This is the path of JPL Mac. Should manually create datacasts folder. Change it when deploying to server.
	    	//String allFeedRootPath = "/Users/felixxie/Desktop/SummerProject/FeedGenerator/src/datacasts";		//TODO: This is my mac pro folder. when deploy, change this one.
	    	String datasetFeedPath = allFeedRootPath.concat("/").concat(shortName).concat("-feed");
	    	String configFilePath = datasetFeedPath.concat("/config.cfg");
	    	String datasetItemPath = datasetFeedPath.concat("/items");
	    	
	    	
	    	String publishToolPath = "/export/00/www/aeolus/htdocs/datacasting-publishing-tools-3.0.2a";	//TODO: This is path of server!!
	    	//String publishToolPath = "/Users/felixxie/Documents/workspace/FeedGenerator/src/datacasting-publishing-tools-3.1.0";	//TODO: This is the path of JPL Mac.
	    	//String publishToolPath = "/Users/felixxie/Desktop/SummerProject/FeedGenerator/src/datacasting-publishing-tools-3.1.0"; //TODO: This is the path in my Mac pro. when deploy, change this one.
	    	String IngestItemPath = publishToolPath.concat("/IngestItem");
	    	String GenerateFeedPath = publishToolPath.concat("/GenerateFeed");
	    	
	    	
	    	//String cleanDSstoreCommand = "find ".concat(allFeedRootPath).concat(" -name \"*.DS_Store\" -type f -delete"); 
	    	
	    	
	    	File file4DirCreation = new File(datasetItemPath.concat("/"));
	    	//Runtime.getRuntime().exec(cleanDSstoreCommand);		//TODO: may or may not need this on server
	    	
    		if (file4DirCreation.exists() && file4DirCreation.list().length > 0) {		//only when there is items folder and items in items folder, i.e.there's items to be ingested, then perform action.
    			
    			//--------- remove .DS_Store -----------
    			DSStoreRemover.execute(datasetItemPath, ".DS_Store");
    			//--------- end of removing .DS_Store ---------
    			
    			//avoid racing: loop until .DS_Store is deleted
    			//File DSStoreFile = new File(datasetItemPath.concat("/.DS_Store"));
    			//if (DSStoreFile.exists()) {
    			
    			//./IngestItem -c /Users/felixxie/Documents/src/datacasts/MOD02QKM-feed/config.cfg -d /Users/felixxie/Documents/src/datacasts/MOD02QKM-feed/items
    			String ingestCommand = IngestItemPath.concat(" -e --config=").concat(configFilePath).concat(" -d ").concat(datasetItemPath);			//TODO: this is server version of publisher
    			//String ingestCommand = IngestItemPath.concat(" -c ").concat(configFilePath).concat(" -d ").concat(datasetItemPath);			//TODO: this is local version of publisher
    			//Runtime.getRuntime().exec(ingestCommand);
    			
    			//Runtime.getRuntime().exec(cleanDSstoreCommand);		//TODO: may or may not need this on server
    			//String cleanDSstoreXMLCommand = "find ".concat(allFeedRootPath).concat(" -name \"*.DS_Store.xml\" -type f -delete");
    			//Runtime.getRuntime().exec(cleanDSstoreXMLCommand);		//TODO: may or may not need this on server
    			//------------------------
    			Runtime.getRuntime().exec(ingestCommand);
    			//Process p = Runtime.getRuntime().exec(ingestCommand);
    			//Scanner scanner = new Scanner(p.getInputStream());
    		    //while (scanner.hasNext()) {
    		        //System.out.println(scanner.nextLine());
    		    //}
    			//}
    			//----------------------
    			
    			
    			//------------------------------------- GenerateFeed ---------------
    			String itemsXMLpath = datasetFeedPath.concat("/items-xml/");
    			String queuePath = datasetFeedPath.concat("/queue/");
    			File itemsXMLCreation = new File(itemsXMLpath);
    			File queueCreation = new File(queuePath);
    			
    			boolean ready = false;		//this signifies that the ingestion is finished and ready to generate feed.
    			
    			//Runtime.getRuntime().exec(cleanDSstoreXMLCommand);		//TODO: may or may not need this on server
    			//Runtime.getRuntime().exec(cleanDSstoreCommand);		//TODO: may or may not need this on server
    			
    			while (ready == false) {	//loop until items-xml and queue have been created: ingestion finished. This is ok when items folder doesn't have item inside.
    				//System.out.println("hangs here!!");
    				if (itemsXMLCreation.exists() && queueCreation.exists()) {

    					//------ remove the orginal feed if there's one --------
    					//String feed = datasetFeedPath.concat("/").concat(shortName).concat(".xml");
    					//File feedFile = new File(feed);
    					//if (feedFile.exists())
    						//feedFile.delete();
    					//------end of removal--------------------------
    					//
    					
    					//Runtime.getRuntime().exec(cleanDSstoreCommand);
    	    			//Runtime.getRuntime().exec(cleanDSstoreXMLCommand);		//TODO: may or may not need this on server
    					//Runtime.getRuntime().exec(cleanDSstoreCommand);		//TODO: may or may not need this on server
    	    			
    					//System.out.println("goes in here!!");
    	    			//./GenerateFeed -c /Users/felixxie/Desktop/SummerProject/FeedGenerator/src/datacasts/MOD02QKM-feed/config.cfg -r 
    					String generateCommand = GenerateFeedPath.concat(" --config=").concat(configFilePath);		//TODO: this is the server version
    	    			//String generateCommand = GenerateFeedPath.concat(" -c ").concat(configFilePath).concat(" -r");		//TODO: this is the local version
    	    			//System.out.println(generateCommand);
    	    			Runtime.getRuntime().exec(generateCommand);
    	    			//Process np = Runtime.getRuntime().exec(generateCommand);
    	    			//Scanner nscanner = new Scanner(np.getInputStream());
    	    		    //while (nscanner.hasNext()) {
    	    		        //System.out.println(nscanner.nextLine());
    	    		    //}
    	    			
    					ready = true;
    				}
    			}
    			
    		}
    			
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

}	