package com.github.purnet.scrabblegamebot;

import java.util.Map;

public class LetterInfo {
	private Map<String, Integer> points;
	private Map<String, Integer> distribution;
	public Map<String, Integer> getPoints() {
		return points;
	}
	public void setPoints(Map<String, Integer> points) {
		this.points = points;
	}
	public Map<String, Integer> getDistribution() {
		return distribution;
	}
	public void setDistribution(Map<String, Integer> distribution) {
		this.distribution = distribution;
	}
	
}
