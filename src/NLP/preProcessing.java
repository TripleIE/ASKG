package NLP;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class preProcessing {

	
    // using Stanford APIs 
	public static StanfordCoreNLP pipeline ;
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		//pipeline = getStanfordCoreNLP() ;
		//getTokens("hellow I'm my; you, me do") ;
		getParseTreeSentence("hellow I'm my; you, me do") ;

	}

	public static StanfordCoreNLP  getStanfordCoreNLP()
	{
		
		if (pipeline == null)
		{
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("pos.model", "E:\\Ptriple\\Vendor\\parser\\english-left3words-distsim.tagger");
	   // props.put("ner.model", "E:\\Ptriple\\Vendor\\parser\\english.all.3class.caseless.distsim.crf.ser.gz");
	   // props.put("parse.model", "E:\\Ptriple\\Vendor\\parser\\englishPCFG.caseless.ser.gz");
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
	   
	    	pipeline = new StanfordCoreNLP(props);
		}
	    
	    return pipeline ;

    
	}
	
	public static List<String> getTokens(String text) throws FileNotFoundException{
		
		
		  List<String> wordList = new ArrayList<String>();
		  PTBTokenizer ptbt = new PTBTokenizer( new StringReader(text), new CoreLabelTokenFactory(), "");
		  for (CoreLabel label; ptbt.hasNext(); )
		  {
		    label = (CoreLabel) ptbt.next();
		    System.out.println(label);
		    wordList.add(label.toString());
		  }
		  
		  return wordList ;
	}
	
	public static List<String> getSentences(String text)
	{
		String[] SENTENCE_DELIMS = {".", "?", "!", "!!", "!!!", "??", "?!", "!?"};
	    Reader reader = new StringReader(text);
	    DocumentPreprocessor docprocess = new DocumentPreprocessor(reader);
	    docprocess.setSentenceFinalPuncWords(SENTENCE_DELIMS);
	    List<String> sentenceList = new ArrayList<String>();

	    for (List<HasWord> sentence : docprocess) {
	       String sentenceString = Sentence.listToString(sentence);
	       sentenceList.add(sentenceString.toString());
	    }
        
	    return sentenceList ;
	}

	
	public static Tree  getParseTreeSentence(String sentance)
	{
		
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("pos.model", "E:\\Ptriple\\Vendor\\parser\\english-left3words-distsim.tagger");
	    props.put("ner.model", "E:\\Ptriple\\Vendor\\parser\\english.all.3class.caseless.distsim.crf.ser.gz");
	    props.put("parse.model", "E:\\Ptriple\\Vendor\\parser\\englishPCFG.caseless.ser.gz");
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
	   
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(sentance);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    Tree tree = null ; 
	    for(CoreMap sentence: sentences)
	    {
	    	
	      tree = sentence.get(TreeAnnotation.class);
	      break ;

	    }
		return tree;
    
	}
	
	public static String  getlemma(String sentance)
	{
		
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("pos.model", "X:\\KG_ANR\\Vendor\\parser\\english-left3words-distsim.tagger");
	    props.put("ner.model", "X:\\KG_ANR\\Vendor\\parser\\english.all.3class.caseless.distsim.crf.ser.gz");
	    props.put("parse.model", "X:\\KG_ANR\\Vendor\\parser\\englishPCFG.caseless.ser.gz");
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
	   
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(sentance);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    String Sent = "" ;
	    for(CoreMap sentence: sentences)
	    {
	    	

		      for (CoreLabel token: sentence.get(TokensAnnotation.class)) 
		      {
		    	 
		        // this is the text of the token
		        String word = token.get(LemmaAnnotation.class);
		        Sent += word + " ";
		      }

	      break ;

	    }
	    
		return Sent.trim();
    
	}
	
}
