//package gov.nasa.jpl.datacasting.lance.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class GeoUtil {
   public static boolean datelineCrossoverOccurs(double pLon, double qLon) {	//TODO: what is plon, qlon? Is this for bbox??
      if ((pLon < 0.0 && qLon > 0.0) || (pLon > 0.0 && qLon < 0.0)) {
         double negVal, posVal;
         if (pLon < 0.0) {
            negVal = pLon;
            posVal = qLon;
         } else {
            negVal = qLon;
            posVal = pLon;
         }
         
         if (posVal > (negVal + 180.0))
            return true;
      }
      
      return false;
   }
   
   public static boolean datelineCrossoverOccurs(Coordinate[] polyCoords) {
      for (int i = 0; i < polyCoords.length - 1; i++) {
         if (datelineCrossoverOccurs(polyCoords[i].x, polyCoords[i + 1].x)) {
            return true;
         }
      }
      return false;
   }
   
   public static Coordinate[] getBBox(Coordinate[] polyCoords) {
      List<Double> latitude = new ArrayList<Double>();
      List<Double> positiveLon = new ArrayList<Double>();
      List<Double> negativeLon = new ArrayList<Double>();
      
      Coordinate[] bbox = new Coordinate[2];		//the coordinate size is only 2!!
      for (int i = 0; i < polyCoords.length; i++) {
         if (polyCoords[i].x >= 0) {
            positiveLon.add(polyCoords[i].x);
         } else {
            negativeLon.add(polyCoords[i].x);
         }
         latitude.add(polyCoords[i].y);
      }
      
      Collections.sort(latitude);
      Collections.sort(positiveLon);
      Collections.sort(negativeLon);
      
      Coordinate lowerLeft = new Coordinate(positiveLon.get(0), latitude.get(0));	//lowerLeft is of type Coordinate!!
      Coordinate upperRight = new Coordinate(negativeLon.get(negativeLon.size() - 1), latitude.get(latitude.size() - 1));
      
      bbox[0] = lowerLeft;
      bbox[1] = upperRight;
      
      return bbox;
   }
   
   public static double getBBoxWidth(Coordinate[] bbox) {
      return (180 - bbox[0].x) + (180 - Math.abs(bbox[1].x));
   }
   
   public static double getBBoxHeight(Coordinate[] bbox) {
      return bbox[1].y - bbox[0].y;
   }
}
