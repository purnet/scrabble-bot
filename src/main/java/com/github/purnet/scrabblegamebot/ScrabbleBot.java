package com.github.purnet.scrabblegamebot;

import java.util.ArrayList;

public class ScrabbleBot {
	private ArrayList<LexiconNode> nodes;
	private ArrayList<String> dictionary;
	
	ScrabbleBot(){
		dictionary = new ArrayList<String>();
		nodes = new ArrayList<LexiconNode>();
		LexiconNode l = new LexiconNode(' ');
		nodes.add(l);
	}
	
//	ScrabbleBot(String dictionaryHash){
//		
//	}
	
	public void populateLexicon(){
		
		for (String word : dictionary) {
			int currentNode = 0;
			for (char l : word.toCharArray()) {
				char letter = Character.toLowerCase(l);
				boolean edgeFound = false;
				for (int e : nodes.get(currentNode).edges) {
					if (letter == nodes.get(e).letter) {
						currentNode = e;
						edgeFound = true;
						break;
					}
				}
				if (!edgeFound) {
					LexiconNode ln = new LexiconNode(letter);
					nodes.add(ln);
					int newNode = nodes.size() -1;
					nodes.get(currentNode).edges.add(newNode);
					currentNode = newNode;
				}
			}
		}
	}
}
