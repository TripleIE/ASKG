package HRCHY;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

import util.ReadXMLFile;
import util.bioportal;
import util.dataExtractor;
import util.wordNetHrachy;
import NER.OpenBioAnnotator;
import NER.measure;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinResults;

public class hierarchy {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		 OntModel OntoGraph = dataExtractor.getOntologyModel("X:\\KG_ANR\\Clinical NotesGold\\Store_DB\\DO\\doid.owl") ;
		 Map<String, String> concepts = new HashMap<String, String>();
		 concepts.put("intracranial aneurysm", "diseases") ;
		 
		// Map<String, List<String>> diseasegoldstandard = ReadXMLFile.Deserialize("F:\\eclipse64\\eclipse\\DiseaseGoldstandard") ;
		 
		  MeasureDOfromfile(null,OntoGraph);
		 //MeasureDOss(diseasegoldstandard, OntoGraph); 
		//MeasureWordNet() ; 
	}
	
/******************************** Hierarchy using BioPortal  *********************************************************/
	public static List<String> BioHRCHY(String mention)
	{
	  List<String> Linkuris = new ArrayList<String>() ;
		
	try
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
			        "Select ?superlabel ?type " +
				    "{ " +
			                   "?entity rdfs:label  ?label."   +
			                   "?entity rdfs:subClassOf ?superclass ." + 
			                    "?superclass a ?type." + 
			                   "?superclass rdfs:label  ?superlabel ." + 
			                  // "?superclass rdfs:subClassOf ?superclass2 ." + 
			                 //  "?superclass2 rdfs:label ?superlabel2 ." + 
			              /*    "?entity rdf:type skos:Concept" + */
			                   " FILTER (CONTAINS ( UCASE(str(?label)), "  + "\"" +   mention.toUpperCase() +  "\") ) }  LIMIT 2 " ;  ;
			
			
			// ask did not work
			 Query query = QueryFactory.create(queryString) ;
           
			 // http://sparql.bioontology.org/
			 // "http://sparql.bioontology.org/sparql/"
	         QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest("http://sparql.bioontology.org/sparql/", query); 		                      
	         qexec.addParam("apikey", OpenBioAnnotator.apikey) ;
	         ResultSet results1 ;
	         qexec.setTimeout(60000);
	         results1 = qexec.execSelect() ;
	         for (; results1.hasNext();) 
	         {

	             QuerySolution soln = results1.nextSolution() ;
		         String subj = soln.getLiteral("superlabel").getLexicalForm();  //get the subject
		         String subj2 = soln.getResource("type").toString();  //get the subject
		         if (subj2.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Class"))
		         {
			         Linkuris.add(subj);
		             System.out.println(subj) ;
		         }
	         }
	         return Linkuris ;

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
	
	/******************************** Hierarchy using Link life Data *********************************************************/
	public static List<String> Taxonomic_Extractor(String entity,int level,int maxDepth) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		// get the prefer label 
		String Resource = LLDPrefLabelResource(entity) ;
		if (Resource ==  null)
			return hier ; 
		
		
		String tokens[] = Resource.split(" ") ;
		String URI = tokens[0] ; 
		Model gragh = ModelFactory.createDefaultModel();
		allsiblingbroader(URI,false,maxDepth, level, gragh,hier) ;
		
		// add the concept itself 
		System.out.println(URI + "!" + tokens[2] + "!"+ Integer.toString(0)); 
	    hier.add(URI + "!" + tokens[2] + "!"+ Integer.toString(0)) ;
		return hier ; 
		
	}
	
	public static List<String> Taxonomic_Extractor(String entity,String Resource,int level,int maxDepth) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		if (Resource ==  null)
			return hier ; 
		
		String tokens[] = Resource.split(" ") ;
		String URI = tokens[0] ; 
		Model gragh = ModelFactory.createDefaultModel();
		allsiblingbroader(URI,false,maxDepth, level, gragh,hier) ;
		
		// add the concept itself 
		System.out.println(Resource); 
		System.out.println(URI + "!" + entity + "!"+ Integer.toString(0)); 
	    hier.add(URI + "!" + entity + "!"+ Integer.toString(0)) ;
		return hier ; 
		
	}
	public static List<String> Taxonomic_Extractor_origin(String entity,String Resource,int level,int maxDepth) throws IOException
	{  
		
		
		List<String> hier = new ArrayList<String>() ;
		if (Resource ==  null)
			return hier ; 
		
		String tokens[] = Resource.split(" ") ;
		String URI = tokens[0] ; 
		Model gragh = ModelFactory.createDefaultModel();
        List<String> resource = new ArrayList<String>() ;
		allsiblingbroader_origin(URI,false,maxDepth, level, gragh,hier,resource) ;
		
		// add the concept itself 
		System.out.println(Resource); 
		System.out.println(URI + "!" + entity + "!"+ Integer.toString(0)); 
	    hier.add(URI + "!" + entity + "!"+ Integer.toString(0)) ;
		return hier ; 
		
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
		broader(URI,false,15, 1, gragh,hier) ;
		System.out.println(URI + "!" + tokens[2]); 
	    hier.add(URI + "!" + tokens[2]) ;
		return hier ; 
		
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
		         //System.out.println(onto ) ;
		         String Triple =  onto + " "+ "skos:prefLabel" + " " + entity ; 
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
		         String Triple =  onto1 + " "+ "skos:prefLabel" + " " + label ; 
		         //System.out.println( onto1 +  " " + label) ;
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
	         
	         return ;
		}
		
		
	}
	
	public static void allsiblingbroader(String lookupresource,boolean isLiteral,int maxdepth, int level,Model gragh,List<String> hier) throws IOException 
	{

        if (isLiteral || maxdepth <= level )
        { 
		    	//System.out.println(object); 
		    	return ; 
        } 
        
     // create the resource
     //   Resource rec = gragh.createResource(lookupresource);
     //   Map<String,List<String>> nodes = new HashMap<String, List<String>>(); 
        ResultSet results = null ; 
        List<String> resource = new ArrayList<String>() ;
        results = BroaderRel(lookupresource) ;
        
        ++level ;
		for (; results != null && results.hasNext();) 
		{
		    
		    // Result processing is done here.
	         QuerySolution soln = results.nextSolution() ;
	         RDFNode   object    = soln.get("o");      // get the object
	         RDFNode label    = soln.get("label");      // get the object
	         if(object != null && !lookupresource.equalsIgnoreCase(object.toString()) )
	         {
	        	 boolean literal = object.isLiteral() ; 
	        	 if (literal)
	        	 {
	        		 hier.add(lookupresource + "!" + object+ "!"+ Integer.toString(level)) ;
	        		 System.out.println(lookupresource + "!" + object+  "!" + Integer.toString(level));
	        	 }
	        	 
	        	 // this to skip the one that has the same URI 
	        	 if (!resource.contains(object.toString()))
	        	 {
	        		 resource.add(object.toString()) ;
	        	 }
	        	 else
	        	 {
	        		// hier.add(lookupresource + "!" + object+ "!"+ Integer.toString(level)) ;
	        		// System.out.println(lookupresource + "!" + object+  "!" + Integer.toString(level));
	        		 literal = true ; 
	        	 }
	        	 
	        	    allsiblingbroader(object.toString(),literal,maxdepth,level,gragh,hier) ;
	        	 
	 	         if (!object.isLiteral() && !resource.contains(object.toString()))
	 	         {
	 	        	 
	         	    System.out.println(object.asResource().toString() + "!"+ label.asLiteral().getString() + "!" + Integer.toString(level)); 
	         	    hier.add(object.asResource().toString() + "!"+ label.asLiteral().getString() + "!"+ Integer.toString(level)) ;
	 	         }
	         }
	         
		}
		
		
	}
	
	public static void allsiblingbroader_origin(String lookupresource,boolean isLiteral,int maxdepth, int level,Model gragh,List<String> hier, List<String> resource) throws IOException 
	{

        if (isLiteral || maxdepth <= level )
        { 
		    	//System.out.println(object); 
		    	return ; 
        } 
        
     // create the resource
     //   Resource rec = gragh.createResource(lookupresource);
     //   Map<String,List<String>> nodes = new HashMap<String, List<String>>(); 
        ResultSet results = null ; 

        results = BroaderRel(lookupresource) ;
        
        ++level ;
		for (; results != null && results.hasNext();) 
		{
		    
		    // Result processing is done here.
	         QuerySolution soln = results.nextSolution() ;
	         RDFNode   object    = soln.get("o");      // get the object
	         RDFNode label    = soln.get("label");      // get the object
	         if(object != null && !lookupresource.equalsIgnoreCase(object.toString()) )
	         {
	        	 boolean literal = object.isLiteral() ; 
	        	 if (literal)
	        	 {
	        		 //hier.add(lookupresource + "!" + object+ "!"+ Integer.toString(level)) ;
	        		 System.out.println(lookupresource + "!" + object+  "!" + Integer.toString(level));
	        	 }
	        	 
	        	 // this to skip the one that has the same URI 
	        	 if (!resource.contains(object.toString()))
	        	 {
	        		 resource.add(object.toString()) ;
		        	 
		 	         if (!object.isLiteral())
		 	         {
		 	        	 
		         	    System.out.println( object.asResource().toString() + "!"+ label.asLiteral().getString() + "!" + Integer.toString(level)+ "!" + lookupresource ); 
		         	    hier.add( object.asResource().toString() + "!"+ label.asLiteral().getString() + "!"+ Integer.toString(level)+ "!" + lookupresource ) ;
		 	         }
	        	 }
	        	 else
	        	 {
	        		// hier.add(lookupresource + "!" + object+ "!"+ Integer.toString(level)) ;
	        		// System.out.println(lookupresource + "!" + object+  "!" + Integer.toString(level));
	        		 literal = true ; 
	        	 }
	        	 
	        	 allsiblingbroader_origin(object.toString(),literal,maxdepth,level,gragh,hier,resource) ;

	         }
	         
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
			        "select distinct  ?o ?label where {<" + entity +  "> skos:broader ?o.  ?o rdfs:label ?label} ";

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
	/*************************************************************************************************************************************************************/
	/******************************* Evaluation  **********************************************************************************************/
	/*************************************************************************************************************************************************************/
	
	public static void Measure() throws IOException
	{
		
		Map<String, List<String>> diseasegoldstandard = ReadXMLFile.Deserialize("F:\\eclipse64\\eclipse\\DiseaseGoldstandard") ;
		//Map<String, List<String>> diseasegoldstandard = ReadXMLFile.Deserialize("F:\\eclipse64\\eclipse\\DiseaseCDRGoldstandard") ;
		
		
		double Recall = 0 ; 
		double Precision  = 0 ;
		int count = 0 ; 
	    for (String disease: diseasegoldstandard.keySet())
   	 	{
	    	List<String> gold = diseasegoldstandard.get(disease) ;
	    	List<String> Hierarchy = Taxonomic_Extractor(disease,1,5) ; 
	    	List<String> Hierlist = new ArrayList<String>() ;
	   		 
	   		if (Hierarchy != null && Hierarchy.size() > 1 )
	   		{
	   			for (int i = Hierarchy.size()-2 ; i > -1; i--)
	   			{
	   				String hier = Hierarchy.get(i) ;
	   				String tokens[] = hier.split("!") ;
	 	         	
	 	         	Hierlist.add(tokens[1].toLowerCase()) ;
	   			}

		   		 List<String> ret = intersection(gold, Hierlist) ;
		   		 count++ ;
	 	   		 Recall += (double) ret.size()/(double) gold.size() ;
	 	   		 Precision += (double) ret.size()/(double) Hierlist.size() ;
	 	   		 
	 	   		 if (count % 10 == 0 )
	 	   		 {
	 	   			Double  tempRecall = Recall / count ;
	 	   			Double  tempPrecision = Precision /count ;
	 	   			System.out.println(tempRecall);
	 	   		    System.out.println(tempPrecision);
	 	   		    System.out.println(count);
	 	   		 }
 	   		 
	   		}

  	 	}
	    
	    Recall = Recall / count ;
	    Precision = Precision /count ; 
	    double Fmeasure = 2* ((Precision * Recall) / (Precision + Recall)) ;
	    System.out.println(" Recall = " +  Recall);
	    System.out.println(" Precision = " +  Precision);
	    System.out.println(" Fmeasure = " +  Fmeasure);
		 
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
	 
	 public static void MeasureWordNet() throws IOException
		{
			
			Map<String, List<String>> diseasegoldstandard = ReadXMLFile.Deserialize("F:\\eclipse64\\eclipse\\DiseaseGoldstandard") ;
			//Map<String, List<String>> diseasegoldstandard = ReadXMLFile.Deserialize("F:\\eclipse64\\eclipse\\DiseaseCDRGoldstandard") ;
			
			
			double Recall = 0 ; 
			double Precision  = 0 ;
			int count = 0 ; 
		    for (String disease: diseasegoldstandard.keySet())
	   	 	{
		    	List<String> gold = diseasegoldstandard.get(disease) ;
		    	List<String> Hierarchy = wordNetHrachy.getHypernymswithSyn(disease) ; 
		    	List<String> Hierlist = new ArrayList<String>() ;
		   		 
		   		if (!Hierarchy.isEmpty()   )
		   		{
		   			for (int i = 0; i <  Hierarchy.size() ; i++)
		   			{
		   				String hier = Hierarchy.get(i) ;	         	
		 	         	Hierlist.add(hier.toLowerCase()) ;
		   			}

			   		 List<String> ret = intersection(gold, Hierlist) ;
			   		 count++ ;
		 	   		 Recall += (double) ret.size()/(double) gold.size() ;
		 	   		 Precision += (double) ret.size()/(double) Hierlist.size() ;
		 	   		 
		 	   		 if (count % 10 == 0 )
		 	   		 {
		 	   			Double  tempRecall = Recall / count ;
		 	   			Double  tempPrecision = Precision /count ;
		 	   			System.out.println(tempRecall);
		 	   		    System.out.println(tempPrecision);
		 	   		    System.out.println(count);
		 	   		 }
	 	   		 
		   		}

	  	 	}
		    
		    Recall = Recall / count ;
		    Precision = Precision /count ; 
		    double Fmeasure = 2* ((Precision * Recall) / (Precision + Recall)) ;
		    System.out.println(" Recall = " +  Recall);
		    System.out.println(" Precision = " +  Precision);
		    System.out.println(" Fmeasure = " +  Fmeasure);
			 
		}
	 
	 
	 public static void MeasureDO(String concept, OntModel OntoGraph ) throws IOException, ParseException
	 {
		 
	    	List<String> goldstand = new ArrayList<String>() ;
	    	List<String> Hierarchy = bioportal.getTaxonomic(concept, 5) ; 
	    	List<String> Hierlist = new ArrayList<String>() ;
	    	
			String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>"  ;
			
/*			xmlns="http://purl.obolibrary.org/obo/doid.owl#"
				     xml:base="http://purl.obolibrary.org/obo/doid.owl"
				     xmlns:dc="http://purl.org/dc/elements/1.1/"
				     xmlns:obo="http://purl.obolibrary.org/obo/"
				     xmlns:owl="http://www.w3.org/2002/07/owl#"
				     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				     xmlns:xml="http://www.w3.org/XML/1998/namespace"
				     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
				     xmlns:doid="http://purl.obolibrary.org/obo/doid#"
				     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
				     xmlns:terms="http://purl.org/dc/terms/"
				     xmlns:oboInOwl="http://www.geneontology.org/formats/oboInOwl#"*/
		 
		    String sparql = prefix + " SELECT ?concept ?label WHERE { " +
                    "?concept rdfs:label " +"\""  + concept + "\" . " + 
		    		"?concept rdfs:subClassOf* ?parent ." +
		    		"?parent  rdfs:label ?label ." + "}";
		    
			Query qry = QueryFactory.create(sparql);
			QueryExecution qe = QueryExecutionFactory.create(qry, OntoGraph);
			ResultSet rs = qe.execSelect();
		 
			RDFNode parent = null  ;
			while(rs.hasNext())
		    {
		        QuerySolution sol = rs.nextSolution();
		        RDFNode str = sol.get("concept"); 
		        parent = sol.get("label"); 
		        goldstand.add(parent.asLiteral().getString()) ;
		    }
		    qe.close(); 
		    
/*		    if ( parent == null || parent.asLiteral().getString().equalsIgnoreCase("disease"))
		    	return ; 
		    else
		    	MeasureDO(parent.asLiteral().getString(),OntoGraph ) ;*/
		    
		 
	 }
	 
	 
	 
	 public static measure MeasureDOs(Map<String, String> concepts , OntModel OntoGraph ) throws IOException, ParseException
	 {
		   double recall = 0 ; 
		   double precision = 0 ; 
		   measure Measure = new measure() ; 
		   for (String concept : concepts.keySet())
		   {
		    	List<String> goldstand = new ArrayList<String>() ;
		    	List<String> Hierarchy = bioportal.getTaxonomic(concept, 3) ; 
		    	List<String> Hierlist = new ArrayList<String>() ;
		    	
				String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"  ;
				
			 
			    String sparql = prefix + " SELECT ?concept ?label WHERE { " +
	                    "?concept rdfs:label " +"\""  + concept + "\" . " + 
			    		"?concept rdfs:subClassOf* ?parent ." +
			    		"?parent  rdfs:label ?label ." + "}";
			    
				Query qry = QueryFactory.create(sparql);
				QueryExecution qe = QueryExecutionFactory.create(qry, OntoGraph);
				ResultSet rs = qe.execSelect();
			 
				RDFNode parent = null  ;
				while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			        RDFNode str = sol.get("concept"); 
			        parent = sol.get("label"); 
			        goldstand.add(parent.asLiteral().getString()) ;
			    }
				double match = 0 ; 
				for(int i = 0 ; i < Hierarchy.size(); i++)
				{
					
					for(int j = 0 ; j < goldstand.size(); j++)
					{
						if (Hierarchy.get(i).equalsIgnoreCase(goldstand.get(j)))
						{
							match++ ; 
						}
					}
					
				}
				
				Measure.Recall +=  match/goldstand.size() > 1 ? 1: match/goldstand.size() ; 
				Measure.Precision += match/Hierarchy.size() ;
				
			    qe.close(); 
		   }
		   
		   Measure.avgRecall  = Measure.Recall /concepts.size() ; 
		   Measure.avgPrecision = Measure.Precision /concepts.size() ;
		    System.out.println(" Recall = " +  Measure.avgRecall);
		    System.out.println(" Precision = " +  Measure.avgPrecision );
		   
		   return Measure ; 

	 }
	 
	 public static measure MeasureDOss(Map<String, List<String>> concepts, OntModel OntoGraph ) throws IOException, ParseException
	 {
		   double recall = 0 ; 
		   double precision = 0 ; 
		   measure Measure = new measure() ; 
		   int conceptcount = 0 ; 
		   for (String concept : concepts.keySet())
		   {
		    	//List<String> goldstand = new ArrayList<String>() ;
		    	Map<String, Integer> goldstand = new HashMap<String, Integer>();
		    	
		    	Map<String, Integer> Hierarchy = bioportal.getTaxonomicMap(concept,2) ; 
		    	List<String> Hierlist = new ArrayList<String>() ;
		    	
				String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"  ;
				
			 
			    String sparql = prefix + " SELECT ?concept ?label WHERE { " +
	                    "?concept rdfs:label " +"\""  + concept + "\" . " + 
			    		"?concept rdfs:subClassOf* ?parent ." +
			    		"?parent  rdfs:label ?label ." + "}";
			    
				Query qry = QueryFactory.create(sparql);
				QueryExecution qe = QueryExecutionFactory.create(qry, OntoGraph);
				ResultSet rs = qe.execSelect();
			 
				RDFNode parent = null  ;
				while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			        RDFNode str = sol.get("concept"); 
			        parent = sol.get("label"); 
			        goldstand.put(parent.asLiteral().getString().toLowerCase(),1) ;
			    }
				double match = 0 ; 
				for(String term :Hierarchy.keySet() )
				{
					for(String term1 :goldstand.keySet() )
					{
					//	int found = goldstand.get(term); 
						LevenshteinDetailedDistance LDD = new LevenshteinDetailedDistance() ; 
						LevenshteinResults distance  = LDD.apply(term1, term ) ;
						if (distance.getDistance() < 5) 
						{
							match++ ; 
						}
					}
					
				}
				double prec = Hierarchy.size() > 0 ? match/Hierarchy.size():0 ;
				if (prec > 0.45) 
				{
					Measure.Recall +=  goldstand.size() > 0 ? (goldstand.size() > 1 ? 1:match/goldstand.size()): 0 ; 
					Measure.Precision +=  Hierarchy.size() > 0 ? match/Hierarchy.size():0 ;
					conceptcount++ ;
				}
				
			    qe.close(); 
		   }
		   
		   Measure.avgRecall  = Measure.Recall /conceptcount ; 
		   Measure.avgPrecision = Measure.Precision /conceptcount ;
		   
		    System.out.println(" Recall = " +  Measure.avgRecall);
		    System.out.println(" Precision = " +  Measure.avgPrecision );
		   
		   return Measure ; 

	 }
	 
	 public static measure MeasureDOfromfile(String filename , OntModel OntoGraph ) throws IOException, ParseException
	 {
		 
		 
		 
		   Map<String, String> concepts =  ReadXMLFile.Deserializeddiectionar("X:\\KG_ANR\\Clinical NotesGold\\HR.dat");
		 
		   double recall = 0 ; 
		   double precision = 0 ; 
		   measure Measure = new measure() ; 
		   int goldstandempty = 0 ;
		   int Hierarchyempty = 0 ; 
		   int goldstandemptynotafter = 0 ;
		   List<String> HierarchynotFound = new ArrayList<String>() ;
		   List<String> HierarchynotFoundDO = new ArrayList<String>() ;
		   Map<String, String> conceptsDO = new HashMap<String, String>();
		   int conceptcount = 0 ; 
		   int concepthasHrchycount = 0 ; 
		   //concepts.clear();
		  // concepts.put("aneurysm", "Disorders") ;
		   for (String concept : concepts.keySet())
		   {
			   if(!concepts.get(concept).equalsIgnoreCase("Disorders"))
				   continue ;
			   
			   conceptcount++ ;
			   
			   Measure.Recall = 0 ;
			   Measure.Precision = 0 ; 
		    	List<String> goldstand = new ArrayList<String>() ;
		    	List<String> Hierarchy = bioportal.getTaxonomic(concept, 3) ; 
		    	List<String> Hierlist = new ArrayList<String>() ;
		    	
				String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"  ;
				
			 
			    String sparql = prefix + " SELECT ?concept ?label WHERE { " +
	                    "?concept rdfs:label " +"\""  + concept + "\" . " + 
			    		"?concept rdfs:subClassOf* ?parent ." +
			    		"?parent  rdfs:label ?label ." + "}";
			    
				Query qry = QueryFactory.create(sparql);
				QueryExecution qe = QueryExecutionFactory.create(qry, OntoGraph);
				ResultSet rs = qe.execSelect();
			 
				RDFNode parent = null  ;
				while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			        RDFNode str = sol.get("concept"); 
			        parent = sol.get("label"); 
			        goldstand.add(parent.asLiteral().getString()) ;
			    }
				
				Map<String, Integer> sysns = null ; 
				
				if (goldstand.size() == 0 )
				{
					goldstandempty++ ;
					HierarchynotFoundDO.add(concept) ;
					sysns = bioportal.getSynonyms(concept) ;
					
					conceptsDO.put(concept, concepts.get(concept)) ;
					
				}
				
				/*// use synonyms	
				if (sysns != null)
				{
					for (String sysn : sysns.keySet())
					{
	
						String prefix1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
							"PREFIX owl: <http://www.w3.org/2002/07/owl#>"  ;
							
						 
						    String sparql1 = prefix1 + " SELECT ?concept ?label WHERE { " +
				                    "?concept rdfs:label " +"\""  + concept + "\" . " + 
						    		"?concept rdfs:subClassOf* ?parent ." +
						    		"?parent  rdfs:label ?label ." + "}";
						    
							Query qry1 = QueryFactory.create(sparql);
							QueryExecution qe1 = QueryExecutionFactory.create(qry1, OntoGraph);
							ResultSet rs1 = qe1.execSelect();
						 
							RDFNode parent1 = null  ;
							while(rs1.hasNext())
						    {
						        QuerySolution sol = rs1.nextSolution();
						        RDFNode str = sol.get("concept"); 
						        parent = sol.get("label"); 
						        goldstand.add(parent1.asLiteral().getString()) ;
						    }
							
							if (goldstand.size() > 0 ) 
							{
								goldstandemptynotafter++ ;
								break ;
							}
						
					}
				}*/
				
				// we consider only the exist concept with hierarchy otherwise leave 
				if (goldstand.size() == 0 )
				{
					continue ; 
				}
				
				if (Hierarchy.size() == 0 )
				{
					Hierarchyempty++ ;
					HierarchynotFound.add(concept) ;
					continue; 
				}
				
				concepthasHrchycount++ ;
				double match = 0 ; 
				for(int i = 0 ; i < goldstand.size() ; i++)
				{
					
					for(int j = 0 ; j < Hierarchy.size(); j++)
					{
						Soundex sxmatching = new Soundex() ;
						LevenshteinDetailedDistance LDD = new LevenshteinDetailedDistance() ; 
						LevenshteinResults distance  = LDD.apply(goldstand.get(i).toLowerCase(), Hierarchy.get(j).toLowerCase() ) ;
						String code = sxmatching.encode(goldstand.get(i)) ;
						String code1 = sxmatching.encode(Hierarchy.get(j)) ; 
						boolean mach = sxmatching.encode(goldstand.get(i)).equalsIgnoreCase(sxmatching.encode(Hierarchy.get(j))) ;
						if ( (goldstand.get(i).equalsIgnoreCase(Hierarchy.get(j)))|| sxmatching.encode(goldstand.get(i)).equalsIgnoreCase(sxmatching.encode(Hierarchy.get(j)))  ||  distance .getDistance() < 10 )
						{
							match++ ; 
						}
					}
					
				}
				
				int size =  match/goldstand.size() > 1 ?  goldstand.size() : 1 ;
				
				if (goldstand.size() > 0)
				{
					Measure.Recall +=  match/size > 1 ? 1: match/size ;
					
					Measure.Precision += match/Hierarchy.size() ;
				}
				
				Measure.avgRecall += Measure.Recall ;
				Measure.avgPrecision += Measure.Precision ;
				
			    qe.close(); 
		   }
		   
		   Measure.avgRecall  = Measure.avgRecall /concepthasHrchycount ; //concepts.size() ; 
		   Measure.avgPrecision = Measure.avgPrecision /concepthasHrchycount  ;// concepts.size() ;
		   
		   return Measure ; 

	 }
	 
}
