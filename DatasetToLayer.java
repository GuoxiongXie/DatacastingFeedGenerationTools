import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class DatasetToLayer {
	
    public static void getLayerAndImageFormat (Granule gran, String shortName, String mappingPath) throws Exception {	//this time gran contains metadataLink of the granule
        //Create a "parser factory" for creating SAX parsers
        SAXParserFactory spfac = SAXParserFactory.newInstance();

        //Now use the parser factory to create a SAXParser object
        SAXParser sp = spfac.newSAXParser();

        //Create an instance of ListOfGranulesParser (I created this).
        DatasetToLayerParser handler = new DatasetToLayerParser(gran, shortName);

        //Finally, tell the parser to parse the input and notify the handler
        sp.parse(mappingPath, handler);
       
        
      //return the array storing layer and imageFormat here.
        //return handler.layerImagePackage;
    }

}