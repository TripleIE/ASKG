package word2vec;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by agibsonccc on 10/9/14.
 *
 * Neural net that processes text into wordvectors. See below url for an in-depth explanation.
 * https://deeplearning4j.org/word2vec.html
 */
public class Word2VecRawTextExample {

    private static Logger log = LoggerFactory.getLogger(Word2VecRawTextExample.class);
    public static void main(String[] args) throws Exception
    {
        Readrel("X:\\KG_ANR\\Clinical NotesGold\\relations1.xml","X:\\KG_ANR\\Clinical NotesGold\\relations_score1.xml");
    }

    public static double score(String concept1, String concept2,Word2Vec vec )
    {




       /* // Gets Path to Text file
        String filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath();

        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        *//*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         *//*
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");

        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        log.info("Closest Words:");
        Collection<String> lst = vec.wordsNearestSum("day", 10);
        log.info("10 Words closest to 'day': {}", lst);*/
        double score = vec.similarity(concept1,concept2);

        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());

        return score ;
    }

    public static List<String> Readrel(String filename, String output) {

        try {

            File gModel = new File("X:\\KG_ANR\\Vendor\\GoogleNews-vectors-negative300.bin");

            Word2Vec vec1 = WordVectorSerializer.readWord2VecModel(gModel);

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


                Writestringtofile("<sentence>", output);
                Writestringtofile("  <Text>" + text + "</Text>", output);

                NodeList rels =  eBElement.getElementsByTagName("Rel") ;
                for (int j = 0; j < rels .getLength()  ; j++)
                {
                    String rel = rels.item(j).getTextContent() ;
                    String[] cns = rel.split(",");
                    System.out.println(cns[0]);
                    System.out.println(cns[2]);
                    double score = score(cns[0], cns[2],vec1 ) ;
                    Writestringtofile("  <Rel>" + rels.item(j).getTextContent() +  "," + score + "</Rext>", output);
                }
                Writestringtofile("</sentence>", output);
            }
            return sent;

        } catch (Exception e) {
            e.printStackTrace();
            return null ;
        }



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
}
