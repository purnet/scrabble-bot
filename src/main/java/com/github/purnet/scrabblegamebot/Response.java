package com.github.purnet.scrabblegamebot;

public class Response {
	private int code;
	private String status;
	private String body;
	
	Response(int c, String s, String b){
		code = c;
		status = s;
		body = b;
	}
	public int getCode() {
		return code;
	}
	public String getStatus() {
		return status;
	}
	public String getBody() {
		return body;
	}
	
}
