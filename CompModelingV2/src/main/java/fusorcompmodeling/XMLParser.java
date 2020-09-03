/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import matheval.MathEval;

/**
 *
 * @author guberti
 */
public class XMLParser {
    String path;
    MathEval eval;
    public XMLParser(String path) {
        this.path = path;
        eval = new MathEval();
        eval.setConstant("Pi", Math.PI);
    }
    
    public List<GridComponent> parseObjects() throws FileNotFoundException {
        List<GridComponent> parts = new ArrayList<>();

        try {

            File file = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("shape");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) nNode;
                        GridComponent e = parseElement(element);
                        parts.add(e);
                    }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File path is invalid!");
            throw e;
        } catch (Exception e) { 
            e.printStackTrace();
        }
        return parts;
    }
    
    GridComponent parseElement(Element element) {
        // All elements have x, y, z, phi, theta, radius, and type
        String type = element.getElementsByTagName("type").item(0).getTextContent();
        VectorB v = new VectorB();
        v.x = Double.parseDouble(element.getElementsByTagName("x").item(0).getTextContent());
        v.y = Double.parseDouble(element.getElementsByTagName("y").item(0).getTextContent());
        v.z = Double.parseDouble(element.getElementsByTagName("z").item(0).getTextContent());
        
        v.phi = parseDouble(element, "phi");
        v.theta = parseDouble(element, "theta");
                
        double radius = parseDouble(element, "radius");
        int charge = parseCharge(element);

        switch (type) {
            case "Cylinder":            
                double height = parseDouble(element, "height");
                return new Cylinder(v, radius, height, charge, false);
            case "TorusSegment":
                double radius2 = parseDouble(element, "radius2");
                double phi2 = parseDouble(element, "phi2");
                double phi3 = parseDouble(element, "phi3");
                return new TorusSegment(v, radius, phi2, phi3, radius2, charge, true);
            case "Sphere":            
                return new Sphere(v, radius, charge);
            default:
                System.out.println("Unknown type " + type);
                return null;
        }
    }
    public double parseDouble(Element element, String tag) {
        String textContent = element.getElementsByTagName(tag).item(0).getTextContent();
        return eval.evaluate(textContent);
    }
    public int parseCharge(Element element) {
        return Integer.parseInt(element.getElementsByTagName("charge").item(0).getTextContent());
    }
}
