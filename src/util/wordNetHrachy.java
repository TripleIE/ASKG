package util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class wordNetHrachy {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//testDictionary() ; 
		getHypernyms("Heart") ; 
	}
	
	public static void testDictionary () throws IOException {

		 // construct the URL to the Wordnet dictionary directory
		// String wnhome = System . getenv (" WNHOME ") ;
		 String path = "C:\\Program Files (x86)\\WordNet\\2.1\\dict"  ; 
		 URL url = new URL ("file", null , path ) ;

		 // construct the dictionary object and open it
		 IDictionary dict = new Dictionary ( url ) ;
		 dict . open () ;
		 // look up first sense of the word "dog "
		 IIndexWord idxWord = dict . getIndexWord ("dog", POS . NOUN ) ;
		 IWordID wordID = idxWord . getWordIDs () . get (0) ;
		 IWord word = dict . getWord ( wordID ) ;
		 System . out . println ("Id = " + wordID ) ;
		 System . out . println (" Lemma = " + word . getLemma () ) ;
		 System . out . println (" Gloss = " + word . getSynset () . getGloss () ) ;
		}
	
	public static List<String> getHypernyms ( String concept) throws IOException {

		List<String> hier = new ArrayList<String>() ;
		
		 String path = "C:\\Program Files (x86)\\WordNet\\2.1\\dict"  ; 
		 URL url = new URL ("file", null , path ) ;

		 // construct the dictionary object and open it
		 IDictionary dict = new Dictionary ( url ) ;
		 dict . open () ;
		 // get the synset
		 IIndexWord idxWord = dict . getIndexWord (concept, POS . NOUN ) ;
		 if (idxWord   == null)
		 {
			 return hier; 
		 }
		 IWordID wordID = idxWord . getWordIDs () . get (0) ; // 1st meaning
		 IWord word = dict . getWord ( wordID ) ;
		 ISynset synset = word . getSynset () ;

		 // get the hypernyms
		 List < ISynsetID > hypernyms =
		 synset . getRelatedSynsets ( Pointer . HYPERNYM ) ;

		 // print out each h y p e r n y m s id and synonyms
		 List < IWord > words ;
		 for( ISynsetID sid : hypernyms ) 
		 {
			 words = dict . getSynset ( sid ) . getWords () ;
			 System . out . print ( sid + " {") ;
			 for( Iterator < IWord > i = words . iterator () ; i . hasNext () ;) 
			 {
				  String entity = i . next () . getLemma ().replace("_", " ") ;
				 	System . out . print ( entity ) ;
				 	hier.add(entity ) ;
				 	break ; 
			 }
			 System . out . println ("}") ;
		 }
		 
		 return hier ;
	}

	public static List<String> getHypernymswithSyn ( String concept) throws IOException {

		List<String> hier = new ArrayList<String>() ;
		
		 String path = "C:\\Program Files (x86)\\WordNet\\2.1\\dict"  ; 
		 URL url = new URL ("file", null , path ) ;

		 // construct the dictionary object and open it
		 IDictionary dict = new Dictionary ( url ) ;
		 dict . open () ;
		 // get the synset
		 IIndexWord idxWord = dict . getIndexWord (concept, POS . NOUN ) ;
		 if (idxWord   == null)
		 {
			 return hier; 
		 }
		 IWordID wordID = idxWord . getWordIDs () . get (0) ; // 1st meaning
		 IWord word = dict . getWord ( wordID ) ;
		 ISynset synset = word . getSynset () ;

		 // get the hypernyms
		 List < ISynsetID > hypernyms =
		 synset . getRelatedSynsets ( Pointer . HYPERNYM ) ;

		 // print out each h y p e r n y m s id and synonyms
		 List < IWord > words ;
		 for( ISynsetID sid : hypernyms ) 
		 {
			 words = dict . getSynset ( sid ) . getWords () ;
			 System . out . print ( sid + " {") ;
			 for( Iterator < IWord > i = words . iterator () ; i . hasNext () ;) 
			 {
				  String entity = i . next () . getLemma ().replace("_", " ") ;
				 	System . out . print ( entity ) ;
				 	hier.add(entity ) ; 
			 }
			 System . out . println ("}") ;
		 }
		 
		 return hier ;
	}		 
		// All dicovered concepts are looked up in Wordnet and a list of all hypernym synsets is assembled, after that we eveluated the result with the gold stnadrd.

}
