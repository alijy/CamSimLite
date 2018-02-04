import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class Settings {
	
	Field field;
	int zoomCount = 0;
	Double[] zooms = new Double[20];
	ArrayList<Camera> cameras = new ArrayList<Camera>();
	ArrayList<Object> objects = new ArrayList<Object>();
	RandomNumberGenerator rand;

	
	public Settings(String XmlFilePath, RandomNumberGenerator rand){		
		this.rand = rand;		
		try {			
//			System.out.println("Reading XML file ....");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(XmlFilePath);
			doc.getDocumentElement().normalize();

			getField(doc);
			getZooms(doc);
			getCameras(doc);			
			getObjects(doc);
			getEvents(doc);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Reading field details
	 */
	private void getField(Document doc) {
		NodeList nList = doc.getElementsByTagName("simulation");
		Node nNode = nList.item(0);

		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element eField = (Element) nNode;
			field = new Field(Double.parseDouble(eField.getAttribute("min_x")),
					Double.parseDouble(eField.getAttribute("min_y")),
					Double.parseDouble(eField.getAttribute("max_x")),
					Double.parseDouble(eField.getAttribute("max_y")));
		}		
	}
	
	
	/**
	 * Reading cameras' zoom levels from input xml file
	 */
	private void getZooms(Document doc) {		
		NodeList nList = doc.getElementsByTagName("zoom");			
		String nZoom = nList.item(0).getTextContent();							
		Scanner scanner = new Scanner(nZoom);
		scanner.useDelimiter(" ");

		while (scanner.hasNextDouble()) {
			zooms[zoomCount] = scanner.nextDouble();
			zoomCount++;
		}
		
		scanner.close();		
	}
	
	
	/**
	 * Reading camera settings from input xml file
	 */
	private void getCameras(Document doc) {
		NodeList nList = doc.getElementsByTagName("camera");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eCamera = (Element) nNode;
				Camera cam = new Camera(eCamera.getAttribute("name"),
						Double.parseDouble(eCamera.getAttribute("x")),
						Double.parseDouble(eCamera.getAttribute("y")),
						Arrays.copyOfRange(this.zooms, 0, zoomCount));

				cameras.add(cam);
			}
		}
	}

	
	/**
	 * Reading object settings from input xml file
	 */
	private void getObjects(Document doc) {
		NodeList nList = doc.getElementsByTagName("object");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eObject = (Element) nNode;
				Object obj = new Object(eObject.getAttribute("features"),
						Double.parseDouble(eObject.getAttribute("x")),
						Double.parseDouble(eObject.getAttribute("y")),
						Double.parseDouble(eObject.getAttribute("heading")),
						Double.parseDouble(eObject.getAttribute("speed")),
						field, rand);
				
				objects.add(obj);
			}
		}
	}
	
	
	/**
	 * Reading (event)object settings with waypoints from input xml file
	 */
	private void getEvents(Document doc) {
		NodeList nList = doc.getElementsByTagName("event");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eEvent = (Element) nNode;
				
				if ((eEvent.getAttribute("participant").equals("object")) &&
						(eEvent.getAttribute("event").equals("add"))) {

					NodeList sList = eEvent.getElementsByTagName("waypoint");
			      	ArrayList<Point2D> waypoints = new ArrayList<Point2D>();
					for (int j=0 ; j<sList.getLength() ; j++) {
						Node sNode = sList.item(j);
						if (sNode.getNodeType() == Node.ELEMENT_NODE) {
							Element sEvent = (Element) sNode;
					      	waypoints.add(new Point2D.Double(Double.parseDouble(sEvent.getAttribute("x")), 
					      			Double.parseDouble(sEvent.getAttribute("y"))));
						}
					}
	
					Object obj = new Object(eEvent.getAttribute("name"),
							Integer.parseInt(eEvent.getAttribute("timestep")),
							Double.parseDouble(eEvent.getAttribute("speed")),
							waypoints, field);
					
					objects.add(obj);
				}
			}
		}
	}
	
	
	
}
