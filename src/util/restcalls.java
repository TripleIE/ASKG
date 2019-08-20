package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class restcalls {

	
    public static String httpGet(String urlStr) throws IOException {
  	  URL url = new URL(urlStr);
  	  HttpURLConnection conn =
  	      (HttpURLConnection) url.openConnection();
      System.out.println(conn.getResponseCode());
  	  if (conn.getResponseCode() != 200) {
  	    throw new IOException(conn.getResponseMessage());
  	  }

  	  
  	  // Buffer the result into a string
  	  BufferedReader rd = new BufferedReader(
  	      new InputStreamReader(conn.getInputStream()));
  	  StringBuilder sb = new StringBuilder();
  	  String line;
  	  while ((line = rd.readLine()) != null) {
  	    sb.append(line);
  	  }
  	  rd.close();

  	  conn.disconnect();
  	  return sb.toString();
  	}
    
    public static String get(String urlToGet, String API_KEY) 
    {
    	
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("ontologies", "MESH");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static String httpGetBio(String urlStr) throws IOException {
    	String apiKEY = "apikey=396993d0-4ce2-4123-93de-214e9b9ebcf2" ; 
  	  URL url = new URL(urlStr + apiKEY);
  	  HttpURLConnection conn =
  	      (HttpURLConnection) url.openConnection();
  	      conn.setRequestProperty("Accept", "application/json");
  	  if (conn.getResponseCode() != 200) {
  	    return null ; 
  	  }

  	  // Buffer the result into a string
  	  BufferedReader rd = new BufferedReader(
  	      new InputStreamReader(conn.getInputStream()));
  	  StringBuilder sb = new StringBuilder();
  	  String line;
  	  while ((line = rd.readLine()) != null) {
  	    sb.append(line);
  	  }
  	  rd.close();

  	  conn.disconnect();
  	  
  	  System.out.println(sb.toString());
  	  return sb.toString();
  	}
    
    
}
