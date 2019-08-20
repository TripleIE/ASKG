package NER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JList;

import util.ReadXMLFile;
import HRCHY.hierarchy;
import RICH.Enrichment;
import gov.nih.nlm.nls.metamap.ConceptPair;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;


public class umlsMapping {
	public static final int MAPSCORE = -900 ;
	public static final int MINMAPSCORE = -700 ;
	static final int MINThreshhold = 3 ;
	static String SGTChim[] = {"aapp", "antb", "chem", "clnd", "eico", "elii", "enzy", "hops", "horm", "imft", "irda", "inch", "lipd", "nsba", "nnon", "orch", "opco", "phsu", "rcpt", "strd", "vita"} ;
	static String SGTDiso[] = {"acab", "anab",  "comd", "cgab", "dsyn", "emod", "fndg", "inpo", "mobd", "neop", "sosy"} ;
	static String SGTACTI[] = {}; 
    
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		getKeywordAnnotation("diabetes",false,false, false);

	}
	
	// function : getKeywordAnnotation
	// Input : keyword
	// output : true/false
	// the function accepts a key word and returns true if the keyword is concept 

	public static boolean getKeywordAnnotation(String keyword, boolean includeSyn,boolean includeHrchy, boolean useSemanticGroupType) throws Exception
	{
		
	
		MetaMapApi api = new MetaMapApiImpl();
		List<String> theOptions = new ArrayList<String>();
	    theOptions.add("-y");  // turn on Word Sense Disambiguation
	    theOptions.add("-u");  //  unique abrevation 
	    theOptions.add("--negex");  
	    theOptions.add("-v");
	    theOptions.add("-l");
	    theOptions.add("-c");   // use relaxed model that  containing internal syntactic structure, such as conjunction.
	    if (theOptions.size() > 0) {
	      api.setOptions(theOptions);
	    }
	    
	    int resultCode = getconcept(keyword,api,useSemanticGroupType) ;
	    if (resultCode == 1)
	    {
	    	return true ; 
	    }
	    else
	    {
	    	
	    	// map score should meet the min
	    	if (includeSyn && resultCode == MINThreshhold) 
	    	{
		    	// based on  synonyms
		    	Map<String, Integer> getSyn = Enrichment.getSynonyms(keyword); 
		    	for (String syn:getSyn.keySet())
		    	{
		    	    if (getconcept(syn,api,useSemanticGroupType) == 1)
		    	    {
		    	    	return true ; 
		    	    }
		    	}
	    	}
	    	
	    	// map score should meet the min
	    	if (includeHrchy && resultCode == MINThreshhold) 
	    	{
		    	// based on  Hrchy
	    		List<String>  Hrchys = hierarchy.BioHRCHY(keyword); 
		    	for (String hrchy:Hrchys)
		    	{
		    	    if (getconcept(hrchy,api,useSemanticGroupType) == 1)
		    	    {
		    	    	return true ; 
		    	    }
		    	}
	    	}
	    	// based on hierarchy  
	    }

		return false ; 
	}
	
	
	
	
	public static Map<String, Integer> getAnnotations(String text) throws Exception
	{
		
	
		MetaMapApi api = new MetaMapApiImpl();
		List<String> theOptions = new ArrayList<String>();
	    theOptions.add("-y");  // turn on Word Sense Disambiguation
	    theOptions.add("-u");  //  unique abrevation 
	    //theOptions.add("--negex");  
	    theOptions.add("-v");
	    theOptions.add("-l");
	    theOptions.add("-c");   // use relaxed model that  containing internal syntactic structure, such as conjunction.
	    if (theOptions.size() > 0) {
	      api.setOptions(theOptions);
	    }
	    
		// find all concepts that exist in the UMLS
		Map<String, Integer> metmapconcepts = getconcepts(text,api) ;
		return metmapconcepts ;
	}
	
	public static Map<String, String> getconcepts_SemanticGroup(String sentence, MetaMapApi api,JList SGT) throws Exception
	{
		    sentence = sentence.toLowerCase() ;
			
			Map<String, String> semanticgroup = ReadXMLFile.Deserializeddiectionar("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\SemanticGroupDirAbbr.dat") ;
			Map<String, Integer> SemanticGp   = new HashMap<String, Integer>();
			Map<String, String>  concepts = new HashMap<String, String>(); 
			
		 	List<Result> resultList = api.processCitationsFromString(sentence);
		    Result result = resultList.get(0);
		    System.out.println(result);
		    List<String> conceptsIdentified = new ArrayList<String>();
		    List<String> actionsIdentified = new ArrayList<String>();
		    List<Negation> negList = result.getNegations();
		    
		    if (negList.size() > 0)
		    {
		      System.out.println("Negations:");
		      for (Negation e: negList) {
		        System.out.println("type: " + e.getType());
		        System.out.print("Trigger: " + e.getTrigger() + ": [");
		        for (Position pos: e.getTriggerPositionList()) {
		          System.out.print(pos  + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPairs: [");
		        for (ConceptPair pair: e.getConceptPairList()) {
		          System.out.print(pair + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPositionList: [");
		        for (Position pos: e.getConceptPositionList()) {
		          System.out.print(pos + ",");
		        }
		        System.out.println("]");
		      }
		    } else {
		    	System.out.println(" None.");
		    }
		    try {
		    	for (Utterance utterance: result.getUtteranceList()) {
		    		System.out.println("Utterance:");
		    		System.out.println(" Id: " + utterance.getId());
		    		System.out.println(" Utterance text: " + utterance.getString());
		    		System.out.println(" Position: " + utterance.getPosition());
		    		for (PCM pcm: utterance.getPCMList()) {
		    			System.out.println("Phrase:");
		    			  System.out.println(" text: " + pcm.getPhrase().getPhraseText());
		    			  System.out.println("Mappings:");
		    	          for (Mapping map: pcm.getMappingList()) {
		    	            System.out.println(" Map Score: " + map.getScore());
		    	            for (Ev mapEv: map.getEvList()) {
		    	              System.out.println("   Score: " + mapEv.getScore());
		    	              System.out.println("   Concept Id: " + mapEv.getConceptId());
		    	              System.out.println("   Concept Name: " + mapEv.getConceptName());
		    	              System.out.println("   Preferred Name: " + mapEv.getPreferredName());
		    	              System.out.println("   Matched Words: " + mapEv.getMatchedWords());
		    	              System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
		    	              System.out.println("   MatchMap: " + mapEv.getMatchMap());
		    	              System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
		    	              System.out.println("   is Head?: " + mapEv.isHead());
		    	              System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
		    	              System.out.println("   Sources: " + mapEv.getSources());
		    	              System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
		    	              System.out.println("   Negation Info: " + mapEv.getNegationStatus());    	              
		    	              if(mapEv.getNegationStatus() != 1 && mapEv.getScore() <= -700 )
		    	            	  
		    	              {
		    	            	SemanticGp.clear();
		    	          		for (String type:mapEv.getSemanticTypes())
		    	        		{
		    	          			//is it part of the selected list 
		    	        			String group = semanticgroup.get(type); 
		    	        			
		    	        			if (group != null && !group.isEmpty())
		    	        			{
		    	        				SemanticGp.put(group, 1) ;
		    	        			}
		    	          			
		    	        		}
		    	          		
		    	          		String semGroup = "";
		    	          		if(!SemanticGp.isEmpty())
		    	          		{		    	          			
		    	          			for(String gp:SemanticGp.keySet()) 
		    	          			{
		    	          				
			    	          			
			    	          			for(Object type:SGT.getSelectedValuesList())
			    						{
			    							if (type.toString().equals(gp) )
			    							{
			    								semGroup += gp + " " ;
			    							}
			    									
			    						}
		    	          				
		    	          				 
		    	          			}
		    	          			 if (!semGroup.isEmpty())
		    	          				 concepts.put(mapEv.getPreferredName(), semGroup) ;
		    	          		}
		    	          		
			    	            	 
		    	              }
		    	              
		    	              
		    	            }
		    		}
		    	}
				
			}
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return concepts ;
	}
	public static Map<String, String> getconcepts_SemanticGroup(String sentence, MetaMapApi api) throws Exception
	{
		    sentence = sentence.toLowerCase() ;
			
			Map<String, String> semanticgroup = ReadXMLFile.Deserializeddiectionar("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\SemanticGroupDirAbbr.dat") ;
			Map<String, Integer> SemanticGp   = new HashMap<String, Integer>();
			Map<String, String>  concepts = new HashMap<String, String>(); 
			
		 	List<Result> resultList = api.processCitationsFromString(sentence);
		    Result result = resultList.get(0);
		    System.out.println(result);
		    List<String> conceptsIdentified = new ArrayList<String>();
		    List<String> actionsIdentified = new ArrayList<String>();
		    List<Negation> negList = result.getNegations();
		    
		    if (negList.size() > 0)
		    {
		      System.out.println("Negations:");
		      for (Negation e: negList) {
		        System.out.println("type: " + e.getType());
		        System.out.print("Trigger: " + e.getTrigger() + ": [");
		        for (Position pos: e.getTriggerPositionList()) {
		          System.out.print(pos  + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPairs: [");
		        for (ConceptPair pair: e.getConceptPairList()) {
		          System.out.print(pair + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPositionList: [");
		        for (Position pos: e.getConceptPositionList()) {
		          System.out.print(pos + ",");
		        }
		        System.out.println("]");
		      }
		    } else {
		    	System.out.println(" None.");
		    }
		    try {
		    	for (Utterance utterance: result.getUtteranceList()) {
		    		System.out.println("Utterance:");
		    		System.out.println(" Id: " + utterance.getId());
		    		System.out.println(" Utterance text: " + utterance.getString());
		    		System.out.println(" Position: " + utterance.getPosition());
		    		for (PCM pcm: utterance.getPCMList()) {
		    			System.out.println("Phrase:");
		    			  System.out.println(" text: " + pcm.getPhrase().getPhraseText());
		    			  System.out.println("Mappings:");
		    	          for (Mapping map: pcm.getMappingList()) {
		    	            System.out.println(" Map Score: " + map.getScore());
		    	            for (Ev mapEv: map.getEvList()) {
		    	              System.out.println("   Score: " + mapEv.getScore());
		    	              System.out.println("   Concept Id: " + mapEv.getConceptId());
		    	              System.out.println("   Concept Name: " + mapEv.getConceptName());
		    	              System.out.println("   Preferred Name: " + mapEv.getPreferredName());
		    	              System.out.println("   Matched Words: " + mapEv.getMatchedWords());
		    	              System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
		    	              System.out.println("   MatchMap: " + mapEv.getMatchMap());
		    	              System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
		    	              System.out.println("   is Head?: " + mapEv.isHead());
		    	              System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
		    	              System.out.println("   Sources: " + mapEv.getSources());
		    	              System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
		    	              System.out.println("   Negation Info: " + mapEv.getNegationStatus());    	              
		    	              if(mapEv.getNegationStatus() != 1 && mapEv.getScore() <= -700 )
		    	            	  
		    	              {
		    	            	SemanticGp.clear();
		    	          		for (String type:mapEv.getSemanticTypes())
		    	        		{
		    	          			//is it part of the selected list 
		    	        			String group = semanticgroup.get(type); 
		    	        			
		    	        			if (group != null && !group.isEmpty())
		    	        			{
		    	        				SemanticGp.put(group, 1) ;
		    	        			}
		    	          			
		    	        		}
		    	          		
		    	          		String semGroup = "";
		    	          		if(!SemanticGp.isEmpty())
		    	          		{
		    	          			for(String gp:SemanticGp.keySet()) 
		    	          			{
		    	          				semGroup += gp + " " ; 
		    	          			}
		    	          			
		    	          			 concepts.put(mapEv.getPreferredName().toLowerCase(), semGroup) ;
		    	          		}
		    	          		
			    	            	 
		    	              }
		    	              
		    	              
		    	            }
		    		}
		    	}
				
			}
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return concepts ;
	}
	
	public static Map<String, Integer> getconcepts(String sentence, MetaMapApi api) throws Exception
	{
		    sentence = sentence.toLowerCase() ;
			

			Map<String, Integer>  concepts = new HashMap<String, Integer>(); 
			
		 	List<Result> resultList = api.processCitationsFromString(sentence);
		    Result result = resultList.get(0);
		    System.out.println(result);
		    List<String> conceptsIdentified = new ArrayList<String>();
		    List<String> actionsIdentified = new ArrayList<String>();
		    List<Negation> negList = result.getNegations();
		    
		    if (negList.size() > 0)
		    {
		      System.out.println("Negations:");
		      for (Negation e: negList) {
		        System.out.println("type: " + e.getType());
		        System.out.print("Trigger: " + e.getTrigger() + ": [");
		        for (Position pos: e.getTriggerPositionList()) {
		          System.out.print(pos  + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPairs: [");
		        for (ConceptPair pair: e.getConceptPairList()) {
		          System.out.print(pair + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPositionList: [");
		        for (Position pos: e.getConceptPositionList()) {
		          System.out.print(pos + ",");
		        }
		        System.out.println("]");
		      }
		    } else {
		    	System.out.println(" None.");
		    }
		    try {
		    	for (Utterance utterance: result.getUtteranceList()) {
		    		System.out.println("Utterance:");
		    		System.out.println(" Id: " + utterance.getId());
		    		System.out.println(" Utterance text: " + utterance.getString());
		    		System.out.println(" Position: " + utterance.getPosition());
		    		for (PCM pcm: utterance.getPCMList()) {
		    			System.out.println("Phrase:");
		    			  System.out.println(" text: " + pcm.getPhrase().getPhraseText());
		    			  System.out.println("Mappings:");
		    	          for (Mapping map: pcm.getMappingList()) {
		    	            System.out.println(" Map Score: " + map.getScore());
		    	            for (Ev mapEv: map.getEvList()) {
		    	              System.out.println("   Score: " + mapEv.getScore());
		    	              System.out.println("   Concept Id: " + mapEv.getConceptId());
		    	              System.out.println("   Concept Name: " + mapEv.getConceptName());
		    	              System.out.println("   Preferred Name: " + mapEv.getPreferredName());
		    	              System.out.println("   Matched Words: " + mapEv.getMatchedWords());
		    	              System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
		    	              System.out.println("   MatchMap: " + mapEv.getMatchMap());
		    	              System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
		    	              System.out.println("   is Head?: " + mapEv.isHead());
		    	              System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
		    	              System.out.println("   Sources: " + mapEv.getSources());
		    	              System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
		    	              System.out.println("   Negation Info: " + mapEv.getNegationStatus());    	              
		    	              if(mapEv.getNegationStatus() != 1 && mapEv.getScore() <= -700 )
		    	            	  
		    	              {
		    	            	  Boolean found = false ; 
		    	            	  for (String stg: SGTChim)
		    	            	  {
		    	            		if (mapEv.getSemanticTypes().contains(stg)) 
		    	            		{
		    	            			found  = true ; 
		    	            			break ; 
		    	            		}
		    	            	  }
		    	            	  
		    	            	  if(!found){
			    	            	  for (String stg: SGTDiso)
			    	            	  {
			    	            		if (mapEv.getSemanticTypes().contains(stg)) 
			    	            		{
			    	            			found  = true ; 
			    	            			break ; 
			    	            		}
			    	            	  }
		    	            	  }
		    	            	  
		    	            	  if (found)
		    	            	  {
			    	            	  List<String> s = mapEv.getMatchedWords() ; //mapEv.getConceptName(); 
			    	            	  Position pos =  mapEv.getPositionalInfo().get(0) ;
			    	            	  String entity = sentence.substring(pos.getX(), pos.getY()+pos.getX()) ;
			    	            	  conceptsIdentified.add(entity .toLowerCase());
		    	            	  }
		    	              }
		    	              
		    	              
		    	            }
		    		}
		    	}
				
			}
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    for(int i = 0; i< conceptsIdentified.size(); i++)
	        {
	      	  System.out.println(conceptsIdentified.get(i));
	      	  concepts.put(conceptsIdentified.get(i).toLowerCase(), 1) ;
	        }
		    
		    return concepts ;
	}
	

	
	public static int  getconcept(String keyword, MetaMapApi api,boolean useSemanticGroupType) throws Exception
	{
		keyword = keyword.toLowerCase() ;
			

			Map<String, Integer>  concepts = new HashMap<String, Integer>(); 
			
		 	List<Result> resultList = api.processCitationsFromString(keyword);
		    Result result = resultList.get(0);
		    System.out.println(result);
		    List<String> conceptsIdentified = new ArrayList<String>();
		    List<String> actionsIdentified = new ArrayList<String>();
		    List<Negation> negList = result.getNegations();
		    int found = 0 ; 
		    
		    if (negList.size() > 0)
		    {
		      System.out.println("Negations:");
		      for (Negation e: negList) {
		        System.out.println("type: " + e.getType());
		        System.out.print("Trigger: " + e.getTrigger() + ": [");
		        for (Position pos: e.getTriggerPositionList()) {
		          System.out.print(pos  + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPairs: [");
		        for (ConceptPair pair: e.getConceptPairList()) {
		          System.out.print(pair + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPositionList: [");
		        for (Position pos: e.getConceptPositionList()) {
		          System.out.print(pos + ",");
		        }
		        System.out.println("]");
		      }
		    } else {
		    	System.out.println(" None.");
		    }
		    try {
		    	for (Utterance utterance: result.getUtteranceList()) {
		    		System.out.println("Utterance:");
		    		System.out.println(" Id: " + utterance.getId());
		    		System.out.println(" Utterance text: " + utterance.getString());
		    		System.out.println(" Position: " + utterance.getPosition());
		    		for (PCM pcm: utterance.getPCMList()) {
		    			System.out.println("Phrase:");
		    			  System.out.println(" text: " + pcm.getPhrase().getPhraseText());
		    			  System.out.println("Mappings:");
		    	          for (Mapping map: pcm.getMappingList()) {
		    	            System.out.println(" Map Score: " + map.getScore());
		    	            int phraseScore = map.getScore() ;
		    	            if (phraseScore <= MAPSCORE )
		    	            {
			    	            for (Ev mapEv: map.getEvList())
			    	            {
			    	              System.out.println("   Score: " + mapEv.getScore());
			    	              System.out.println("   Concept Id: " + mapEv.getConceptId());
			    	              System.out.println("   Concept Name: " + mapEv.getConceptName());
			    	              System.out.println("   Preferred Name: " + mapEv.getPreferredName());
			    	              System.out.println("   Matched Words: " + mapEv.getMatchedWords());
			    	              System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
			    	              System.out.println("   MatchMap: " + mapEv.getMatchMap());
			    	              System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
			    	              System.out.println("   is Head?: " + mapEv.isHead());
			    	              System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
			    	              System.out.println("   Sources: " + mapEv.getSources());
			    	              System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
			    	              System.out.println("   Negation Info: " + mapEv.getNegationStatus());    	              
			    	              if(mapEv.getNegationStatus() != 1 && mapEv.getScore() <= -700 )
			    	            	  
			    	              {
			    	            	  if (useSemanticGroupType)
			    	            	  {
				    	            	  found = 0 ; 
				    	            	  for (String stg: SGTChim)
				    	            	  {
				    	            		if (mapEv.getSemanticTypes().contains(stg)) 
				    	            		{
				    	            			found  = 1 ; 
				    	            			break ; 
				    	            		}
				    	            	  }
				    	            	  
				    	            	  if(found == 0 ){
					    	            	  for (String stg: SGTDiso)
					    	            	  {
					    	            		if (mapEv.getSemanticTypes().contains(stg)) 
					    	            		{
					    	            			found  = 1 ; 
					    	            			break ; 
					    	            		}
					    	            	  }
				    	            	  }
			    	            	  }
			    	            	  else
			    	            	  {
			    	            		  found = 1 ; 
			    	            	  }
			    	            	  
			    	              }
			    	              
			    	              
			    	            }
		    	            }
		    	            else
		    	            {
		    	            	if (phraseScore <= MINMAPSCORE )
		    	            	{
		    	            		found  = 3 ; 
		    	            	}
		    	            }
		    		}
		    	}
				
			}
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    for(int i = 0; i< conceptsIdentified.size(); i++)
	        {
	      	  System.out.println(conceptsIdentified.get(i));
	        }
		   
		    return found ;
	}
	
	
	
	public static Map<String, String> getNegconcepts(String sentence, MetaMapApi api) throws Exception
	{
		    sentence = sentence.toLowerCase() ;
			
			Map<String, String> semanticgroup = ReadXMLFile.Deserializeddiectionar("X:\\KG_ANR\\KGraph\\SemanticGroupDirKG.xml") ;
			Map<String, Integer> SemanticGp   = new HashMap<String, Integer>();
			Map<String, String>  concepts = new HashMap<String, String>(); 
			
		 	List<Result> resultList = api.processCitationsFromString(sentence);
		    Result result = resultList.get(0);
		    System.out.println(result);
		    List<String> conceptsIdentified = new ArrayList<String>();
		    List<String> actionsIdentified = new ArrayList<String>();
		    List<Negation> negList = result.getNegations();
		    
		    if (negList.size() > 0)
		    {
		      System.out.println("Negations:");
		      for (Negation e: negList) {
		        System.out.println("type: " + e.getType());
		        System.out.print("Trigger: " + e.getTrigger() + ": [");
		        for (Position pos: e.getTriggerPositionList()) {
		          System.out.print(pos  + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPairs: [");
		        for (ConceptPair pair: e.getConceptPairList()) {
		          System.out.print(pair + ",");
		        }
		        System.out.println("]");
		        System.out.print("ConceptPositionList: [");
		        for (Position pos: e.getConceptPositionList()) {
		          System.out.print(pos + ",");
		        }
		        System.out.println("]");
		      }
		    } else {
		    	System.out.println(" None.");
		    }
		    try {
		    	for (Utterance utterance: result.getUtteranceList()) {
		    		System.out.println("Utterance:");
		    		System.out.println(" Id: " + utterance.getId());
		    		System.out.println(" Utterance text: " + utterance.getString());
		    		System.out.println(" Position: " + utterance.getPosition());
		    		for (PCM pcm: utterance.getPCMList()) {
		    			System.out.println("Phrase:");
		    			  System.out.println(" text: " + pcm.getPhrase().getPhraseText());
		    			  System.out.println("Mappings:");
		    	          for (Mapping map: pcm.getMappingList()) {
		    	            System.out.println(" Map Score: " + map.getScore());
		    	            for (Ev mapEv: map.getEvList()) {
		    	              System.out.println("   Score: " + mapEv.getScore());
		    	              System.out.println("   Concept Id: " + mapEv.getConceptId());
		    	              System.out.println("   Concept Name: " + mapEv.getConceptName());
		    	              System.out.println("   Preferred Name: " + mapEv.getPreferredName());
		    	              System.out.println("   Matched Words: " + mapEv.getMatchedWords());
		    	              System.out.println("   Semantic Types: " + mapEv.getSemanticTypes());
		    	              System.out.println("   MatchMap: " + mapEv.getMatchMap());
		    	              System.out.println("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
		    	              System.out.println("   is Head?: " + mapEv.isHead());
		    	              System.out.println("   is Overmatch?: " + mapEv.isOvermatch());
		    	              System.out.println("   Sources: " + mapEv.getSources());
		    	              System.out.println("   Positional Info: " + mapEv.getPositionalInfo());
		    	              System.out.println("   Negation Info: " + mapEv.getNegationStatus());    
		    	              
		    	              // Neg concepts
		    	              if(mapEv.getNegationStatus() == 1 && mapEv.getScore() <= -700 )
		    	            	  
		    	              {
/*		    	            	SemanticGp.clear();
		    	          		for (String type:mapEv.getSemanticTypes())
		    	        		{
		    	          			//is it part of the selected list 
		    	        			String group = semanticgroup.get(type); 
		    	        			
		    	        			if (group != null && !group.isEmpty())
		    	        			{
		    	        				SemanticGp.put(group, 1) ;
		    	        			}
		    	          			
		    	        		}
		    	          		
		    	          		String semGroup = "";
		    	          		if(!SemanticGp.isEmpty())
		    	          		{
		    	          			for(String gp:SemanticGp.keySet()) 
		    	          			{
		    	          				semGroup += gp + " " ; 
		    	          			}
		    	          			
		    	          			for (String matchword : mapEv.getMatchedWords())
		    	          			{
		    	          			
		    	          			 concepts.put(matchword.toLowerCase(), semGroup) ;
		    	          			}
		    	          		}*/
		    	          		

		    	          			
		    	          			concepts.put(mapEv.getConceptName(), null) ;
	    		          			for (String matchword : mapEv.getMatchedWords())
		    	          			{
		    	          			
		    	          			 concepts.put(matchword.toLowerCase(), null) ;
		    	          			}

			    	            	 
		    	              }
		    	              
		    	              
		    	            }
		    		}
		    	}
				
			}
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    return concepts ;
	}
	
	
}
