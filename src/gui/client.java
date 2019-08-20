package gui;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.json.simple.parser.ParseException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import context.implementation.ConText;
import NER.Engine;
import NER.ontologyMapping;
import NER.umlsMapping;
import NLP.preProcessing;
import ONTO.BioPontologyfactory;
import ONTO.ontologyfactory;
import util.*;
import word2vec.Word2VecRawTextExample;

public class client {
	  static  String skos = "http://www.w3.org/2004/02/skos/core#" ;
	   static String  rdfs = "http://www.w3.org/2000/01/rdf-schema#" ;
	   static String  rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ;
	   static String  owl = "http://www.w3.org/2002/07/owl#" ;
	   static String  lo = "http://www.ou.org/AKG#" ;
	   public static Word2Vec vec = null ;

	public static void main(String[] args) throws Exception
	{
		//File gModel = new File("X:\\KG_ANR\\Vendor\\GoogleNews-vectors-negative300.bin");

		//vec = WordVectorSerializer.readWord2VecModel(gModel);
		// TODO Auto-generated method stub
		OntModel OntoGraph = ModelFactory.createOntologyModel();
		OntoGraph.setNsPrefix( "skos", skos ) ;
		OntoGraph.setNsPrefix( "lo", lo ) ;
		File CNs[] =  readfiles.readAllFileFromPath("X:\\KG_ANR\\Clinical Notes") ;
		
		// MetaMap for Neg concepts 
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


		Word2vec.createModel() ;
		 String PrimaryConcept  = "Intracranial Aneurysm" ;
		OntClass recIA = BioPontologyfactory.classToontclass("Intracranial Aneurysm",OntoGraph);

		String PrimaryConcept1  = "Clinical Notes" ;
		OntClass recCN  = BioPontologyfactory.classToontclass ("Clinical Notes",OntoGraph);


		OntModel ICAOnto = dataExtractor.riskFactorExtractor("X:\\Distance Supervision NER\\Data Medline_PubMed\\data\\ica_ontology_updated_aug_27.owl")  ;
		 
		// text processing and concept extraction  
		for (int i = 0; i < CNs.length; i++)
		{

			// create instance  for clinical note

			Individual clinicalN =  BioPontologyfactory.instanceToOnto(CNs[i].getName(),recCN,OntoGraph) ;
			Individual individualIA =  BioPontologyfactory.instanceToOnto(PrimaryConcept+ "_" + CNs[i].getName(),recIA,OntoGraph) ;

			    // reading clinical notes 
			    String CN = readfiles.readLinestostring(CNs[i].toURL());
				int count = 0 ;
				int counter = 0 ; 

				// text processing 
				//List<String> titleList = preProcessing.getSentences(CN);
				List<String> titleList = ReadXMLFile.ReadAneCNSent(CNs[i].getAbsolutePath()) ;
				
				Map<String, String> conceptswoNeg = new HashMap<String, String>();
				for (String title : titleList)
				{


					// create instance out of each class.

					/*************************************************************************************************
					 *                                      get Concepts
					 */
					/*************************************************************************************************/
					
					// = preProcessing.getlemma(title) ; 
					
					// concept extraction 
					String text  = removestopwords.removestopwordfromsen(title) ;
					Map<String, Integer> mentions = NGramAnalyzer.entities(1,4, text) ;
					Map<String, String> allconcepts = null ;
					
					
					// get all negation concepts
					//Negex https://code.google.com/archive/p/negex/source
					
					ConText myApplication = new ConText();
					String context_output = "";
					Map<String, String> NegConcepts = new HashMap<String, String>();
					for (String mention : mentions.keySet())
					{
						ArrayList<String> res = null;
						res = myApplication.applyContext(mention, title );
						if(res!=null)
						{
							context_output += "ConText for '"+res.get(0)+"':\n"+
							"Sentence: '"+res.get(1)+"'\n"+
							"Negation: '"+res.get(2)+"'\n"+
							"Temporality: '"+res.get(3)+"'\n"+
							"Experiencer: '"+res.get(4)+"'\n\n";
							if (!res.get(2).toString().equalsIgnoreCase("Affirmed")) 
								NegConcepts.put(mention,res.get(2).toString()) ;
							
						}
					}
					
					// get concepts,risk factors,Side, and Location
					allconcepts = bioportal.getConcepts(mentions)  ;
					
					// get Gender 
					allconcepts.putAll(Engine.getGender(title));
					
					// get race 
					allconcepts.putAll(Engine.getRace(title));
					
					//get type 
					allconcepts.putAll(Engine.getType(title));
					
					// get age
					allconcepts.putAll(Engine.getAge(title));
					
					// get size
					allconcepts.putAll(Engine.getSize(title));
					
					
					// get status
					allconcepts.putAll(Engine.getStatus(title));
					
					// get tests
					allconcepts.putAll(Engine.getTest(title));
					
				    //***************************************************************************
					// Neg concept 
					//*****************************************************************************
/*					Map<String, String> NegConcepts = umlsMapping.getNegconcepts(title, api) ;
					
					
					// to remove the Neg concepts 
		            if (NegConcepts.size() != 0)
		            {
						for (String cpNoNeg:allconcepts.keySet())
						{
							if (NegConcepts.get(cpNoNeg) == null)
							{
								conceptswoNeg.put(cpNoNeg, allconcepts.get(cpNoNeg)) ;
							}
						}
		            }
		            else
		            {
		            	conceptswoNeg.putAll(allconcepts);
		            }
					
					
		            
		            // concepts after removing neg
					Map<String, String> concepts = new HashMap<String, String>();
					
					for ( String cpt : conceptswoNeg.keySet())
					{
						if (!cpt.isEmpty())
						{
							String semGroup = conceptswoNeg.get(cpt).trim() ;
							//if (allconcepts.get(cpt).contains("T184")||allconcepts.get(cpt).contains("T047")||allconcepts.get(cpt).contains("T190"))
							if (semGroup.compareTo("Disorders") == 0|| semGroup.compareTo("Chemicals & Drugs") == 0 || semGroup.compareTo("Anatomy") == 0  ||  semGroup.compareTo("Procedures") ==0 
									||semGroup.compareTo("Gender") == 0 || semGroup.compareTo("Size") == 0|| semGroup.compareTo("Race") == 0 || semGroup.compareTo("Age") == 0
									|| semGroup.compareTo("Type") == 0)
							{
		
								concepts.put(cpt, conceptswoNeg.get(cpt))	;
							}
						} 
						
					}*/
					
		            // concepts after removing neg
					Map<String, String> concepts = new HashMap<String, String>();
					
					// to remove the Neg concept 
			        if (NegConcepts.size() != 0)
			        {
						for (String cpNoNeg:allconcepts.keySet())
						{
							if (NegConcepts.get(cpNoNeg) == null)
							{
								concepts.put(cpNoNeg, allconcepts.get(cpNoNeg)) ;
							}
						}
			        }
			        else
			        {
			        	concepts.putAll(allconcepts);
			        }
			        
			        allconcepts.clear();
			        allconcepts.putAll(concepts);
					
					/*******************************************end Neg***********************************************/
					
					
					/*************************************************************************************************
					 *                                      Print Concepts
					 */
					/*************************************************************************************************/
					for ( String cpt : allconcepts.keySet())
					{
						String semanticGroup = allconcepts.get(cpt);
						System.out.println("("+cpt + ","+ semanticGroup+ ")") ;
					}
					
					//ReadXMLFile.Serializeddiectionary(allconcepts, "X:\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\KGConceps.xml");
					/*************************************************************************************************/
					
					Map<String, String> conceptsURI= new HashMap<String, String>();
					
					for ( String cpt : allconcepts.keySet())
					{
						if (!cpt.isEmpty())
						{
							String URI = BioPontologyfactory.classToOntowithfrq_avg(cpt,allconcepts.get(cpt),CNs.length, OntoGraph); 
							if (URI != null)
							    conceptsURI.put(cpt, URI) ;
						}
					}
					int ii = 1 ;
					if (ii == 0 ) {
						/*************************************************************************************************
						 *                                      Taxonomic Relations
						 */
						/*************************************************************************************************/

						for (String cpt : conceptsURI.keySet()) {
							if (!cpt.isEmpty()) {
								String URI = conceptsURI.get(cpt);
								if (URI != null)
									BioPontologyfactory.loadTaxonomic(cpt, URI, OntoGraph);
							}
						}
					}
						/*************************************************************************************************
						 *                                      End of Taxonomic Relations
						 */
						/*************************************************************************************************/


						/*************************************************************************************************
						 *                                       Relations
						 */
						/*************************************************************************************************/


					BioPontologyfactory.IGender_Value(allconcepts, clinicalN.getURI(), OntoGraph, ICAOnto);
					BioPontologyfactory.ihas_Age(allconcepts, OntoGraph,clinicalN.getURI());
					BioPontologyfactory.ihas_Race(allconcepts, OntoGraph,clinicalN.getURI());
					BioPontologyfactory.ihas_Disease_Association(allconcepts, OntoGraph, clinicalN.getURI());
					BioPontologyfactory.ihas_Symptom(allconcepts, OntoGraph,clinicalN.getURI());
					BioPontologyfactory.iTread_by(allconcepts, OntoGraph,clinicalN.getURI());
					BioPontologyfactory.ihas_Riskfactor(allconcepts, OntoGraph, clinicalN.getURI());
					BioPontologyfactory.iDiagnosed_by(allconcepts, OntoGraph,clinicalN.getURI());
					BioPontologyfactory.ihas_Aneuryrm(PrimaryConcept, OntoGraph,clinicalN.getURI());
					BioPontologyfactory.iVascular_Location(allconcepts, OntoGraph, individualIA.getURI() );
					BioPontologyfactory.ihas_Size(allconcepts, OntoGraph,individualIA.getURI() );
					BioPontologyfactory.iVessel_Side(allconcepts, OntoGraph, individualIA.getURI());
					BioPontologyfactory.ihas_Type(allconcepts,OntoGraph, ICAOnto,individualIA.getURI());
					BioPontologyfactory.iRupture_Status(allconcepts, OntoGraph,individualIA.getURI());

					if (ii != 0 )
					{
						OntoGraph.write(System.out, "RDF/XML-ABBREV");


						if (true) {
							readfiles.Writestringtofile("<sentence>", BioPontologyfactory.relResult);
							readfiles.Writestringtofile("  <Text>" + title + "</Text>", BioPontologyfactory.relResult);
						}

						//implicity


						// Word2vec





					//	BioPontologyfactory.Rupture_Status(allconcepts, PrimaryConceptURI, OntoGraph);





						// Word2vec




						OntoGraph.write(System.out, "RDF/XML-ABBREV");


						if (true) {
							readfiles.Writestringtofile("</sentence>", BioPontologyfactory.relResult);
						}

						/*************************************************************************************************
						 *                                       Instance Relations
						 */
						/*************************************************************************************************/
					/*OntModel ICAOnto = dataExtractor.riskFactorExtractor("X:\\Distance Supervision NER\\Data Medline_PubMed\\data\\ica_ontology_updated_April2.owl")  ;
					 
				    Resource CNote = BioPontologyfactory.classToOnto_URI (BioPontologyfactory.lo + CNs[i].getName().replace(".", "_"),OntoGraph) ;
					OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
					BioPontologyfactory.ihas_Disease_Association(allconcepts, OntoGraph,CNote); // resource 
					BioPontologyfactory.ihas_Riskfactor(allconcepts, OntoGraph,CNote,ICAOnto); // resource 
					BioPontologyfactory.ihas_Size(allconcepts, OntoGraph,CNote); // literal 
					
					BioPontologyfactory.iRupture_Status(allconcepts,OntoGraph, ICAOnto, CNote);
					BioPontologyfactory.ihas_Age(allconcepts,OntoGraph, CNote);
					BioPontologyfactory.ihas_Type(allconcepts,OntoGraph, ICAOnto, CNote);
					BioPontologyfactory.ihas_Value(allconcepts,OntoGraph, ICAOnto, CNote);
					BioPontologyfactory.iVascular_Location(allconcepts,OntoGraph, CNote, ICAOnto);
					BioPontologyfactory.ihas_Race(allconcepts,OntoGraph, CNote);;
					BioPontologyfactory.ihas_Symptom(allconcepts, OntoGraph,CNote);
					BioPontologyfactory.iTread_by(allconcepts, OntoGraph,CNote);
					BioPontologyfactory.iDiagnosed_by(allconcepts, OntoGraph,CNote);
					BioPontologyfactory.iVessel_Side(allconcepts, OntoGraph,CNote,ICAOnto);
					OntoGraph.write(System.out, "RDF/XML-ABBREV") ; 
					*/

						/*****************************************************************************************************************/
					 
					 /*  
					for ( String cpt : allconcepts.keySet())
					{
						if (!cpt.isEmpty())
						{
							

							String URI = BioPontologyfactory.classToOnto(cpt,allconcepts.get(cpt),OntoGraph);
							if (URI != null)
							{		

								BioPontologyfactory.semTypeToOnto(cpt,URI,OntoGraph) ;
								BioPontologyfactory.synonymToOnto(cpt, URI, OntoGraph);
								BioPontologyfactory.definitionToOnto(cpt, URI, OntoGraph);
								BioPontologyfactory.prefLabelToOnto(cpt, URI, OntoGraph);
								
								
								BioPontologyfactory.has_symptomToOnto(PrimaryConcept, cpt, OntoGraph);
								BioPontologyfactory.treated_byToOnto(PrimaryConcept, cpt, OntoGraph);
								BioPontologyfactory.diagnoses_byToOnto(PrimaryConcept, cpt, OntoGraph);
								BioPontologyfactory.locationToOnto(PrimaryConcept, cpt, OntoGraph);
								BioPontologyfactory.riskFactorToOnto1(PrimaryConcept, cpt, OntoGraph);
								
								OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
							}

							//BioPontologyfactory.SymptomsToOnto(cpt, concepts, OntoGraph);
							//BioPontologyfactory.TreatsToOnto(cpt, concepts, OntoGraph);
							//BioPontologyfactory.ComorbidToOnto1("brain aneurysm", concepts, OntoGraph) ;
							//OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
					        //BioPontologyfactory.SymptomsToOnto(cpt,concepts,OntoGraph);
						}
					}
					count++ ; 
					counter++; 
					 if (count == 50)
					 {
						     System.out.println("+++++++++++++++++++++++++++++++++");
						     System.out.println(counter);
						     System.out.println("+++++++++++++++++++++++++++++++++");
					        // Creating a File object that represents the disk file.
					        PrintStream o = new PrintStream(new File("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ANEBioPortal1.rdf"));
					        
					        // Store current System.out before assigning a new value
					        PrintStream console = System.out;
					 
					        // Assign o to output stream
					        System.setOut(o);	
							OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
							
					        // Use stored value for output stream
					        System.setOut(console);
					        count = 0 ; 
					 } */
					} // end if of != 0


				}
			/*OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
			// Creating a File object that represents the disk file.
			PrintStream o = new PrintStream(new File("X:\\KG_ANR\\Clinical Notes\\KGConcepts_relation3.owl"));

			// Store current System.out before assigning a new value
			PrintStream console = System.out;

			// Assign o to output stream
			System.setOut(o);
			OntoGraph.write(System.out, "RDF/XML-ABBREV") ;*/

			//System.setOut(console );

			System.out.println("++++++++++++++Done+++++++++++++++");
		}
		OntoGraph.write(System.out, "RDF/XML-ABBREV") ;
        // Creating a File object that represents the disk file.
        PrintStream o = new PrintStream(new File("X:\\KG_ANR\\Clinical Notes\\KGConcepts_relation2.owl"));
        
        // Store current System.out before assigning a new value
        PrintStream console = System.out;
 
        // Assign o to output stream
        System.setOut(o);
		OntoGraph.write(System.out, "RDF/XML-ABBREV") ;

		System.setOut(console );

		System.out.println("++++++++++++++Done+++++++++++++++");

	}
}

