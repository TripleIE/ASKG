package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;


public class readfiles {
	
	public static void main(String[] args)
	{

	    //TDBstore(null) ;
        try {
            removestopwordsfromfilesfromclinicalnote();
        }
        catch(Exception e)
        {

        }
	}
	
	
	/**
     * Reads the all lines from a file and places it a String array. In each 
     * record in the String array we store a training example text.
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    public static String readLinestostring(URL url) throws IOException
    {

        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String outline = "" ;
        try 
        {

            String line;
            int counter = 0 ; 
            int filecounter = 0 ;  
            while ((line = bufferedReader.readLine()) != null) {
            	
            	outline += line  + " " ;
/*            	counter ++ ;
            	if (counter > 1000 )
            	{
            		counter = 0 ; 
            		filecounter++ ;
            		String fname =  "data" + Integer.toString(filecounter) ;
            		Writestringtofile(outline,url.getPath().replaceAll("Genia4EReval2", fname) ) ;
            		outline = " " ;
            	}*/
            }
        }
        finally
        {
        }
        bufferedReader.close();
        return outline ;

    }
	/**
     * Reads the all lines from a file and places it a String array. In each 
     * record in the String array we store a training example text.
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    public static String[] readLines(URL url) throws IOException
    {

        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
        List<String> lines;
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        try 
        {
            lines = new ArrayList<String>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        finally
        {
        }
        

        return lines.toArray(new String[lines.size()]);

    }
    
    public static List<String> readLinesbylines(URL url) throws IOException
    {

        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
        List<String> lines;
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        try 
        {
            lines = new ArrayList<String>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
            	if (!line.trim().isEmpty())
                  lines.add(line);
            }
        }
        finally
        {
        }
        

        return lines;

    }
    
    public static Map<String, Integer> readLinesbylinesToMap(URL url) throws IOException
    {

    	Map<String, Integer> concepts = new HashMap<String, Integer>();
        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
        List<String> lines;
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        try 
        {
            lines = new ArrayList<String>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
            	if (!line.trim().isEmpty())
            		concepts.put(line.trim().toLowerCase(),1);
            }
        }
        finally
        {
        }
        

        return concepts;

    }
    
    /**
     * Reads the all lines from a file and places it in file as line 
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    
    public static void readLinesToaline(URL url,String filename) throws IOException
    {

        Reader fileReader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));

        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String Oneline ;
        try 
        {
           // lines = new ArrayList<String>();
            String line;
            Oneline = "" ;
            while ((line = bufferedReader.readLine()) != null) {
            	Oneline = Oneline.concat(line) ; 
               // lines.add(line);
            }
        }
        finally
        {
        }
      /**************************************************************/ 
      /* Write it to File  */
      /**************************************************************/ 
        BufferedWriter out = null;
        try  
        {
            FileWriter fstream = new FileWriter("C:\\Users\\mazina\\Desktop\\School\\mike Wu\\aclImdb\\train\\pos\\"+ filename + "out.txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            Oneline = Oneline.replaceAll("[\r\n]+", " ");
            Oneline = Oneline.concat("\r\n") ;
            out.write(Oneline);
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            if(out != null) {
                out.close();
            }
        }


    } 
    

	
    public static  File[] readAllFileFromPath(String path ) throws IOException {

        

        
        //  trainingFiles.put("French", NaiveBayesExample.class.getResource("trainingset.txt"));
          //trainingFiles.put("English", NaiveBayesExample.class.getResource("training.language.en.txt"));
        //  trainingFiles.put("French", NaiveBayesExample.class.getResource("training.language.fr.txt"));
       // trainingFiles.put("German", NaiveBayesExample.class.getResource("training.language.de.txt"));
        
        //loading examples in memory
        Map<String, String[]> trainingExamples = new HashMap <String, String[]>();
        
        //File folderpos = new File("C:\\Users\\mazina\\Desktop\\School\\mike Wu\\aclImdb\\train\\pos\\");
        File folderpos = new File(path);
        File[] listOfFiles = folderpos.listFiles();
//        for (int i = 0; i < listOfFiles.length; i++) {
//        	  File file = listOfFiles[i];
//        	  if (file.isFile() && file.getName().endsWith(".txt")) {
//        		  //readLinesToaline(file.toURL(),"pos");
//        		  readLinestostring(file.toURL());
//        	  } 
//        	}
//        File folderneg = new File("C:\\Users\\mazina\\Desktop\\School\\mike Wu\\aclImdb\\train\\neg\\");
//        File[] listOfFilesnewnew = folderneg.listFiles();
//        for (int i = 0; i < listOfFilesnewnew.length; i++) {
//        	  File fileneg = listOfFilesnewnew[i];
//        	  if (fileneg.isFile() && fileneg.getName().endsWith(".txt")) {
//        		  //readLinesToaline(fileneg.toURL(),"neg");
//        		  readLinestostring(fileneg.toURL());
//        	  } 
//        	}
        return listOfFiles ;
    }

    
    public static File createfile(String path, String text) 
    {

    	
    	
    	Writer writer = null;

    	try 
    	{
    	    writer = new BufferedWriter(new OutputStreamWriter(
    	          new FileOutputStream( path), "utf-8"));
    	    writer.write(text);
    	} 
    	catch (IOException ex) 
    	{
    	  // report
    	} 
    	finally
    	{
    	   try {writer.close();} catch (Exception ex) {/*ignore*/}
    	}

		return null;
    	
    }
    public static void Writestringtofile(String str,String filename) throws IOException
    {


      /**************************************************************/ 
      /* Write it to File  */
      /**************************************************************/ 
        BufferedWriter out = null;
        try  
        {
            FileWriter fstream = new FileWriter( filename, true); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write(str);
            out.newLine();
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            if(out != null) {
                out.close();
            }
        }


    }
    
    public static void Writestringtofilenonewline(String str,String filename) throws IOException
    {


      /**************************************************************/ 
      /* Write it to File  */
      /**************************************************************/ 
        BufferedWriter out = null;
        try  
        {
            FileWriter fstream = new FileWriter( filename, true); //true tells to append data.
            out = new BufferedWriter(fstream);
            if (!str.equals("new"))
            	out.write(str);
            else 
            	out.newLine();
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            if(out != null) {
                out.close();
            }
        }


    }
    public static void TDBstore(String file) 
    {
    	// open TDB dataset
    	
    	String directory = "F:\\TempDB\\Store_DB\\DO";
    	com.hp.hpl.jena.query.Dataset dataset = TDBFactory.createDataset(directory);

    	Model tdb = dataset.getDefaultModel();

    	// read the input file
    	String source = "F:\\TempDB\\Store_DB\\DO\\doid.owl";
    	FileManager.get().readModel( tdb, source);
    	dataset.commit();//INCLUDE THIS STAMEMENT
    	tdb.close();
    	dataset.close();
    }
    public static void removestopwordsfromfiles()throws Exception
    {
        File CNs[] =  readfiles.readAllFileFromPath("X:\\KG_ANR\\book") ;
        for (int i = 0; i < CNs.length; i++)
        {

                String text  = removestopwords.removestopwordfromsen(readLinestostring(CNs[i].toURL())) ;
                readfiles.Writestringtofile(text,"X:\\KG_ANR\\Clinical NotesGold\\trainingbook.txt");


        }
    }

    public static void removestopwordsfromfilesfromclinicalnote()throws Exception
    {
        File CNs[] =  readfiles.readAllFileFromPath("X:\\KG_ANR\\ClinicalN\\Shared by Mazen") ;
        for (int i = 0; i < CNs.length; i++)
        {

            String text  = removestopwords.removestopwordfromsen(readLinestostring(CNs[i].toURL())) ;
            readfiles.Writestringtofile(text,"X:\\KG_ANR\\Clinical NotesGold\\trainingnote.txt");


        }
    }
}


