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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;


@SuppressWarnings("serial")
public class ScrabbleBotServlet extends HttpServlet {
	
    private static final String persistenceLayerUrl = "http://localhost:8080/gamedataaccesslayer";
    private ScrabbleBot bot;
	
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		bot = new ScrabbleBot();
		// TODO Registration and move populate dict to this place with correct url
		this.populateDictionary("","http://localhost:8000/sowpods.txt");
		super.init();
	}
	
	public ScrabbleBot getBot() {
		return bot;
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
	
	private void populateDictionary(String dictionaryHash, String dictionaryUrl){
		Response r = makeHTTPRequest(dictionaryUrl, null, "GET");
		BufferedReader bufReader = new BufferedReader(new StringReader(r.getBody()));
		String line=null;
		ArrayList<String> temp = this.bot.getDictionary();
		try {
			while( (line=bufReader.readLine()) != null )
			{
				temp.add(line);
			}
			this.bot.setDictionary(temp);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static Response makeHTTPRequest(String targetURL, String body, String reqType) {
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
	
	
	private JSONRPC2Response processRequest(JSONRPC2Request req) {
		//TODO implement error handle and exception if method is unknown or unsupported
		JSONRPC2Response resp = null;
		switch (req.getMethod()) {
	        case "Scrabble.NewGame":  
	        	resp = createGame(req);
	            break;
	        case "Scrabble.NextMove":
	        	resp = createMove(req);
	        	break;
	        default: 
                break;
		}
		
		return resp;
	}
	
	private JSONRPC2Response createGame(JSONRPC2Request req) {
		Map<String,Object> params = req.getNamedParams();

		JSONObject body = new JSONObject();
		JSONObject variables = new JSONObject();
		ArrayList<JSONObject> assets = new ArrayList<JSONObject>();
		JSONObject gameboard = new JSONObject();
		JSONObject dictionary = new JSONObject();
		JSONObject letters = new JSONObject();
		variables.put("merkneraGameId", params.get("gameid"));
		variables.put("status", "PREPARING");
		variables.put("players", params.get("players"));
		
		Map<String, Object> asset = (Map<String, Object>) params.get("gameboard");
		gameboard.put("code", asset.get("sha1"));
		gameboard.put("name", "gameboard");
		gameboard.put("url", asset.get("url"));
		assets.add(gameboard);
		
		asset = (Map<String, Object>) params.get("dictionary");
		dictionary.put("code", asset.get("sha1"));
		dictionary.put("name", "dictionary");
		dictionary.put("url", asset.get("url"));
		assets.add(dictionary);
		
		asset = (Map<String, Object>) params.get("letters");
		letters.put("code", asset.get("sha1"));
		letters.put("name", "letters");
		letters.put("url", asset.get("url"));
		assets.add(letters);
		
		variables.put("assets", assets);
		
		body.put("query", "mutation insertGame ($merkneraGameId: Int!, $status: String, $players: [PlayerInput], $assets: [AssetInput]) {game: createGame(merkneraGameId: $merkneraGameId, status: $status, playerInput: $players, assetInput: $assets) { merkneraGameId,  status,  players {playerName, playerNumber }, assets {assetUrl, assetCode, assetName}} }");
		body.put("variables", variables.toString());
		
		Response r = makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
		Map<String, Object> respMap = new HashMap<String, Object>(); 
		//TODO check if assets are downloaded and if not respond with preparing
		//     kick off an async task to download them and send a ready status once done
		respMap.put("status", "READY");
		JSONRPC2Response resp = new JSONRPC2Response(respMap, req.getID());
		return resp;
	}
	
	private JSONRPC2Response createMove(JSONRPC2Request req) {
		Map<String,Object> params = req.getNamedParams();
		JSONObject body = new JSONObject();
		JSONObject variables = new JSONObject();
		JSONObject gameStateJSON = new JSONObject();
		gameStateJSON.put("gamestate", params.get("gamestate"));
				
		ObjectMapper mapper = new ObjectMapper();
		GameState gs = new GameState();
		try {
			gs = mapper.readValue(gameStateJSON.toJSONString(), GameState.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		variables.put("merkneraGameId", params.get("gameid"));
		variables.put("tiles", params.get("tiles"));
		variables.put("state", params.get("gamestate"));

		body.put("query", "mutation insertMove ($merkneraGameId: Int!, $state: String!, $tiles: String!){ game: createMove(merkneraGameId : $merkneraGameId, gameState : $state, tiles: $tiles\n  ) {id, gameState, tiles  }}");
		body.put("variables", variables.toString());
		Response r = makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
		Map<String, Object> respMap = new HashMap<String, Object>();
		JSONRPC2Response resp = new JSONRPC2Response(respMap, req.getID());
		return resp;
	}

}
