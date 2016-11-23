package org.gcube.search.sru.consumer.parser.sruparser.tree;

public class GCQLPrefix {
	
	public String identifier;
	public String name;
	
	public GCQLPrefix(String identifier, String name) {
		this.identifier = identifier;
		this.name = name;
	}
	
	public String toCQL() {
		String prefixStr = "> " + identifier + " = " + name;
		return prefixStr;
	}

}
