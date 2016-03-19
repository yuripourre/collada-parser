package br.com.etyllica.loader.collada.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GeometryNode {

	public Set<VerticesNode> vertices = new HashSet<VerticesNode>();
	public Map<String, float[]> floatArrays = new HashMap<String, float[]>();
    public Map<String, SourceNode> sources = new HashMap<String, SourceNode>();
	
	//public Map<String, float[]> source = new HashMap<String, float[]>();
	
}
