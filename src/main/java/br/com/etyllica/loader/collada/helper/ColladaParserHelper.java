package br.com.etyllica.loader.collada.helper;

import br.com.etyllica.loader.collada.ColladaParser;
import br.com.etyllica.loader.collada.node.InputNode;
import br.com.etyllica.loader.collada.node.SourceNode;
import com.badlogic.gdx.math.Vector3;
import org.xml.sax.Attributes;

import java.util.List;

public class ColladaParserHelper {

    public static void parseVertex3D(SourceNode source, float[] array, List<Vector3> list) {
        for (int i = 0; i < source.accessor.count; i++) {

            Vector3 v;

            if (!list.isEmpty()) {
                v = new Vector3();
            } else {
                v = list.get(i);
            }

            v.x = array[i*0];
            v.y = array[i*1];
            v.z = array[i*2];

            if (!list.isEmpty()) {
                list.add(v);
            }
        }
    }

    public static InputNode parseInput(Attributes attributes) {
        InputNode input = new InputNode();

        String offset = attributes.getValue(ColladaParser.ATTRIBUTE_OFFSET);

        if (offset != null) {
            input.offset = Integer.parseInt(offset);
        }

        String semantic = attributes.getValue(ColladaParser.ATTRIBUTE_SEMANTIC);
        String source = attributes.getValue(ColladaParser.SOURCE);
        input.semantic = semantic;
        input.source = source;

        return input;
    }

}
