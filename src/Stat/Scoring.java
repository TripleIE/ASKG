package Stat;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class Scoring {

	 static String  lo = "http://www.lifeOnto.org/lifeOnto#" ;
	private static long clinicalNotesTotal = 0 ; 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public static void setClinicalNotesTotal(long Total )
	{
		clinicalNotesTotal = Total; 
	}
	public static void setOccurence_Probability(String conceptURI, Resource recPrimaryConcept, OntModel OntoGraph)
	{
		Resource r1 = null ; 
			
		if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
		{
			
			// get the frequency 
			
			final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
			Statement st = r1.getProperty(p2) ;
			RDFNode node = st.getObject();
			long frequency = node.asLiteral().getLong() ;
			
			
			// update the Occurence_Probability by one. 
			final Property p3 = ResourceFactory.createProperty(lo + "Occurence_Probability") ;
			Statement stm = r1.getProperty(p3) ;
			RDFNode node1 = stm.getObject();
			double Occurence_Probability = node1.asLiteral().getDouble() ;
			
			// (frequency/clinicalNotesTotal)/100
			Occurence_Probability = ( frequency /clinicalNotesTotal) / 100 ; 
			stm = stm.changeLiteralObject( Occurence_Probability) ;
		}
		else
		{	
			// set Occurence_Probability value to 0 in first time  
			final Property p2 = ResourceFactory.createProperty(lo + "Occurence_Probability") ;
			r1.addLiteral(p2, 0);
		}
		
	}
	
	public static void Tier_Rank(String conceptURI, Resource recPrimaryConcept, OntModel OntoGraph)
	{
		Resource r1 = null ; 
			
		if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
		{
	
			// update the Occurence_Probability by one. 
			final Property p1 = ResourceFactory.createProperty(lo + "Occurence_Probability") ;
			Statement stm = r1.getProperty(p1) ;
			RDFNode node = stm.getObject();
			double Occurence_Probability = node.asLiteral().getDouble() ;

			// update the Occurence_Probability by one. 
			final Property p2 = ResourceFactory.createProperty(lo + "Tier_Rank") ;
			Statement stm1 = r1.getProperty(p2) ;
			RDFNode node1 = stm1.getObject();
			int Tier_Rank = 0 ; 
			
			if (Occurence_Probability > 90.0 &&  Occurence_Probability <= 100.00)
				Tier_Rank = 1 ;
			else if (Occurence_Probability > 80.0 &&  Occurence_Probability <= 90.0)
				Tier_Rank = 2 ;
			else if (Occurence_Probability > 70.0 &&  Occurence_Probability <= 80.0)
				Tier_Rank = 3 ;
			else if (Occurence_Probability > 60.0 &&  Occurence_Probability <= 70.0)
				Tier_Rank = 4 ;
			else 
				Tier_Rank = 5 ;
				
			stm1 = stm1.changeLiteralObject( Tier_Rank) ;
				
		}
		else
		{	
			// set Occurence_Probability value to 0 in first time  
			final Property p2 = ResourceFactory.createProperty(lo + "Tier_Rank") ;
			r1.addLiteral(p2, 0);
		}
		
	}

}
