package com.github.purnet.scrabblegamebot;

import java.util.ArrayList;
import java.util.List;

public class LexiconNode {
	public String letter;
	public List<Integer> edges;
	public boolean terminal;
	
	LexiconNode(char letter){
		this.letter = String.valueOf(letter);
		this.edges = new ArrayList<Integer>();
		this.terminal = false;
	}
	
	public String getEdgesFlat() {
		StringBuilder s = new StringBuilder();
		for (int i : edges) {
			s.append(i);
			s.append(", ");
		}
		return s.toString();
	}
}
