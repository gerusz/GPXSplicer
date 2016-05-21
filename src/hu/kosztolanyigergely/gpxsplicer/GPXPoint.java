package hu.kosztolanyigergely.gpxsplicer;

import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Date;

/**
 * Created by gerusz on 2016. 05. 18..
 */
public class GPXPoint {

    static NumberFormat format = NumberFormat.getInstance(Locale.UK);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat dateFormatWithZome = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    private static final SimpleDateFormat msdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final SimpleDateFormat msdateFormatWithZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static XPathExpression heartRateXpath;
    private static final int timeOffsetInMinutes;
    private static final Calendar calendar;

    static {

        try {
            heartRateXpath = XPathFactory.newInstance().newXPath().compile("TrackPointExtension/hr");
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
            heartRateXpath = null;
        }
        format.setMaximumFractionDigits(50);

        calendar = Calendar.getInstance();
        timeOffsetInMinutes = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000);
    }

    public double latitude;
    public double longitude;
    public Double elevation;
    public Integer heartRate;
    public Date time;

    public void writeToElement(Element trackSegmentElement) {
        Element node = trackSegmentElement.getOwnerDocument().createElement("trkpt");
        node.setAttribute("lat", format.format(latitude));
        node.setAttribute("lon", format.format(longitude));
        Element timeNode = trackSegmentElement.getOwnerDocument().createElement("time");
        timeNode.setTextContent(outputFormat.format(time));
        node.appendChild(timeNode);

        if(elevation != null) {
            Element elevationNode = trackSegmentElement.getOwnerDocument().createElement("ele");
            elevationNode.setTextContent(format.format(elevation));
            node.appendChild(elevationNode);
        }

        if(heartRate != null) {
            Element extensionsNode = trackSegmentElement.getOwnerDocument().createElement("extensions");
            node.appendChild(extensionsNode);

            Element tpextNode = trackSegmentElement.getOwnerDocument().createElementNS("http://www.garmin.com/xmlschemas/TrackPointExtension/v1", "gpxtpx:TrackPointExtension");
            extensionsNode.appendChild(tpextNode);

            Element hrNode = trackSegmentElement.getOwnerDocument().createElementNS("http://www.garmin.com/xmlschemas/TrackPointExtension/v1", "gpxtpx:hr");
            hrNode.setTextContent(Integer.toString(heartRate));
            tpextNode.appendChild(hrNode);
        }

        trackSegmentElement.appendChild(node);
    }

    public GPXPoint(Node node) {
        try {
            latitude = format.parse(node.getAttributes().getNamedItem("lat").getTextContent()).doubleValue();
            longitude = format.parse(node.getAttributes().getNamedItem("lon").getTextContent()).doubleValue();
            if (node.hasChildNodes()) {
                Node child = node.getFirstChild();
                while(null != (child = child.getNextSibling())) {
                    if (child.getNodeName().equalsIgnoreCase("time")) {
                        String timeString = child.getTextContent();
                        if (timeString.length() == 20) {
                            time = dateFormat.parse(timeString);
                            calendar.setTime(time);
                            calendar.add(Calendar.MINUTE, timeOffsetInMinutes);
                            time = calendar.getTime();
                        } else if (timeString.length() == 24) {
                            time = msdateFormat.parse(timeString);
                            calendar.setTime(time);
                            calendar.add(Calendar.MINUTE, timeOffsetInMinutes);
                            time = calendar.getTime();
                        } else if (timeString.length() == 25) {
                            time = dateFormatWithZome.parse(timeString);
                        } else if (timeString.length() == 29) {
                            time = msdateFormatWithZone.parse(timeString);
                        } else {
                            time = new Date(Long.parseLong(timeString));
                        }
                    } else if (child.getNodeName().equalsIgnoreCase("ele")) {
                        elevation = format.parse(child.getTextContent()).doubleValue();
                    } else if (child.getNodeName().equalsIgnoreCase("extensions")) {
                        Node hrNode = (Node) heartRateXpath.evaluate(child, XPathConstants.NODE);
                        heartRate = Integer.parseInt(hrNode.getTextContent());
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public GPXPoint() {
    }
}
