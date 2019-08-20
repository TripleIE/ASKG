
package util;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.text.similarity.LevenshteinDetailedDistance;


public class TextMatching {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		Soundex sxmatching = new Soundex() ;
		
        String name1 = "beer";
        String name2 = "bear";
        String name3 = "bearer";
         
        System.out.println(sxmatching.encode(name1));
        System.out.println(sxmatching.encode(name2));
        System.out.println(sxmatching.encode(name3));
        
        
        LevenshteinDetailedDistance LDD = new LevenshteinDetailedDistance() ; 
        System.out.println(LDD.apply("bear", "beer" ));
        System.out.println(LDD.apply("bearer","beer"));


	}
	
	

}
