package util;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.jayway.jsonpath.JsonPath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileInputStream;
import java.util.Properties;

public class KGgoogle {

	public static Properties properties = new Properties();
	  public static void main(String[] args) {
	    try {
	     // properties.load(new FileInputStream("kgsearch.properties"));

	      HttpTransport httpTransport = new NetHttpTransport();
	      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	      JSONParser parser = new JSONParser();
	      GenericUrl url = new GenericUrl("https://kgsearch.googleapis.com/v1/entities:search");
	      String str = "" ; 
	     url.put("query", "Taylor Swift");
	      url.put("limit", "10");
	     url.put("indent", "true");
	     url.put("key", "AIzaSyA3XNI2fCrlJ8moB_xK5kgThp55WzxN6qk");
	      HttpRequest request = requestFactory.buildGetRequest(url);
	      HttpResponse httpResponse = request.execute();
	      JSONObject response = (JSONObject) parser.parse(httpResponse.parseAsString());
	      JSONArray elements = (JSONArray) response.get("itemListElement");
	      for (Object element : elements) {
	        System.out.println(JsonPath.read(element, "$.result.name").toString());
	      }
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	  }

	
	

}
