package org.sadun.util.ant;

/**
 * @author cris
 */
public class Resource {
	
	String name;
	
	public Resource() {
	}
	
	public void setName(String name) { this.name=name; }
	
	public String toString() { 
		if (name==null) throw new RuntimeException("Internal error: name of resource not set");
		return name; 
	}
	
}
