package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class dateValidater {
	
	

	// List of all date formats that we want to parse.
	// Add your own format here.
	private static List<SimpleDateFormat> 
			dateFormats = new ArrayList<SimpleDateFormat>() {{
			add(new SimpleDateFormat("M/dd/yyyy"));
			add(new SimpleDateFormat("dd.M.yyyy"));
			add(new SimpleDateFormat("M/dd/yyyy hh:mm:ss a"));
			add(new SimpleDateFormat("dd.M.yyyy hh:mm:ss a"));
			add(new SimpleDateFormat("dd.MMM.yyyy"));
			add(new SimpleDateFormat("dd-MMM-yyyy"));
		}
	};
	
	
	/**
	 * validate String with various formats into java.util.Date
	 * 
	 * @param input
	 *            Date as a string
	 * @return java.util.Date object if input string is parsed 
	 * 			successfully else returns false
	 */
	public static Boolean  isDate(String input) {
		Date date = null;
		if(null == input) {
			return false;
		}
		for (SimpleDateFormat format : dateFormats) {
			try {
				format.setLenient(false);
				date = format.parse(input);
			} catch (ParseException e) {

			}
			if (date != null) {
				return true ; 
			}
		}

		return false;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

	}
	
	
	


   

}
