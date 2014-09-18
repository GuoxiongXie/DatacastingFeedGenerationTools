/*
 * This is the temporary start point of the program.
 * Currently it takes user's input dataset shortName to perform the search.
 *
 */

public class StartPoint {

    public static void main(String[] args) throws Exception {	//args[0] is shortName, args[1] is itemsPerPage.
    	
    	String shortName;
    	String itemsPerPage;
    	String layerMappingPath;
    	String sysCenterMappingPath;
    	
    	int length = args.length;
    	
    	if (length <= 1) {
    		System.out.println("You need to enter dataset shortName and specify itemsPerPage in the response");
    	}
    	
    	else {
    		shortName = args[0];
    		itemsPerPage = args[1];
    		layerMappingPath = "/home/felixxie/FeedGenerator/GIBS-Layers-to-Dataset";		//TODO: this is the server version
    		//mappingPath = "/Users/felixxie/Documents/workspace/FeedGenerator/src/GIBS-Layers-to-Dataset";		//TODO: JPL mac version
    		
    		sysCenterMappingPath = "/home/felixxie/FeedGenerator/Dataset-to-System-to-dataCenter";
    		
    		
    		//get the system
    		String[] sysCenterArr = DatasetToSystemToDataCenter.getSystemAndDataCenter(shortName,sysCenterMappingPath);
    		String systemName = sysCenterArr[0];
    		String dataCenter = sysCenterArr[1];
    		
    		if (systemName.equalsIgnoreCase("ECHO"))
    			new EchoGranuleRequest(shortName, itemsPerPage, layerMappingPath, dataCenter);		//this call to constructor triggers all the action
    		else if (systemName.equalsIgnoreCase("PODAAC"))
    			new PodaacGranuleRequest(shortName, itemsPerPage, layerMappingPath);	//no dataCenter is needed for PO.DAAC
    		//new systems to be added here
    		
    		System.exit(0);
    	}
    }

}	