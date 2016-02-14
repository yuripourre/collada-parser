import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import br.com.etyllica.loader.collada.ColladaParser;
import br.com.etyllica.util.PathHelper;

public class XMLReader {
	
	static String path = PathHelper.currentDirectory().substring(5)+"../assets/model.dae";
	
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, XMLStreamException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new File(path), new ColladaParser());
    }

}