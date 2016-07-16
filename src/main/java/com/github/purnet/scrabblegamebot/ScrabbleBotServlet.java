package com.github.purnet.scrabblegamebot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.minidev.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;


@SuppressWarnings("serial")
public class ScrabbleBotServlet extends HttpServlet {

    private BotManager botManager;
	
	@Override
	public void init() throws ServletException {
		botManager = new BotManager();
		// TODO Registration
		super.init();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("Hello World. Get is not yet implemented");
		resp.getOutputStream().write("Hello World. Get is not yet implemented".getBytes());
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		resp.setHeader("Access-Control-Allow-Origin", "*");

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

	
	private JSONRPC2Response processRequest(JSONRPC2Request req) throws JsonProcessingException, ServletException {
		//TODO implement error handle and exception if method is unknown or unsupported
		JSONRPC2Response resp = null;
		switch (req.getMethod()) {
			case "Status.Ping":  
				//TODO 
	            break;
	        case "Scrabble.NewGame":  
	        	resp = botManager.createGame(req);
	            break;
	        case "Scrabble.NextMove":
	        	resp = botManager.createMove(req);
	        	break;
	        case "Scrabble.Complete":
	        	//TODO shut down game
	        	botManager.endGame(req);
	        	break;
	        case "Scrabble.Error":
	        	//TODO print out error somewhere or log
	        	break;
	        default: 
                break;
		}
		
		return resp;
	}
	

}
