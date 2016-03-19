import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import br.com.abby.core.vbo.Group;
import br.com.abby.core.vbo.VBO;
import org.xml.sax.SAXException;

import br.com.etyllica.loader.collada.ColladaParser;
import br.com.etyllica.util.PathHelper;

public class XMLReader {
	
	static String path = PathHelper.currentDirectory().substring(5)+"../assets/model.dae";
	
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, XMLStreamException {
        ColladaParser colladaParser = new ColladaParser();

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new File(path), colladaParser);

        VBO vbo = colladaParser.getVBO();
        System.out.println("Groups: "+vbo.getGroups());
        System.out.println("Faces: "+vbo.getFaces());
        System.out.println("Vertices: "+vbo.getVertices());

        for(Group group: vbo.getGroups()) {
            System.out.println("Name: "+group.getName());
            System.out.println("Faces: "+group.getFaces().size());
        }
    }

}