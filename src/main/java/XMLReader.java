import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import br.com.abby.core.vbo.Face;
import br.com.abby.core.vbo.Group;
import br.com.abby.core.vbo.VBO;
import org.xml.sax.SAXException;

import br.com.etyllica.loader.collada.ColladaParser;
import br.com.etyllica.util.PathHelper;

public class XMLReader {
	
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, XMLStreamException {

        String ASSETS = PathHelper.currentDirectory().substring(5)+"../assets/";
        String ASSIMP = ASSETS+"assimp/Collada/";

        String path = ASSETS+"model.dae";
        //String path = ASSIMP+"cube.dae";
        //String path = ASSIMP+"cube_triangulate.dae";
        //String path = ASSIMP+"COLLADA.dae";

        ColladaParser colladaParser = new ColladaParser();

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new File(path), colladaParser);

        VBO vbo = colladaParser.getVBO();
        printVBO(vbo);
    }

    private static void printVBO(VBO vbo) {
        System.out.println("Groups: "+vbo.getGroups());
        System.out.println("Faces: "+vbo.getFaces());
        System.out.println("Vertices: "+vbo.getVertices());

        for(Group group: vbo.getGroups()) {
            System.out.println("Name: "+group.getName());
            System.out.println("Faces: "+group.getFaces().size());
            for(Face face: group.getFaces()) {
                for(int index : face.vertexIndex) {
                    System.out.print(index);
                    System.out.print(" ");
                }
                System.out.println(" ");
            }
        }
    }

}