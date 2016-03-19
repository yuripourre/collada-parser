package br.com.etyllica.loader.collada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.com.abby.core.vbo.Face;
import br.com.abby.core.vbo.Group;
import br.com.abby.core.vbo.VBO;
import br.com.etyllica.loader.collada.node.GeometryNode;
import br.com.etyllica.loader.collada.node.InputNode;
import br.com.etyllica.loader.collada.node.VerticesNode;

public class ColladaParser extends DefaultHandler {

    private static final String GEOMETRY = "geometry"; //Equivalent to Object in OBJ
    private static final String SOURCE = "source";
    private static final String FLOAT_ARRAY = "float_array";
    private static final String VERTICES = "vertices";
    private static final String TRIANGLES = "triangles";
    private static final String LINES = "lines";
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

    private String lastName;
    private String currentName;
    private String sourceId;
    private String currentId;
    private int count;

    private String currentPrimitive = TRIANGLES;

    private Group currentGroup;
    private GeometryNode currentGeometry;
    private VerticesNode currentVertices;

    private VBO vbo = new VBO();

    private Map<String, GeometryNode> geometries = new HashMap<String, GeometryNode>();
    private Map<Integer, InputNode> inputs = new LinkedHashMap<Integer, InputNode>();
    private Map<String, Integer> sourceOffsets = new HashMap<String, Integer>();
    private List<Float> vertices = new ArrayList<Float>();

    int partsCount = 0;
    StringBuilder builder = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        lastName = currentName;
        currentName = qName;

        System.out.println("<" + qName + ">");

        if (GEOMETRY.equals(qName)) {
            String id = attributes.getValue("id");
            currentGeometry = new GeometryNode();
            currentGroup = new Group(id);
            geometries.put(id, currentGeometry);
        } else if (SOURCE.equals(qName)) {
            sourceId = attributes.getValue("id");
        } else if (FLOAT_ARRAY.equals(qName)) {
            currentId = attributes.getValue("id");
            count = Integer.parseInt(attributes.getValue(ATTRIBUTE_COUNT));

            System.out.println("VertexSize: " + vertices.size());
            sourceOffsets.put(sourceId, vertices.size());
        } else if (TRIANGLES.equals(qName)) {
            currentPrimitive = TRIANGLES;

            inputs.clear();
            //Face Group
            String material = attributes.getValue(ATTRIBUTE_MATERIAL);
            count = Integer.parseInt(attributes.getValue(ATTRIBUTE_COUNT));
        } else if (LINES.equals(qName)) {
            currentPrimitive = LINES;
        } else if (VERTICES.equals(qName)) {
            currentId = attributes.getValue("id");
            System.out.println("Id: " + currentId);
            currentVertices = new VerticesNode();
        } else if (INPUT.equals(qName)) {
            InputNode input = parseInput(attributes);

            if (VERTICES.equals(lastName)) {
                if (SEMANTIC_POSITION.equals(input.semantic)) {
                    String sourceId = input.source;
                    float[] array = currentGeometry.floatArrays.get(sourceId);
                    currentVertices.position = array;
                } else if (SEMANTIC_NORMAL.equals(input.semantic)) {
                    String sourceId = input.source;
                    float[] array = currentGeometry.floatArrays.get(sourceId);
                    currentVertices.normal = array;
                }
            } else {
                inputs.put(input.offset, input);
            }
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

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length);

        if (!hasText(text)) {
            return;
        }

        if (FLOAT_ARRAY.equals(currentName)) {
            text = fixLine(text);
            if(text.isEmpty())
                return;
            parseFloatArray(text);
        } else if (PRIMITIVE.equals(currentName)) {
            if (TRIANGLES.equals(currentPrimitive)) {
                text = fixLine(text);
                if(text.isEmpty())
                    return;
                parseTriangles(text);
            } else if (LINES.equals(currentPrimitive)) {
                //Ignore for now
            }
        }
    }

    private String fixLine(String line) {
        return line.replaceAll("\n", "").trim();
    }

    private void parseTriangles(String text) {
        int inputsCount = inputs.size();
        int partsLength = countParts(text);

        System.out.println((partsCount + partsLength) + "/" + count + "(*" + inputsCount + "*3) = " + (count * inputsCount * 3));

        String string;

        if (partsCount + partsLength < count * inputsCount * 3) {
            builder.append(text);
            partsCount += partsLength;
            return;
        } else {
            builder.append(text);
            string = builder.toString();
        }

        String[] parts = string.split(" ");

        int i = 0;
        for (; i < count * inputsCount * 3; i += inputsCount * 3) {
            Face face = new Face(3);

            for (InputNode input : inputs.values()) {
                int offset = input.offset;

                if (SEMANTIC_VERTEX.equals(input.semantic)) {
                    face.vertexIndex[0] = Integer.parseInt(parts[i + 0 * inputsCount + offset]);
                    face.vertexIndex[1] = Integer.parseInt(parts[i + 1 * inputsCount + offset]);
                    face.vertexIndex[2] = Integer.parseInt(parts[i + 2 * inputsCount + offset]);
                } else if (SEMANTIC_NORMAL.equals(input.semantic)) {
                    face.normalIndex[0] = Integer.parseInt(parts[i + 0 * inputsCount + offset]);
                    face.normalIndex[1] = Integer.parseInt(parts[i + 1 * inputsCount + offset]);
                    face.normalIndex[2] = Integer.parseInt(parts[i + 2 * inputsCount + offset]);
                } else if (SEMANTIC_TEXTCOORD.equals(input.semantic)) {
                    face.textureIndex[0] = Integer.parseInt(parts[i + 0 * inputsCount + offset]);
                    face.textureIndex[1] = Integer.parseInt(parts[i + 1 * inputsCount + offset]);
                    face.textureIndex[2] = Integer.parseInt(parts[i + 2 * inputsCount + offset]);
                }
            }

            currentGroup.getFaces().add(face);
        }

        vbo.getGroups().add(currentGroup);

        partsCount = 0;
        builder = new StringBuilder();
    }

    StringBuilder floatBuilder = new StringBuilder();

    private void parseFloatArray(String text) {
        int partsLength = countParts(text);

        System.out.println((partsCount + partsLength) + "/" + count);

        if (partsCount + partsLength < count) {
            //TODO
            //Count is always adding 1 part in append
            String[] parts = text.split(" ");
            System.out.println("Part[i] " + parts[0]);
            System.out.println("Part[F] " + parts[parts.length - 1]);
            floatBuilder.append(text);

            if(!text.endsWith(" ")) {
                partsLength--;
            }
            partsCount += partsLength;
            return;
        } else {
            String[] parts = text.split(" ");
            System.out.println("Part[i] " + parts[0]);
            System.out.println("Part[F] " + parts[parts.length - 1]);
            floatBuilder.append(text);
        }

        String string = floatBuilder.toString();
        floatBuilder = new StringBuilder();

        partsLength = countParts(text);

        System.out.println("REAL PARSING: " + (partsCount + partsLength) + "/" + count);

        String[] parts = string.split(" ");
        float[] array = new float[count];

        int i = 0;
        for (; i < count; i++) {
            float n = Float.parseFloat(parts[i]);
            array[i] = n;
            vertices.add(n);
        }

        currentGeometry.floatArrays.put(sourceId, array);

        partsCount = 0;
    }

    private boolean hasText(String string) {
        //string = string.trim();
        return !string.isEmpty();
    }

    public static int countParts(String text) {
        return countOccurrences(text, ' ') + 1;
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
