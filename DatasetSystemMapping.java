/*
 * This class keeps a table maps a dataset shortName to System like ECHO and PO.DAAC
 * This is used for distinguishing the system when making request to perform data discovery.
 * If there's no match in the table, the dataset will default to be ECHO.
 * 
 */

import java.util.Hashtable;

public class DatasetSystemMapping {
	
	//private static Hashtable<String, Integer> granuleSeenBefore = new Hashtable<String, Integer>();
	public static Hashtable<String, String> datasetSysTable;		//key is shortName, value is system name

    public static String getSystem(String shortName) {		//will return the name of the system such as PO.DAAC and ECHO
    	
    	//construct the Hashtable; note that ECHO won't be added to the table; if dataset is not found in table, then default to be ECHO
    	datasetSysTable = new Hashtable<String, String>();
    	
    	datasetSysTable.put("JPL-L2P-MODIS_A", "PO.DAAC");
    	datasetSysTable.put("JPL-L2P-MODIS_T", "PO.DAAC");
    	//new entries to be added here
    	
    	//performs lookup
    	if (datasetSysTable.containsKey(shortName))
    		return datasetSysTable.get(shortName);		//returns the system
    	else
    		return "ECHO";			//if not found, default to be ECHO
    }
}	