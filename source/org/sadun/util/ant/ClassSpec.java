package org.sadun.util.ant;

/**
 * @author cris
 */
public class ClassSpec {
	
	String name;
	
	public ClassSpec() {
	}
	
	public void setName(String name) { this.name=name; }
	
	public boolean equals(Object obj) {
		if (! (obj instanceof ClassSpec)) return false;
		return name.equals(((ClassSpec)obj).name);
	}
	
}
