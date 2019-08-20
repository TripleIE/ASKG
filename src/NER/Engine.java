package NER;

import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import context.implementation.ConText;
import util.NGramAnalyzer;
import util.ReadXMLFile;
import util.bioportal;
import util.dataExtractor;
import util.ica;
import util.readfiles;
import util.removestopwords;
import NLP.preProcessing;
import Stat.Scoring;

public class Engine {

	
	static Map<String, String> goldConceptsfound = new HashMap<String, String>();
	
	public static void main(String[] args) throws Exception
	{
		getConceptsbyOneClinicalNotess(null) ;
		//removestopwordsfromfiles();
		//mainold(null);
	}
	
	public static void getConceptsbyOneClinicalNote(String[] args) throws Exception {
		
		
		boolean longmatch = false ; 

		// TODO Auto-generated method stub
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		Map<String, String> concepts = new HashMap<String, String>();
		Map<String, Integer> longmatchcncepts = new HashMap<String, Integer>();
		Map<String, Integer> lmCconcepts = new HashMap<String, Integer>();
		// List<String> sentences = preProcessing.getSentences(CN) ; 
		Map<String, String> conceptsWNeg = new HashMap<String, String>();
		List<String> sentences = ReadXMLFile.ReadAneCNSent("X:\\KG_ANR\\Clinical NotesGold\\Recordid6_950_672018_Annotated_Madan_190225.xml") ; 
		double Totrecall = 0 ; 
		double Totpreciation = 0 ; 
		String result = "X:\\KG_ANR\\Clinical NotesGold\\Recordid6_950_672018_Annotated_Madan_190225_result13.xml" ;
		

		// MetaMap
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


		for (String sentence: sentences)
		{
			
			//Map<String, String> NegConcepts = umlsMapping.getNegconcepts(sentence, api) ; 
			
			readfiles.Writestringtofile("<sentence>", result);
		    readfiles.Writestringtofile("	<Text>" + sentence + "</Text>", result);
			
			String text = removestopwords.removestopwordfromsen(sentence) ;
			mentions = NGramAnalyzer.entities(1,3, text) ;	
			
			// clear concepts so we don't measure the old sentences 
			concepts.clear();
			conceptsWNeg.clear();
			
			
			//Negex https://code.google.com/archive/p/negex/source
			
			ConText myApplication = new ConText();
			String context_output = "";
			Map<String, String> NegConcepts = new HashMap<String, String>();
			for (String mention : mentions.keySet())
			{
				ArrayList res = null;
				res = myApplication.applyContext(mention, sentence);
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

			
			//for (String mention : mentions.keySet())
			{


				
				// get concepts,risk factors,Side, and Location
				concepts.putAll(bioportal.getConcepts(mentions))  ;
				
				// get Gender 
				concepts.putAll(Engine.getGender(sentence));
				
				// get race 
				concepts.putAll(Engine.getRace(sentence));
				
				//get type 
				concepts.putAll(Engine.getType(sentence));
				
				// get age
				concepts.putAll(Engine.getAge(sentence));
				
				// get size
				concepts.putAll(Engine.getSize(sentence));
				
				
				// get status
				concepts.putAll(Engine.getStatus(sentence));
				
				// get tests
				concepts.putAll(Engine.getTest(sentence));
				
			} 
			
			
			// to remove the Neg concept 
            if (NegConcepts.size() != 0)
            {
				for (String cpNoNeg:conceptsWNeg.keySet())
				{
					if (NegConcepts.get(cpNoNeg) == null)
					{
						concepts.put(cpNoNeg, conceptsWNeg.get(cpNoNeg)) ;
					}
				}
            }
            else
            {
            	concepts.putAll(conceptsWNeg);
            }
			
			
		    for (String mention : concepts.keySet())
		    {
				 
				readfiles.Writestringtofile("	<Concept>" + mention + "|" + concepts.get(mention)  + "</Concept>",  result);
		    	
		    }
		    
			readfiles.Writestringtofile("</sentence>", result);
		    
		    
			// Measurement  evaluation using gold standard
			List<String> GoldStandard = ReadXMLFile.ReadAneCN("X:\\KG_ANR\\Clinical NotesGold\\Recordid6_950_672018_Annotated_Madan_190225.xml",sentence) ; 
			
			double recall = 0 ; 
			double preciation = 0 ; 

			int conceptscount  =  0  ;
			double hits = 0 ;
			
			
			
			if (GoldStandard.size() != 0 || (GoldStandard.size() != 0  && concepts.size() != 0 ) )
			{
				conceptscount = GoldStandard.size() ;
				for (String concept: concepts.keySet())
				{
					
					for (String Golds: GoldStandard)
					{
						
						// 35-year --> 35 year
						if ( Golds.replace("-", " ").toLowerCase().contains(concept))
						{
							hits++ ; 
							break ; 
						}
					}
				}
				
				if (conceptscount != 0)
				{
					if (hits <= conceptscount)
						recall = hits/conceptscount ; 
					else
						recall = 1 ; 
				}
				
				if (concepts.size() != 0 )
					preciation = hits/concepts.size() ; 
				
				Totrecall += recall ; 
				Totpreciation += preciation ; 
			}
			else
			{
				Totrecall += 1 ; 
				Totpreciation += 1 ; 
			}

		}
		
		// Serialize concepts 
	    ReadXMLFile.Serializeddiectionary(concepts, "X:\\KG_ANR\\Clinical NotesGold\\HR.dat");
		
		System.out.println("Recall : " + Totrecall/sentences.size());
		System.out.println("preciation : " + Totpreciation/sentences.size());
		
		readfiles.Writestringtofile("Recall : " + Totrecall/sentences.size(), result);
		readfiles.Writestringtofile("preciation : " + Totpreciation/sentences.size(), result);
		
		
		
	}
	
	
	
public static void getConceptsbyOneClinicalNotess(String[] args) throws Exception {
		
		
		boolean longmatch = false ; 

		// TODO Auto-generated method stub
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		Map<String, String> concepts = new HashMap<String, String>();
		Map<String, Integer> longmatchcncepts = new HashMap<String, Integer>();
		Map<String, Integer> lmCconcepts = new HashMap<String, Integer>();
		// List<String> sentences = preProcessing.getSentences(CN) ; 
		Map<String, String> conceptsWNeg = new HashMap<String, String>();
 
		double Totrecall = 0 ; 
		double Totpreciation = 0 ; 
		String result = "X:\\KG_ANR\\Clinical NotesGold\\allresults.xml" ;
		

		// MetaMap
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
	    int senCount = 0 ; 
	    File CNs[] =  readfiles.readAllFileFromPath("X:\\KG_ANR\\xmlClinicalnote") ;
	    for (int i = 0; i < CNs.length; i++)
	    {
	
	    	List<String> sentences = ReadXMLFile.ReadAneCNSent(CNs[i].getAbsolutePath()) ; 
	    
			for (String sentence: sentences)
			{
				senCount++ ;
				
				//Map<String, String> NegConcepts = umlsMapping.getNegconcepts(sentence, api) ; 
				
				readfiles.Writestringtofile("<sentence>", result);
			    readfiles.Writestringtofile("	<Text>" + sentence + "</Text>", result);
				
				String text = removestopwords.removestopwordfromsen(sentence) ;
				mentions = NGramAnalyzer.entities(1,4, text) ;
				
				// clear concepts so we don't measure the old sentences 
				concepts.clear();
				conceptsWNeg.clear();
				
				
				//Negex https://code.google.com/archive/p/negex/source
				
				ConText myApplication = new ConText();
				String context_output = "";
				Map<String, String> NegConcepts = new HashMap<String, String>();
				for (String mention : mentions.keySet())
				{
					ArrayList res = null;
					res = myApplication.applyContext(mention, sentence);
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
		
				
				//for (String mention : mentions.keySet())
				{
		
		
					
					// get concepts,risk factors,Side, and Location
					concepts.putAll(bioportal.getConcepts(mentions))  ;
					
					// get Gender 
					concepts.putAll(Engine.getGender(sentence));
					
					// get race 
					concepts.putAll(Engine.getRace(sentence));
					
					//get type 
					concepts.putAll(Engine.getType(sentence));
					
					// get age
					concepts.putAll(Engine.getAge(sentence));
					
					// get size
					concepts.putAll(Engine.getSize(sentence));
					
					
					// get status
					concepts.putAll(Engine.getStatus(sentence));
					
					// get tests
					concepts.putAll(Engine.getTest(sentence));
					
				} 
				
				
				// to remove the Neg concept 
		        if (NegConcepts.size() != 0)
		        {
					for (String cpNoNeg:conceptsWNeg.keySet())
					{
						if (NegConcepts.get(cpNoNeg) == null)
						{
							concepts.put(cpNoNeg, conceptsWNeg.get(cpNoNeg)) ;
						}
					}
		        }
		        else
		        {
		        	concepts.putAll(conceptsWNeg);
		        }
				
				
			    for (String mention : concepts.keySet())
			    {
					 
					readfiles.Writestringtofile("	<Concept>" + mention + "|" + concepts.get(mention)  + "</Concept>",  result);
			    	
			    }
			    
				readfiles.Writestringtofile("</sentence>", result);
			    
			    
				// Measurement  evaluation using gold standard
				List<String> GoldStandard = ReadXMLFile.ReadAneCN(CNs[i].getAbsolutePath(),sentence) ; 
				
				double recall = 0 ; 
				double preciation = 0 ; 
		
				int conceptscount  =  0  ;
				double hits = 0 ;
				
				
				
				if (GoldStandard.size() != 0 || (GoldStandard.size() != 0  && concepts.size() != 0 ) )
				{
					conceptscount = GoldStandard.size() ;
					for (String concept: concepts.keySet())
					{
						
						for (String Golds: GoldStandard)
						{
							
							// 35-year --> 35 year
							if ( Golds.replace("-", " ").toLowerCase().contains(concept))
							{
								hits++ ; 
								break ; 
							}
						}
					}
					
					if (conceptscount != 0)
					{
						if (hits <= conceptscount)
							recall = hits/conceptscount ; 
						else
							recall = 1 ; 
					}
					
					if (concepts.size() != 0 )
						preciation = hits/concepts.size() ; 
					
					Totrecall += recall ; 
					Totpreciation += preciation ; 
				}
				else
				{
					Totrecall += 1 ; 
					Totpreciation += 1 ; 
				}
		
			}
			
			System.out.println("Recall : " + Totrecall/senCount);
			System.out.println("preciation : " + Totpreciation/senCount);
	    }
		
		// Serialize concepts 
	    ReadXMLFile.Serializeddiectionary(concepts, "X:\\KG_ANR\\Clinical NotesGold\\HR.dat");
			
		System.out.println("Recall : " + Totrecall/senCount);
		System.out.println("preciation : " + Totpreciation/senCount);
		
		readfiles.Writestringtofile("Recall : " + Totrecall/senCount, result);
		readfiles.Writestringtofile("preciation : " + Totpreciation/senCount, result);
		
		
		
	}

	public static void removestopwordsfromfiles()throws Exception
	{
		File CNs[] =  readfiles.readAllFileFromPath("X:\\KG_ANR\\Clinical Notes") ;
		for (int i = 0; i < CNs.length; i++) {
			// reading clinical notes
			List<String> sentences = ReadXMLFile.ReadAneCNSent(CNs[i].getAbsolutePath()) ;

			for (String sentence: sentences)
			{
				String text  = removestopwords.removestopwordfromsen(sentence) ;
				text += "." ;
				readfiles.Writestringtofile(text,"X:\\KG_ANR\\Clinical NotesGold\\training.txt");
			}
		}
	}
public static void getConceptsbyClinicalNotes(String[] args) throws Exception {
		
		
		boolean longmatch = false ; 

		// TODO Auto-generated method stub
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		Map<String, String> concepts = new HashMap<String, String>();
		Map<String, Integer> longmatchcncepts = new HashMap<String, Integer>();
		Map<String, Integer> lmCconcepts = new HashMap<String, Integer>();
		// List<String> sentences = preProcessing.getSentences(CN) ; 
		Map<String, String> conceptsWNeg = new HashMap<String, String>();
		//List<String> sentences = ReadXMLFile.ReadAneCNSent("X:\\KG_ANR\\Clinical NotesGold\\Recordid6_950_672018_Annotated_Madan_190225.xml") ; 
		double Totrecall = 0 ; 
		double Totpreciation = 0 ; 
		String result = "X:\\KG_ANR\\Clinical NotesGold\\Recordid6_950_672018_Annotated_Madan_190225_result11.xml" ;
		

		// MetaMap
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
	    
	    File CNs[] =  readfiles.readAllFileFromPath("X:\\KG_ANR\\Clinical Notes") ;
	    for (int i = 0; i < CNs.length; i++)
	    {
	
	    	   List<String> sentences = ReadXMLFile.ReadAneCNSent(CNs[i].getAbsolutePath()) ; 
	    
				for (String sentence: sentences)
				{
					
					//Map<String, String> NegConcepts = umlsMapping.getNegconcepts(sentence, api) ; 
					
				//	readfiles.Writestringtofile("<sentence>", result);
				//	readfiles.Writestringtofile("	<Text>" + sentence + "</Text>", result);
					
					String text = removestopwords.removestopwordfromsen(sentence) ;
					mentions = NGramAnalyzer.entities(1,3, text) ;	
					
					//concepts.clear();
					//conceptsWNeg.clear();
					
		
					
					//for (String mention : mentions.keySet())
					{
		
		
						
						// get concepts,risk factors,Side, and Location
						concepts.putAll(bioportal.getConcepts(mentions))  ;
						
						// get Gender 
						concepts.putAll(Engine.getGender(sentence));
						
						// get race 
						concepts.putAll(Engine.getRace(sentence));
						
						//get type 
						concepts.putAll(Engine.getType(sentence));
						
						// get age
						concepts.putAll(Engine.getAge(sentence));
						
						// get size
						concepts.putAll(Engine.getSize(sentence));
						
						
						// get status
						concepts.putAll(Engine.getStatus(sentence));
						
						// get tests
						concepts.putAll(Engine.getTest(sentence));
						
					} 
		
				    for (String mention : concepts.keySet())
				    {
						 
						readfiles.Writestringtofile("	<Concept>" + mention + "|" + concepts.get(mention)  + "</Concept>",  result);
				    	
				    }
				    
					readfiles.Writestringtofile("</sentence>", result);
				    
				   /******************************** Evaluation ***********************************************/ 
					// Measurement  evaluation using gold standard
/*					List<String> GoldStandard = ReadXMLFile.ReadAneCN("X:\\KG_ANR\\Clinical NotesGold\\recordid_935_7192018_Annotated_Madan_190225.xml",sentence) ; 
					
					double recall = 0 ; 
					double preciation = 0 ; 
		
					int conceptscount  =  0  ;
					double hits = 0 ;
					
					if (GoldStandard.size() != 0 || (GoldStandard.size() != 0  && concepts.size() != 0 ) )
					{
						conceptscount = GoldStandard.size() ;
						for (String concept: concepts.keySet())
						{
							
							for (String Golds: GoldStandard)
							{
								
								// 35-year --> 35 year
								if ( Golds.replace("-", " ").toLowerCase().contains(concept))
								{
									hits++ ; 
									break ; 
								}
							}
						}
						
						if (conceptscount != 0)
						{
							if (hits <= conceptscount)
								recall = hits/conceptscount ; 
							else
								recall = 1 ; 
						}
						
						if (concepts.size() != 0 )
							preciation = hits/concepts.size() ; 
						
						Totrecall += recall ; 
						Totpreciation += preciation ; 
					}
					else
					{
						Totrecall += 1 ; 
						Totpreciation += 1 ; 
					}*/
		
				}
        }
		
		// Serialize concepts 
	    ReadXMLFile.Serializeddiectionary(concepts, "X:\\KG_ANR\\Clinical NotesGold\\HR.dat");
/*		
		System.out.println("Recall : " + Totrecall/sentences.size());
		System.out.println("preciation : " + Totpreciation/sentences.size());
		
		readfiles.Writestringtofile("Recall : " + Totrecall/sentences.size(), result);
		readfiles.Writestringtofile("preciation : " + Totpreciation/sentences.size(), result);*/
		
		
		
	}
	
	public static void mainold(String[] args) throws Exception {
		
		
		String filename3 = "X:\\KG_ANR\\Clinical Notes\\Khalid_23_April_62823_CH_3_Kroll_2019.xml" ;
		String filenameq = "X:\\KG_ANR\\Clinical Notes\\Khalid_23_April_62823_CH_3_Kroll_2019.txt" ;
		File fXmlFileq = new File(filenameq);
		String CNq = readfiles.readLinestostring(fXmlFileq.toURL());
		List<String> sentencesq = preProcessing.getSentences(CNq) ; 
		
		for (String sentence: sentencesq)
		{
			readfiles.Writestringtofile("<sentence>", filename3);
			readfiles.Writestringtofile("	<Text>" + sentence + "</Text>", filename3);
			readfiles.Writestringtofile("	<Concept>" + "????????|????????????" + "</Concept>", filename3);
			readfiles.Writestringtofile("</sentence>", filename3);
		}
		

		
		boolean longmatch = false ; 
		//getIntracranialAneurysmAvaluation() ;
		String text = "" ;  //Diabetes is a chronic condition associated with abnormally high levels of glucose in the blood" ;
		//String Mfilename = "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\CN2concepts.txt" ;
		
		//String Mfilename = "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\CN_3.txt" ;
		//File mfXmlFile = new File(Mfilename);
		
		
	//	String filename = "X:\\KG_ANR\\Clinical NotesGold\\Recordid6_950_672018_Annotated_Madan_190225.xml";
	//	File fXmlFile = new File(filename);
	//	String CN = readfiles.readLinestostring(fXmlFile.toURL());
		// TODO Auto-generated method stub
		Map<String, Integer> mentions = new HashMap<String, Integer>();
		Map<String, String> concepts = new HashMap<String, String>();
		Map<String, Integer> longmatchcncepts = new HashMap<String, Integer>();
		Map<String, Integer> lmCconcepts = new HashMap<String, Integer>();
		// List<String> sentences = preProcessing.getSentences(CN) ; 
		Map<String, String> conceptsWNeg = new HashMap<String, String>();
		List<String> sentences = ReadXMLFile.ReadAneCNSent("X:\\KG_ANR\\Clinical NotesGold\\recordid_935_7192018_Annotated_Madan_190225.xml") ; 
		
/*		String filename3 = "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\CNGoldStandard.txt" ;
		for (String sentence: sentences)
		{
			readfiles.Writestringtofile("<sentence>", filename3);
			readfiles.Writestringtofile("	<Text>" + sentence + "</Text>", filename3);
			readfiles.Writestringtofile("	<Concept>" + "????????|????????????" + "</concept>", filename3);
			readfiles.Writestringtofile("</sentence>", filename3);
		} */
		
		List<String>  listSG = new ArrayList<String>();
		listSG.add("Chemicals or Drugs") ;
		listSG.add("Disorders") ;
		listSG.add("Devices") ;
		listSG.add("Physiology") ;
		listSG.add("Anatomy") ;
		listSG.add("Physiology") ;
		listSG.add("Activities & Behaviors") ;
		double Totrecall = 0 ; 
		double Totpreciation = 0 ; 
		String result = "X:\\KG_ANR\\Clinical NotesGold\\recordid_935_7192018_Annotated_Madan_190225_result.xml" ;
		

		// MetaMap
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
	    
	    
		for (String sentence: sentences)
		{
			
			//Map<String, String> NegConcepts = umlsMapping.getNegconcepts(sentence, api) ; 
			
			readfiles.Writestringtofile("<sentence>", result);
			readfiles.Writestringtofile("	<Text>" + sentence + "</Text>", result);
			
			text = removestopwords.removestopwordfromsen(sentence) ;
			mentions = NGramAnalyzer.entities(1,3, text) ;	
			
			concepts.clear();
			conceptsWNeg.clear();
			
/*			// get concepts from linked life data
		    concepts.putAll(ontologyMapping.getAnnotationSG(mentions,listSG)) ; 
		    for (String mention : concepts.keySet())
		    {
				 
				readfiles.Writestringtofile("	<Concept>" + mention + "|" + concepts.get(mention)  + "</Concept>",  result);
		    	
		    }
		    
		    // get concept from bioportal 
			for (String mention : mentions.keySet())
			{
				String cpSG = null ; 
				if ((cpSG = bioportal.isConceptWithspecficSG(mention))  != null) 
				{
				   concepts.put(mention.toLowerCase(), cpSG); 
				   readfiles.Writestringtofile("	<Concept>" + mention + "|" + cpSG  + "</Concept>",  result);
				}
			}  */
			

			  
			
			
			// get concepts from linked life data
		   // concepts.putAll(ontologyMapping.getAnnotationSG(mentions,listSG)) ; 
		    
/*			for (String mention : mentions.keySet())
			{
				String cpSG = null ; 
				if ((cpSG = bioportal.isConceptWithspecficSG(mention))  != null) 
				{
					conceptsWNeg.put(mention.toLowerCase(), cpSG); 
				}
				else
				{
					if (ica.isConcept(mention.toLowerCase(),null))
					{
						conceptsWNeg.put(mention.toLowerCase(), "Disorders");
					}
				}
			} */
			
			//for (String mention : mentions.keySet())
			{


				
				// get concepts,risk factors,Side, and Location
				concepts = bioportal.getConcepts(mentions)  ;
				
				// get Gender 
				concepts.putAll(Engine.getGender(sentence));
				
				// get race 
				concepts.putAll(Engine.getRace(sentence));
				
				//get type 
				concepts.putAll(Engine.getType(sentence));
				
				// get age
				concepts.putAll(Engine.getAge(sentence));
				
				// get size
				concepts.putAll(Engine.getSize(sentence));
				
				
				// get status
				concepts.putAll(Engine.getStatus(sentence));
				
				// get tests
				concepts.putAll(Engine.getTest(sentence));
				
			} 
/*			// to remove the Neg concept 
            if (NegConcepts.size() != 0)
            {
				for (String cpNoNeg:conceptsWNeg.keySet())
				{
					if (NegConcepts.get(cpNoNeg) == null)
					{
						concepts.put(cpNoNeg, conceptsWNeg.get(cpNoNeg)) ;
					}
				}
            }
            else
            {
            	concepts.putAll(conceptsWNeg);
            }*/
			
			
		    for (String mention : concepts.keySet())
		    {
				 
				readfiles.Writestringtofile("	<Concept>" + mention + "|" + concepts.get(mention)  + "</Concept>",  result);
		    	
		    }
		    
			readfiles.Writestringtofile("</sentence>", result);
		    
		    
			// Measurement  evaluation using gold standard
			List<String> GoldStandard = ReadXMLFile.ReadAneCN("X:\\KG_ANR\\Clinical NotesGold\\recordid_935_7192018_Annotated_Madan_190225.xml",sentence) ; 
			
			double recall = 0 ; 
			double preciation = 0 ; 

			int conceptscount  =  0  ;
			double hits = 0 ;
			
			if (GoldStandard.size() != 0 || (GoldStandard.size() != 0  && concepts.size() != 0 ) )
			{
				conceptscount = GoldStandard.size() ;
				for (String concept: concepts.keySet())
				{
					
					for (String Golds: GoldStandard)
					{
						
						// 35-year --> 35 year
						if ( Golds.replace("-", " ").toLowerCase().contains(concept))
						{
							hits++ ; 
							break ; 
						}
/*						else if(ica.isConcept(concept.toLowerCase(),null))
						{
							hits++ ; 
							break ; 
						}*/
					}
				}
				
				if (conceptscount != 0)
				{
					if (hits <= conceptscount)
						recall = hits/conceptscount ; 
					else
						recall = 1 ; 
				}
				
				if (concepts.size() != 0 )
					preciation = hits/concepts.size() ; 
				
				Totrecall += recall ; 
				Totpreciation += preciation ; 
			}
			else
			{
				Totrecall += 1 ; 
				Totpreciation += 1 ; 
			}

		}
		
		// Serialize concepts 
	//	ReadXMLFile.Serializeddiectionary(concepts, "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\discoveredConceptsCN3_1_4Bo.dat");
		
		System.out.println("Recall : " + Totrecall/sentences.size());
		System.out.println("preciation : " + Totpreciation/sentences.size());
		
		readfiles.Writestringtofile("Recall : " + Totrecall/sentences.size(), result);
		readfiles.Writestringtofile("preciation : " + Totpreciation/sentences.size(), result);
		
		// do only long match , so we here remove all concepts that part of long match concept
		//******************************************************************************************
/*		if (longmatch )
		{
			    lmCconcepts.putAll(concepts);
				Map<String, Integer> Mconcepts = readfiles.readLinesbylinesToMap(mfXmlFile.toURL()) ;
				int count = 0 ; 
				 
				for (String concept: concepts.keySet())
				{
					boolean found = false ;
					
					for (String lmCconcept: lmCconcepts.keySet())
					{
						if (lmCconcept.length() > concept.length() &&  lmCconcept.contains(concept))
						{
							found = true ; 
							break; 
						}
					}
					
					if(!found)
						longmatchcncepts.put(concept.toLowerCase(), 1) ;
				}
				concepts.clear();
				concepts.putAll(longmatchcncepts);
		}*/
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		
	}
	/*****************************************************************************************************************/
	/**************************************** get gender concepts ****************************************************/
	public static Map<String,String> getGender(String sentence) 
	{
		
		String[] tokens = sentence.split(" "); 
		Map<String, String> concepts = new HashMap<String, String>();
		
		if ( tokens != null)
		{
			String[] flist= {"female","woman", "miss","lady","mother","wife","girl"} ; 
			String[] mlist= {"male","man","father","husband", "guy", "boy"} ; 
			int fCount = 0 ; 
			int mCount = 0 ; 
			for (int i =0; i < tokens.length; i++ )
			{
				
				for (int f = 0; f < flist.length; f++)
				{
					if (tokens[i].compareToIgnoreCase(flist[f]) == 0 )
					{
						concepts.put( flist[f],"Gender")  ;
					}
				}
				
				for (int m = 0; m < mlist.length; m++)
				{
					if (tokens[i].compareToIgnoreCase(mlist[m]) == 0 )
					{
						concepts.put(mlist[m],"Gender")  ; 
					}
				}
			}
			


		}
		
		return concepts ; 
	}
	
	
	/*****************************************************************************************************************/
	/**************************************** get size ****************************************************/
	public static Map<String,String> getSize(String sentence) 
	{
		
		Map<String, String> concepts = new HashMap<String, String>();
		
		{
			//{25 mm, 12-20 Mm, 1/4 inch, 1/4 to 1/5 inch,2.5 mm, 1.5-2.4 mm}
			String[] regex= {"[0-9]+\\s+[m|M][m|M]","[0-9]+-[0-9]+\\s+[m|M][m|M]","[0-9]+/[0-9]+\\s+[i|I][n|N][c|C][h|H]","[0-9]+/[0-9]+\\s+[t|T][o|O]\\s+[0-9]+/[0-9]+\\s+[i|I][n|N][c|C][h|H]","[0-9]+.[0-9]+\\s+[m|M][m|M]","[0-9]+.[0-9]+-[0-9]+.[0-9]+\\s+[m|M][m|M]"} ; 
			for (int i =0; i < regex.length; i++ )
			{
				Pattern pattern = Pattern.compile(regex[i]);
				Matcher matcher = pattern.matcher(sentence);
				
		        while (matcher.find()) {
		            System.out.println(matcher.group());
		            concepts.put(matcher.group(),"Size") ;
		        }
			}
		}
		
		return concepts ; 
	}
	
	/*****************************************************************************************************************/
	/**************************************** get Age ****************************************************/
	public static Map<String,String> getAge(String sentence) 
	{
		
		Map<String, String> concepts = new HashMap<String, String>();
		{
			//{25 years,25 old,26 y.o.,35-year}
			String[] regex= {"[0-9]+\\s+[Y/y][E/e][A|a][R/r][S|s]*","[0-9]+\\s+[O/o][L/l][D|d]","[0-9]+\\s+[Y/y][.][O|o]","[0-9]+[-]+[Y/y][E/e][A|a][R/r][S|s]*"} ; 
			for (int i =0; i < regex.length; i++ )
			{
				Pattern pattern = Pattern.compile(regex[i]);
				Matcher matcher = pattern.matcher(sentence);
				
		        while (matcher.find()) {
		            System.out.println(matcher.group());
		            concepts.put(matcher.group(),"Age") ;
		        }
			}
		}
		
		return concepts ; 
	}
	
	/*****************************************************************************************************************/
	/**************************************** get Race concepts ****************************************************/
	public static Map<String,String> getRace(String sentence) 
	{
		Map<String, String> concepts = new HashMap<String, String>();
			//Asian/Oriental
			//Black/African American
			//Native American/American Indian
			//White/Caucasian
			
			String[] racelist= {"Asian","Oriental", "Black","African American","Native American","American Indian","White","Caucasian"} ; 

			{
				
				for (int index = 0; index < racelist.length; index++)
				{
					if (sentence.toLowerCase().contains(racelist[index].toLowerCase())  )
					{
						concepts.put(racelist[index],"Race")  ;
					}
				}
			}
		
		return concepts ; 
	}
	
	
	/*****************************************************************************************************************/
	/**************************************** get Race concepts ****************************************************/
	public static Map<String,String> getType(String sentence) 
	{
		  Map<String, String> concepts = new HashMap<String, String>();
		  //Saccular 
		  //Fusiform
		 // Dissection
			
			String[] racelist= {"Saccular","Fusiform", "Dissection"} ; 

			{
				
				for (int index = 0; index < racelist.length; index++)
				{
					if (sentence.toLowerCase().contains(racelist[index].toLowerCase())  )
					{
						concepts.put(racelist[index],"Type")  ;
					}
				}
			}
		
		return concepts ; 
	}
	
	/*****************************************************************************************************************/
	/**************************************** get Race concepts ****************************************************/
	public static Map<String,String> getStatus(String sentence) 
	{
		  Map<String, String> concepts = new HashMap<String, String>();
			String[] racelist= {"Ruptured","un_Ruptured", "unruptured"} ; 

			{
				
				for (int index = 0; index < racelist.length; index++)
				{
					if (sentence.toLowerCase().contains(racelist[index].toLowerCase())  )
					{
						concepts.put(racelist[index],"Status")  ;
					}
				}
			}
		
		return concepts ; 
	}
	
	/*****************************************************************************************************************/
	/**************************************** get Race concepts ****************************************************/
	public static Map<String,String> getTest(String sentence) 
	{
		  Map<String, String> concepts = new HashMap<String, String>();
			String[] racelist= {" MRI "," CT ", " EEG "," MRA "," CTA "} ; 

			{
				
				for (int index = 0; index < racelist.length; index++)
				{
					if (sentence.toLowerCase().contains(racelist[index].toLowerCase())  )
					{
						concepts.put(racelist[index],"Tests or Procedures")  ;
					}
				}
			}
		
		return concepts ; 
	}
	
	/*************************************************************************************************************************************************************************/
	/************************************************************ using SemRep Gold Standard Annotation : https://semrep.nlm.nih.gov/GoldStandard.html *******************************************************************************************/
	/*************************************************************************************Annotator C: Adjudication (adjudicated.xml) 
	 * @throws IOException ************************************************************************************/
	
	 public static void getSemRepAvaluation() throws IOException
	  {
		  Map<String, List<String>> sentence =  ReadSemRepGoldStandard()  ; 
		  
		  getmeasureSemRep(sentence);
		  
	  }
	  public static void getIntracranialAneurysmAvaluation() throws IOException
	  {
		  Map<String, List<String>> sentence =  ReadIntracranialAneurysmGoldStandard()  ; 
		  
		  getmeasureSemRep(sentence);
		  
	  }
	  
	  public static Map<String, List<String>> ReadIntracranialAneurysmGoldStandard() {

		    Map<String, List<String>> goldstandard = null ;
		    goldstandard =  null ;
	        int count = 0 ; 
		    if (goldstandard== null )
		    try {

	       String filename = "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\data\\Intracranialaneurysm.xml" ;


		    goldstandard  = new HashMap<String, List<String>>();
			File fXmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	        
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList rowList = doc.getElementsByTagName("ROW");

			for (int i = 0; i < rowList.getLength()  ; i++)
			{
				List<String> conclist = new ArrayList<String>() ;
				NodeList rowchildren = rowList.item(i).getChildNodes();
				
				// get the predications
				String Sentence = null ; 
				String subject = null ;
				String object = null ;
				for (int j = 0; j < rowchildren.getLength(); j++)
				{
					String sent = rowchildren.item(j).getNodeName() ;
					
					if ("SENTENCE".equals(rowchildren.item(j).getNodeName())) 
					{
						Sentence = rowchildren.item(j).getTextContent() ;
					}
					if ("SUBJECT_NAME".equals(rowchildren.item(j).getNodeName())) 
					{
						subject = rowchildren.item(j).getTextContent() ;
					}
					if ("OBJECT_NAME".equals(rowchildren.item(j).getNodeName())) 
					{
						object = rowchildren.item(j).getTextContent() ;
					}
	
				}
				
				if (Sentence != null)
				{
					count ++ ;
					conclist.add(subject);
					conclist.add(object) ;
					goldstandard.put(Sentence + " " + Integer.toString(count), conclist) ;
					 
				}
				else
				{
					conclist.add(subject);
				}
				
				
				//goldstandard.put(sentence, conclist) ;
			}

		    } 
		    catch (Exception e) {
			e.printStackTrace();
		    }
		    
		    return goldstandard;
		  } ;
	  public static Map<String, List<String>> ReadSemRepGoldStandard() {

		    Map<String, List<String>> goldstandard = null ;
		    goldstandard =  null ;
		    if (goldstandard== null )
		    try {

	       String filename = "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\data\\adjudicated.xml" ;


		    goldstandard  = new HashMap<String, List<String>>();
			File fXmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	        
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList sentenceList = doc.getElementsByTagName("Sentence");
	        int count = 0 ; 
			for (int i = 0; i < sentenceList.getLength()  ; i++)
			{
				List<String> conclist = new ArrayList<String>() ;
				NodeList predicationsList = sentenceList.item(i).getChildNodes();
				String sentence = sentenceList.item(i).getAttributes().item(2).getTextContent() ;
				
				// get the predications
				for (int j = 0; j < predicationsList.getLength(); j++)
				{
					NodeList predicationList = predicationsList.item(j).getChildNodes();
					 // get the predication
					for (int j1 = 0; j1 < predicationList.getLength(); j1++)
					{
						NodeList sub_objList = predicationList.item(j1).getChildNodes();
						// subject or object 
						for (int j2 = 0; j2 < sub_objList.getLength(); j2++)
						{
							if ("Subject".equals(sub_objList.item(j2).getNodeName()) || "Object".equals(sub_objList.item(j2).getNodeName()))
							{
								String concept = sub_objList.item(j2).getAttributes().item(1).getTextContent() ;
								conclist.add(concept.toLowerCase()) ;
							}
						}
						
					}
	
				}
				
				
				goldstandard.put(sentence, conclist) ;
			}

		    } 
		    catch (Exception e) {
			e.printStackTrace();
		    }
		    return goldstandard;
		  } ;
	
		  
		  
		  public static String getmeasureSemRep(Map<String, List<String>> Sentences) throws IOException
			{
				

//				double avgRecall = 0.0  ; 
//				double avgPrecision = 0.0 ;
//				double avgFmeasure = 0.0 ; 
//				int size_ = titles.size() ;
				int counter = 0 ; 
				measure result = null ;
				measure synResult = null ;
				measure ansResult = null ;
				measure allcResult = new measure() ;  
				measure allsynResult = new measure() ;  
				measure allansResult = new measure() ;  
				measure totResult = new measure() ;
				for(String Sentence : Sentences.keySet())
				{
					counter++  ; 
					// get concepts of the Sentence 
					List<String> GoldSndconcepts = Sentences.get(Sentence); 
					
					try {
						
						
						Map<String, Integer> concepts = new HashMap<String, Integer>();
						Map<String, Integer> synConcepts = new HashMap<String, Integer>();
						Map<String, Integer> ansConcepts = new HashMap<String, Integer>();
						Map<String, Integer> keywords = new HashMap<String, Integer>();
						Sentence  = removestopwords.removestopwordfromsen(Sentence ) ;
						keywords = NGramAnalyzer.entities(1,4, Sentence) ;
						//concepts = ontologyMapping.getAnnotation(mentions)  ;
						for(String keyword: keywords.keySet())
						{
							Map<String, Integer> mentions  = new HashMap<String, Integer>(); 
							mentions.put(keyword, 1);
							Map<String, String> allconcepts = null ;
							allconcepts = bioportal.getConcepts(mentions)  ;
							Map<String, String> ccpts = new HashMap<String, String>();
							
							for ( String cpt : allconcepts.keySet())
							{
								if (!cpt.isEmpty())
								{
									concepts.put(keyword, 1) ;
								} 
							}
							
						}
						result = getPRF(Sentence,concepts,GoldSndconcepts) ; 
						// with no syn and ans
						allcResult.avgRecall =  result.avgRecall +  allcResult.avgRecall ;
						allcResult.avgPrecision =  result.avgPrecision  + allcResult.avgPrecision ;
						allcResult.avgFmeasure =  result.avgFmeasure  + allcResult.avgFmeasure ;
						
	/*					synResult = getPRF(title,synConcepts,GoldSndconcepts) ; 
						
						allsynResult.avgRecall =  synResult .avgRecall +  allsynResult.avgRecall ;
						allsynResult.avgPrecision =  synResult .avgPrecision  + allsynResult.avgPrecision ;
						allsynResult.avgFmeasure =  synResult .avgFmeasure  + allsynResult.avgFmeasure ;
						
						ansResult = getPRF(title,ansConcepts,GoldSndconcepts) ; 
						
						allansResult.avgRecall =  ansResult.avgRecall +  allansResult.avgRecall ;
						allansResult.avgPrecision =  ansResult.avgPrecision  + allansResult.avgPrecision ;
						allansResult.avgFmeasure =  ansResult.avgFmeasure  + allansResult.avgFmeasure ;
						
						totResult.avgRecall =  result.avgRecall +  synResult.avgRecall + ansResult.avgRecall + totResult.avgRecall ;
						totResult.avgPrecision =  result.avgPrecision +  synResult.avgPrecision + ansResult.avgPrecision + totResult.avgPrecision  ;
						totResult.avgFmeasure =  result.avgFmeasure+  synResult.avgFmeasure+ ansResult.avgFmeasure  + totResult.avgFmeasure;*/
						

						
						
						
		                
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				allcResult.avgRecall =  allcResult.avgRecall  / Sentences.size() ;
				allcResult.avgPrecision =  allcResult.avgPrecision / Sentences.size() ;
				allcResult.avgFmeasure =  allcResult.avgFmeasure / Sentences.size() ;
				
	/*			allsynResult.avgRecall =  allsynResult.avgRecall  / titles.size() ;
				allsynResult.avgPrecision =  allsynResult.avgPrecision / titles.size() ;
				allsynResult.avgFmeasure =  allsynResult.avgFmeasure / titles.size() ;
				
				allansResult.avgRecall =  allansResult.avgRecall  / titles.size() ;
				allansResult.avgPrecision =  allansResult.avgPrecision / titles.size() ;
				allansResult.avgFmeasure =  allansResult.avgFmeasure / titles.size() ;
				
				
				totResult.avgRecall = totResult.avgRecall  / titles.size() ;
				totResult.avgPrecision = totResult.avgPrecision  / titles.size() ;
				totResult.avgFmeasure = totResult.avgFmeasure / titles.size() ;*/
				
				String output = Double.toString(allcResult.avgRecall) + " " +  Double.toString(allcResult.avgPrecision) +" " +  Double.toString(allcResult.avgFmeasure) ;
				ReadXMLFile.Serializeddiectionary(goldConceptsfound, "C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\data\\GoldnotFound.xml");
				return output;
			}
	/*************************************************************************************************************************************************************************/
	/************************************************************ Evaluation Part 
	 * @throws IOException *******************************************************************************************/
	  public static void getAvaluation() throws IOException
	  {
		  Map<String, List<String>> titles =  ReadCDR_TestSet_BioC()  ; 
		  String result = getmeasureRPF( titles) ;
		  System.out.println(result);
		  
	  }
	  
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
					
					
					Map<String, Integer> concepts = new HashMap<String, Integer>();
					Map<String, Integer> mentions = new HashMap<String, Integer>();
					
					
					title  = removestopwords.removestopwordfromsen(title ) ;
					mentions = NGramAnalyzer.entities(1,3, title ) ;
					// removed to run the gui
					//concepts = ontologyMapping.getAnnotation(mentions)  ;


					String[] arr = new String[concepts.size()] ;
					int i = 0 ; 
					for( String concept : concepts.keySet())
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
				if ("infon".equals(childList.item(j).getNodeName()) && "title".equals(childList.item(j).getTextContent()))
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
	  
	  
	  // prefer
	  
	  public static String getmeasureRPF(Map<String, List<String>> titles) throws IOException
		{
			

//			double avgRecall = 0.0  ; 
//			double avgPrecision = 0.0 ;
//			double avgFmeasure = 0.0 ; 
//			int size_ = titles.size() ;
			int counter = 0 ; 
			measure result = null ;
			measure synResult = null ;
			measure ansResult = null ;
			measure allcResult = new measure() ;  
			measure allsynResult = new measure() ;  
			measure allansResult = new measure() ;  
			measure totResult = new measure() ;
			for(String title : titles.keySet())
			{
				counter++  ; 
				// get concepts of the title 
				List<String> GoldSndconcepts = titles.get(title); 
				
				try {
					
					
					Map<String, Integer> concepts = new HashMap<String, Integer>();
					Map<String, Integer> synConcepts = new HashMap<String, Integer>();
					Map<String, Integer> ansConcepts = new HashMap<String, Integer>();
					Map<String, Integer> keywords = new HashMap<String, Integer>();
					title  = removestopwords.removestopwordfromsen(title ) ;
					keywords = NGramAnalyzer.entities(1,6, title ) ;
					//concepts = ontologyMapping.getAnnotation(mentions)  ;
					for(String keyword: keywords.keySet())
					{
						if (ontologyMapping.getKeywordAnnotation(keyword) )
						{
							if (ontologyMapping.getSemanticGroupTypeDISO_CHEM(keyword) )
							{
								concepts.put(keyword, 1) ;
							}
						}
/*						else if (ontologyMapping.getKeywordSynAnnotation(keyword))
						{
							synConcepts.put(keyword, 1) ;
						}
						else if (ontologyMapping.getKeywordAnsAnnotation(keyword))
						{
							ansConcepts.put(keyword, 1) ;
						}*/
						
					}
					result = getPRF(title,concepts,GoldSndconcepts) ; 
					// with no syn and ans
					allcResult.avgRecall =  result.avgRecall +  allcResult.avgRecall ;
					allcResult.avgPrecision =  result.avgPrecision  + allcResult.avgPrecision ;
					allcResult.avgFmeasure =  result.avgFmeasure  + allcResult.avgFmeasure ;
					
/*					synResult = getPRF(title,synConcepts,GoldSndconcepts) ; 
					
					allsynResult.avgRecall =  synResult .avgRecall +  allsynResult.avgRecall ;
					allsynResult.avgPrecision =  synResult .avgPrecision  + allsynResult.avgPrecision ;
					allsynResult.avgFmeasure =  synResult .avgFmeasure  + allsynResult.avgFmeasure ;
					
					ansResult = getPRF(title,ansConcepts,GoldSndconcepts) ; 
					
					allansResult.avgRecall =  ansResult.avgRecall +  allansResult.avgRecall ;
					allansResult.avgPrecision =  ansResult.avgPrecision  + allansResult.avgPrecision ;
					allansResult.avgFmeasure =  ansResult.avgFmeasure  + allansResult.avgFmeasure ;
					
					totResult.avgRecall =  result.avgRecall +  synResult.avgRecall + ansResult.avgRecall + totResult.avgRecall ;
					totResult.avgPrecision =  result.avgPrecision +  synResult.avgPrecision + ansResult.avgPrecision + totResult.avgPrecision  ;
					totResult.avgFmeasure =  result.avgFmeasure+  synResult.avgFmeasure+ ansResult.avgFmeasure  + totResult.avgFmeasure;*/
					

					
					
					
	                
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			allcResult.avgRecall =  allcResult.avgRecall  / titles.size() ;
			allcResult.avgPrecision =  allcResult.avgPrecision / titles.size() ;
			allcResult.avgFmeasure =  allcResult.avgFmeasure / titles.size() ;
			
/*			allsynResult.avgRecall =  allsynResult.avgRecall  / titles.size() ;
			allsynResult.avgPrecision =  allsynResult.avgPrecision / titles.size() ;
			allsynResult.avgFmeasure =  allsynResult.avgFmeasure / titles.size() ;
			
			allansResult.avgRecall =  allansResult.avgRecall  / titles.size() ;
			allansResult.avgPrecision =  allansResult.avgPrecision / titles.size() ;
			allansResult.avgFmeasure =  allansResult.avgFmeasure / titles.size() ;
			
			
			totResult.avgRecall = totResult.avgRecall  / titles.size() ;
			totResult.avgPrecision = totResult.avgPrecision  / titles.size() ;
			totResult.avgFmeasure = totResult.avgFmeasure / titles.size() ;*/
			
			String output = Double.toString(allcResult.avgRecall) + " " +  Double.toString(allcResult.avgPrecision) +" " +  Double.toString(allcResult.avgFmeasure) ;
			
			return output;
		}
	  public static measure getPRF(String  titles, Map<String, Integer> concepts,List<String> GoldSndconcepts)
	  {
			
		measure result = new measure() ; 
		result.Recall = 0.0  ; 
		result.Precision = 0.0 ;
		result.Fmeasure = 0.0 ; 
		try {

			String[] arr = new String[concepts.size()] ;
			int i = 0 ; 
			for( String concept : concepts.keySet())
			{
				arr[i] = concept.toLowerCase() ;
				i++ ; 
			}

			// measure the recall precision and  F-measure 
			double relevent = 0 ;
			for( String concept : arr)
			{
				
			   for( String GoldSndconcept : GoldSndconcepts)
			   {
	               if (GoldSndconcept.equalsIgnoreCase(concept.toLowerCase()))
	               {
	            	   relevent++ ; 
	            	   goldConceptsfound.put(GoldSndconcept, titles) ;
	               }
			   }

			}
			
			// calculate the Recall 
			//For example for text search on a set of documents recall is the number of correct results divided by the number of results that should have been returned
			double recall = 0.0 ; 
			if (GoldSndconcepts.size() > 0  )
			{
				recall = relevent / GoldSndconcepts.size() ;
			}

			result.Recall = recall ; 
			result.avgRecall = recall  + result.avgRecall ; 
			
			double precision = 0 ; 
			if ( arr.length > 0  )
			   precision = relevent / arr.length ;
			
			result.Precision = precision ;	
			result.avgPrecision = precision + result.avgPrecision ;  
			
			double Fmeasure  = 0.0 ;
			if (precision + recall > 0 )
			   Fmeasure = 2* ((precision * recall) / (precision + recall)) ;
			result.Fmeasure = Fmeasure  ;
			result.avgFmeasure = Fmeasure + result.avgFmeasure  ;
            
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result ;
	}

}
