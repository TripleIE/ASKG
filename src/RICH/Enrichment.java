package RICH;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;










import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.restcalls;
import NER.OpenBioAnnotator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.sun.jndi.toolkit.url.Uri;



public class Enrichment {

	static String URL = "http://data.bioontology.org/annotator?whole_word_only=true&exclude_synonyms=false&exclude_numbers=true&ontologies=MESH&text=" ;

	public static void main(String[] args) throws IOException, ParseException {
		
		
		// TODO Auto-generated method stub
		
		 Synonyms("Acquired Immunity") ;
		 //LLDPrefLabelResource("levemir") ;
		//LLDDefinition("levemir") ;
		//LLDontology("levemir") ;
		//LLDCategory ("levemir") ;

		//LLDDefinition("saccular aneurysm") ;
		//LLDCategory ("saccular aneurysm") ;
		// LLDontology("aneurysm") ;
		//LLDontology("Thrombin") ;
		//LLDSynonym("saccular aneurysms") ;
		// LLDHierarchy("cetirizine hydrochloride") ;
		// LLDHierarchy("hypercholesterolemia") ;
		//LLDHierarchy("thyroid") ;
		//LLDSynonym("hypercholesterolemia") ;
		//LLDHierarchy("cholesterol") ;
		//LLDPrefLabelResource("thyroid") ;
		//LLDHierarchy("diabetes") ;
		
		//LLDSynonymMeasure() ;
		
		//LLDHierarchyMeasure() ;
		
		//LLDNarrowerHierarchy("Disease") ;

		//getSynonyms("glucose metabolism disease")  ;

		
		
	}
	public static String getURI(String concept)
	{
		String triple = LLDPrefLabelResource(concept) ;
		if (triple != null && !triple.isEmpty())
		{
			String[] token = triple.split("@") ;
			String URI = token[0] ;
			return URI; 
			
		}
		return null ; 
	}
	
	public static String getPrefLabel(String concept)
	{
		String triple = LLDPrefLabelResource(concept) ;
		if (triple != null && !triple.isEmpty())
		{
			String[] token = triple.split("@") ;
			String prefLabel = token[2] ;
			return prefLabel; 
			
		}
		return null ; 
	}
	public static Map<String, Integer> getSynonyms(String text) throws IOException, ParseException
	
	{
		String URL = "http://data.bioontology.org/search?whole_word_only=true&ontologies=MESH&q=" ;
	 	Map<String, Integer> synConcepts = new HashMap<String, Integer>();
		String request = URL + URLEncoder.encode(text, "UTF-8") ; ; 
		String jRespond= restcalls.get(request,OpenBioAnnotator.apikey) ;
		System.out.println(jRespond);
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(jRespond) ;
		JSONObject msgs =  (JSONObject) obj;
    	JSONArray msg = (JSONArray) msgs.get("collection") ;
    	System.out.println(msg.toString());
        Iterator iterator = msg.iterator();
        while (iterator.hasNext()) 
        {
        	JSONObject annotinnerObj = (JSONObject) iterator.next();
        	
           // System.out.println(annotinnerObj.get("synonym").toString());
            JSONArray syn = (JSONArray) annotinnerObj.get("synonym") ;
            if (syn == null)
            	continue ; 
            
            Iterator synIterator = syn.iterator();
            while (synIterator.hasNext()) 
            {
            	String synObj =  (String) synIterator.next();
            	synConcepts.put(synObj.toString(), 1);
            	
            }
            // we use only the first set of syn
            break ; 
  
	    }
	    
	    return synConcepts ;
		
	}
	
	public static  List<String> Synonyms(String entity)
	{
		List<String> terms = new ArrayList<String>() ;
		
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX lld: <http://linkedlifedata.com/resource/>" + 
				"PREFIX skos-xl: <http://www.w3.org/2008/05/skos-xl#>" +
				"PREFIX umls-concept: <http://linkedlifedata.com/resource/umls/id/>" +
     	        "select Distinct ?term ?label ?note " +
			    "where { " 
			        + " ?s ?p  "   + "\"" +   entity +  "\"."  
			        + " ?s skos:inScheme  lld:umls."
			        + " ?s skos-xl:altLabel ?term."
			        + " ?term skos-xl:literalForm ?label."
			        + "?term skos:note ?note."
		            + " } " ; 
		
/*		String queryString ="PREFIX p: <http://dbpedia.org/property/>"+
							"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
							"PREFIX category: <http://dbpedia.org/resource/Category:>"+
							"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
							"PREFIX geo: <http://www.georss.org/georss/>"+
							"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
					        "select distinct  ?label " +
						    "where { " +
					                   "?s " + "rdfs:label|skos:prefLabel|skos:altLabel" + "\"" +   entity +  "\"." + 
					                   "?s rdfs:label ?label" + 
					            " } "  ;*/

		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql?", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 	
			for (; results.hasNext();) 
			{
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         String term = soln.get("term").toString();  //get the subject
		         String label = soln.get("label").toString();  //get the subject
		         String note = soln.get("note").toString();  //get the subject
		         terms.add(label);
		          System.out.println(term + "!" + label + "!" + note ) ;
			}
			return terms ;
		}
		catch(Exception  e)
		{
			System.out.println(e.getMessage()) ;
		}
		return null;
		
		
	}
	
	
		
	 public static  List<String> intersection(List<String> list1, List<String> list2) {
	        List<String> list = new ArrayList<String>();

	        for (String t : list1) {
	            if(list2.contains(t)) {
	                list.add(t);
	            }
	        }
	        return list;
	    }

	public static List<String> LLDHierarchy(String entity) throws IOException
	{  

		List<String> hier = new ArrayList<String>() ;
		String Resource = LLDPrefLabelResource(entity) ;
		if (Resource ==  null)
			return hier ; 
		String tokens[] = Resource.split(" ") ;
		String URI = tokens[0] ;
		Model gragh = ModelFactory.createDefaultModel();
		broader(URI,false,1, 1, gragh,hier) ;
		System.out.println(URI + "!" + tokens[2]); 
		//if (URI.contains("http://linkedlifedata.com/resource/chebi")) 
	    hier.add(URI + "!" + tokens[2]) ;
		return hier ; 
		
	}
	public static List<String> LLDHierarchy(String entity,String Resource) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		if (Resource ==  null)
			return hier ; 
		String tokens[] = Resource.split(" ") ;
		String URI = tokens[0] ;
		Model gragh = ModelFactory.createDefaultModel();
		broader(URI,false,1, 1, gragh,hier) ;
		System.out.println(URI + "!" + tokens[2]); 
		//if (URI.contains("http://linkedlifedata.com/resource/chebi")) 
	    hier.add(URI + "!" + tokens[2]) ;
		return hier ; 
		
	}
	public static List<String> LLDHierarchyTopURI(String entity,String Label) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		String Resource = entity ;
		if (entity ==  null || entity.isEmpty())
			return hier ; 
		

		String URI = entity ;
		Model gragh = ModelFactory.createDefaultModel();
		broader(URI,false,5, 1, gragh,hier) ;
		System.out.println(URI + "!" + Label); 
		hier.add(URI + "!" + Label) ;
		return hier ; 
		
	}
	
	public static void broader(String lookupresource,boolean isLiteral,int maxdepth, int level,Model gragh,List<String> hier) throws IOException 
	{
		
        if (isLiteral || maxdepth < level )
        {
		    	//System.out.println(object); 
		    	return ; 
        } 
        
     // create the resource
        Resource rec = gragh.createResource(lookupresource);
        Map<String,List<String>> nodes = new HashMap<String, List<String>>(); 
        ResultSet results = null ; 
        results = BroaderRel(lookupresource) ;
        
        ++level ;
		for (; results != null && results.hasNext();) 
		{
		    
		    
		    // Result processing is done here.
	         QuerySolution soln = results.nextSolution() ;
	         RDFNode   object    = soln.get("o");      // get the object
	         RDFNode label    = soln.get("label");      // get the object
	         if(object != null)
	         {
	        	 if (object.isLiteral())
	        	 {
	        		 hier.add(lookupresource + "!" + object) ;
	        		System.out.println(lookupresource + "!" + object);
	        	 }
	        	 broader(object.toString(),object.isLiteral(),maxdepth,level,gragh,hier) ;
	        	 
	 	         if (!object.isLiteral())
	 	         {
	 	        	 
	         	    System.out.println(object.asResource().toString() + "!"+ label.asLiteral().getString()); 
	         	    hier.add(object.asResource().toString() + "!"+ label.asLiteral().getString()) ;
	 	         }
	         }
	         
	         // we use only the first retrieved resource 
	      //  return ;
		}
		
		
	}
	
	public static  ResultSet BroaderRel(String entity) {

		//Querying remote SPARQL services	
			ResultSet results = null ; 
			try 
			{
			   //  String ontology_service = "http://lod.openlinksw.com/sparql";
			 //  String ontology_service = "http://sparql.hegroup.org/sparql/";
			     String ontology_service = "http://linkedlifedata.com/sparql";
			
			String sparqlQuery=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>" +
				    //"select distinct  ?o ?p where {<http://dbpedia.org/resource/Michelle_Obama> ?p ?o. } ";
			        "select distinct  ?o ?label where {<" + entity +  "> skos:broader  ?o.  ?o rdfs:label ?label} ";

			QueryExecution x = QueryExecutionFactory.sparqlService(ontology_service, sparqlQuery);
			 results = x.execSelect();
			}
			catch(QueryParseException e)
			{
				System.out.println(e.getMessage()); 
				return null ; 
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage()); 
				 return null ; 
			}
			//ResultSetFormatter.out(System.out, results);
			 return results ;
		}
	
	
	public static List<String> LLDHierarchyProperty(String entity,String Label) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		String Resource = entity ;
		if (entity ==  null || entity.isEmpty())
			return hier ; 
		

		String URI = entity ;
		Model gragh = ModelFactory.createDefaultModel();
		broaderProperty(URI,false,5, 1, gragh,hier) ;
		System.out.println(URI + "!" + Label); 
		hier.add(URI + "!" + Label) ;
		return hier ; 
		
	}
	public static void broaderProperty(String lookupresource,boolean isLiteral,int maxdepth, int level,Model gragh,List<String> hier) throws IOException 
	{
		
        if (isLiteral || maxdepth < level )
        {
		    	//System.out.println(object); 
		    	return ; 
        } 
        
     // create the resource
        Resource rec = gragh.createResource(lookupresource);
        Map<String,List<String>> nodes = new HashMap<String, List<String>>(); 
        ResultSet results = null ; 
        results = BroaderPropertyRel(lookupresource) ;
        
        ++level ;
		for (; results != null && results.hasNext();) 
		{

//		    Statement stmt      = iter.nextStatement();  // get next statement
//		    Resource  subject   = stmt.getSubject();     // get the subject
//		    Property  predicate = stmt.getPredicate();   // get the predicate
//		    RDFNode   object    = stmt.getObject();      // get the objecttain
		    
		    
		    // Result processing is done here.
	         QuerySolution soln = results.nextSolution() ;
	         RDFNode   object    = soln.get("o");      // get the object
	         RDFNode label    = soln.get("label");      // get the object
	         if(object != null)
	         {
	        	 if (object.isLiteral())
	        	 {
	        		 hier.add(lookupresource + "!" + object) ;
	        		System.out.println(lookupresource + "!" + object);
	        	 }
	        	 broaderProperty(object.toString(),object.isLiteral(),maxdepth,level,gragh,hier) ;
	        	 
	 	         if (!object.isLiteral())
	 	         {
	 	        	 
	         	    System.out.println(object.asResource().toString() + "!"+ label.asLiteral().getString()); 
	         	    hier.add(object.asResource().toString() + "!"+ label.asLiteral().getString()) ;
	 	         }
	         }

		}
		
		
	}
	
	
	public static  ResultSet BroaderPropertyRel(String entity) {

		//Querying remote SPARQL services	
			ResultSet results = null ; 
			try 
			{
			   //  String ontology_service = "http://lod.openlinksw.com/sparql";
			 //  String ontology_service = "http://sparql.hegroup.org/sparql/";
			     String ontology_service = "http://linkedlifedata.com/sparql";
			
			String sparqlQuery=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>" +
				    //"select distinct  ?o ?p where {<http://dbpedia.org/resource/Michelle_Obama> ?p ?o. } ";
			        "select distinct  ?o ?label where { <" + entity +  "> rdfs:subPropertyOf  ?o.  ?o rdfs:label ?label} ";

			QueryExecution x = QueryExecutionFactory.sparqlService(ontology_service, sparqlQuery);
			 results = x.execSelect();
			}
			catch(QueryParseException e)
			{
				System.out.println(e.getMessage()); 
				return null ; 
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage()); 
				 return null ; 
			}
			//ResultSetFormatter.out(System.out, results);
			 return results ;
		}
	public static List<String> LLDNHierarchyTopURI(String entity,String Label) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		if (entity ==  null || entity.isEmpty())
			return hier ; 
		

		String URI = entity ;
		Model gragh = ModelFactory.createDefaultModel();
		narrower(URI,false,5, 1, gragh,hier) ;
		System.out.println(URI + "!" + Label); 
		hier.add(URI + "!" + Label) ;
		return hier ; 
		
	}
	
	public static List<String> LLDNarrowerHierarchy(String entity) throws IOException
	{
		List<String> hier = new ArrayList<String>() ;
		String Resource = LLDPrefLabelResource(entity) ;
		if (Resource ==  null)
			return hier ; 
		String tokens[] = Resource.split(" ") ;
		String URI = tokens[0] ;
		Model gragh = ModelFactory.createDefaultModel();
		narrower(URI,false,10, 1, gragh,hier) ;
		System.out.println(URI + "!" + tokens[2]); 
		hier.add(URI + "!" + tokens[2]) ;
		return hier ; 
		
	}
	public static void narrower(String lookupresource,boolean isLiteral,int maxdepth, int level,Model gragh,List<String> hier) throws IOException 
	{
		
        if (isLiteral || maxdepth < level )
        {
		    	//System.out.println(object); 
		    	return ; 
        } 
        
     // create the resource
        Resource rec = gragh.createResource(lookupresource);
        Map<String,List<String>> nodes = new HashMap<String, List<String>>(); 
        ResultSet results = null ; 
        results = NarrowerRel(lookupresource) ;
        
        ++level ;
		for (; results != null && results.hasNext();) 
		{

//		    Statement stmt      = iter.nextStatement();  // get next statement
//		    Resource  subject   = stmt.getSubject();     // get the subject
//		    Property  predicate = stmt.getPredicate();   // get the predicate
//		    RDFNode   object    = stmt.getObject();      // get the objecttain
		    
		    
		    // Result processing is done here.
	         QuerySolution soln = results.nextSolution() ;
	         RDFNode   object    = soln.get("o");      // get the object
	         RDFNode label    = soln.get("label");      // get the object
	         if(object != null)
	         {
	        	 if (object.isLiteral())
	        	 {
	        		 hier.add(lookupresource + "!" + object) ;
	        		System.out.println(lookupresource + "!" + object);
	        	 }
	        	 narrower(object.toString(),object.isLiteral(),maxdepth,level,gragh,hier) ;
	        	 
	 	         if (!object.isLiteral())
	 	         {
	 	        	 
	         	    System.out.println(object.asResource().toString() + "!"+ label.asLiteral().getString()); 
	         	    hier.add(object.asResource().toString() + "!"+ label.asLiteral().getString()) ;
	 	         }
	         }
	         
	         return ;
		}
		
		
	}
	
	public static  ResultSet NarrowerRel(String entity) {

		//Querying remote SPARQL services	
			ResultSet results = null ; 
			try 
			{
			   //  String ontology_service = "http://lod.openlinksw.com/sparql";
			 //  String ontology_service = "http://sparql.hegroup.org/sparql/";
			     String ontology_service = "http://linkedlifedata.com/sparql";
			
			String sparqlQuery=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>" +
				    //"select distinct  ?o ?p where {<http://dbpedia.org/resource/Michelle_Obama> ?p ?o. } ";
			        "select distinct  ?o ?label where {<" + entity +  "> skos:narrower  ?o.  ?o rdfs:label ?label} ";

			QueryExecution x = QueryExecutionFactory.sparqlService(ontology_service, sparqlQuery);
			 results = x.execSelect();
			}
			catch(QueryParseException e)
			{
				System.out.println(e.getMessage()); 
				return null ; 
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage()); 
				 return null ; 
			}
			//ResultSetFormatter.out(System.out, results);
			 return results ;
		}
	
	public static String LLDPrefLabelResource(String entity)
	{

		// we look up the resource that has preflabel match the entity 

		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
		        "select distinct  ?s " +
			    "where { " +
		                   "?s " + "skos:prefLabel*"  + "\"" +   entity +  "\"." + 
		                   "?s a skos:Concept." + 
		            " } " +
		            "LIMIT 50" ;
		

		
		// now creating query object
		try
		{
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query);
			ResultSet results ;
			qexec.setTimeout(30000);
			results = qexec.execSelect(); 
			boolean found = false ; 
			String onto = null ;
			for (; results.hasNext();) 
			{
			    // Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         onto = soln.get("s").asResource().getURI();  //get the subject
		         System.out.println(onto ) ;
		         String Triple =  onto + "@"+ "skos:prefLabel" + "@" + entity ; 
		         return Triple  ;
			}
			 
			
			// we look up all resources that has describe match the entity and also has preflabel.
			
			String queryStringwide=
					"PREFIX p: <http://dbpedia.org/property/>"+
					"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
					"PREFIX category: <http://dbpedia.org/resource/Category:>"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
					"PREFIX geo: <http://www.georss.org/georss/>"+
					"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
			        "select distinct  ?s ?label " +
				    "where { " +
			                   "?s " + "?p"  + "\"" +   entity +  "\"." + 
			                   "?s a skos:Concept." + 
			                   "?s skos:prefLabel ?label" + 
			            " } " +
			            "LIMIT 50" ;
			Query query1 = QueryFactory.create(queryStringwide);
			QueryExecution qexec1 = QueryExecutionFactory.sparqlService("http://linkedlifedata.com/sparql", query1);
			ResultSet results1 ;
			qexec1.setTimeout(30000);
			results1 = qexec1.execSelect();
			for (; results1.hasNext();) 
			{
			    // Result processing is done here.
		         QuerySolution soln1 = results1.nextSolution() ;
		         String onto1 = soln1.get("s").asResource().getURI();  //get the subject
		         String label = soln1.get("label").toString();  //get the subject
		         String Triple =  onto1 + "@"+ "skos:prefLabel" + "@" + label ; 
		         System.out.println( onto1 +  " " + label) ;
		         return Triple ;
			}
			

		}
		catch(QueryParseException e)
		{
			System.out.println(e.getMessage()) ;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage()) ;
		}
		return null;
		
	}
	
	public static List<String> LLDontology(String entity)
	{

		List<String> cats = new ArrayList<String>() ;
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
		        "select distinct  ?s " +
			    "where { " +
		                   "?s " + "?p"  + "\"" +   entity +  "\"." + 
		                   "?s a skos:Concept." + 
		            " } " +
		            "LIMIT 50" ;
		

		
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
		         String onto = soln.get("s").asResource().getURI();  //get the subject
		         String[] tokens = onto.split("/") ;
		         if(tokens != null)
		         {
		        	 System.out.println(tokens[4] ) ;
		        	 cats.add(tokens[4]) ;
		        	 
		         }
		        // cats.add(onto) ;
		         //System.out.println(onto ) ;
			}
			return cats ;
		}
		catch(QueryParseException e)
		{
			System.out.println(e.getMessage()) ;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage()) ;
		}
		return null;
		
	}
	
	public static List<String> LLDCategory(String entity)
	{

		List<String> cats = new ArrayList<String>() ;
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
		        "select distinct  ?cat ?label " +
			    "where { " +
		                   "?s " + "rdfs:label|skos:prefLabel|skos:altLabel"  + "\"" +   entity +  "\"." + 
		                   "?s a ?cat." +
		                   "?s a skos:Concept." + 
		                   "?cat rdfs:label|skos:prefLabel|skos:altLabel ?label" +
		            " } " +
		            "LIMIT 50" ;
		

		
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
		         String cat = soln.get("cat").toString();  //get the subject
		         String label = soln.get("label").toString();  //get the subject

		         cats.add( label) ;
		         if ( !label.equals("Concept@en") && !label.equals("UMLS Concept") ) 
		        	 System.out.println(label ) ;
			}
			return cats ;
		}
		catch(QueryParseException e)
		{
			System.out.println(e.getMessage()) ;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage()) ;
			
		}
		return null;
		
	}
	public static List<String> LLDCategorybyURI(String entity)
	{

		List<String> cats = new ArrayList<String>() ;
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
		        "select distinct  ?cat ?label " +
			    "where { " +
		                   "<" +entity + ">" +  " a ?cat." +
		                   "<" +entity + ">" + " a skos:Concept." + 
		                   "?cat rdfs:label|skos:prefLabel|skos:altLabel ?label" +
		            " } " +
		            "LIMIT 50" ;
		

		
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
		         String cat = soln.get("cat").toString();  //get the subject
		         String label = soln.get("label").toString();  //get the subject

		         cats.add( label.toLowerCase()) ;
		         if ( !label.equals("Concept@en") && !label.equals("UMLS Concept") ) 
		        	 System.out.println(label ) ;
			}
			return cats ;
		}
		catch(QueryParseException e)
		{
			System.out.println(e.getMessage()) ;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage()) ;
			
		}
		return null;
		
	}
	public static List<String> LLDDefinition(String entity)
	{

		List<String> defs = new ArrayList<String>() ;
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX geo: <http://www.georss.org/georss/>"+
				"PREFIX w3: <http://www.w3.org/2002/07/owl#>"+
		        "select distinct  ?def " +
			    "where { " +
		                   "?s " + "rdfs:label|skos:prefLabel|skos:altLabel" + "\"" +   entity +  "\"." + 
		                   "?s skos:note|skos:definition ?def." + 
		            " } " +
		            "LIMIT 50" ;
		

		
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
		         String def = soln.get("def").toString();  //get the subject

		         defs.add( def) ;
		         System.out.println(def ) ;
			}
			return defs ;
		}
		catch(QueryParseException e)
		{
			System.out.println(e.getMessage()) ;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage()) ;
		}
		return null;
		
	}

	
	
	public static String getSemanticGroupType(String mention)
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
		        "select ?semtgroup  where" +
			    "{ " +
		                  "?entity ?predicate" +  " \"" +   mention +  "\". "   +
		                 // "?entity rdf:type skos:Concept." + 
		                  "?entity rdf:type ?st." +
		                  " ?st calbc:inGroup ?semtgroup" +
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
				//Result processing is done here.
		         QuerySolution soln = results.nextSolution() ;
		         RDFNode semtGroupType = soln.get("semtgroup");  //get the subject
		         return semtGroupType.asNode().getLocalName() ;
		         
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

	
	
	
}
