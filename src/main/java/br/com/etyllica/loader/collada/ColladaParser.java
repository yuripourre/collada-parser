package br.com.etyllica.loader.collada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.etyllica.loader.collada.helper.ColladaParserHelper;
import br.com.etyllica.loader.collada.node.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.com.abby.core.vbo.Face;
import br.com.abby.core.vbo.Group;
import br.com.abby.core.vbo.VBO;

public class ColladaParser extends DefaultHandler {

    public static final String GEOMETRY = "geometry"; //Equivalent to Object in OBJ
    public static final String SOURCE = "source";
    public static final String FLOAT_ARRAY = "float_array";
    public static final String VERTICES = "vertices";
    public static final String TRIANGLES = "triangles";
    public static final String LINES = "lines";
    public static final String PRIMITIVE = "p";
    public static final String INPUT = "input";
    public static final String ACCESSOR = "accessor";

    public static final String ATTRIBUTE_COUNT = "count";
    public static final String ATTRIBUTE_MATERIAL = "material";
    public static final String ATTRIBUTE_OFFSET = "offset";
    public static final String ATTRIBUTE_SEMANTIC = "semantic";

    private static final String SEMANTIC_NORMAL = "NORMAL";
    private static final String SEMANTIC_POSITION = "POSITION";
    private static final String SEMANTIC_VERTEX = "VERTEX";
    private static final String SEMANTIC_TEXTCOORD = "TEXCOORD";

    private String lastName;
    private String currentName;
    private String sourceId;
    private String currentId;
    private int count;
    private int accessorCount;

    private String currentPrimitive = TRIANGLES;

    private Group currentGroup;
    private GeometryNode currentGeometry;
    private VerticesNode currentVertices;
    private SourceNode currentSource;

    List<Vector3> vertices = new ArrayList<Vector3>();
    List<Vector3> normals = new ArrayList<Vector3>();
    List<Vector2> textures = new ArrayList<Vector2>();


    private VBO vbo = new VBO();

    private Map<String, GeometryNode> geometries = new HashMap<String, GeometryNode>();
    private Map<Integer, InputNode> inputs = new LinkedHashMap<Integer, InputNode>();
    private Map<String, Integer> sourceOffsets = new HashMap<String, Integer>();

    int partsCount = 0;

    StringBuilder floatBuilder = new StringBuilder();
    StringBuilder triangleBuilder = new StringBuilder();

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
            currentSource = new SourceNode();
            currentSource.id = attributes.getValue("id");

            currentGeometry.sources.put("#" + currentSource.id, currentSource);
        } else if (FLOAT_ARRAY.equals(qName)) {
            currentId = attributes.getValue("id");
            System.out.print("(ID:" + currentId + ")");
            count = Integer.parseInt(attributes.getValue(ATTRIBUTE_COUNT));

            System.out.println("VertexSize: " + vertices.size());
            sourceOffsets.put(sourceId, vertices.size());
            currentSource.floatArrayId = currentId;
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
            currentVertices = new VerticesNode();
        } else if (ACCESSOR.equals(qName)) {
            AccessorNode accessorNode = new AccessorNode();
            accessorNode.count = Integer.parseInt(attributes.getValue("count"));
            accessorNode.source = attributes.getValue(SOURCE);
            accessorNode.stride = Integer.parseInt(attributes.getValue("stride"));

            currentSource.accessor = accessorNode;

        } else if (INPUT.equals(qName)) {
            InputNode input = ColladaParserHelper.parseInput(attributes);

            if (VERTICES.equals(lastName)) {

                if (SEMANTIC_POSITION.equals(input.semantic)) {
                    String sourceId = input.source;
                    SourceNode source = currentGeometry.sources.get(sourceId);
                    source.offsetPosition = vertices.size();
                    float[] array = source.floatArray;
                    AccessorNode accessor = source.accessor;

                    currentVertices.position = array;

                    //3d vector
                    if (accessor.stride == 3) {
                        List<Vector3> list = new ArrayList<Vector3>(accessor.count);
                        ColladaParserHelper.parseVertex3D(source, array, list);
                        vertices.addAll(list);
                    } else if (accessor.stride == 2) {
                        List<Vector2> list = new ArrayList<Vector2>(accessor.count);
                        ColladaParserHelper.parseVertex2D(source, array, list);
                        for (Vector2 v : list) {
                            vertices.add(new Vector3(v.x, v.y, 0));
                        }
                    }

                } else if (SEMANTIC_NORMAL.equals(input.semantic)) {
                    String sourceId = input.source;
                    SourceNode source = currentGeometry.sources.get(sourceId);
                    source.offsetNormal = normals.size();
                    float[] array = source.floatArray;

                    AccessorNode accessor = source.accessor;

                    currentVertices.normal = array;

                    if (accessor.stride == 3) {
                        List<Vector3> list = new ArrayList<Vector3>(accessor.count);
                        ColladaParserHelper.parseVertex3D(source, array, list);
                        normals.addAll(list);
                    } else if (accessor.stride == 2) {
                        List<Vector2> list = new ArrayList<Vector2>(accessor.count);
                        ColladaParserHelper.parseVertex2D(source, array, list);
                        for (Vector2 v : list) {
                            normals.add(new Vector3(v.x, v.y, 0));
                        }
                    }
                } else if (SEMANTIC_TEXTCOORD.equals(input.semantic)) {
                    String sourceId = input.source;
                    SourceNode source = currentGeometry.sources.get(sourceId);
                    source.offsetTexture = textures.size();
                    float[] array = source.floatArray;

                    AccessorNode accessor = source.accessor;

                    if (accessor.stride == 2) {
                        List<Vector2> list = new ArrayList<Vector2>(accessor.count);
                        ColladaParserHelper.parseVertex2D(source, array, list);
                        textures.addAll(list);
                    }
                }
            } else {
                inputs.put(input.offset, input);
            }
        }
    }

    @Override
    public void endDocument() {
        vbo.getVertices().addAll(vertices);
        vbo.getNormals().addAll(normals);
        vbo.getTextures().addAll(textures);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length);

        if (!hasText(text)) {
            return;
        }

        if (FLOAT_ARRAY.equals(currentName)) {
            text = fixLine(text);
            if (text.isEmpty())
                return;
            parseFloatArray(text);
        } else if (PRIMITIVE.equals(currentName)) {
            if (TRIANGLES.equals(currentPrimitive)) {
                text = fixLine(text);
                if (text.isEmpty())
                    return;
                parseTriangles(text);
            } else if (LINES.equals(currentPrimitive)) {
                //Ignore for now
            }
        }
    }

    private String fixLine(String line) {
        if (line.startsWith("\n")) {
            return line.replaceAll("\n", "").trim();
        }
        return line;
    }

    private void parseTriangles(String text) {
        int inputsCount = inputs.size();
        int partsLength = countParts(text);

        System.out.println((partsCount + partsLength) + "/" + count + "(*" + inputsCount + "*3) = " + (count * inputsCount * 3));

        triangleBuilder.append(text);

        if (partsCount + partsLength < count * inputsCount * 3) {
            partsLength--;
            partsCount += partsLength;
            return;
        }

        String string = triangleBuilder.toString();
        triangleBuilder = new StringBuilder();

        String[] parts = string.split(" ");

        int i = 0;
        for (; i < count * inputsCount * 3; i += inputsCount * 3) {
            Face face = new Face(3);

            for (InputNode input : inputs.values()) {
                int vertexOffset;
                int offset = input.offset;

                if (SEMANTIC_VERTEX.equals(input.semantic)) {
                    vertexOffset = currentSource.offsetPosition;
                    face.vertexIndex[0] = Integer.parseInt(parts[i + 0 * inputsCount + offset]) + vertexOffset;
                    face.vertexIndex[1] = Integer.parseInt(parts[i + 1 * inputsCount + offset]) + vertexOffset;
                    face.vertexIndex[2] = Integer.parseInt(parts[i + 2 * inputsCount + offset]) + vertexOffset;
                } else if (SEMANTIC_NORMAL.equals(input.semantic)) {
                    vertexOffset = currentSource.offsetNormal;
                    face.normalIndex[0] = Integer.parseInt(parts[i + 0 * inputsCount + offset]) + vertexOffset;
                    face.normalIndex[1] = Integer.parseInt(parts[i + 1 * inputsCount + offset]) + vertexOffset;
                    face.normalIndex[2] = Integer.parseInt(parts[i + 2 * inputsCount + offset]) + vertexOffset;
                } else if (SEMANTIC_TEXTCOORD.equals(input.semantic)) {
                    vertexOffset = currentSource.offsetTexture;
                    face.textureIndex[0] = Integer.parseInt(parts[i + 0 * inputsCount + offset]) + vertexOffset;
                    face.textureIndex[1] = Integer.parseInt(parts[i + 1 * inputsCount + offset]) + vertexOffset;
                    face.textureIndex[2] = Integer.parseInt(parts[i + 2 * inputsCount + offset]) + vertexOffset;
                }
            }

            currentGroup.getFaces().add(face);
        }

        vbo.getGroups().add(currentGroup);

        partsCount = 0;
    }

    private void parseFloatArray(String text) {
        int partsLength = countParts(text);

        System.out.println((partsCount + partsLength) + "/" + count);

        floatBuilder.append(text);

        if (partsCount + partsLength < count) {
            partsLength--;
            partsCount += partsLength;
            return;
        }

        String string = floatBuilder.toString();
        floatBuilder = new StringBuilder();

        String[] parts = string.split(" ");
        float[] array = new float[count];

        int i = 0;
        for (; i < count; i++) {
            float n = Float.parseFloat(parts[i]);
            array[i] = n;
        }

        currentGeometry.floatArrays.put(sourceId, array);
        currentSource.floatArray = array;

        partsCount = 0;
    }

    private boolean hasText(String string) {
        return !string.isEmpty();
    }

    private static int countParts(String text) {
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

    public VBO getVBO() {
        return vbo;
    }

}
