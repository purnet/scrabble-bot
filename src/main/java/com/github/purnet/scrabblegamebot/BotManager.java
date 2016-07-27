package com.github.purnet.scrabblegamebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import net.minidev.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.purnet.scrabblegamebot.jsonmapping.Asset;
import com.github.purnet.scrabblegamebot.jsonmapping.CreateGameResponseWrapper;
import com.github.purnet.webhelperlib.HTTPRequestHelper;
import com.github.purnet.webhelperlib.Response;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class BotManager {
	private static final String persistenceLayerUrl = "http://localhost:8080/gamedataaccesslayer";
	private Map<Integer,ScrabbleBot> scrabbleBots;
	
	public BotManager(){
		scrabbleBots = new HashMap<Integer, ScrabbleBot>();
	}
	
	public void addNewBot(int gameId, List<Asset> assets) {
		ScrabbleBot bot = new ScrabbleBot();
		for (Asset a : assets) {
			switch (a.getAssetName()) {
				case "dictionary":  
					bot.populateDictionary(a.getAssetContent());
		            break;
		        case "letters":  
		        	bot.setLetterPoints(a.getAssetContent());
		            break;
		        case "gameboard":
		        	bot.setStandardBoard(a.getAssetContent());
		        	break;
		        default: 
	                break;
			}
		}
		bot.populateLexicon();
		scrabbleBots.put(gameId, bot);
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
		variables.put("gameType", "Scrabble");
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
		
		body.put("query", "mutation insertGame ($merkneraGameId: Int!, $status: String, $gameType: String!, $players: [PlayerInput], $assets: [AssetInput]) {createGame: createGame(merkneraGameId: $merkneraGameId, status: $status, gameType: $gameType, playerInput: $players, assetInput: $assets) { assets { assetName, assetContent}} }");
		body.put("variables", variables.toString());
		
		Response r = HTTPRequestHelper.makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
		ObjectMapper mapper = new ObjectMapper();
		CreateGameResponseWrapper gameResp = null;
		try {
			gameResp = mapper.readValue(r.getBody(), CreateGameResponseWrapper.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addNewBot(Integer.valueOf(params.get("gameid").toString()), gameResp.getData().getcreateGame().getAssets());
		
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
		int gameId = Integer.valueOf(params.get("gameid").toString());
		gameStateJSON.put("gamestate", params.get("gamestate"));
		gameStateJSON.put("tiles", params.get("tiles"));
					
		variables.put("merkneraGameId", params.get("gameid"));
		variables.put("tiles", params.get("tiles"));
		variables.put("state", params.get("gamestate"));

		body.put("query", "mutation insertMove ($merkneraGameId: Int!, $state: String!, $tiles: String!){ game: createMove(merkneraGameId : $merkneraGameId, gameState : $state, tiles: $tiles\n  ) {id, gameState, tiles  }}");
		body.put("variables", variables.toString());
		Response r = HTTPRequestHelper.makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
			
		if (!scrabbleBots.containsKey(gameId)){
			body = new JSONObject();
			variables = new JSONObject();
			variables.put("id", Integer.valueOf(params.get("gameid").toString()));
			body.put("query", "query getGame($id: Int!){ createGame: game(id: $id){ assets { assetName, assetContent}} }");
			body.put("variables", variables.toString());
			r = HTTPRequestHelper.makeHTTPRequest(persistenceLayerUrl, body.toString(), "POST");
			ObjectMapper mapper = new ObjectMapper();
			CreateGameResponseWrapper gameResp = null;
			try {
				gameResp = mapper.readValue(r.getBody(), CreateGameResponseWrapper.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			addNewBot(Integer.valueOf(params.get("gameid").toString()), gameResp.getData().getcreateGame().getAssets());
		}
		
	    Move result = scrabbleBots.get(gameId).makeBestMove(gameStateJSON.toJSONString());
		
		JSONRPC2Response resp = new JSONRPC2Response(result, req.getID());

		return resp;
	}
	
	public void endGame(JSONRPC2Request req) throws JsonProcessingException {
		Map<String,Object> params = req.getNamedParams();
		scrabbleBots.remove(Integer.valueOf(params.get("gameid").toString()));	
	}
	
}
