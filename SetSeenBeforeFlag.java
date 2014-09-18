import java.io.File;
 
public class SetSeenBeforeFlag {
 
  public static void setFlag(Granule gran, String shortName, String pattern) {		//pattern is the granule title: AIRS.2013.07.15.110.L2.RetSup.v6.0.7.0.G13196094610.hdf
 
	  gran.isSeenBefore = false;		//default it to false for now, will set it to true when it is found in folders.
	  
	  SetSeenBeforeFlag setter = new SetSeenBeforeFlag();
	  
	  String allFeedRootPath = "/home/felixxie/FeedGenerator/datacasts/";		//TODO: This is the path of server.
	  String oneFeedRootPath = allFeedRootPath.concat(shortName).concat("-feed/");		// datacasts/MOD02QKM-feed/   
	  String XMLfolderPath = oneFeedRootPath.concat("items-xml"); 						// datacasts/MOD02QKM-feed/items-xml
  	
	  //note that the items-xml folder is generated only when publisher is called, so we should check if it exists
	  //if items-xml doesn't even exist, then isSeenBefore should be false; if exists, then search items-xml folder see if the file exists.
	  File XMLfolderFile = new File(XMLfolderPath);
	  
	  if (!XMLfolderFile.exists()) {
		  gran.isSeenBefore = false;
		  return;					//return immediately, dont search.
	  }
	  else {						//items-xml exists, search it recursively for that file
		  setter.searchDirectory(gran, XMLfolderFile, pattern);  
	  }
  }
 
  
  //this is the start point of search
  public void searchDirectory(Granule gran, File directory, String pattern) {		//pattern is the granule title: AIRS.2013.07.15.110.L2.RetSup.v6.0.7.0.G13196094610.hdf
 
	if (directory.isDirectory()) {
	    search(gran, directory, pattern);
	} else {
	    System.out.println(directory.getAbsoluteFile() + " is not a directory!");		//this won't happen
	}
 
  }
  
  
  //this is a recursive call
  private void search(Granule gran, File directory, String pattern) {	
		  	 
	        //do you have permission to read this directory?	
		    if (directory.canRead()) {
		    	for (File temp : directory.listFiles()) {
		    		if (temp.isDirectory()) {
		    			search(gran, temp, pattern);		//recursively check the sub-directory
		    		}
		    		else {			//not directory, then we check if this is the file.
		    			if (temp.getName().contains(pattern)) {
		    				gran.isSeenBefore = true;
		    				return;
		    			}
		    		}
		    	}
	 
		    } else {		//no permission to read file
		    	System.out.println(directory.getAbsoluteFile() + "Permission Denied");
		    }  
  }
	 
}