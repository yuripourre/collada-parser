import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.com.abby.core.vbo.VBO;
import br.com.etyllica.loader.collada.node.GeometryNode;
import br.com.etyllica.loader.collada.node.InputNode;
import br.com.etyllica.util.PathHelper;

public class XMLReader extends DefaultHandler {

	private static final String GEOMETRY = "geometry";
	private static final String SOURCE = "source";
	private static final String FLOAT_ARRAY = "float_array";
	private static final String TRIANGLES = "triangles";
	private static final String PRIMITIVE = "p";
	private static final String INPUT = "input";
	
	private static final String ATTRIBUTE_COUNT = "count";
	private static final String ATTRIBUTE_MATERIAL = "material";
	private static final String ATTRIBUTE_OFFSET = "offset";
	private static final String ATTRIBUTE_SEMANTIC = "semantic";
	
	private static final String SEMANTIC_NORMAL = "NORMAL";
	private static final String SEMANTIC_POSITION = "POSITION";
	private static final String SEMANTIC_VERTEX = "VERTEX";
	private static final String SEMANTIC_TEXTCOORD = "TEXCOORD";
	
	static String path = PathHelper.currentDirectory().substring(5)+"../assets/model.dae";
	
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, XMLStreamException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new File(path), new XMLReader());
    }

    private String currentName;
    private String sourceId;
    private String currentId;
    private int count;
    
    private GeometryNode currentGeometry;
    
    private VBO vbo = new VBO();
    
    private Map<String, GeometryNode> geometries = new HashMap<String, GeometryNode>();
    private List<InputNode> inputs = new ArrayList<InputNode>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentName = qName;
        System.out.println("<"+qName+">");
        
        if(GEOMETRY.equals(qName)) {
        	String id = attributes.getValue("id");
        	currentGeometry = new GeometryNode();
        	geometries.put(id, currentGeometry);
        } else if(SOURCE.equals(qName)) {
        	sourceId = attributes.getValue("id");
        } else if(FLOAT_ARRAY.equals(qName)) {
        	currentId = attributes.getValue("id");
        	count = Integer.parseInt(attributes.getValue(ATTRIBUTE_COUNT));
        } else if(TRIANGLES.equals(qName)) {
        	inputs.clear();
        	//Face Group
        	String material = attributes.getValue(ATTRIBUTE_MATERIAL);
        	count = Integer.parseInt(attributes.getValue(ATTRIBUTE_COUNT));
        } else if(INPUT.equals(qName)) {
        	InputNode input = parseInput(attributes);
        	inputs.add(input);
        }
        
    }

	private InputNode parseInput(Attributes attributes) {
		InputNode input = new InputNode();

		String offset = attributes.getValue(ATTRIBUTE_OFFSET);
		
		if (offset != null) {
			input.offset = Integer.parseInt(offset);
		}
		
		String semantic = attributes.getValue(ATTRIBUTE_SEMANTIC);
		String source = attributes.getValue(SOURCE);
		input.semantic = semantic;
		input.source = source;
		
		return input;
	}

    int partsCount = 0;
    StringBuilder builder = new StringBuilder();
    
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length);
        if (!hasText(text)) {
        	return;
        }
        
        if(FLOAT_ARRAY.equals(currentName)) {
        	parseFloatArray(text);
        } else if(PRIMITIVE.equals(currentName)) {
        	//TODO Fix this
        	//parseTriangles(text);
        }
    }

    private void parseTriangles(String text) {
    	int partsLength = countParts(text);
		
		System.out.println((partsCount+partsLength)+"/"+count);
		
		String string;
		
		if (partsCount + partsLength < count) {
			builder.append(text);
			partsCount += partsLength;
			return;
		} else {
			builder.append(text);
			string = builder.toString();
		}
		
		String[] parts = string.split(" ");
		int[] array = new int[count];
		
		int i = 0;
		for(; i < count; i++) {
			array[i] = Integer.parseInt(parts[i]);
		}
		
		partsCount = 0;
		builder = new StringBuilder();
    }
    
	private void parseFloatArray(String text) {
		int partsLength = countParts(text);
		
		System.out.println((partsCount+partsLength)+"/"+count);
		
		String string;
		
		if (partsCount + partsLength < count) {
			//TODO     	
			//Count is always adding 1 part in append
			builder.append(text);
			partsCount += partsLength;
			return;
		} else {
			builder.append(text);
			string = builder.toString();
		}
		
		String[] parts = string.split(" ");
		float[] array = new float[count];
		
		int i = 0;
		for(; i < count; i++) {
			array[i] = Float.parseFloat(parts[i]);
		}
		
		currentGeometry.floatArrays.put(sourceId, array);
		
		partsCount = 0;
		builder = new StringBuilder();
	}

    private boolean hasText(String string) {
        string = string.trim();
        return !string.isEmpty();
    }
    
    public static int countParts(String text) {
    	return countOccurrences(text, ' ')+1;
    }
    
    public static int countOccurrences(String text, char needle) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == needle) {
                 count++;
            }
        }
        return count;
    }
}