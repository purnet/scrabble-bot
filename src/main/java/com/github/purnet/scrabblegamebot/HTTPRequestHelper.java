package com.github.purnet.scrabblegamebot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HTTPRequestHelper {
	private HTTPRequestHelper(){
		throw new AssertionError();
	}
	
	static Response makeHTTPRequest(String targetURL, String body, String reqType) {
		  HttpURLConnection connection = null;  
		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection)url.openConnection();
		    connection.setRequestMethod(reqType);
		    connection.setConnectTimeout(30000);
		    connection.setReadTimeout(30000);
		    
		    if (reqType =="POST"){
		    	connection.setRequestProperty("Accept", "application/json");  
			    if (body != null) {
			    	connection.setRequestProperty("Content-Type", "application/json"); 
			    }
	
		    }
		    
		    connection.setUseCaches(false);
		    connection.setDoOutput(true);
	
		    if (reqType =="POST"){
		    	//Send request
			    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
			    wr.writeBytes(body);
			    wr.close();
		    }
	
		    //Get Response  
		    int respCode = connection.getResponseCode();
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder respBody = new StringBuilder(); // or StringBuffer if not Java 5+ 
		    String line;
		    while((line = rd.readLine()) != null) {
		    	respBody.append(line);
		    	respBody.append('\r');
		    }
		    rd.close();
		    Response response = new Response(respCode, null, respBody.toString());
		    return response;
		  } catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if(connection != null) {
		      connection.disconnect(); 
		    }
		  }
	}
}
