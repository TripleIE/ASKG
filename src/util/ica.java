package util;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ica {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OntModel OntoGraph = dataExtractor.getOntologyModel("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\ica.owl") ;
		isConcept("Basilar Artery",OntoGraph) ;
	}
	
	// consider the keyword as concept if the return search has class type 
	public static boolean  isConcept(String keyword, OntModel OntoGraph) 
    {
		
		OntoGraph = dataExtractor.getOntologyModel("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\ica.owl") ;
		keyword = keyword.replace(" ","_") ; 
		String prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"  ;
				
			 
			    String sparql = prefix + " SELECT ?concept WHERE { " +
	                    "?concept a owl:Class "  +   "}";
			    
				Query qry = QueryFactory.create(sparql);
				QueryExecution qe = QueryExecutionFactory.create(qry, OntoGraph);
				ResultSet rs = qe.execSelect();
			 
				RDFNode parent = null  ;
				while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			        RDFNode str = sol.get("concept"); 
			       String concept =  str.asResource().getLocalName();
	               if( concept != null && concept.equalsIgnoreCase(keyword))
	               {
	            	   return true ; 
	            	   
	               }
 
			    }
		return false ; 
    }

}
