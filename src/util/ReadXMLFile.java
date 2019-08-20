package util;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import ONTO.BioPontologyfactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReadXMLFile {
    public static void main(String[] args) {
    	//ReadPMC("F:\\TempDB\\PMCxxxx\\PMC0029XXXXX\\PMC2900151.xml") ;
    	//ReadChEBi("F:\\TempDB\\chebi.owl") ;
    	Readrel("X:\\KG_ANR\\Clinical NotesGold\\relations1.xml","X:\\KG_ANR\\Clinical NotesGold\\relations_score.xml"); 
    	//ReadAneCN("C:\\Users\\mazina\\Desktop\\School\\Khalid\\Paper\\Distance Supervision NER\\Data Medline_PubMed\\ClinicalNote\\CNGoldStandardJ.xml", "Has history of migraine headaches , 2 mm aneurysm of mid-cavernaous right internal carotid artery and asthma .") ; ; 
    }

    
    public static List<String>  ReadAneCN(String filename, String element) {

        try {

    	File fXmlFile = new File(filename);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	doc.getDocumentElement().normalize();
    	List<String> cps = new ArrayList<String>();
    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
    	NodeList sents =  doc.getElementsByTagName("sentence") ; 
    	for (int i = 0; i < sents .getLength()  ; i++) 
    	{
			 Node nBNode = sents.item(i) ; // binding
			 Element eBElement = (Element) nBNode;
			 System.out.println("Root element :" + eBElement.getNodeName());
			 String text =  eBElement.getElementsByTagName("Text").item(0).getTextContent() ; 
			 if(element.equalsIgnoreCase(text))
			 {
				 NodeList concepts =  eBElement.getElementsByTagName("Concept") ; 
				 for (int j = 0; j < concepts .getLength()  ; j++) 
				 {
					 if ( !concepts.item(j).getTextContent().contains("others"))
					     cps.add(concepts.item(j).getTextContent()) ; 
				 }
				 
				 return cps ; 
				 
			 }
    	}
    	 return cps ;
    	 
        } catch (Exception e) {
    	e.printStackTrace();
    	 return null ; 
        }
		
        
      }
    
    public static List<String>  ReadAneCNSent(String filename) {

        try {

    	File fXmlFile = new File(filename);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	doc.getDocumentElement().normalize();
    	List<String> sent = new ArrayList<String>();
    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
    	NodeList sents =  doc.getElementsByTagName("sentence") ; 
    	for (int i = 0; i < sents .getLength()  ; i++) 
    	{
    		try {
				Node nBNode = sents.item(i); // binding
				Element eBElement = (Element) nBNode;
				System.out.println("Root element :" + eBElement.getNodeName());
				String text = eBElement.getElementsByTagName("Text").item(0).getTextContent();
				sent.add(text);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	 return sent;
    	 
        } catch (Exception e) {
    	e.printStackTrace();
    	 return null ; 
        }
		
        
      }
    
    
    public static List<String>  Readrel(String filename,String output) {

        try {

    	File fXmlFile = new File(filename);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	doc.getDocumentElement().normalize();
    	List<String> sent = new ArrayList<String>();
    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
    	NodeList sents =  doc.getElementsByTagName("sentence") ; 
    	for (int i = 0; i < sents .getLength()  ; i++) 
    	{
			 Node nBNode = sents.item(i) ; // binding
			 Element eBElement = (Element) nBNode;
			 System.out.println("Root element :" + eBElement.getNodeName());
			 String text =  eBElement.getElementsByTagName("Text").item(0).getTextContent() ; 
			 sent.add(text) ;
			 
			 
			readfiles.Writestringtofile("<sentence>", output);
			readfiles.Writestringtofile("  <Text>" + text + "</Text>", output);
			
			 NodeList rels =  eBElement.getElementsByTagName("Rel") ; 
			 for (int j = 0; j < rels .getLength()  ; j++) 
			 {
				 String rel = rels.item(j).getTextContent() ; 
				 String[] cns = rel.split(",");
				 System.out.println(cns[0]);
				 System.out.println(cns[2]);
				 readfiles.Writestringtofile("  <Rel>" + rels.item(j).getTextContent() + "</Rext>", output);
			 }
			 readfiles.Writestringtofile("</sentence>", output);
    	}
    	 return sent;
    	 
        } catch (Exception e) {
    	e.printStackTrace();
    	 return null ; 
        }
		
        
      }
    
    public static List<String>  ReadPubmed(String xmlRecords, String element) {

        try {

        	//xmlRecords =  xmlRecords.replaceAll("<!DOCTYPE eSearchResult PUBLIC \"-//NLM//DTD esearch 20060628//EN\" \"https://eutils.ncbi.nlm.nih.gov/eutils/dtd/20060628/esearch.dtd\">", "") ;
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(new InputSource(new StringReader(xmlRecords)));

    	//optional, but recommended
    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    	doc.getDocumentElement().normalize();

    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList IDs=  doc.getElementsByTagName(element) ; 
        if(IDs.getLength() == 0  )
        	return null; 
        
        List<String> ids = new ArrayList<String>();
        
        for( int i = 0 ; i < IDs.getLength();i++)
        {
        	ids.add(IDs.item(i).getTextContent()) ;
        }
        return ids;
        } catch (Exception e) {
    	e.printStackTrace();
    	 return null ; 
        }
		
        
      }
    
    public static String ReadPMC(String filename) {

        try {

    	File fXmlFile = new File(filename);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	//optional, but recommended
    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    	doc.getDocumentElement().normalize();

    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        String text =  doc.getElementsByTagName("article-title").item(0).getTextContent() ; 
    	text = text.replaceAll("\n", " ") ;
    	System.out.println("----------------------------");
    	 return text ;
    	 
        } catch (Exception e) {
    	e.printStackTrace();
    	 return null ; 
        }
        
      }
    
    public static String ReadChEBi(String filename) {

        try {

    	File fXmlFile = new File(filename);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	//optional, but recommended
    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    	doc.getDocumentElement().normalize();

    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        String text =  doc.getElementsByTagName("article-title").item(0).getTextContent() ; 
    	text = text.replaceAll("\n", " ") ;
    	System.out.println("----------------------------");
    	 return text ;
    	 
        } catch (Exception e) {
    	e.printStackTrace();
    	 return null ; 
        }
        
      }
    
    
    public static void ReadLLD(String filename) {

        try {

    	File fXmlFile = new File(filename);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(fXmlFile);

    	doc.getDocumentElement().normalize();
    	Map<String, Map<String,String>> list = new HashMap<String, Map<String,String>>();
    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList resultnodes =  doc.getElementsByTagName("result") ; 
        for (int i = 0; i < resultnodes.getLength()  ; i++)  
        {
        	System.out.println( resultnodes.item(i).getNodeName()); 
        	Node nNode = resultnodes.item(i) ;
        	if (nNode.getNodeType() == Node.ELEMENT_NODE)
        	{
        		 Element eElement = (Element) nNode;
        		 NodeList Bnodes =  eElement.getElementsByTagName("binding") ;
     			 String Concept1 = null ; 
    			 String Concept2 = null ; 
    			 String labelconcept1  = null;
    			 String labelconcept2 = null; 
        		 for (int ii = 0; ii < Bnodes.getLength()  ; ii++)
        		 {
   
        			 
        			 Node nBNode = Bnodes.item(ii) ; // binding
        			 Element eBElement = (Element) nBNode;
        			 String Label = eBElement.getAttribute("name"); 
        			 
        			 if(Label.equals("concept1"))
        			 {
        				 Concept1 =  eBElement.getTextContent().trim(); 
        			 }
        			 
        			 if(Label.equals("concept2"))
        			 {
        				 Concept2 =  eBElement.getTextContent().trim(); 
        			 }
        			 
           			 if(Label.equals("labelconcept1"))
        			 {
           				labelconcept1 =  eBElement.getTextContent().trim(); 
        			 }
           			 
           			 if(Label.equals("labelconcept2"))
        			 {
           				labelconcept2 =  eBElement.getTextContent().trim(); 
        			 }	 
        		 }
    			 
        		 Map<String,String> nlist = list.get(labelconcept1) ;
    			 if (nlist == null )
    			 {
    				  nlist = new HashMap<String,String>();  
    				  nlist.put(labelconcept2,Concept2) ; 
    				  nlist.put(labelconcept1,Concept1) ;
    				  list.put(labelconcept1, nlist) ;
    			 }
    			 else
    			 {
    				 nlist.put(labelconcept2,Concept2) ;
    				 list.put(labelconcept1, nlist) ;
    			 }
    			 
    			 Map<String,String> nnlist = list.get(labelconcept2) ;
    			 if (nnlist == null )
    			 {
    				  nnlist = new HashMap<String,String>();  
    				  nnlist.put(labelconcept1,Concept1) ;
    				  nlist.put(labelconcept2,Concept2) ;
    				  list.put(labelconcept2, nlist) ;
    			 }
    			 else
    			 {
    				 nnlist.put(labelconcept1,Concept1) ; 
    				 list.put(labelconcept2, nlist) ;
    			 }
        		 
        	}
        	
            
        }
        	SerializedLLD(list,"F:\\eclipse64\\data\\skosmappingRelation.dat") ;
        } catch (Exception e) {
    	e.printStackTrace();
 
        }

        
        
      }  
  public static String Read(String filename) {

    try {

	File fXmlFile = new File(filename);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);

	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
    String text =  doc.getElementsByTagName("TEXT").item(0).getTextContent() ; 
	System.out.println( doc.getElementsByTagName("TEXT").item(0).getTextContent());
	text = text.replaceAll("\n", " ") ;
	System.out.println("----------------------------");
	 return text ;
	 
    } catch (Exception e) {
	e.printStackTrace();
	 return null ; 
    }
    
  }

 
  
	  
	  public static Map<String, List<String>> ReadCDR_TestSet_BioCDisease() {

		    Map<String, List<String>> goldstandard = null ;
		    goldstandard =  Deserialize("F:\\eclipse64\\eclipse\\Diseaselist") ;
		    if (goldstandard== null )
		    try {

	        String filename = "F:\\eclipse64\\eclipse\\CDR_TestSet.BioC.xml" ;
	       // String filename = "F:\\eclipse64\\eclipse\\CDR_TestSet.BioCtest.xml" ;
		    goldstandard  = new HashMap<String, List<String>>();
			File fXmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
	        
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList passageList = doc.getElementsByTagName("passage");
			int count  = 0 ; 
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
							}
							
							if ("annotation".equals(childList1.item(kk).getNodeName()))
							{
								NodeList childList2 = childList.item(kk).getChildNodes();
								Boolean found = false ; 
								for (int kkk = 0; kkk < childList2.getLength(); kkk++)
								{
									if ("infon".equals(childList2.item(kkk).getNodeName()))
									{
										// conclist.add(childList2.item(kkk).getTextContent().trim().toLowerCase()) ;
									//	 System.out.println(childList2.item(kkk).getTextContent()
								     //               .trim());
										 if ("Disease".equals(childList2.item(kkk).getTextContent().trim()))
										 {
											 found = true ; 

												System.out.println(childList2.item(kkk).getTextContent()
											                  .trim());
										 }
										 
									}
									if ("text".equals(childList2.item(kkk).getNodeName()) && found)
									{
										 conclist.add(childList2.item(kkk).getTextContent().trim().toLowerCase()) ;
										 System.out.println(childList2.item(kkk).getTextContent().toLowerCase()
								                    .trim());
										 count++ ;
										 found = false ;
									}
								}

							}
						}
						
						goldstandard.put(title,conclist) ;
						
					}
				}
	           
			}
				Serialized(goldstandard,"F:\\eclipse64\\eclipse\\Diseaselist") ;
		    } 
		    catch (Exception e) {
			e.printStackTrace();
		    }
		    return goldstandard;
		  } ;
  
		  
		  public static Map<String, List<String>> ReadCDR_TestSet_BioCChebi() {

			    Map<String, List<String>> goldstandard = null ;
			    goldstandard =  Deserialize("F:\\eclipse64\\eclipse\\Chebilist") ;
			    if (goldstandard== null )
			    try {

		        String filename = "F:\\eclipse64\\eclipse\\CDR_TestSet.BioC.xml" ;
		       // String filename = "F:\\eclipse64\\eclipse\\CDR_TestSet.BioCtest.xml" ;
			    goldstandard  = new HashMap<String, List<String>>();
				File fXmlFile = new File(filename);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
		        
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();

				NodeList passageList = doc.getElementsByTagName("passage");
				int count  = 0 ; 
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
								}
								
								if ("annotation".equals(childList1.item(kk).getNodeName()))
								{
									NodeList childList2 = childList.item(kk).getChildNodes();
									Boolean found = false ; 
									for (int kkk = 0; kkk < childList2.getLength(); kkk++)
									{
										if ("infon".equals(childList2.item(kkk).getNodeName()))
										{
											// conclist.add(childList2.item(kkk).getTextContent().trim().toLowerCase()) ;
										//	 System.out.println(childList2.item(kkk).getTextContent()
									     //               .trim());
											 if ("Chemical".equals(childList2.item(kkk).getTextContent().trim()))
											 {
												 found = true ; 

													System.out.println(childList2.item(kkk).getTextContent()
												                  .trim());
											 }
											 
										}
										if ("text".equals(childList2.item(kkk).getNodeName()) && found)
										{
											 conclist.add(childList2.item(kkk).getTextContent().trim().toLowerCase()) ;
											 System.out.println(childList2.item(kkk).getTextContent().toLowerCase()
									                    .trim());
											 count++ ;
											 found = false ;
										}
									}

								}
							}
							
							goldstandard.put(title,conclist) ;
							
						}
					}
		           
				}
					Serialized(goldstandard,"F:\\eclipse64\\eclipse\\Chebilist") ;
			    } 
			    catch (Exception e) {
				e.printStackTrace();
			    }
			    return goldstandard;
			  } ;
		
	  
			public static  void Serializeddir(List<String> dictionary,String fileout) throws IOException
			 {

			     try
			     {
			    	 // Create output stream.
			         FileOutputStream fileOut =
			         new FileOutputStream(fileout);
				     // Create XML encoder.
				     XMLEncoder xenc = new XMLEncoder(fileOut);
			
				     // Write object.
				     xenc.writeObject(dictionary);
				     xenc.close();
			         fileOut.close();
			         System.out.printf("Serialized data is saved in" + fileout);
			     }catch(IOException i)
			     {
			          i.printStackTrace();
			     }
			 }  
		public static  void Serializeddir(Map<String, Integer> dictionary,String fileout) throws IOException
		 {

		     try
		     {
		    	 // Create output stream.
		         FileOutputStream fileOut =
		         new FileOutputStream(fileout);
			     // Create XML encoder.
			     XMLEncoder xenc = new XMLEncoder(fileOut);
		
			     // Write object.
			     xenc.writeObject(dictionary);
			     xenc.close();
		         fileOut.close();
		         System.out.printf("Serialized data is saved in" + fileout);
		     }catch(IOException i)
		     {
		          i.printStackTrace();
		     }
		 }  
		public static  void Serializeddiectionary(Map<String, String> dictionary,String fileout) throws IOException
		 {

		     try
		     {
		    	 // Create output stream.
		         FileOutputStream fileOut =
		         new FileOutputStream(fileout);
			     // Create XML encoder.
			     XMLEncoder xenc = new XMLEncoder(fileOut);
		
			     // Write object.
			     xenc.writeObject(dictionary);
			     xenc.close();
		         fileOut.close();
		         System.out.printf("Serialized data is saved in" + fileout);
		     }catch(IOException i)
		     {
		          i.printStackTrace();
		     }
		 } 
		
		 public static  Map<String, String> Deserializeddiectionar(String fileout)
		   {
			       Map<String, String> dictionary ;
				   FileInputStream fileIn;
				try {
					fileIn = new FileInputStream(fileout);
				
				   XMLDecoder decoder =  new XMLDecoder(fileIn);
				   dictionary  = (Map<String, String>)decoder.readObject();
				   decoder.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			      return dictionary;
		   }
		 
		 public static  Map<String, String> Deserializeddiectionar(InputStream fileout)
		   {
			       Map<String, String> dictionary ;
				try {
				
				   XMLDecoder decoder =  new XMLDecoder(fileout);
				   dictionary  = (Map<String, String>)decoder.readObject();
				   decoder.close();
				} catch (Exception e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			      return dictionary;
		   }
		
		 public static  List<String> Deserializedirlis(String fileout)
		   {
			 	   List<String> dictionary ;
				   FileInputStream fileIn;
				try {
					fileIn = new FileInputStream(fileout);
				
				   XMLDecoder decoder =  new XMLDecoder(fileIn);
				   dictionary  = (List<String>)decoder.readObject();
				   decoder.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			      return dictionary;
		   }
		 public static  Map<String, Integer> Deserializedir(String fileout)
		   {
			       Map<String, Integer> dictionary ;
				   FileInputStream fileIn;
				try {
					fileIn = new FileInputStream(fileout);
				
				   XMLDecoder decoder =  new XMLDecoder(fileIn);
				   dictionary  = (Map<String, Integer>)decoder.readObject();
				   decoder.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			      return dictionary;
		   }
		 
			public static  void Serializedlabel(Map<String, List<Integer>> dictionary,String fileout) throws IOException
			 {

			     try
			     {
			    	 // Create output stream.
			         FileOutputStream fileOut =
			         new FileOutputStream(fileout);
				     // Create XML encoder.
				     XMLEncoder xenc = new XMLEncoder(fileOut);
			
				     // Write object.
				     xenc.writeObject(dictionary);
				     xenc.close();
			         fileOut.close();
			         System.out.printf("Serializedlabel data is saved in" + fileout);
			     }catch(IOException i)
			     {
			          i.printStackTrace();
			     }
			 }	
			
			 public static  Map<String, List<Integer>> Deserializedirlabel(String fileout)
			   {
				 Map<String, List<Integer>> dictionary ;
					   FileInputStream fileIn;
					try {
						fileIn = new FileInputStream(fileout);
					
					   XMLDecoder decoder =  new XMLDecoder(fileIn);
					   dictionary  = (Map<String, List<Integer>>)decoder.readObject();
					   decoder.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				      return dictionary;
			   }
	public static  void Serialized(Map<String, List<String>> dictionary,String fileout) throws IOException
	 {

	     try
	     {
	    	 // Create output stream.
	         FileOutputStream fileOut =
	         new FileOutputStream(fileout);
		     // Create XML encoder.
		     XMLEncoder xenc = new XMLEncoder(fileOut);
	
		     // Write object.
		     xenc.writeObject(dictionary);
		     xenc.close();
	         fileOut.close();
	         System.out.printf("Serialized data is saved in" + fileout);
	     }catch(IOException i)
	     {
	          i.printStackTrace();
	     }
	 }
	
	
	
	public static  void SerializedLLD(Map<String, Map<String, String>> list,String fileout) throws IOException
	 {

	     try
	     {
	    	 // Create output stream.
	         FileOutputStream fileOut =
	         new FileOutputStream(fileout);
		     // Create XML encoder.
		     XMLEncoder xenc = new XMLEncoder(fileOut);
	
		     // Write object.
		     xenc.writeObject(list);
		     xenc.close();
	         fileOut.close();
	         System.out.printf("Serialized data is saved in" + fileout);
	     }catch(IOException i)
	     {
	          i.printStackTrace();
	     }
	 }
	
	 public static  Map<String, Map<String, String>> DeserializeLLD(String fileout)
	   {
		     HashMap<String, Map<String, String>> dictionary ;
			   FileInputStream fileIn;
			try {
				fileIn = new FileInputStream(fileout);
			
			   XMLDecoder decoder =  new XMLDecoder(fileIn);
			   dictionary  = (HashMap<String, Map<String, String>>) decoder.readObject();
			   decoder.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		      return dictionary;
	   }
	
	public static  void Serialized(HashMap<String, Map<String, List<String>>> dictionary,String fileout) throws IOException
	 {

	     try
	     {
	    	 // Create output stream.
	         FileOutputStream fileOut =
	         new FileOutputStream(fileout);
		     // Create XML encoder.
		     XMLEncoder xenc = new XMLEncoder(fileOut);
	
		     // Write object.
		     xenc.writeObject(dictionary);
		     xenc.close();
	         fileOut.close();
	         System.out.printf("Serialized data is saved in" + fileout);
	     }catch(IOException i)
	     {
	          i.printStackTrace();
	     }
	 }
	
	public static  void SerializedT(Map<String, Map<String, List<String>>> dictionary,String fileout) throws IOException
	 {

	     try
	     {
	    	 // Create output stream.
	         FileOutputStream fileOut =
	         new FileOutputStream(fileout);
		     // Create XML encoder.
		     XMLEncoder xenc = new XMLEncoder(fileOut);
	
		     // Write object.
		     xenc.writeObject(dictionary);
		     xenc.close();
	         fileOut.close();
	         System.out.printf("Serialized data is saved in" + fileout);
	     }catch(IOException i)
	     {
	          i.printStackTrace();
	     }
	 }
	 public static  Map<String, Map<String, List<String>>> DeserializeT(String fileout)
	   {
		       Map<String, Map<String, List<String>>> dictionary ;
			   FileInputStream fileIn;
			try {
				fileIn = new FileInputStream(fileout);
			
			   XMLDecoder decoder =  new XMLDecoder(fileIn);
			   dictionary  = (Map<String, Map<String, List<String>>>) decoder.readObject();
			   decoder.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		      return dictionary;
	   }
	 public static  Map<String, List<String>> Deserialize(String fileout)
	   {
			   Map<String, List<String>> dictionary ;
			   FileInputStream fileIn;
			try {
				fileIn = new FileInputStream(fileout);
			
			   XMLDecoder decoder =  new XMLDecoder(fileIn);
			   dictionary  = (Map<String, List<String>>)decoder.readObject();
			   decoder.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		      return dictionary;
	   }
	 

	}