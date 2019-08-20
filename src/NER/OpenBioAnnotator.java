package NER;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import util.restcalls;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class OpenBioAnnotator {
	public static String apikey = "396993d0-4ce2-4123-93de-214e9b9ebcf2" ;
	// static String URL = "http://data.bioontology.org/annotator?text=" ;
	//static String URL = "http://data.bioontology.org/annotator?ontologies=MESH&text=" ;
	//static String URL = "http://data.bioontology.org/annotator?whole_word_only=true&exclude_synonyms=true&exclude_numbers=true&ontologies=MESH,SNOMEDCT,DRON,RXNORM,DOID,CHEBI,CHEMINF,DTO,CHEMBIO&text=" ;
	static String URL = "http://data.bioontology.org/annotator?whole_word_only=true&exclude_synonyms=true&exclude_numbers=true&ontologies=MESH,DOID&text=" ;
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		
		//getAnnotations("Melanoma is a malignant tumor of melanocytes which+are found predominantly in skin but also in the bowel and the+eye") ;
		getAvaluation() ;

	}
	
	
	// Annotator function
	// return all concepts within text input 
	public static Map<String, Integer> getAnnotations(String text) throws IOException, ParseException
	{
	 	Map<String, Integer> concepts = new HashMap<String, Integer>();
		String request = URL + URLEncoder.encode(text, "UTF-8") ; ; 
		String jRespond= restcalls.get(request,apikey) ;
		System.out.println(jRespond);
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(jRespond) ;
		JSONArray msgs =  (JSONArray) obj;
		Iterator item = msgs.iterator();

		 // take each value from the json array separately
	    while (item.hasNext()) 
	    {
	    	JSONObject innerObj = (JSONObject) item.next();
	    	JSONArray msg = (JSONArray) innerObj.get("annotations") ;
	    	System.out.println(msg.toString());

	        Iterator iterator = msg.iterator();
	        while (iterator.hasNext()) 
	        {
	        	JSONObject annotinnerObj = (JSONObject) iterator.next();
	            System.out.println(annotinnerObj.get("matchType").toString());
	           // if (annotinnerObj.get("matchType").toString().equals("PREF"))
	            {
	            	concepts.put(annotinnerObj.get("text").toString(), 1) ;
	            }
  
	        }

	    }
	    
	    return concepts ;
		
	}
	
	
	/*************************************************************************************************************************************************************************/
	/************************************************************ Evaluation Part *******************************************************************************************/
	  public static void getAvaluation()
	  {
		  Map<String, List<String>> titles =  ReadCDR_TestSet_BioC()  ; 
		  String result = getmeasure( titles) ;
		  System.out.println(result);
		  
	  }
	
	  public static Map<String, List<String>> ReadCDR_TestSet_BioC() {

		    Map<String, List<String>> goldstandard = null ;
		    goldstandard =  null ;
		    if (goldstandard== null )
		    try {

	       String filename = "F:\\eclipse64\\eclipse\\CDR_TestSet.BioC.xml" ;


		    goldstandard  = new HashMap<String, List<String>>();
			File fXmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	        
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList passageList = doc.getElementsByTagName("passage");
	        int count = 0 ; 
			for (int i = 0; i < passageList.getLength()  ; i++)
			{
				List<String> conclist = new ArrayList<String>() ;
				NodeList childList = passageList.item(i).getChildNodes();
				for (int j = 0; j < childList.getLength(); j++)
				{
					if ("infon".equals(childList.item(j).getNodeName()) && "abstract".equals(childList.item(j).getTextContent()))
					{
						String title = null ; 
						NodeList childList1 = passageList.item(i).getChildNodes();
						for (int kk = 0; kk < childList1.getLength(); kk++)
						{
							 System.out.println(childList1.item(kk).getNodeName());
							if ("text".equals(childList1.item(kk).getNodeName()))
							{
								 System.out.println(childList1.item(kk).getTextContent()
						                    .trim());
								title = childList1.item(kk).getTextContent()
					                    .trim().toLowerCase() ;

								continue ; 
							}
							
							if ("annotation".equals(childList1.item(kk).getNodeName()))
							{
								NodeList childList2 = childList.item(kk).getChildNodes();
								for (int kkk = 0; kkk < childList2.getLength(); kkk++)
								{
									if ("text".equals(childList2.item(kkk).getNodeName()))
									{
										 conclist.add(childList2.item(kkk).getTextContent().trim().toLowerCase()) ;
										 System.out.println(childList2.item(kkk).getTextContent()
								                    .trim());

									}
								}

							}
						}
						
						goldstandard.put(title,conclist) ;
						
					}
				}
	           
			}

		    } 
		    catch (Exception e) {
			e.printStackTrace();
		    }
		    return goldstandard;
		  } ;
	
	public static String getmeasure(Map<String, List<String>> titles)
	{
		

		double avgRecall = 0.0  ; 
		double avgPrecision = 0.0 ;
		double avgFmeasure = 0.0 ; 
		int size_ = titles.size() ;
		int counter = 0 ; 
		for(String title : titles.keySet())
		{
			counter++  ; 
			// get concepts of the title 
			List<String> GoldSndconcepts = titles.get(title); 
			
			try {
				
				
				Map<String, Integer> OBAconcepts = new HashMap<String, Integer>();
				Map<String, Integer> mentions = new HashMap<String, Integer>();
				
				OBAconcepts = getAnnotations(title)  ;


				String[] arr = new String[OBAconcepts.size()] ;
				int i = 0 ; 
				for( String concept : OBAconcepts.keySet())
				{
					arr[i] = concept.toLowerCase() ;
					i++ ; 
				}

				// measure the recall precision and  F-measure 
				double relevent = 0 ;
				for( String concept : arr)
				{
                   if (GoldSndconcepts.contains(concept.toLowerCase()))
                   {
                	   relevent++ ; 
                   }
					
				}
				
				// calculate the Recall 
				//For example for text search on a set of documents recall is the number of correct results divided by the number of results that should have been returned
				double recall = 0.0 ; 
				if (GoldSndconcepts.size() > 0  )
				{
					recall = relevent / GoldSndconcepts.size() ;
				}

				avgRecall = recall  + avgRecall; 
				
				double precision = 0 ; 
				if ( arr.length > 0  )
				   precision = relevent / arr.length ;
				
				 avgPrecision += precision ;	
				
				double Fmeasure  = 0.0 ;
				if (precision + recall > 0 )
				   Fmeasure = 2* ((precision * recall) / (precision + recall)) ;
				   avgFmeasure = Fmeasure + avgFmeasure ;

                
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		avgRecall = avgRecall / titles.size() ;
		avgPrecision = avgPrecision / titles.size() ;
		avgFmeasure = avgFmeasure / titles.size() ;
		
		String result = Double.toString(avgRecall) + " " +  Double.toString(avgPrecision) +" " +  Double.toString(avgFmeasure) ;
		return result ;
	}
	
	

}
