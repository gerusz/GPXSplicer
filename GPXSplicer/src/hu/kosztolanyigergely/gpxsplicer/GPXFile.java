package hu.kosztolanyigergely.gpxsplicer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gerusz on 2016. 05. 18..
 */
public class GPXFile {

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    public String activityName;
    public String trackName;

    public List<GPXPoint> trackPoints;

    public GPXFile() {
        trackPoints = new ArrayList<>();
    }

    public GPXFile(Document doc) {
        trackPoints = new ArrayList<>();
        try {
            loadFromXmlDocument(doc);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private void loadFromXmlDocument(Document doc) throws XPathExpressionException {
        Node nameNode = (Node) xPath.compile("/gpx/metadata/name").evaluate(doc, XPathConstants.NODE);
        activityName = nameNode.getTextContent();

        Node trackNode = (Node) xPath.compile("/gpx/trk").evaluate(doc, XPathConstants.NODE);
        Node trackNameNode = (Node) xPath.compile("name").evaluate(trackNode, XPathConstants.NODE);
        if (trackNameNode != null) {
            trackName = trackNameNode.getTextContent();
        }
        Node trackSegmentNode = (Node) xPath.compile("trkseg").evaluate(trackNode, XPathConstants.NODE);
        Node trackPointNode = trackSegmentNode.getFirstChild();
        if(trackPoints == null) {
            trackPoints = new LinkedList<>();
        }
        while(null != (trackPointNode = trackPointNode.getNextSibling())) {
            while(trackPointNode != null && trackPointNode.getNodeName() != "trkpt") {
                trackPointNode = trackPointNode.getNextSibling();
            }
            if(trackPointNode == null) {
                break;
            }
            GPXPoint trackPoint = new GPXPoint(trackPointNode);
            trackPoints.add(trackPoint);
        }
    }

    public void saveXmlDocument(String filePath) {
        try {
            File gpxFile = new File(filePath);
            gpxFile.delete();
            gpxFile.createNewFile();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();
            Element rootElement = doc.createElement("gpx");
            rootElement.setAttribute("version", "1.1");
            rootElement.setAttribute("creator", "GPX Splicer Java 0.1");
            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1/gpx.xsd");
            rootElement.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
            rootElement.setAttribute("xmlns:gpxtpx", "http://www.garmin.com/xmlschemas/TrackPointExtension/v1");
            doc.appendChild(rootElement);

            Element metadataNode = doc.createElement("metadata");
            rootElement.appendChild(metadataNode);
            Element nameNode = doc.createElement("name");
            nameNode.setTextContent(activityName);
            metadataNode.appendChild(nameNode);

            Element trackNode = doc.createElement("trk");
            rootElement.appendChild(trackNode);
            Element trackNameNode = doc.createElement("name");
            trackNameNode.setTextContent(trackName);
            trackNode.appendChild(trackNameNode);

            Element trackSegmentNode = doc.createElement("trkseg");
            trackNode.appendChild(trackSegmentNode);
            for(GPXPoint point : trackPoints) {
                point.writeToElement(trackSegmentNode);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(gpxFile);

            transformer.transform(source, result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GPXFile(String filePath) {
        try {
            File gpxFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(gpxFile);
            doc.getDocumentElement().normalize();

            loadFromXmlDocument(doc);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}