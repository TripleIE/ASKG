package ONTO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import com.hp.hpl.jena.ontology.Individual;
import gui.client;
import org.bytedeco.javacpp.annotation.Const;
import org.json.simple.parser.ParseException;

import util.Word2vec;
import util.bioportal;
import util.dataExtractor;
import util.readfiles;
import HRCHY.hierarchy;
import RICH.Enrichment;
import Stat.Scoring;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import word2vec.Word2VecRawTextExample;
import util.Word2vec ;

public class BioPontologyfactory {

   public static  String skos = "http://www.w3.org/2004/02/skos/core#" ;
   static String  rdfs = "http://www.w3.org/2000/01/rdf-schema#" ;
   static String  rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ;
   static String  owl = "http://www.w3.org/2002/07/owl#" ;
   public static String  lo = "http://www.lifeOnto.org/lifeOnto#" ;
   public static String  ilo = "http://www.lifeOnto.org/lifeOnto_instance#" ;
   static String  ica=  "http://www.mii.ucla.edu/~willhsu/ontologies/ica_ontology#" ;
   final static  double THRESHOLD = 0.50 ;

   
   /****************************************************************
    *             setting for turning recording relations
    */
   static public boolean evl = true; 
   static public String relResult = "X:\\KG_ANR\\Clinical NotesGold\\W2V10relations.xml" ;
   
   
   // An ontology model is an extension of the Jena RDF model 
  // static    OntModel OntoGraph = ModelFactory.createOntologyModel(); 
	
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		riskFactorToOnto1(null,"alcohol", null) ;
//		ethnicityToOnto("Venous Thromboembolism whiteguy Black","alcohol",null);

	}

	
	public static OntModel createOntoBioP (String concept,OntModel OntoGraph) throws IOException, ParseException
	{

		// generating owl:class
		String URI = classToOnto (concept,OntoGraph);
		if(URI != null)
		{
			sameAsToOnto(concept,URI,OntoGraph) ;
		    prefLabelToOnto(concept,URI,OntoGraph) ;
		    //OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
			synonymToOnto(concept,URI,OntoGraph)	;
			definitionToOnto (concept,URI,OntoGraph) ;
			semTypeToOnto(concept,URI,OntoGraph) ;
			//OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
			//loadTaxonomic(concept,URI,1,OntoGraph);	
			//OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
		}
		return OntoGraph ;
	}
	
	public static OntModel createOntoBioP (String concept) throws IOException, ParseException
	{
		OntModel OntoGraph = ModelFactory.createOntologyModel();
		OntoGraph.setNsPrefix( "skos", skos ) ;
		
		// generating owl:class
		String URI = classToOnto (concept,OntoGraph);
		sameAsToOnto(concept,URI,OntoGraph) ;
	    prefLabelToOnto(concept,URI,OntoGraph) ;
	   // OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
		synonymToOnto(concept,URI,OntoGraph)	;
		definitionToOnto (concept,URI,OntoGraph) ;
		semTypeToOnto(concept,URI,OntoGraph) ;
		OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
//		loadTaxonomic(concept,URI,1,OntoGraph);	
		//OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
		return OntoGraph ;
	}
	
	
	 // adding a concept as owl class and rdfs:label
	public static String classToOntowithfrq(String concept,String SemGroup, OntModel OntoGraph)
	{
		// the URI is equal to preflabel uri
		String conceptURI = bioportal.getConceptID(concept);
		System.out.println("classToOnto");
		Resource r = null ; 
		
		// create app URI
		if(conceptURI == null )
		{
			conceptURI = lo + concept.replace(" ", "_") ;
		}
			
		//if (conceptURI != null )
		{
			if (( r= OntoGraph.getOntClass(conceptURI) ) == null)
			{
				OntClass rec = OntoGraph.createClass(conceptURI);
				// assign a Label 
				final Property p = ResourceFactory.createProperty(rdfs + "label") ;
				rec.addProperty(p, concept);
				
				final Property p1 = ResourceFactory.createProperty(lo + "has_frequency") ;
				rec.addLiteral(p1, 1);
				
				final Property p2 = ResourceFactory.createProperty(lo + "Semantic_Group") ;
				rec.addLiteral(p2, SemGroup);
				
				return conceptURI ;
			}
			else
			{
				// add the frequency 
				final Property p2 = ResourceFactory.createProperty(lo + "has_frequency") ;
				Statement st = r.getProperty(p2) ;
				if (st != null)
				{
					RDFNode node = st.getObject();
					long value = node.asLiteral().getLong() ;
					st = st.changeLiteralObject(value+1) ;
				}
				return conceptURI ;
			}
		}
	}
	
	 // adding a concept as owl class and rdfs:label and create instance
		public static String classToOntowithfrq_avg(String concept,String SemGroup,float total, OntModel OntoGraph)
		{
			// the URI is equal to preflabel uri
			String conceptURI = bioportal.getConceptID(concept);
			System.out.println("classToOnto");
			Resource r = null ; 
			
			// create app URI
			if(conceptURI == null )
			{
				conceptURI = lo + concept ;
			}
				
			//if (conceptURI != null )
			{
				if (( r= OntoGraph.getOntClass(conceptURI) ) == null)
				{
					OntClass rec = OntoGraph.createClass(conceptURI);
					// create instance out of each class.
					instanceToOnto(concept,rec,OntoGraph) ;

					// assign a Label 
					final Property p = ResourceFactory.createProperty(rdfs + "label") ;
					rec.addProperty(p, concept);
					
					final Property p1 = ResourceFactory.createProperty(lo + "has_frequency") ;
					rec.addLiteral(p1, 1);
					
					final Property p3 = ResourceFactory.createProperty(lo + "avg") ;
					rec.addLiteral(p3, 1/total);
					
					final Property p2 = ResourceFactory.createProperty(lo + "Semantic_Group") ;
					rec.addLiteral(p2, SemGroup);
					
					return conceptURI ;
				}
				else
				{
					// add the frequency 
					final Property p2 = ResourceFactory.createProperty(lo + "has_frequency") ;
					Statement st = r.getProperty(p2) ;
					if (st != null)
					{
						RDFNode node = st.getObject();
						long value = node.asLiteral().getLong() ;
						st = st.changeLiteralObject(value+1) ;
					}
					
					// add the AVG 
					final Property p3 = ResourceFactory.createProperty(lo + "has_AVG") ;
					Statement stm = r.getProperty(p3) ;
					if (stm != null)
					{
						RDFNode node = stm.getObject();
						float value = node.asLiteral().getFloat();
						stm = stm.changeLiteralObject(value/total) ;
					}
					return conceptURI ;
				}
			}
		}
	
	    // adding a concept as owl class and rdfs:label
		public static String classToOnto (String concept,String SemGroup, OntModel OntoGraph)
		{
			// the URI is equal to preflabel uri
			String conceptURI = bioportal.getConceptID(concept);
			System.out.println("classToOnto");
			Resource r = null ; 
			
			// create app URI
			if(conceptURI == null )
			{
				conceptURI = lo + concept ;
			}
				
			//if (conceptURI != null )
			{
				if (( r= OntoGraph.getOntClass(conceptURI) ) == null)
				{
					OntClass rec = OntoGraph.createClass(conceptURI);
					// assign a Label 
					final Property p = ResourceFactory.createProperty(rdfs + "label") ;
					rec.addProperty(p, concept);
					
					final Property p1 = ResourceFactory.createProperty(lo + "has_frequency") ;
					rec.addLiteral(p1, 1);
					
					final Property p2 = ResourceFactory.createProperty(lo + "Semantic_Group") ;
					rec.addLiteral(p2, SemGroup);
					
					return conceptURI ;
				}
				else
				{
/*					// add the frequency 
					final Property p2 = ResourceFactory.createProperty(lo + "has_frequency") ;
					Statement st = r.getProperty(p2) ;
					if (st != null)
					{
						RDFNode node = st.getObject();
						long value = node.asLiteral().getLong() ;
						st = st.changeLiteralObject(value+1) ;
					}*/
					return conceptURI ;
				}
			}
		}
		
		       
	
	// adding a concept as owl class and rdfs:label and the frequency 
	public static String classToOnto (String concept,OntModel OntoGraph)
	{
		// the URI is equal to preflabel uri
		String conceptURI = bioportal.getConceptID(concept);
		System.out.println("classToOnto");
		Resource r = null ; 
		
		// create app URI
		if(conceptURI == null )
		{
			conceptURI = lo + concept.replace(" ", "_") ;
		}
		
		if (conceptURI != null )
		{
			if (( r= OntoGraph.getOntClass(conceptURI) ) == null)
			{
				OntClass rec = OntoGraph.createClass(conceptURI);
				// assign a Label 
				final Property p = ResourceFactory.createProperty(rdfs + "label") ;
				rec.addProperty(p, concept);
				
				final Property p1 = ResourceFactory.createProperty(lo + "has_frequency") ;
				rec.addLiteral(p1, 1);
				return conceptURI ;
			}
			else
			{
				return conceptURI ;
			}
			/*else
			{
				final Property p2 = ResourceFactory.createProperty(lo + "has_frequency") ;
				Statement st = r.getProperty(p2) ;
				RDFNode node = st.getObject();
				long value = node.asLiteral().getLong() ;
				st = st.changeLiteralObject(value+1) ;
			}*/
		}
		
		return null;
	}

	// adding a concept as owl class and rdfs:label and the frequency
	public static OntClass classToontclass (String concept,OntModel OntoGraph)
	{
		// the URI is equal to preflabel uri
		String conceptURI = bioportal.getConceptID(concept);
		System.out.println("classToOnto");
		Resource r = null ;

		// create app URI
		if(conceptURI == null )
		{
			conceptURI = lo + concept.replace(" ", "_") ;
		}

		if (conceptURI != null )
		{
			if (( r= OntoGraph.getOntClass(conceptURI) ) == null)
			{
				OntClass rec = OntoGraph.createClass(conceptURI);
				// assign a Label
				final Property p = ResourceFactory.createProperty(rdfs + "label") ;
				rec.addProperty(p, concept);

				final Property p1 = ResourceFactory.createProperty(lo + "has_frequency") ;
				rec.addLiteral(p1, 1);
				return rec ;
			}

			/*else
			{
				final Property p2 = ResourceFactory.createProperty(lo + "has_frequency") ;
				Statement st = r.getProperty(p2) ;
				RDFNode node = st.getObject();
				long value = node.asLiteral().getLong() ;
				st = st.changeLiteralObject(value+1) ;
			}*/
		}

		return null;
	}

	public static Individual  instanceToOnto (String concept,OntClass ontoclass, OntModel OntoGraph)
	{

		Resource r = null ;

		// uri - The URI of the new individual
			String conceptURI = ilo + concept.replace(" ", "_") ;

		if (conceptURI != null )
		{

			{
				//Answer a new individual that has this class as its rdf:type

				Individual regIndividual = ontoclass.createIndividual(conceptURI);
				// assign a Label
				final Property p = ResourceFactory.createProperty(rdfs + "label") ;
				regIndividual.addProperty(p, concept);

				final Property p1 = ResourceFactory.createProperty(lo + "has_frequency") ;
				regIndividual.addLiteral(p1, 1);
				return regIndividual  ;
			}
			/*else
			{
				final Property p2 = ResourceFactory.createProperty(lo + "has_frequency") ;
				Statement st = r.getProperty(p2) ;
				RDFNode node = st.getObject();
				long value = node.asLiteral().getLong() ;
				st = st.changeLiteralObject(value+1) ;
			}*/
		}

		return null;
	}
	// adding a concept as owl class and rdfs:label
	public static OntClass classToOnto_URI (String conceptURI,OntModel OntoGraph)
	{
		// the URI is equal to preflabel uri
		System.out.println("classToOnto");
		if (conceptURI != null )
		{
			OntClass rec = OntoGraph.createClass(conceptURI);
			return rec ;
		}
		return null;
	}
	// adding a concept as owl class and rdfs:label
	public static OntClass classToOnto_URI (String conceptURI,String label, OntModel OntoGraph)
	{
		// the URI is equal to preflabel uri
		System.out.println("classToOnto");
		if (conceptURI != null )
		{
			OntClass rec = OntoGraph.createClass(conceptURI);
			// assign a Label 
			final Property p = ResourceFactory.createProperty(rdfs + "label") ;
			rec.addProperty(p, label);
			
			return rec ;
		}
		return null;
	}
	
	// generating synonyms with skos:altLabel
	public static void synonymToOnto (String concept,String URI,OntModel OntoGraph) throws IOException, ParseException
	{
		
		// generating synonyms wiht skos:altLabel
		Map<String, Integer> Synonyms =  bioportal.getSynonyms(concept);
		System.out.println("getontoSynonym");
		Resource r = null ; 
		for (String syn: Synonyms.keySet())
		{
			if ( ( r= OntoGraph.getOntClass(URI) ) != null)
			{
				final Property p = ResourceFactory.createProperty(skos + "altLabel") ;
				r.addProperty(p, syn);
			}
			else
				break ; 
			
		}
	}
	
	// generating prefLable with skos:prefLabel
	public static void prefLabelToOnto(String concept,String URI,OntModel OntoGraph) throws IOException, ParseException
	{
		
		// generating synonyms wiht skos:altLabel
		String prefLabel = bioportal.getPrefLabels(concept);
		System.out.println("prefLabel");
		if(prefLabel != null)
		{
			Resource r = null ; 
			if ( ( r= OntoGraph.getOntClass(URI) ) != null)
			{
					final Property p = ResourceFactory.createProperty(skos + "prefLabel") ;
					r.addProperty(p, prefLabel);
			}
		}
	}
	
	
	public static void loadTaxonomic(String concept,String URI,OntModel OntoGraph) throws IOException, ParseException {
		
		
		// if the concept has already hierarchy do not generate new one
		if (bioportal.getHierarchyURI(URI,OntoGraph).size() > 0 )
			return ; 
		
		List<String>   listTaxon = bioportal.getTaxonomic(concept,1) ; 
 
		// get the class reference 
		OntClass child = OntoGraph.getOntClass(URI) ; 
		OntClass r1 = null ; 
		// loop though hierarchy 
		for(String hier: listTaxon)
		{
			// get the uri of the parent 
			String conceptURI = bioportal.getConceptID(hier);
			if(conceptURI ==  null)
				continue ; 
			// check if the parent already exit in the graph 
			if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
			{
				child.addSuperClass(r1);
			}
			else
			{
				// create new class and assign it as parent 
				String uri = classToOnto (hier,OntoGraph);
				r1 = OntoGraph.getOntClass(uri) ;
				final Property p1 = ResourceFactory.createProperty(rdfs + "label") ;
				r1.addProperty(p1, hier);
				child.addSuperClass(r1);
			}
			child = r1 ; 
		}

	}
	
	// generating definition 
	public static void definitionToOnto (String concept,String URI,OntModel OntoGraph) throws IOException, ParseException
	{
		
		// generating synonyms wiht skos:altLabel
		Map<String, Integer> defs =  bioportal.getDefinitions(concept);
		System.out.println("getontoDefinition");
		Resource r = null ; 
		for (String def:  defs.keySet())
		{
			if ( ( r= OntoGraph.getOntClass(URI) ) != null)
			{
				final Property p = ResourceFactory.createProperty(skos + "definition") ;
				r.addProperty(p, def);
			}
			else
				break ; 
			
		}
	}
	
	public static void semTypeToOnto (String concept,String URI,OntModel OntoGraph) throws IOException, ParseException
	{
		
		// generating synonyms wiht skos:altLabel
		Map<String, Integer> semTypes =  bioportal.getSemanticTypes(concept);
		System.out.println("getontoSemantic Type");
		Resource r = null ; 
		for (String semType:  semTypes.keySet())
		{
			if ( ( r= OntoGraph.getOntClass(URI) ) != null)
			{
				final Property p = ResourceFactory.createProperty(skos + "type") ;
				r.addProperty(p, semType);
				
				
				final Property p1 = ResourceFactory.createProperty(lo + "Semantic_Type_Label") ;
				r.addProperty(p1, bioportal.getSemanticTypeNames(semType));
			}
			else
				break ; 
			
		}
	}
	public static void sameAsToOnto (String concept,String URI,OntModel OntoGraph) throws IOException, ParseException
	{
		
		// generating synonyms wiht skos:altLabel
		Map<String, Integer> sameases =  bioportal.getSameas(concept,URI);
		System.out.println("sameAsToOnto Type");
		Resource r = null ; 
		if (sameases == null)
			return ; 
		for (String sameas:  sameases.keySet())
		{
			if ( ( r= OntoGraph.getOntClass(URI) ) != null)
			{
				final Property p = ResourceFactory.createProperty(owl + "sameAs") ;
				r.addProperty(p,sameas);
			}
			else
				break ; 
			
		}
	}

	// create relation has_Disease_Association between discovered disorder concepts and CN
	public static void has_Aneurysm(OntModel OntoGraph, String CN,String classURI) throws IOException
	{
		System.out.println("has_Aneurysm");
		Resource rout = null ;
		{
			// Word2vec
			double score = 1 ; // Word2VecRawTextExample.score(disorder.get(i),disorder.get(j), client.vec) ;
			String conceptURIout = bioportal.getConceptID("Intracranial Aneurysm");
			Individual individual  = instanceToOnto(CN,OntoGraph.getOntClass(classURI),OntoGraph);
			if (individual != null  && score  > 0.5)
			{
				final Property has_Aneurysm = ResourceFactory.createProperty(lo + "has_Aneurysm") ; // create new property
				individual.addProperty(has_Aneurysm,rout);

				if (evl)
				{
					readfiles.Writestringtofile("  <Rel>" + CN + ",has_Aneurysm," + "Intracranial Aneurysm" + "</Rel>",  relResult);
				}

			}
		}

	}

	// create relation has_Disease_Association between discovered disorder concepts and CN
	public static void has_Disease_Association_into(Map<String, String> concepts,OntModel OntoGraph, String CN) throws IOException
	{
		List<String> disorder = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Disorders"))
				disorder.add(cp) ;
		}


		// create new has_Disease_Association relation between the discovered concepts and CN

		System.out.println("has_Disease_Association");
		Resource rout = null ;
		Resource rin = null ;
		for (int i = 0; i < disorder.size(); i++)
		{


				// Word2vec
				double score = 1 ; // Word2VecRawTextExample.score(disorder.get(i),disorder.get(j), client.vec) ;
				String conceptURIout = bioportal.getConceptID(disorder.get(i));
				if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null  && score  > 0.5)
				{
					Resource cn  = OntoGraph.getOntClass(CN) ;
					final Property has_Disease_Association = ResourceFactory.createProperty(lo + "has_Disease_Association") ; // create new property
					cn.addProperty(has_Disease_Association ,rout);

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + cn + ",has_Disease_Association," + disorder.get(i) + "</Rel>",  relResult);
					}

				}
		}

	}
	// create relation has_Disease_Association between discovered disorder concepts and CN
	public static void has_Disease_Association(Map<String, String> concepts,OntModel OntoGraph) throws IOException
	{
		List<String> disorder = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Disorders"))
				disorder.add(cp) ;
		}


		// create new has_Disease_Association relation between the discovered concepts

		System.out.println("has_Disease_Association");
		Resource rout = null ;
		Resource rin = null ;
		for (int i = 0; i < disorder.size(); i++)
		{
			for (int j = i+1 ; j < disorder.size(); j++)
			{
				if (disorder.get(i).equalsIgnoreCase(disorder.get(j)))
					continue ;
                 // Word2vec
				double score = 1 ; // Word2VecRawTextExample.score(disorder.get(i),disorder.get(j), client.vec) ;
				String conceptURIout = bioportal.getConceptID(disorder.get(i));
				String conceptURIin = bioportal.getConceptID(disorder.get(j));
				if (conceptURIout != null && conceptURIin != null )

					if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null && ( rin = OntoGraph.getOntClass(conceptURIin) ) != null && score  > 0.5)
					{
						final Property has_Disease_Association = ResourceFactory.createProperty(lo + "has_Disease_Association") ; // create new property
						rout.addProperty(has_Disease_Association ,conceptURIin);

						if (evl)
						{
							readfiles.Writestringtofile("  <Rel>" + disorder.get(i) + ",has_Disease_Association," + disorder.get(j) + "</Rel>",  relResult);
						}

					}
			}


		}

	}


	// create relation has_Race between discovered Gender & Race concepts 
	public static void has_Race(Map<String, String> concepts,OntModel OntoGraph) throws IOException, ParseException
	{
		List<String> gender = new ArrayList<String>();
		
		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Gender"))
				gender.add(cp) ;
		}
		
		List<String> race = new ArrayList<String>();
		
		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Race"))
				race.add(cp) ;
		}
		
		// create new has_Race relation between the discovered concepts

		System.out.println("has_Race");
		Resource rout = null ;
		Resource rin = null ;
        for (int i = 0; i < gender.size(); i++)
		{
        	for (int j = 0 ; j < race.size(); j++)
        	{
 
        		 String conceptURIout = lo + gender.get(i);
        		 String conceptURIin = lo + race.get(j);
        		 if (conceptURIout != null && conceptURIin != null )
        		 {
        			 if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null && ( rin = OntoGraph.getOntClass(conceptURIin) ) != null) 
        			 {
        				 final Property has_Disease_Association = ResourceFactory.createProperty(lo + "has_Race") ; // create new property 
        				 rout.addProperty(has_Disease_Association ,conceptURIin);
        				 
        				 if (evl)
        				 {
        					 readfiles.Writestringtofile("  <Rel>" + gender.get(i) + ",has_Race," + race.get(j) + "</Rel>",  relResult);
        				 }
        				 // get the concept resource 
        			 }
        		 }
        			 
        			 
        	}
		}
			
	}
	

	
	
	// create relation has_Symptom between discovered disorder & sign concepts 
	public static void has_Symptom(Map<String, String> concepts,OntModel OntoGraph) throws IOException, ParseException
	{
		List<String> disorder = new ArrayList<String>();
		
		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Disorders"))
				disorder.add(cp) ;
		}
		
		List<String> sign = new ArrayList<String>();
		
		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Sign or Symptom"))
				sign.add(cp) ;
		}
		
		// create new has_Symptom relation between the discovered concepts

		System.out.println("has_Symptom");
		Resource rout = null ;
		Resource rin = null ;
        for (int i = 0; i < disorder.size(); i++)
		{
        	for (int j = 0 ; j < sign.size(); j++)
        	{
				 double score = 1; // Word2VecRawTextExample.score(disorder.get(i),sign.get(j), client.vec) ;
        		 String conceptURIout = bioportal.getConceptID(disorder.get(i));
        		 String conceptURIin = bioportal.getConceptID(sign.get(j));
        		 if (conceptURIout != null && conceptURIin != null )
        		 {
        			 if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null && ( rin = OntoGraph.getOntClass(conceptURIin) ) != null) 
        			 {
        				 final Property has_Disease_Association = ResourceFactory.createProperty(lo + "has_Symptom") ; // create new property 
        				 rout.addProperty(has_Disease_Association ,conceptURIin);
        				 
        				 if (evl)
        				 {
        					 readfiles.Writestringtofile("  <Rel>" + disorder.get(i) + ",has_Symptom," + sign.get(j) + "</Rel>",  relResult);
        				 }
        				 // get the concept resource 
        			 }
        		 }
        			 
        			 
        	}
		}
			
	}
	

	// create relation Tread_by between discovered disorder & chemical concepts 
		public static void Tread_by (Map<String, String> concepts,OntModel OntoGraph) throws IOException, ParseException
		{
			List<String> disorder = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				String test = concepts.get(cp).trim();
				if (concepts.get(cp).trim().equalsIgnoreCase("Disorders"))
					disorder.add(cp) ;
			}
			
			List<String> chem = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Chemicals or Drugs"))
					chem.add(cp) ;
			}
			
			// create new has_Symptom relation between the discovered concepts

			System.out.println("Tread_by");
			Resource rout = null ;
			Resource rin = null ;
	        for (int i = 0; i < disorder.size(); i++)
			{
	        	for (int j = 0 ; j < chem.size(); j++)
	        	{
	 
	        		 String conceptURIout = bioportal.getConceptID(disorder.get(i));
	        		 String conceptURIin = bioportal.getConceptID(chem.get(j));
	        		 if (conceptURIout != null && conceptURIin != null )
	        		 {
	        			 if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null && ( rin = OntoGraph.getOntClass(conceptURIin) ) != null) 
	        			 {
	        				 final Property has_Disease_Association = ResourceFactory.createProperty(lo + "Tread_by") ; // create new property 
	        				 rout.addProperty(has_Disease_Association ,conceptURIin);
	        				 
	        				 if (evl)
	        				 {
	        					 readfiles.Writestringtofile("  <Rel>" + disorder.get(i) + ",Tread_by," + chem.get(j) + "</Rel>",  relResult);
	        				 }
	        				 // get the concept resource 
	        			 }
	        		 }
	        			 
	        			 
	        	}
			}
				
		}
		

	
		
		// create relation Diagnosed_by between discovered disorder & Tests & Procedures
				public static void Diagnosed_by (Map<String, String> concepts,OntModel OntoGraph) throws IOException, ParseException
				{
					List<String> disorder = new ArrayList<String>();
					
					for (String cp:  concepts.keySet())
					{
						if (concepts.get(cp).trim().equalsIgnoreCase("Disorders"))
							disorder.add(cp) ;
					}
					
					List<String> diag = new ArrayList<String>();
					
					for (String cp:  concepts.keySet())
					{
						if (concepts.get(cp).trim().equalsIgnoreCase("Tests or Procedures"))
							diag.add(cp) ;
					}
					
					// create new has_Symptom relation between the discovered concepts

					System.out.println("Diagnosed_by");
					Resource rout = null ;
					Resource rin = null ;
			        for (int i = 0; i < disorder.size(); i++)
					{
			        	for (int j = 0 ; j < diag.size(); j++)
			        	{
			 
			        		 String conceptURIout = bioportal.getConceptID(disorder.get(i));
			        		 String conceptURIin = bioportal.getConceptID(diag.get(j));
			        		 if (conceptURIout != null && conceptURIin != null )
			        		 {
			        			 if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null && ( rin = OntoGraph.getOntClass(conceptURIin) ) != null) 
			        			 {
			        				 final Property has_Disease_Association = ResourceFactory.createProperty(lo + "Diagnosed_by") ; // create new property 
			        				 rout.addProperty(has_Disease_Association ,conceptURIin);
			        				 
			        				 if (evl)
			        				 {
			        					 readfiles.Writestringtofile("  <Rel>" + disorder.get(i) + ",Diagnosed_by," + diag.get(j) + "</Rel>",  relResult);
			        				 }
			        				 // get the concept resource 
			        			 }
			        		 }
			        			 
			        			 
			        	}
					}
						
				}


				// create relation Vessel_Side discovered location & side
				public static void Vessel_Side (Map<String, String> concepts,OntModel OntoGraph,OntModel ICAOnto) throws IOException, ParseException
				{
					List<String> loc= new ArrayList<String>();
					
					for (String cp:  concepts.keySet())
					{
						if (concepts.get(cp).trim().equalsIgnoreCase("Location"))
							loc.add(cp) ;
					}
					
					List<String> side = new ArrayList<String>();
					
					for (String cp:  concepts.keySet())
					{
						if (concepts.get(cp).trim().equalsIgnoreCase("Side"))
							side.add(cp) ;
					}
					
					// create new has_Symptom relation between the discovered concepts

					System.out.println("Vessel_Side");
					Resource rout = null ;
					Resource rin = null ;
			        for (int i = 0; i < loc.size(); i++)
					{
			        	for (int j = 0 ; j < side.size(); j++)
			        	{
			 
			        		 String conceptURIout = bioportal.getLocationURI(loc.get(i), ICAOnto);
			        		 String conceptURIin = bioportal.getSide(side.get(j), ICAOnto);
			        		 
			        		 if (conceptURIout != null && conceptURIin != null )
			        		 {
			        			 if (( rout = OntoGraph.getOntClass(conceptURIout) ) != null && ( rin = OntoGraph.getOntClass(conceptURIin) ) != null) 
			        			 {
			        				 final Property has_Disease_Association = ResourceFactory.createProperty(lo + "Vessel_Side") ; // create new property 
			        				 rout.addProperty(has_Disease_Association ,conceptURIin);
			        				 
			        				 if (evl)
			        				 {
			        					 readfiles.Writestringtofile("  <Rel>" + loc.get(i) + ",Vessel_Side," + side.get(j) + "</Rel>",  relResult);
			        				 }
			        				 // get the concept resource 
			        			 }
			        		 }
			        			 
			        			 
			        	}
					}
						
				}		


				
				// create relation has_Riskfactor
				public static void has_Riskfactor(Map<String, String> concepts,String PrimaryConcept, OntModel OntoGraph,OntModel ICAOnto) throws IOException, ParseException
				{
					List<String> rk= new ArrayList<String>();
					
					for (String cp:  concepts.keySet())
					{
						if (concepts.get(cp).trim().equalsIgnoreCase("has_Riskfactor"))
							rk.add(cp) ;
					}
					
					
					// create new has_Size relation between the discovered concepts

					System.out.println("has_Riskfactor");
					
					String cptURI = PrimaryConcept ; ///bioportal.getConceptID(PrimaryConcept);
					Resource recPrimaryConcept = null ;

					for (String rsk:  rk)
					{
						rsk = bioportal.getRiskFactorURI(rsk, ICAOnto) ;
												
						if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null && rsk != null && !recPrimaryConcept.toString().equalsIgnoreCase(rsk.toString()) ) // get the primary concept resource
						{
							final Property has_Riskfactor  = ResourceFactory.createProperty(lo + "has_Riskfactor") ; // create new property
							recPrimaryConcept.addProperty(has_Riskfactor,rsk);
							
	        				 if (evl)
	        				 {
	        					 readfiles.Writestringtofile("  <Rel>" + PrimaryConcept+ ",has_Riskfactor," + rsk + "</Rel>",  relResult);
	        				 }
						}
					}		
				}
				
				

				
		// create relation Vascular_Location 
		public static void Vascular_Location (Map<String, String> concepts,String PrimaryConcept, OntModel OntoGraph,OntModel ICAOnto ) throws IOException, ParseException
		{
			List<String> loc= new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Location"))
					loc.add(cp) ;
			}
			
			
			// create new has_Size relation between the discovered concepts

			System.out.println("Vascular_Location ");
			String cptURI = PrimaryConcept ; // bioportal.getConceptID(PrimaryConcept);
			Resource recPrimaryConcept = null ;

			for (String loct:  loc)
			{
				if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null) // get the primary concept resource
				{
					loct = bioportal.getLocationURI(loct, ICAOnto); 
					final Property Vascular_Location  = ResourceFactory.createProperty(lo + "Vascular_Location") ; // create new property
					recPrimaryConcept.addProperty(Vascular_Location ,loct);
					
	   				 if (evl)
	   				 {
	   					 readfiles.Writestringtofile("  <Rel>" + PrimaryConcept+ ",Vascular_Location," + loct + "</Rel>",  relResult);
	   				 }
				}
			}		
		}
		

	// create relation has_Size between discovered disorder concepts 
		public static void has_Size(Map<String, String> concepts,String PrimaryConcept, OntModel OntoGraph) throws IOException, ParseException
		{
			List<String> size = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Size"))
					size.add(cp) ;
			}
			
			
			// create new has_Size relation between the discovered concepts

			System.out.println("has_Size");
			String cptURI = PrimaryConcept ; //bioportal.getConceptID(PrimaryConcept);
			Resource recPrimaryConcept = null ;

			for (String sze:  size)
			{
				if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null) // get the primary concept resource
				{
					sze = ilo + sze;
					final Property has_Size = ResourceFactory.createProperty(lo + "has_Size") ; // create new property
					recPrimaryConcept.addProperty(has_Size,sze);
				}
  				 if (evl)
  				 {
  					 readfiles.Writestringtofile("  <Rel>" + PrimaryConcept+ ",has_Size," + sze + "</Rel>",  relResult);
  				 }
			}		
		}
		

		
		// create relation Rupture_Status between discovered disorder concepts 
		public static void Rupture_Status(Map<String, String> concepts,String PrimaryConcept, OntModel OntoGraph) throws IOException, ParseException
		{
			List<String> rp = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Status"))
					rp.add(cp) ;
			}
			
			
			// create new has_Size relation between the discovered concepts

			System.out.println("Rupture_Status");
			String cptURI = PrimaryConcept ; //bioportal.getConceptID(PrimaryConcept);
			Resource recPrimaryConcept = null ;

			for (String rpt:  rp)
			{
				if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null) // get the primary concept resource
				{
					final Property has_Size = ResourceFactory.createProperty(lo + "Rupture_Status") ; // create new property
					recPrimaryConcept.addProperty(has_Size,rpt);
					
	  				 if (evl)
	  				 {
	  					 readfiles.Writestringtofile("  <Rel>" + PrimaryConcept+ ",Rupture_Status," + rpt + "</Rel>",  relResult);
	  				 }
				}
			}		
		}
		

		
		// create relation has_Age between discovered disorder concepts 
		public static void has_Age(Map<String, String> concepts,String PrimaryConcept, OntModel OntoGraph) throws IOException, ParseException
		{
			List<String> age = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Age"))
					age.add(cp) ;
			}
			
			
			// create new has_Age relation between the discovered concepts

			System.out.println("has_Age");
			String cptURI = PrimaryConcept ; //bioportal.getConceptID(PrimaryConcept);
			Resource recPrimaryConcept = null ;

			for (String ag:  age)
			{
				if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null) // get the primary concept resource
				{

					final Property has_Age = ResourceFactory.createProperty(lo + "has_Age") ; // create new property
					recPrimaryConcept.addProperty(has_Age,ag);
					
	  				 if (evl)
	  				 {
	  					 readfiles.Writestringtofile("  <Rel>" + PrimaryConcept+ ",has_Age," + ag + "</Rel>",  relResult);
	  				 }
				}
			}		
		}

	// create instance relation Rupture_Status
	public static void iRupture_Status(Map<String, String> concepts,OntModel OntoGraph,String IAURI) throws IOException, ParseException
	{
		List<String> rp = new ArrayList<String>();
		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Status"))
				rp.add(cp) ;
		}

		System.out.println("iRupture_Status");
		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(IAURI)) != null) // get the primary concept resource
		{
			for (String cp : rp)
			{

				Individual icp = getIndividual(cp, OntoGraph);

				if (icp != null)
				{
					final Property Rupture_Status = ResourceFactory.createProperty(lo + "Rupture_Status"); // create new property
					rec.addProperty(Rupture_Status, icp.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + IAURI + ",Rupture_Status" + icp.getURI() + "," + Double.toString(Word2vec.score(cp)) + " </Rel>",  relResult);
					}
				}

			}
		}
	}

	// create instance relation has_Type
	public static void ihas_Type(Map<String, String> concepts, OntModel OntoGraph,OntModel ICAOnto,String IAURI ) throws IOException, ParseException
	{
		List<String> type = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Type"))
				type.add(cp) ;
		}


		// create new has_Type relation between the discovered concepts

		System.out.println("has_Type");

		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(IAURI)) != null) // get the primary concept resource
		{
			for (String cp : type)
			{

				Individual icp = getIndividual(cp, OntoGraph);

				if (icp != null)
				{
					final Property has_Type= ResourceFactory.createProperty(lo + "has_Type"); // create new property
					rec.addProperty(has_Type, icp.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + IAURI + ",has_Type," + icp.getURI() + "," + Double.toString(Word2vec.score(cp)) + " </Rel>",  relResult);
					}
				}

			}
		}
	}
	// create instance relation Vessel_Side
	public static void iVessel_Side (Map<String, String> concepts,OntModel OntoGraph,String IAURI) throws IOException, ParseException
	{

		List<String> side = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Side"))
				side.add(cp) ;
		}

		// create new has_Symptom relation between the discovered concepts

		System.out.println("Vessel_Side");
		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(IAURI)) != null) // get the primary concept resource
		{
			for (String cp : side)
			{

				Individual icp = getIndividual(cp, OntoGraph);

				if (icp != null)
				{
					final Property Vessel_Side = ResourceFactory.createProperty(lo + "Vessel_Side"); // create new property
					rec.addProperty(Vessel_Side, icp.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + IAURI+ ",Vessel_Side," + icp.getURI() + "," + Double.toString(Word2vec.score(cp)) + " </Rel>",  relResult);
					}

				}

			}
		}
	}


	// create instance relation has_Size
	public static void ihas_Size(Map<String, String> concepts,OntModel OntoGraph,String IAURI) throws IOException, ParseException
	{
		List<String> size = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Size"))
				size.add(cp) ;
		}

		// create new has_Size relation between the discovered concepts

		System.out.println("has_Size");

		// create new has_Size relation between the discovered concepts


		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(IAURI)) != null) // get the primary concept resource
		{
			for (String cp : size)
			{

				Individual icp = getIndividual(cp, OntoGraph);

				if (icp != null)
				{
					final Property has_size = ResourceFactory.createProperty(lo + "has_Size"); // create new property
					rec.addProperty(has_size, icp.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + IAURI + ",has_Size," + icp.getURI() + "," + Double.toString(Word2vec.score(cp)) + " </Rel>",  relResult);
					}
				}

			}
		}
	}

	// create instance relation Vascular_Location
	public static void iVascular_Location (Map<String, String> concepts, OntModel OntoGraph,String IA) throws IOException, ParseException
	{
		List<String> loc= new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Location"))
				loc.add(cp) ;
		}


		// create new has_Size relation between the discovered concepts

		System.out.println("Vascular_Location ");

		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(IA)) != null) // get the primary concept resource
		{
			for (String cp : loc)
			{

				Individual icp = getIndividual(cp, OntoGraph);

				if (icp != null)
				{
					final Property Vascular_Location = ResourceFactory.createProperty(lo + "Vascular_Location"); // create new property
					rec.addProperty(Vascular_Location,icp.getURI());
					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + IA + ",Vascular_Location," + icp.getURI() + "," + Double.toString(Word2vec.score(cp)) + " </Rel>",  relResult);
					}

				}

			}
		}
	}

	public static void ihas_Aneuryrm(String concept,OntModel OntoGraph,String CNURI) throws IOException, ParseException
	{
		List<String> concepts  = new ArrayList<String>();
		concepts.add(concept) ;

		// create new has_Symptom relation between the discovered concepts

		System.out.println("has_Aneuryrm");
		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String cp : concepts)
			{

    			Individual icp = getIndividual(cp, OntoGraph);

				if (icp != null)
				{
					final Property has_Aneuryrm = ResourceFactory.createProperty(lo + "has_Aneuryrm"); // create new property
					rec.addProperty(has_Aneuryrm, icp.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",has_Aneuryrm," + icp.getURI() + "," + Double.toString(Word2vec.score(cp)) + " </Rel>",  relResult);
					}
				}

			}
		}

	}

	// create Instance relation Diagnosed_by
	public static void iDiagnosed_by (Map<String, String> concepts,OntModel OntoGraph,String CNURI) throws IOException, ParseException
	{
		List<String> diag = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Tests or Procedures"))
				diag.add(cp) ;
		}

		// create new has_Symptom relation between the discovered concepts

		System.out.println("Diagnosed_by");
		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String dg : diag)
			{


				Individual idg = getIndividual(dg, OntoGraph);

				if (idg != null)
				{
					final Property has_dg = ResourceFactory.createProperty(lo + "Diagnosed_by"); // create new property
					rec.addProperty(has_dg, idg.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",Diagnosed_by," + idg.getURI() + "," + Double.toString(Word2vec.score(dg)) + " </Rel>",  relResult);
					}
				}

			}
		}

	}

	// create instance relation has_Riskfactor
	public static void ihas_Riskfactor(Map<String, String> concepts,OntModel OntoGraph,String CNURI ) throws IOException, ParseException
	{

		List<String> rk= new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Riskfactor"))
				rk.add(cp) ;
		}

		// create new has_Size relation between the discovered concepts

		System.out.println("ihas_Riskfactor");

		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String rs : rk)
			{


				Individual irs = getIndividual(rs, OntoGraph);

				if (irs != null)
				{
					final Property has_rs = ResourceFactory.createProperty(lo + "has_Riskfactor"); // create new property
					rec.addProperty(has_rs, irs.getURI());


					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",has_Riskfactor," + irs.getURI() + "," + Double.toString(Word2vec.score(rs)) + " </Rel>",  relResult);
					}
				}

			}
		}
	}
	// create instance relation Tread_by
	public static void iTread_by (Map<String, String> concepts,OntModel OntoGraph,String CNURI) throws IOException, ParseException
	{

		List<String> chem = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Chemicals or Drugs"))
				chem.add(cp) ;
		}

		// create new has_Symptom relation between the discovered concepts

		System.out.println("Tread_by");

		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String ch : chem)
			{


				Individual ich = getIndividual(ch, OntoGraph);

				if (ich != null)
				{
					final Property has_ch = ResourceFactory.createProperty(lo + "Tread_by"); // create new property
					rec.addProperty(has_ch, ich.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",Tread_by," + ich.getURI() + "," + Double.toString(Word2vec.score(ch)) + " </Rel>",  relResult);
					}
				}

			}
		}
	}
	// create instance relation has_Symptom
	public static void ihas_Symptom(Map<String, String> concepts,OntModel OntoGraph,String CNURI ) throws IOException, ParseException
	{

		List<String> sign = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Sign or Symptom"))
				sign.add(cp) ;
		}

		// create new has_Symptom relation between the discovered concepts

		System.out.println("has_Symptom");

		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String sg : sign)
			{


				Individual isg = getIndividual(sg, OntoGraph);

				if (isg != null)
				{
					final Property has_sg = ResourceFactory.createProperty(lo + "has_Symptom"); // create new property
					rec.addProperty(has_sg, isg.getURI());


					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",has_Disease_Association," + isg.getURI() + "," + Double.toString(Word2vec.score(sg)) + " </Rel>",  relResult);
					}
				}

			}
		}

	}
	// create instance relation has_Disease_Association between discovered disorder concepts
	public static void ihas_Disease_Association(Map<String, String> concepts,OntModel OntoGraph, String CNURI) throws IOException, ParseException
	{
		List<String> disorder = new ArrayList<String>();
		for (String cp:  concepts.keySet())
		{
			String sgt = concepts.get(cp) ;
			if (concepts.get(cp).trim().equalsIgnoreCase("Disorders"))
				disorder.add(cp) ;
		}


		// create new has_Disease_Association relation between the discovered concepts

		System.out.println("has_Disease_Association");

		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String ds : disorder)
			{


				Individual ids = getIndividual(ds, OntoGraph);

				if (ids != null)
				{
					final Property has_ds = ResourceFactory.createProperty(lo + "has_Disease_Association"); // create new property
					rec.addProperty(has_ds, ids.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",has_Disease_Association," + ids.getURI() + "," + Double.toString(Word2vec.score(ds)) + " </Rel>",  relResult);
					}
				}

			}
		}

	}


	// create instance relation has_Race
	public static void ihas_Race(Map<String, String> concepts,OntModel OntoGraph,String CNURI ) throws IOException, ParseException
	{

		List<String> race = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Race"))
				race.add(cp) ;
		}

		// create new has_Race relation between the discovered concepts

		System.out.println("has_Ethnicity");
		Resource rec = null;

		if ((rec = OntoGraph.getIndividual(CNURI)) != null) // get the primary concept resource
		{
			for (String ra : race)
			{


				Individual ira = getIndividual(ra, OntoGraph);

				if (ira != null)
				{
					final Property has_eth = ResourceFactory.createProperty(lo + "has_Ethnicity"); // create new property
					rec.addProperty(has_eth, ira.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + CNURI + ",has_Ethnicity," + ira.getURI() + "," + Double.toString(Word2vec.score(ra)) + " </Rel>",  relResult);
					}
				}

			}
		}

	}
		// create instance relation has_Age 
		public static void ihas_Age(Map<String, String> concepts,OntModel OntoGraph,String CNURI) throws IOException, ParseException
		{
			List<String> age = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Age"))
					age.add(cp) ;
			}
			
			
			// create new has_Age relation between the discovered concepts

			System.out.println("ihas_Age");
			Resource rec = null ;
			if ( ( rec = OntoGraph.getIndividual(CNURI) ) != null) // get the primary concept resource
			{

				for (String ag : age) {

					Individual iage = getIndividual(ag, OntoGraph);
					if (iage != null) {
						final Property has_Age = ResourceFactory.createProperty(lo + "has_Age"); // create new property
						rec.addProperty(has_Age, iage.getURI());

						if (evl)
						{
							readfiles.Writestringtofile("  <Rel>" + CNURI + ",has_Age," + iage.getURI() + "," + Double.toString(Word2vec.score(ag)) + " </Rel>",  relResult);
						}
					}
				}
			}
		}
		
		// create relation has_Type between discovered disorder concepts 
		public static void has_Type(Map<String, String> concepts,String PrimaryConcept, OntModel OntoGraph,OntModel ICAOnto ) throws IOException, ParseException
		{
			List<String> type = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Type"))
					type.add(cp) ;
			}
			
			
			// create new has_Type relation between the discovered concepts

			System.out.println("has_Type");
			String cptURI = PrimaryConcept ; //bioportal.getConceptID(PrimaryConcept);
			Resource recPrimaryConcept = null ;

			for (String ty:  type)
			{
				if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null) // get the primary concept resource
				{
					ty = bioportal.getType(ty, ICAOnto);
					if (ty != null)
					{
						final Property has_Type = ResourceFactory.createProperty(lo + "has_Type") ; // create new property
						recPrimaryConcept.addProperty(has_Type,ty);
						
		  				 if (evl)
		  				 {
		  					 readfiles.Writestringtofile("  <Rel>" + PrimaryConcept+ ",has_Type," + ty + "</Rel>",  relResult);
		  				 }
					}
				}
			}		
		}
		


		public static Individual getIndividual(String concept, OntModel OntoGraph)
		{
			// uri - The URI of the new individual
			String conceptURI = ilo + concept.replace(" ", "_") ;
			Individual individual  = OntoGraph.getIndividual(conceptURI) ;

			if (individual != null )
			{
				return individual ;
			}

			return null ;
		}

	// create  instance relation Gender_Value between discovered gender concepts
	public static void IGender_Value(Map<String, String> concepts,String CN, OntModel OntoGraph,OntModel ICAOnto) throws IOException, ParseException
	{
		List<String> gen = new ArrayList<String>();

		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Gender"))
				gen.add(cp) ;
		}


		// create new has_Type relation between the discovered concepts

		System.out.println("Gender_Value");
		String cptURI = CN; //bioportal.getConceptID(PrimaryConcept);
		Resource rec= null ;

		for (String gend:  gen)
		{
			if ( ( rec= OntoGraph.getOntResource(cptURI) ) != null) // get the primary concept resource
			{
				Individual igend = getIndividual(gend,OntoGraph) ;

				if (igend != null)
				{
					final Property Gender_Value = ResourceFactory.createProperty(lo + "Gender_Value") ; // create new property
					rec.addProperty(Gender_Value,igend.getURI());

					if (evl)
					{
						readfiles.Writestringtofile("  <Rel>" + cptURI+ ",Gender_Value," + igend.getURI() + "," + Double.toString(Word2vec.score(gend)) + " </Rel>",  relResult);
					}
				}
			}
		}
	}


		// create relation has_value between discovered gender concepts 
		public static void Gender_Value(Map<String, String> concepts,String CN, OntModel OntoGraph,OntModel ICAOnto) throws IOException, ParseException
		{
			List<String> gen = new ArrayList<String>();
			
			for (String cp:  concepts.keySet())
			{
				if (concepts.get(cp).trim().equalsIgnoreCase("Gender"))
					gen.add(cp) ;
			}
			
			
			// create new has_Type relation between the discovered concepts

			System.out.println("Gender_Value");
			String cptURI = CN; //bioportal.getConceptID(PrimaryConcept);
			Individual recPrimaryConcept = null ;

			for (String gend:  gen)
			{
				if ( ( recPrimaryConcept= OntoGraph.getIndividual(cptURI) ) != null) // get the primary concept resource
				{
					gend = bioportal.getGender(gend, ICAOnto); 
					if (gend != null)
					{
						final Property Gender_Value = ResourceFactory.createProperty(lo + "Gender_Value") ; // create new property
						recPrimaryConcept.addProperty(Gender_Value,gend);
						
		  				 if (evl)
		  				 {
		  					 readfiles.Writestringtofile("  <Rel>" + CN + ",Gender_Value," + gend + "</Rel>",  relResult);
		  				 }
					}
				}
			}		
		}
		
	// create instance relation has_value 
	public static void ihas_Value(Map<String, String> concepts, OntModel OntoGraph,OntModel ICAOnto,Resource CN) throws IOException, ParseException
	{
		List<String> gen = new ArrayList<String>();
		
		for (String cp:  concepts.keySet())
		{
			if (concepts.get(cp).trim().equalsIgnoreCase("Gender"))
				gen.add(cp) ;
		}

		System.out.println("has_Value");

		for (String gend:  gen)
		{
			
			{
				gend = ilo + gend ;
				final Property has_Value = ResourceFactory.createProperty(lo + "has_Value") ; // create new property
				CN.addProperty(has_Value,gend);
			}
		}		
	}
		
	public static void has_symptomToOnto (String PrimaryConcept,String  cp , OntModel OntoGraph) throws IOException, ParseException
	{

		
		// create new has symptom relation between the PrimaryConcept and discovered concept if discovered concept is symptom semantic type 
		String cptURI = bioportal.getConceptID(PrimaryConcept);
		Map<String, Integer> semTypeCp =  bioportal.getSemanticTypes(PrimaryConcept);
		System.out.println("has_symptomToOnto");
		Resource recPrimaryConcept = null ; 
		Resource r1 = null ;

		{
			// don't add relation for itself
			if(cp.equalsIgnoreCase(PrimaryConcept))
              return ; 
			
			Map<String, Integer> semType =  bioportal.getSemanticTypes(cp);
			
			for (String st:  semType.keySet())
			{
				if(st.equalsIgnoreCase("T184"))  
				{
					String conceptURI = bioportal.getConceptID(cp);
					if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null) // get the primary concept resource 
					{
						final Property has_Symptom = ResourceFactory.createProperty(lo + "has_Symptom") ; // create new property 
						
						
						if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null) // get the concept resource 
						{
							
							// the symptom is already discovered before 
							// then add the frequency  
							recPrimaryConcept.addProperty(has_Symptom,r1);
							
							
							// update the frequency by one. 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							Statement stm = r1.getProperty(p2) ;
							RDFNode node = stm.getObject();
							long value = node.asLiteral().getLong() ;
							stm = stm.changeLiteralObject(value+1) ;
							
							
						}
						else
						{
							
							String uri = classToOnto (cp,OntoGraph);
							r1 = OntoGraph.getOntClass(uri) ;
							recPrimaryConcept.addProperty(has_Symptom,r1);
														
							// set frequency value to 1 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							r1.addLiteral(p2, 1);
							
						}
						
						Scoring.setOccurence_Probability(conceptURI, recPrimaryConcept, OntoGraph);
						Scoring.Tier_Rank(conceptURI, recPrimaryConcept, OntoGraph);
						
					}
					else
						break ; 
					System.out.println(st);
				}
			}
			
		}
	}
	
	public static void treated_byToOnto (String PrimaryConcept,String cp,OntModel OntoGraph) throws IOException, ParseException
	{
		// this function created a treated by relation between the discovred concept and primary concept if the discovered one is Chemicals & Drugs semantic group 
		
		String cptURI = bioportal.getConceptID(PrimaryConcept);
		Map<String, Integer> semGroupCp =  bioportal.getSemanticGroup(PrimaryConcept); 
		

		if ( semGroupCp.size() == 1 && semGroupCp.containsKey("Chemicals or Drugs"))
		   return ; 
		
		
		System.out.println("TreatsToOnto ");
		Resource recPrimaryConcept = null ; 
		Resource r1 = null ;
		
		{
			// don't add relation for itself
			if(cp.equalsIgnoreCase(PrimaryConcept))
				return ; 
			
			Map<String, Integer> semGroup =  bioportal.getSemanticGroup(cp);
			
			for (String st:  semGroup.keySet())
			{
				if(st.equalsIgnoreCase("Chemicals or Drugs"))
				{
					String conceptURI = bioportal.getConceptID(cp);
					if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null)
					{
						final Property p = ResourceFactory.createProperty(lo + "treated_by") ;
						
						
						if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
						{
							recPrimaryConcept.addProperty(p,r1);
							
							// update the frequency by one. 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							Statement stm = r1.getProperty(p2) ;
							RDFNode node = stm.getObject();
							long value = node.asLiteral().getLong() ;
							stm = stm.changeLiteralObject(value+1) ;
						}
						else
						{
							String uri = classToOnto (cp,OntoGraph);
							r1 = OntoGraph.getOntClass(uri) ;
							recPrimaryConcept.addProperty(p,r1);
							
							// set frequency value to 1 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							r1.addLiteral(p2, 1);
						}
						
						Scoring.setOccurence_Probability(conceptURI, recPrimaryConcept, OntoGraph);
						Scoring.Tier_Rank(conceptURI, recPrimaryConcept, OntoGraph);
						
					}
					else
						break ; 
					System.out.println(st);
				}
			}
			
		}
	}
	
	public static void locationToOnto (String PrimaryConcept,String cp,OntModel OntoGraph) throws IOException, ParseException
	{
		
		String cptURI = bioportal.getConceptID(PrimaryConcept);
		Map<String, Integer> semGroupCp =  bioportal.getSemanticGroup(PrimaryConcept); 
		

		if ( semGroupCp.size() == 1 && semGroupCp.containsKey("Anatomy"))
		   return ; 
		
		
		System.out.println("locationToOnto ");
		Resource recPrimaryConcept = null ; 
		Resource r1 = null ;
		{
			// don't add relation for itself
			if(cp.equalsIgnoreCase(PrimaryConcept))
				return ; 
			
			Map<String, Integer> semGroup =  bioportal.getSemanticGroup(cp);
			
			for (String st:  semGroup.keySet())
			{
				if(st.equalsIgnoreCase("Anatomy"))
				{
					String conceptURI = bioportal.getConceptID(cp);
					if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null)
					{
						final Property p = ResourceFactory.createProperty(lo + "location") ;
						
						
						if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
						{
							recPrimaryConcept.addProperty(p,r1);
							// update the frequency by one. 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							Statement stm = r1.getProperty(p2) ;
							RDFNode node = stm.getObject();
							long value = node.asLiteral().getLong() ;
							stm = stm.changeLiteralObject(value+1) ;
						}
						else
						{
							String uri = classToOnto (cp,OntoGraph);
							r1 = OntoGraph.getOntClass(uri) ;
							recPrimaryConcept.addProperty(p,r1);
							
							// set frequency value to 1 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							r1.addLiteral(p2, 1);
						}
						
						Scoring.setOccurence_Probability(conceptURI, recPrimaryConcept, OntoGraph);
						Scoring.Tier_Rank(conceptURI, recPrimaryConcept, OntoGraph);
						
					}
					else
						break ; 
					System.out.println(st);
				}
			}
			
		}
	}
	public static void diagnoses_byToOnto (String PrimaryConcept,String cp,OntModel OntoGraph) throws IOException, ParseException
	{
		
		String cptURI = bioportal.getConceptID(PrimaryConcept);
		Map<String, Integer> semGroupCp =  bioportal.getSemanticGroup(PrimaryConcept); 
		

		if ( semGroupCp.size() == 1 && semGroupCp.containsKey("Device"))
		   return ; 
		
		
		System.out.println("diagnoses_by");
		Resource recPrimaryConcept = null ; 
		Resource r1 = null ;
		{
			// don't add relation for itself
			if(cp.equalsIgnoreCase(PrimaryConcept))
				return ; 
			
			Map<String, Integer> semGroup =  bioportal.getSemanticGroup(cp);
			
			for (String st:  semGroup.keySet())
			{
				if(st.equalsIgnoreCase("Devices"))
				{
					String conceptURI = bioportal.getConceptID(cp);
					if ( ( recPrimaryConcept= OntoGraph.getOntClass(cptURI) ) != null)
					{
						final Property p = ResourceFactory.createProperty(lo + "diagnoses_by") ;
						
						
						if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
						{
							recPrimaryConcept.addProperty(p,r1);
							// update the frequency by one. 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							Statement stm = r1.getProperty(p2) ;
							RDFNode node = stm.getObject();
							long value = node.asLiteral().getLong() ;
							stm = stm.changeLiteralObject(value+1) ;
						}
						else
						{
							String uri = classToOnto (cp,OntoGraph);
							r1 = OntoGraph.getOntClass(uri) ;
							recPrimaryConcept.addProperty(p,r1);
							
							// set frequency value to 1 
							final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
							r1.addLiteral(p2, 1);
						}
						
						
						Scoring.setOccurence_Probability(conceptURI, recPrimaryConcept, OntoGraph);
						Scoring.Tier_Rank(conceptURI, recPrimaryConcept, OntoGraph);
						
					}
					else
						break ; 
					System.out.println(st);
				}
			}
			
		}
	}
	
	public static int genderToOnto(String sentence) 
	{
		
		String[] tokens = sentence.split(sentence); 
		
		if ( tokens != null)
		{
			String[] flist= {"female","women", "miss","lady","mother","wife","girl"} ; 
			String[] mlist= {"male","man","father","husband", "guy", "boy"} ; 
			int fCount = 0 ; 
			int mCount = 0 ; 
			for (int i =0; i < tokens.length; i++ )
			{
				
				for (int f = 0; f < flist.length; f++)
				{
					if (tokens[i].compareToIgnoreCase(flist[f]) == 0 )
					{
						fCount++ ; 
					}
				}
				
				for (int m = 0; m < mlist.length; m++)
				{
					if (tokens[i].compareToIgnoreCase(mlist[m]) == 0 )
					{
						mCount++ ; 
					}
				}
			}
			
			if( fCount > mCount)
			{
				return 1 ; 
			}
			else
			{
				return 2 ; 
			}

		}
		
		return 0 ; 
	}
	
	public static String ethnicityToOnto(String sentence,String concept,OntModel OntoGraph) 
	{
		
		String[] tokens = sentence.split(sentence); 
		 String URI = null ;
		
		if ( tokens != null)
		{
			String[] whiteRace= {"White","Europe", "Middle East","North Africa","caucasian"} ; 
			String[] blackRace= {"black","African American", "Africa"} ; 
			String[] asianRace= {"Asian","Far East", "Southeast Asia", "Indian"} ;
			String[] HawaiianRace= {"Hawaii","Guam", "Samoa", "Pacific Islands"} ;

			int[] race = {0,0,0,0} ;  

			
			for (int j = 0; j < whiteRace.length; j++)
			{
				if (isContain(sentence,whiteRace[j]))
				{
					race[0] =  race[0] + 1 ; 
				}
			}
			
			
			for (int j = 0; j < blackRace.length; j++)
			{
				if (isContain(sentence,blackRace[j]))
				{
					race[1] =  race[1] + 1 ; 
				}
			}
	
			for (int j = 0; j < asianRace.length; j++)
			{
				if (isContain(sentence,asianRace[j]))
				{
					race[2] =  race[2] + 1 ; 
				}
			}
			for (int j = 0; j < HawaiianRace.length; j++)
			{
				if (isContain(sentence,HawaiianRace[j]))
				{
					race[3] =  race[3] + 1 ; 
				}
			}	
			
			int max = 0 ; 
			for (int j = 0; j < race.length; j++)
			{
				if (race[j]> max)
				{
					max =  j ; 
				}
			}; 
			
		  
	       switch (max)
	       {
	       case 0:
	    	  URI = classToOnto ("White",OntoGraph) ;
              break ; 
	       case 1:
	    	   URI = classToOnto ("Black",OntoGraph) ;
	    	   break ; 
	       case 2:
	    	   URI = classToOnto ("Asian",OntoGraph) ;
	    	   break ; 
	       case 3:
	    	   URI = classToOnto ("Hawaiian",OntoGraph) ;
	    	   break ; 
	       default:
	    	   URI = classToOnto ("White",OntoGraph) ;
	    	   break ; 
	    	   
	       }
	       
	       Resource r = null ; 
	       if ( ( r= OntoGraph.getOntClass(URI) ) == null)
	       {
		       // adding new risckfactor relations 
	    	   String cptURI = bioportal.getConceptID(concept);
	    	   Resource resPrimery= OntoGraph.getOntClass(cptURI) ;
		       final Property p = ResourceFactory.createProperty(lo + "RiskFactor") ;//
			   Resource  BlankNodeComorbid_Relation = OntoGraph.createResource() ;
			   resPrimery.addProperty(p,BlankNodeComorbid_Relation) ; 
			   final Property p2 = ResourceFactory.createProperty(lo + "ethnicity") ;
			   Resource res= OntoGraph.getOntClass(URI) ;
			   BlankNodeComorbid_Relation.addProperty(p2,res);
	       }
	       
		}
		return URI ;

	}
	
	
    private static boolean isContain(String source, String subItem){
        String pattern = "\\b"+subItem.toLowerCase()+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(source.toLowerCase());
        return m.find();
   }
    
    public static void riskFactorToOnto1 (String Primaryconcept,String concept,OntModel OntoGraph) throws IOException, ParseException
    
	{
    	String pcptURI = bioportal.getConceptID(Primaryconcept);
    	 Resource res = null ; 
    	// update ICA ontology with new riskfactors
    	OntModel ICAOnto = dataExtractor.riskFactorExtractor("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\data\\ica_ontology_updated_aug_27.owl")  ;
    	
    	
    	// build query string to map the concept to on of the existing predefine riskfactors in ontology 
		String queryString=
				"PREFIX p: <http://dbpedia.org/property/>"+
				"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
				"PREFIX category: <http://dbpedia.org/resource/Category:>"+
				"PREFIX lo: <http://www.lifeOnto.org/lifeOnto#>" +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"Select ?concept ?riskfactCat  where { ?concept rdfs:label|skos:altLabel"  + "\"" +  concept + "\" ."  + 
				"?concept rdfs:subClassOf ?riskfactCat ." +
				"?riskfactCat rdfs:subClassOf ica:risk_factors" + "}";
		
		Model model = ICAOnto.getBaseModel() ;
		//model.write(System.out, "RDF/XML-ABBREV") ;
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		ResultSet results = qexec.execSelect() ;
		String riskfactor = null ;
		String riskfactorCat = null ;

		for ( ; results.hasNext() ; )
	    {
	      QuerySolution soln = results.nextSolution() ;
	      riskfactor = soln.get("?concept").toString() ; 	
	      riskfactorCat= soln.get("?riskfactCat").toString() ;
	      break;
	    }
		
		Resource recPrimaryConcept= null ;
		OntClass recRiskfactor = null ;
		Resource recCatRiskfactor = null ;
		if ( ( recPrimaryConcept= OntoGraph.getOntClass(pcptURI) ) != null && riskfactor != null)
		{
			// create property
			final Property has_riskfactor = ResourceFactory.createProperty(lo + "has_riskfactor") ;//
			final Property frequency = ResourceFactory.createProperty(lo + "frequency") ;
			
			String rfURI = riskfactor ;
			String rgCatURI = riskfactorCat ;
			
			// the riskfactor exist 
			if (( recRiskfactor = OntoGraph.getOntClass(rfURI))  != null  &&   ( recCatRiskfactor = OntoGraph.getOntClass(rgCatURI))  != null )
			{
			    // update  Probability_values 
				
				final Property p2 = ResourceFactory.createProperty(lo + "frequency") ;
				Statement st = recRiskfactor.getProperty(p2) ;
				RDFNode node = st.getObject();
				long value = node.asLiteral().getLong() ;
				st = st.changeLiteralObject(value+1) ;

				
			}
			else
			{
                // adding the new concept and the label
				String uri = classToOnto (rfURI,concept,OntoGraph);
				recRiskfactor = OntoGraph.getOntClass(uri) ;
				recRiskfactor.addLiteral(frequency, 1);
				recRiskfactor.addSuperClass(recCatRiskfactor);
				recPrimaryConcept.addProperty(has_riskfactor,recRiskfactor);

			}
			
			Scoring.setOccurence_Probability(rfURI, recPrimaryConcept, OntoGraph);
			Scoring.Tier_Rank(rfURI, recPrimaryConcept, OntoGraph);
		}

	}
	
	
	public static void ComorbidToOnto (String Primaryconcept,Map<String, String> concepts,OntModel OntoGraph) throws IOException, ParseException
	{
		
		String cptURI = bioportal.getConceptID(Primaryconcept);
		Map<String, Integer> semTypeCp =  bioportal.getSemanticTypes(Primaryconcept);
		
		
		System.out.println("ComorbidToOnto");
		Resource r = null ; 
		Resource r1 = null ;
		for (String cp:  concepts.keySet())
		{
			// don't add relation for itself
			if(cp.equalsIgnoreCase(Primaryconcept))
				continue ; 
			
			Map<String, Integer> semType =  bioportal.getSemanticTypes(cp);
			
			if ( semTypeCp.size() == 1 && semTypeCp.containsKey("T047"))
			{
				
				String conceptURI = bioportal.getConceptID(cp);
				if ( ( r= OntoGraph.getOntClass(cptURI) ) != null)
				{
					final Property p = ResourceFactory.createProperty(lo + "has_Comorbid") ;
					
					
					if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
					{
						r.addProperty(p,r1);
					}
					else
					{
						String uri = classToOnto (cp,OntoGraph);
						r1 = OntoGraph.getOntClass(uri) ;
						r.addProperty(p,r1);
					}
					
				}				
			}
		}

			
	}
	

	
		public static void ComorbidToOnto1 (String Primaryconcept,Map<String, String> concepts,OntModel OntoGraph) throws IOException, ParseException
		{
			
			String cptURI = bioportal.getConceptID(Primaryconcept);
			Map<String, Integer> semTypeCp =  bioportal.getSemanticTypes(Primaryconcept);
			
			
			System.out.println("ComorbidToOnto");
			Resource r = null ; 
			Resource r1 = null ;
			// loop of other concepts 
			for (String cp:  concepts.keySet())
			{
				// don't add relation for itself
				if(cp.equalsIgnoreCase(Primaryconcept))
					continue ; 
				
				// retrive the semantic type 
				Map<String, Integer> semType =  bioportal.getSemanticTypes(cp);
				
				// disease 
				if ( semType.containsKey("T047"))
				{
					
					
					if ( ( r= OntoGraph.getOntClass(cptURI) ) != null)
					{
						String queryString=
								"PREFIX p: <http://dbpedia.org/property/>"+
								"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
								"PREFIX category: <http://dbpedia.org/resource/Category:>"+
								"PREFIX lo: <http://www.lifeOnto.org/lifeOnto#>" +
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
	 
								"Select ?condition ?value where { <" + cptURI + "> "  + "has_disease_association " +  "?Comorbid_Relation." + 
								        "?Comorbid_Relation lo:Disorder ?condition." +
								"?Comorbid_Relation lo:Probability_values ?value }";
						
                        
						final Property p = ResourceFactory.createProperty(lo + "has_disease_association") ;//
						
						// we need to check if this relation already exist in graph before we added it
						
						Model model = OntoGraph.getBaseModel() ;
						//model.write(System.out, "RDF/XML-ABBREV") ;
						Query query = QueryFactory.create(queryString) ;
						QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
						ResultSet results = qexec.execSelect() ;
						for ( ; results.hasNext() ; )
					    {
					      QuerySolution soln = results.nextSolution() ;
					      
					      String Comorbid_Relation = soln.get("?obj").asLiteral().getString() ; 
					      int i = 0 ; 
					      
					    }
						
						
						Resource  BlankNodeComorbid_Relation = OntoGraph.createResource() ;
						r.addProperty(p,BlankNodeComorbid_Relation) ; 
					
						String conceptURI = bioportal.getConceptID(cp);
						final Property p2 = ResourceFactory.createProperty(lo + "Disorder") ;
						final Property p3 = ResourceFactory.createProperty(lo + "Probability_values") ;
						
						if (( r1 = OntoGraph.getOntClass(conceptURI) ) != null)
						{
						
							BlankNodeComorbid_Relation.addProperty(p2,r1);
							BlankNodeComorbid_Relation.addLiteral(p3, 0);
						}
						else
						{
							String uri = classToOnto (cp,OntoGraph);
							r1 = OntoGraph.getOntClass(uri) ;
							BlankNodeComorbid_Relation.addProperty(p2,r1);
							BlankNodeComorbid_Relation.addLiteral(p3, 0);

						}
						
					}				
				}

				
			}
		}
		
		
		
	/*public static void getontoSemanticType (Map<String, Dataset> lookupresources)
	{
		System.out.println("getontoSemanticType");
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getcandidateGraph();
	   		graph.setNsPrefix( "rdf", rdf ) ;
   			String uri = ""; 
   			Map<String, Double> Topuriconfident = dataset.gettopuriconfident() ;
   			
	   		for (String onto: Topuriconfident.keySet())
	   		{
    			uri = onto ;
	   		}
	   		
	   		if (uri.isEmpty())
	   			   continue ; 
	   		
	   		// set the lexical alt label
	   		 List<String> Category = dataset.Category ;
	   		if (Category != null)
	   		{
	   			for (String Definition: Category)
	   			{
	   				Resource rec = graph.createResource(uri);
 	        		// add the property
	 	         	final Property p = ResourceFactory.createProperty(rdf  + "type") ;
	 	         	rec.addProperty(p, Definition);
	   			}
	   			
	   		}
	   		
   	 	}
	}
	


	
	public static void getontosameAs (Map<String, Dataset> lookupresources)
	{
		
		System.out.println("getontosameas");
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getcandidateGraph();
	   		
   			String topuri = ""; 
   			Map<String, Double> uriconfident = dataset.gettopuriconfident() ;
	   		for (String onto: uriconfident.keySet())
	   		{
    			topuri = onto ;
	   		}
	   		
	   		if (topuri.isEmpty())
	   			   continue ; 
	   		
	   		
	   		
	   		graph.setNsPrefix( "owl", owl ) ;
   			
   			List<String> Topuriconfident = dataset.getTopBesturiconfident(3,0.5) ;
   			
   			if (Topuriconfident.size() > 1 )
   			{

		   		int count = 0 ; 
		   		for (String tempuri: Topuriconfident)
		   		{
		   			count++ ;
	    			if (count == 1)
	    				continue ; 
	    			
	    			
	    			
	    			
	    			// create sameas relation 
	    			String[] uri  = tempuri.split("!", 2) ;
	    			if(uri.length > 0 )
	    			{
		   				Resource rec = graph.createResource(topuri);
	 	        		// add the property
		 	         	final Property p = ResourceFactory.createProperty(owl  + "sameAs") ;
		 	         	rec.addProperty(p, uri[0]);
	    			}
	    			
		   		}
   			}
	   		
   	 	}
	}
	

	public static void getontodefinition (Map<String, Dataset> lookupresources)
	{
		
		System.out.println("getontodefinition");
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getcandidateGraph();
	   		graph.setNsPrefix( "skos", skos ) ;
   			String uri = ""; 
   			Map<String, Double> Topuriconfident = dataset.gettopuriconfident() ;
	   		for (String onto: Topuriconfident.keySet())
	   		{
    			uri = onto ;
	   		}
	   		if (uri.isEmpty())
	   			   continue ; 
	   		
	   		// set the lexical alt label
	   		 List<String> Definitions = dataset.Definition ;
	   		if (Definitions != null)
	   		{
	   			for (String Definition: Definitions)
	   			{
	   				Resource rec = graph.createResource(uri);
 	        		// add the property
	 	         	final Property p = ResourceFactory.createProperty( skos + "definition") ;
	 	         	rec.addProperty(p, Definition);
	   			}
	   			
	   		}
	   		
   	 	}
	}
	
	public static void getontoscheme (Map<String, Dataset> lookupresources)
	{
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getcandidateGraph();
	   		graph.setNsPrefix( "skos", skos ) ;
   			String uri = ""; 
   			Map<String, Double> Topuriconfident = dataset.gettopuriconfident() ;
	   		for (String onto: Topuriconfident.keySet())
	   		{
    			uri = onto ;
	   		}
	   		
	   		if (uri.isEmpty())
	   			   continue ;  
	   		
	   		// set the lexical alt label
	   		 List<String> scheme = dataset.ontology ;
	   		if (scheme != null)
	   		{
	   			for (String label: scheme)
	   			{
	   				Resource rec = graph.createResource(uri);
 	        		// add the property
	 	         	final Property p = ResourceFactory.createProperty(skos  + "inScheme") ;
	 	         	rec.addProperty(p, label);
	   			}
	   			
	   		}
	   		
   	 	}
	}
	public static void getontoPreflabel (Map<String, Dataset> lookupresources)
	{
		System.out.println("getontoPreflabel");
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getcandidateGraph();
	   		graph.setNsPrefix( "skos", skos ) ;
   			String uri = ""; 
   			Map<String, Double> Topuriconfident = dataset.gettopuriconfident() ;
   			
   			
   			if (Topuriconfident.size() == 0 )
   			   continue ; 	
   			 
	   		for (String onto: Topuriconfident.keySet())
	   		{
    			uri = onto ;
	   		}
   			
	   		if (uri.isEmpty())
	   			   continue ; 
	   		
   			Map<String,List<String>> syn = new HashMap<String, List<String>>();
   			String PrefLabel = dataset.PrefLabel ;
   			
   			if (PrefLabel == null )
   				continue ; 
   			String tokens[] = PrefLabel.split(" ") ;
			Resource rec = graph.createResource(uri);
			Resource rec1 = graph.createResource(tokens[0]);
    		// add the property
         	final Property p = ResourceFactory.createProperty(skos+ "PrefLabel") ;
         	rec.addProperty(p, rec1);	
         	final Property p1 = ResourceFactory.createProperty(skos + "PrefLabel") ;
         	String Label = tokens[2] ;
         	
         	System.out.println(Label);
         	Label = Label.replace(")", " ") ;
         	Label = Label.replace("(", " ") ;
         	Label = Label.trim() ;
         	rec1.addProperty(p, Label);
   	 	}
	}
	
	public static void getontoSynonym (Map<String, Dataset> lookupresources)
	{
		System.out.println("getontoSynonym");
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getcandidateGraph();
	   		graph.setNsPrefix( "skos", skos ) ;
   			String uri = ""; 
   			
   			Map<String, Double> Topuriconfident = dataset.gettopuriconfident() ;
	   		for (String onto: Topuriconfident.keySet())
	   		{
    			uri = onto ;
	   		}
	   		
	   		if (uri.isEmpty())
	   			   continue ; 
	   		
   			Map<String,List<String>> syn = new HashMap<String, List<String>>();
   			List<String> Syns = dataset.Synonym ;
   			if (Syns == null )	
   				continue ;
   			
   			
   			double max = 0.0 ;
   			List<String> alts = new ArrayList<String>()  ;
   			
	    	for (String synon: Syns)
	    	{    		
	    		String[] words = synon.split("!");  
	    		
	    		if (syn.containsKey(words[1].toLowerCase()))
	    		{
	    			alts = syn.get(words[1].toLowerCase());
	    			if (!alts.contains(words[2].toLowerCase()))  
	    			{
	    				alts.add(words[2].toLowerCase()) ;
	    			}
	    		}
	    		else
	    		{
	    			alts.add(words[2].toLowerCase()) ;
	    			syn.put(words[1].toLowerCase(), alts) ;
	    		}

	    	}
	    	
	    	for (String label: syn.keySet())
	    	{
   				Resource rec = graph.createResource(uri);
	        		// add the property
 	         	final Property p = ResourceFactory.createProperty(skos + "altLabel") ;
 	         	rec.addProperty(p, label);
	    		
	    		
	    		
	    	}
   	 	}
	}
	
	
	public static void getontoassociate (Map<String, Dataset> lookupresources)
	{
		 Map<String, Dataset> tempresources = new HashMap<String, Dataset>();
		 tempresources.putAll(lookupresources);
		// construct whole subgraph for each concept
		for (String concept: lookupresources.keySet())
   	 	{
			System.out.println("******************" + concept + "******************");
			
	   		Dataset dataset = lookupresources.get(concept) ;
	   		Model graph = dataset.getGraph();
	   		Model candidategraph = dataset.getcandidateGraph() ;
			// list the statements in the Model
			StmtIterator iter =  graph.listStatements();
	   		
	    	while (iter.hasNext())
			{
			    Statement stmt      = iter.nextStatement();  // get next statement
			    Resource  subject   = stmt.getSubject();     // get the subject
			    Property  predicate = stmt.getPredicate();   // get the predicate
			    RDFNode   object    = stmt.getObject();      // get the object
			    for (String conceptin: tempresources.keySet()) 
			    {
			    	
			    	if (concept.equals(conceptin))
			    		continue ; 
			   		Dataset datasetin = lookupresources.get(concept) ;
			   		Model graphin = dataset.getGraph();
					// list the statements in the Model
					StmtIterator iterin =  graph.listStatements();
					while (iterin.hasNext())
					{
					    Statement stmtin      = iterin.nextStatement();  // get next statement
					    Resource  subjectin   = stmtin.getSubject();     // get the subject
					    Property  predicatein = stmtin.getPredicate();   // get the predicate
					    RDFNode   objectin    = stmtin.getObject();      // get the object
					    
					    // add a resource
					    if (object.toString().equals(subjectin.toString()) )
					    {
			   				Resource rec = candidategraph.createResource(subject);
		 	        		// add the property
			 	         	final Property p = ResourceFactory.createProperty(predicate.toString()) ;
			 	         	rec.addProperty(p, subjectin.toString());
			 	         	System.out.println(object.toString() + ", " + predicate.toString() + ", " + subjectin.toString());
					    }
					    
					    // add a resource
					    if(object.toString().equals(objectin.toString()) && predicate.toString().equals(predicatein))
					    {
			   				Resource rec = candidategraph.createResource(subject);
		 	        		// add the property
			 	         	final Property p = ResourceFactory.createProperty(predicate.toString()) ;
			 	         	rec.addProperty(p, subjectin.toString());
			 	         	System.out.println(object.toString() + ", " + predicate.toString() + ", " + subjectin.toString());
					    }
			    	
					}

			    }
	   		
   	 		}
   	 	}
	}
	
	
	public static void getontoPropertyHierarchy (String uri,String property ,Model graph) throws IOException
	{
		System.out.println("***********************************getontoHierarchy************************************************");

			
	   		graph.setNsPrefix( "skos", skos ) ;
	   		graph.setNsPrefix( "rdfs", rdfs ) ;
	   		
	   		
	   		
	   		List<String> Hierarchy = Enrichment.LLDHierarchyProperty(uri,property)  ;
	   		
	   		if (Hierarchy != null)
	   		{
	   			for (int i = Hierarchy.size()-2 ; i > -1; i--)
	   			{
	   				String hier = Hierarchy.get(i) ;
	   				String tokens[] = hier.split("!") ;
	   				Resource child = graph.createResource(uri);
	   				Resource parent = graph.createResource(tokens[0]);
	   				
 	        		// add the property
	 	         	final Property p = ResourceFactory.createProperty( rdfs + "subPropertyOf") ;
	 	         	child.addProperty(p, parent);
	 	         	
	 	         	
	 	         	final Property pp = ResourceFactory.createProperty(rdfs + "label") ;
	 	         	parent.addProperty(pp, tokens[1]);
	 	         	uri = tokens[0] ;
	   			}
	   			
	   		}
	   		

	}*/

}
