package com.github.purnet.scrabblegamebot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import net.minidev.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class BotManager {
	private static final String persistenceLayerUrl = "http://localhost:8080/gamedataaccesslayer";
	private Map<Integer,ScrabbleBot> scrabbleBots;
	
	public BotManager(){
		scrabbleBots = new HashMap<Integer, ScrabbleBot>();
	}
	
	public JSONRPC2Response createGame(JSONRPC2Request req) throws ServletException {
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
		
		ScrabbleBot bot = new ScrabbleBot();
		Map<String, Object> asset = (Map<String, Object>) params.get("gameboard");
		gameboard.put("code", asset.get("sha1"));
		gameboard.put("name", "gameboard");
		gameboard.put("url", asset.get("url"));
		assets.add(gameboard);
		try {
			bot.populateGameAsset("", asset.get("url").toString(), "gameboard");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ServletException("Error in initialising servlet");
		}
		
		asset = (Map<String, Object>) params.get("dictionary");
		dictionary.put("code", asset.get("sha1"));
		dictionary.put("name", "dictionary");
		dictionary.put("url", asset.get("url"));
		assets.add(dictionary);
		bot.populateDictionary("",asset.get("url").toString());
		
		asset = (Map<String, Object>) params.get("letters");
		letters.put("code", asset.get("sha1"));
		letters.put("name", "letters");
		letters.put("url", asset.get("url"));
		assets.add(letters);
		try {
			bot.populateGameAsset("", asset.get("url").toString(), "letters");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ServletException("Error in initialising servlet");
		}
		
		bot.populateLexicon();
		scrabbleBots.put(Integer.valueOf(params.get("gameid").toString()), bot);
		
		variables.put("assets", assets);
		
		body.put("query", "mutation insertGame ($merkneraGameId: Int!, $status: String, $players: [PlayerInput], $assets: [AssetInput]) {game: createGame(merkneraGameId: $merkneraGameId, status: $status, playerInput: $players, assetInput: $assets) { merkneraGameId,  status,  players {playerName, playerNumber }, assets {assetUrl, assetCode, assetName}} }");
		body.put("variables", variables.toString());
		
		Response r = HTTPRequestHelper.makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
	
		Map<String, Object> respMap = new HashMap<String, Object>(); 
		//TODO check if assets are downloaded and if not respond with preparing
		//     kick off an async task to download them and send a ready status once done
		respMap.put("status", "READY");
		JSONRPC2Response resp = new JSONRPC2Response(respMap, req.getID());
		return resp;
	}
	
	public JSONRPC2Response createMove(JSONRPC2Request req) throws JsonProcessingException {
		Map<String,Object> params = req.getNamedParams();
		JSONObject body = new JSONObject();
		JSONObject variables = new JSONObject();
		JSONObject gameStateJSON = new JSONObject();
		gameStateJSON.put("gamestate", params.get("gamestate"));
		gameStateJSON.put("tiles", params.get("tiles"));
					
		variables.put("merkneraGameId", params.get("gameid"));
		variables.put("tiles", params.get("tiles"));
		variables.put("state", params.get("gamestate"));

		body.put("query", "mutation insertMove ($merkneraGameId: Int!, $state: String!, $tiles: String!){ game: createMove(merkneraGameId : $merkneraGameId, gameState : $state, tiles: $tiles\n  ) {id, gameState, tiles  }}");
		body.put("variables", variables.toString());
		Response r = HTTPRequestHelper.makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
		
	    Move m = scrabbleBots.get(Integer.valueOf(params.get("gameid").toString())).makeBestMove(gameStateJSON.toJSONString());
		ObjectMapper mapper = new ObjectMapper();
		String resultJSON = mapper.writeValueAsString(m); 
		
		Map<String, Object> respMap = new HashMap<String, Object>();
		respMap.put("result", resultJSON);
		JSONRPC2Response resp = new JSONRPC2Response(respMap, req.getID());

		return resp;
	}
	
	public void endGame(JSONRPC2Request req) throws JsonProcessingException {
		Map<String,Object> params = req.getNamedParams();
		scrabbleBots.remove(Integer.valueOf(params.get("gameid").toString()));	
	}
	
}
