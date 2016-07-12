package com.github.purnet.scrabblegamebot;

import java.util.ArrayList;

public class StandardBoard {
	private ArrayList<ArrayList<BoardSquareScore>> standardBoard;

	public ArrayList<ArrayList<BoardSquareScore>> getStandardBoard() {
		return standardBoard;
	}

	public void setStandardBoard(
			ArrayList<ArrayList<BoardSquareScore>> standardBoard) {
		this.standardBoard = standardBoard;
	}
}
