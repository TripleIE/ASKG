package util;

import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class Word2vec {

	public static Word2Vec vec = null ;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File gModel = new File("X:\\KG_ANR\\Vendor\\GoogleNews-vectors-negative300.bin");

	    Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);

	    int i = 0 ; 

	}

	public static boolean createModel ()
	{
		vec = WordVectorSerializer.readWord2VecModel("X:\\KG_ANR\\Clinical NotesGold\\modelPubMedbookClinicalNgramCBOW4_300.bin");

		return true ;
	}

	public static double score(String concept)  {

		Word2Vec vec  = WordVectorSerializer.readWord2VecModel("X:\\KG_ANR\\Clinical NotesGold\\modelPubMedbookClinicalNgramCBOW4_10.bin");
		double score1 = 0 ;
		double score2 = 0 ;
		try {
			 score1 = vec.similarity("cerebral aneurysm", concept);
		}
		catch (Exception e)
		{
			score1 = 0 ;
		}
		try {
			score2 = vec.similarity("intracranial aneurysm", concept);
		}
		catch (Exception e)
		{
			score2 = 0 ;
		}

		if (score1 > score2)
		{
			return score1;
		}
		else
		{
			return score2 ;
		}
	}

}
