package com.github.purnet.scrabblegamebot;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;


@SuppressWarnings("serial")
public class ScrabbleBotServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("Hello World. Get is not yet implemented");
		resp.getOutputStream().write("Hello World. Get is not yet implemented".getBytes());
	}

	@Override
	protected void doOptions(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doOptions(arg0, arg1);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BufferedReader br = req.getReader();
		String line = null;
		String jsonString = "";

		while ((line = br.readLine()) != null) {
			jsonString = jsonString.concat(line);
		}

		JSONRPC2Request reqIn = null;
		
		try {
			reqIn = JSONRPC2Request.parse(jsonString);

		} catch (JSONRPC2ParseException e) {
			System.out.println(e.getMessage());
			resp.sendError(resp.SC_BAD_REQUEST, "RPC Message Parsing failed. " + e.getMessage());
			return;
		}

		JSONRPC2Response respOut = processRequest(reqIn);
		
		//resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		//resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Content-Type", "application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(respOut.toString());
        resp.getWriter().flush();
        resp.getWriter().close();
	}
	
	
	private JSONRPC2Response processRequest(JSONRPC2Request req) {
		//TODO implement error handle and exception if method is unknown or unsupported
		JSONRPC2Response resp = null;
		switch (req.getMethod()) {
	        case "Scrabble.NewGame":  
	        	 resp = createGame(req);
	             break;
	        default: 
                 break;
		}
		
		return resp;
	}
	
	private JSONRPC2Response createGame(JSONRPC2Request req) {
		//TODO -- call out to DAL and create game in database
		
		Map<String, Object> respMap = new HashMap<String, Object>(); 
		respMap.put("position", 6);
		JSONRPC2Response resp = new JSONRPC2Response(respMap, req.getID());
		return resp;
	}

}
