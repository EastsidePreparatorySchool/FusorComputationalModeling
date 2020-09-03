/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author sfreisem-kirov
 */
public class printShapesXML {

    public final static String[] INFO_ORDER = {"x", "y", "z", "phi", "theta"};

    public static String printShapes(List<GridComponent> parts) {
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            XMLOutputFactory xmlOutFact = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = xmlOutFact.createXMLStreamWriter(sw);
            writer.writeStartDocument();
            writer.writeStartElement("root");
            writer.writeStartElement("config");
            writer.writeStartElement("shapes");
            System.out.println(parts);
            for (int k = 0; k < parts.size(); k++) {
                writer.writeStartElement("shape");
                double[] info = new double[5];
                info[0] = parts.get(k).pos.x;
                info[1] = parts.get(k).pos.y;
                info[2] = parts.get(k).pos.z;
                info[3] = parts.get(k).pos.phi;
                info[4] = parts.get(k).pos.theta;
                for (int i = 0; i < info.length; i++) {
                    writer.writeStartElement(INFO_ORDER[i]);
                    writer.writeCharacters("" + info[i]);
                    writer.writeEndElement();
                }

                writer.writeEndElement();
            }

            // write stuff
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            writer.flush();
        } catch (XMLStreamException exc) {
        } finally {
        }
        return sw.toString();
    }
}
