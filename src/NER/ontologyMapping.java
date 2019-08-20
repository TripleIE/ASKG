package NER;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import NLP.preProcessing;
import RICH.Enrichment;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import util.NGramAnalyzer;
import util.bioportal;
import util.removestopwords;

public class ontologyMapping {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		//getKeywordAnnotation("diabetes") ;
		getKeywordSynAnnotation("diabetes") ;
		//getKeywordAnsAnnotation("diabetes") ;
	}
	
/*	public static Map<String, Integer> getmeasureLODconcepts(String Text)
	{
		
		Map<String, Integer> lodconcepts = new HashMap<String, Integer>();
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		
		try 
		{
				
      			mentions = NGramAnalyzer.entities(1,3, Text) ;
				
				for(String concept : mentions.keySet())
				{
					// no need to examine the stopwords
					if (!concept.isEmpty()  && !removestopwords.removestopwordsingle(concept.trim()) && EntityMentionDetectionSTLLD(concept) ) 
						lodconcepts.put(concept, 1) ;
				}

			                
			} 
		catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return lodconcepts ;
	}*/
	
	
	public static Map<String, Integer> getAnnotationBioP(Map<String, Integer> keywords) throws IOException
	{

		Map<String, Integer> concepts = new HashMap<String, Integer>();
		for(String keyword: keywords.keySet())
		{
			if (bioportal.isConcept(keyword) )
			{
				concepts.put(keyword, 1) ;
			}		
		}
		return concepts ; 
	}
	
	public static Map<String, Integer> getAnnotation(Map<String, Integer> keywords) throws IOException
	{

		Map<String, Integer> concepts = new HashMap<String, Integer>();
		String group = null ; 
		for(String keyword: keywords.keySet())
		{
			if (getKeywordAnnotation(keyword) )
			{
				concepts.put(keyword, 1) ;

			}
/*			else if (getKeywordSynAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}
			else if (getKeywordAnsAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}*/
			
		}
		return concepts ; 
	}
	
	public static Map<String, Integer> getAnnotation(Map<String, Integer> keywords, List<String>  listSG) throws IOException
	{

		Map<String, Integer> concepts = new HashMap<String, Integer>();
		String group = null ; 
		for(String keyword: keywords.keySet())
		{
			if (getKeywordAnnotation(keyword) )
			{
				if ( (group= getSemanticGroup(keyword,listSG)) != null  )
				{
					concepts.put(keyword, 1) ;
				}
			}
/*			else if (getKeywordSynAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}
			else if (getKeywordAnsAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}*/
			
		}
		return concepts ; 
	}
	
	public static Map<String, String> getAnnotationSG(Map<String, Integer> keywords, List<String>  listSG) throws IOException
	{

		Map<String, String> concepts = new HashMap<String, String>();
		String group = null ; 
		for(String keyword: keywords.keySet())
		{
			if (getKeywordAnnotation(keyword) )
			{
				if ( (group= getSemanticGroup(keyword,listSG)) != null  )
				{
					concepts.put(keyword.toLowerCase(), group) ;
				}
			}
/*			else if (getKeywordSynAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}
			else if (getKeywordAnsAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}*/
			
		}
		return concepts ; 
	}
	
	public static Map<String, String> getAnnotation(Map<String, Integer> keywords, JList SGT) throws IOException
	{

		Map<String, String> concepts = new HashMap<String, String>();
		String group = null ; 
		for(String keyword: keywords.keySet())
		{
			if (getKeywordAnnotation(keyword) )
			{
				if ( (group= getSemanticGroup(keyword,SGT)) != null  )
				{
					concepts.put(keyword, group) ;
				}
			}
/*			else if (getKeywordSynAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}
			else if (getKeywordAnsAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, 1) ;
				}
			}*/
			
		}
		return concepts ; 
	}
	public static Map<String, String> getAnnotationWSemanticType(Map<String, Integer> keywords, JList SGTypelist) throws IOException
	{
		String semanticType = null ; 
		Map<String, String> concepts = new HashMap<String, String>();
		String resource ;
		for(String keyword: keywords.keySet())
		{
			if ((resource = getKeywordAnnotationURI(keyword))!= null )
			{
				if ((semanticType = ontologyMapping.getSemanticGroupType(keyword,SGTypelist)) != null )
				{
					concepts.put(keyword, semanticType + "," + resource) ;
				}
			}
			else if (getKeywordSynAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, null) ;
				}
			}
			else if (getKeywordAnsAnnotation(keyword))
			{
				if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
				{
					concepts.put(keyword, null) ;
				}
			}
			
		}
		return concepts ; 
	}
	public static Boolean getKeywordAnnotation(String mention)
	{
		
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
				"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
		        "select ?entity where" +
			    "{ " +
		                   "?entity ?predicate" +  " \"" +   mention +  "\". "   +
		                   " {?entity rdf:type skos:Concept } UNION {?entity rdf:type rdfs:Class } UNION {?entity rdfs:subPropertyOf ?Class }.  " + 
			        " } LIMIT 1"  ;
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
				return true ;
		         
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return false;
		
	}
	public static String getKeywordAnnotationURI(String mention)
	{
		
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
				"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
		        "select ?entity where" +
			    "{ " +
		                   "?entity ?predicate" +  " \"" +   mention +  "\". "   +
		                   " {?entity rdf:type skos:Concept } UNION {?entity rdf:type rdfs:Class } UNION {?entity rdfs:subPropertyOf ?Class }.  " + 
			        " } LIMIT 1"  ;
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         RDFNode   object    = soln.get("entity");      // get the object
				return object.toString() ;
		         
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return null;
		
	}
	public static Boolean getKeywordSynAnnotation(String mention)
	{
		// get the synonyms from rich module 
		 List<String>  listSyn  = Enrichment.Synonyms(mention) ; 
		 
        for (String syn :listSyn  )
        {
			String queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity where" +
				    "{ " +
			                   "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                   " {?entity rdf:type skos:Concept } UNION {?entity rdf:type rdfs:Class } UNION {?entity rdfs:subPropertyOf ?Class }.  " + 
				        " } LIMIT 1"  ;
			
	
			
			// now creating query object
			try
			{
				Query query = QueryFactory.create(queryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
				ResultSet results ;
				qexec.setTimeout(30000);
				results = qexec.execSelect(); 
				for (; results.hasNext();) 
				{
					return true ;
			         
			    }
			}
			catch(QueryParseException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
        }
		
		
		return false;
		
	}
	
	public static Boolean getKeywordAnsAnnotation(String mention) throws IOException
	{
		// get the synonyms from rich module 
		 List<String>  listSyn  = Enrichment.LLDHierarchy(mention) ; 
		 
        for (String syn :listSyn  )
        {
			String queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity where" +
				    "{ " +
			                   "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                   " {?entity rdf:type skos:Concept } UNION {?entity rdf:type rdfs:Class } UNION {?entity rdfs:subPropertyOf ?Class }.  " + 
				        " } LIMIT 1"  ;
			
	
			
			// now creating query object
			try
			{
				Query query = QueryFactory.create(queryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
				ResultSet results ;
				qexec.setTimeout(30000);
				results = qexec.execSelect(); 
				for (; results.hasNext();) 
				{
					return true ;
			         
			    }
			}
			catch(QueryParseException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
        }
		
		
		return false;
		
	}
	public static Boolean getSemanticGroupTypeDISO_CHEM(String mention)
	{
		
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
				"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
		        "select ?entity  where" +
			    "{ " +
		                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
		                 // "?entity rdf:type skos:Concept." + 
		                  "?entity rdf:type ?st." +
		                  "{ ?st calbc:inGroup calbc-group:DISO} UNION { ?st calbc:inGroup calbc-group:CHEM}" +
		        " } LIMIT 1"  ;
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
				return true ;
		         
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return false;
		
	}
	
	public static String getSemanticGroup(String mention, JList sGTypelist)
	{
	    int flag = 0 ; 	
	    String group = "" ;
	    Boolean first = true ; 
        for(Object type:sGTypelist.getSelectedValuesList())
        {
        	
        	if(type.toString().equals("Activities & Behaviors"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:ACTI}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:ACTI}" ;
        		first = false ; 
        	}
        	if(type.toString().equals("Anatomy"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:ANAT}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:ANAT}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Chemicals or Drugs"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:CHEM}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:CHEM}" ;
        		first = false ;
        		first = false ;
        	}
        	if(type.toString().equals("Concepts & Ideas"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:CONC}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:CONC}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Devices"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:DEVI}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:DEVI}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Disorders"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:DISO}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:DISO}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Genes & Molecular Sequences"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:GENE}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:GENE}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Geographic Areas"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:GEOG}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:GEOG}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Living Beings"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:LIVB}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:LIVB}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Objects"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:OBJC}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:OBJC}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Occupations"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:OCCU}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:OCCU}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Organizations"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:ORGA}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:ORGA}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Phenomena"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:PHEN}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:PHEN}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Physiology"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:PHYS}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:PHYS}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Procedures"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:PROC}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:PROC}" ;
        		first = false ;
        	}
        	
        }
        String queryString ; 
        {
			queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity  ?st where" +
				    "{ " +
			                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                 // "?entity rdf:type skos:Concept." + 
			                  "?entity rdf:type ?st." +
			                  group +
			        " } LIMIT 1"  ;
        }
       
        	
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
		
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         RDFNode   object    = soln.get("st");      // get the object
		         
				 return getSemanticGroupTypelabel(object.toString()) ;
		          
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return null;
		
	}
	public static String getSemanticGroup(String mention, List<String>  listSG)
	{
	    int flag = 0 ; 	
	    String group = "" ;
	    Boolean first = true ; 
        for(Object type:listSG)
        {
        	
        	if(type.toString().equals("Activities & Behaviors"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:ACTI}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:ACTI}" ;
        		first = false ; 
        	}
        	if(type.toString().equals("Anatomy"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:ANAT}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:ANAT}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Chemicals or Drugs"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:CHEM}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:CHEM}" ;
        		first = false ;
        		first = false ;
        	}
        	if(type.toString().equals("Concepts & Ideas"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:CONC}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:CONC}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Devices"))
        	{
        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:DEVI}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:DEVI}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Disorders"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:DISO}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:DISO}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Genes & Molecular Sequences"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:GENE}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:GENE}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Geographic Areas"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:GEOG}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:GEOG}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Living Beings"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:LIVB}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:LIVB}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Objects"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:OBJC}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:OBJC}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Occupations"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:OCCU}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:OCCU}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Organizations"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:ORGA}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:ORGA}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Phenomena"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:PHEN}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:PHEN}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Physiology"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:PHYS}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:PHYS}" ;
        		first = false ;
        	}
        	if(type.toString().equals("Procedures"))
        	{

        		if (first)
        			group += "{ ?st calbc:inGroup calbc-group:PROC}" ;
        		else
        			group += "UNION  { ?st calbc:inGroup calbc-group:PROC}" ;
        		first = false ;
        	}
        	
        }
        String queryString ; 
        {
			queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity  ?st where" +
				    "{ " +
			                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                 // "?entity rdf:type skos:Concept." + 
			                  "?entity rdf:type ?st." +
			                  group +
			        " } LIMIT 1"  ;
        }
       
        	
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
		
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         RDFNode   object    = soln.get("st");      // get the object
		         
				 return getSemanticGroupTypelabel(object.toString()) ;
		          
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return null;
		
	}
	public static String getSemanticGroupType(String mention, JList sGTypelist)
	{
	    int flag = 0 ; 	
        for(Object type:sGTypelist.getSelectedValuesList())
        {
        	if(type.toString().equals("Disorders"))
        	{
        		flag = 1 ;
        	}
        	if(type.toString().equals("Chimecal & Drugs"))
        	{
        		flag += 2 ;
        	}
        	
        }
        
        String queryString ; 
        if (flag == 3)
        {
			queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity  ?st where" +
				    "{ " +
			                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                 // "?entity rdf:type skos:Concept." + 
			                  "?entity rdf:type ?st." +
			                  "{ ?st calbc:inGroup calbc-group:DISO} UNION { ?st calbc:inGroup calbc-group:CHEM}" +
			        " } LIMIT 1"  ;
        }
        else if (flag == 2)
        {
			queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity  ?st where" +
				    "{ " +
			                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                 // "?entity rdf:type skos:Concept." + 
			                  "?entity rdf:type ?st." +
			                  "{?st calbc:inGroup calbc-group:CHEM}" +
			        " } LIMIT 1"  ;
        }
        else
        {
			queryString=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
					"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
					"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
			        "select ?entity  ?st where" +
				    "{ " +
			                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
			                 // "?entity rdf:type skos:Concept." + 
			                  "?entity rdf:type ?st." +
			                  "{?st calbc:inGroup calbc-group:DISO}" +
			        " } LIMIT 1"  ;
        }
        	
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
		
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         RDFNode   object    = soln.get("st");      // get the object
		         
				 return getSemanticGroupTypelabel(object.toString()) ;
		          
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return null;
		
	}
	public static String getSemanticGroupTypelabel(String resource)
	{
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX calbc: <http://linkedlifedata.com/resource/calbc/>" +
				"PREFIX calbc-group: <http://linkedlifedata.com/resource/calbc/group/>" + 
		        "select ?label where" +
			    "{ <" + 
		             resource + "> calbc:inGroup ?sTypeGroup. " +
			         "?sTypeGroup rdfs:label ?label" + 
		        " } LIMIT 1"  ;
		
		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			for (; results.hasNext();) 
			{
		
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         RDFNode   object    = soln.get("?label");      // get the object
				 return object.toString() ;
		          
		    }
		}
		catch(QueryParseException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null ; 
	}
	

}
