/* Mazin */
package util;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

public class NGramAnalyzer  {

	    public static List<String> ngrams(int n, String str) {
	        List<String> ngrams = new ArrayList<String>();
	        String[] words = str.split("\\W+");
	        for (int i = 0; i < words.length - n + 1; i++)
	            ngrams.add(concat(words, i, i+n));
	        return ngrams;
	       
	    }

	    public static String concat(String[] words, int start, int end) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = start; i < end; i++)
	            sb.append((i > start ? " " : "") + words[i]);
	        return sb.toString();
	    }

	    public static void main(String[] args) {
	    	entities(2, 3, "This is my-mazin, car.");
	    }
	    
	    public static Map<String, Integer> entities(int min, int max, String sentance ) {
	    	Map<String, Integer> entity = new HashMap<String, Integer>();
			 // Get  entities 
	    	String[] words = sentance.split("\\W+");
						
			if(words.length < max  )
				max = words.length ;
				
	        for (int n = min; n <=max; n++) {
	            for (String ngram : ngrams(n, sentance)){
	                System.out.println(ngram);
	                if ( ngram.length() > 2 && !StringUtils.isNumeric(ngram))
	            	entity.put(ngram,1)  ;
	            }
	            System.out.println();
	        }
	        
	        return   entity ;
	    }
	}
