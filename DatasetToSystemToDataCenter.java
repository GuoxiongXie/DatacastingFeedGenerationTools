import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class DatasetToSystemToDataCenter {
	
    public static String[] getSystemAndDataCenter (String shortName, String sysCenterMappingPath) throws Exception {
        //Create a "parser factory" for creating SAX parsers
        SAXParserFactory spfac = SAXParserFactory.newInstance();

        //Now use the parser factory to create a SAXParser object
        SAXParser sp = spfac.newSAXParser();

        //Create an instance of ListOfGranulesParser (I created this).
        DatasetToSystemToDataCenterParser handler = new DatasetToSystemToDataCenterParser(shortName);

        //Finally, tell the parser to parse the input and notify the handler
        sp.parse(sysCenterMappingPath, handler);
       
        
      //return the array storing system and dataCenter here.
        return handler.sysCenterPackage;
    }

}