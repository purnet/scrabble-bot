package com.github.purnet.scrabblegamebot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class Square {
	public int row;
	public int col;
	public String letter;
	public boolean anchor;
	Set<String> crossChecks;
	
	public Square(int row, int col, String l){
		this.crossChecks = new HashSet<String>();
		this.row = row;
		this.col = col;
		if (Pattern.matches("[a-zA-Z]", l.trim())){
			this.letter = l.trim().toUpperCase();
		} else {
			this.letter = "";
		}
		
	}
}
