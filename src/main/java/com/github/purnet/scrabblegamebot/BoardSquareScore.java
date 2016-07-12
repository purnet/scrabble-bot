package com.github.purnet.scrabblegamebot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoardSquareScore {
	private int LS;
	private int WS;
	private boolean start;
	
	@JsonProperty("LS")
	public int getLS() {
		return LS;
	}
	public void setLS(int LS) {
		this.LS = LS;
	}
	@JsonProperty("WS")
	public int getWS() {
		return WS;
	}
	public void setWS(int WS) {
		this.WS = WS;
	}
	@JsonProperty("start")
	public boolean isStart() {
		return start;
	}
	public void setStart(boolean start) {
		this.start = start;
	}
	
	
}
