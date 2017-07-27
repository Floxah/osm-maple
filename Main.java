package streettograph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*	Het idee is dat de informatie van een graaf ingelezen wordt uit een .osm bestand (XML) en in een aantal outputlijsten wordt gezet.
	Een graaf heeft in principe knopen, takken en coördinaten. In de XML file staat informatie over nodes en ways.
	Nodes krijgen een id, lat en lon mee. Dit gedeelte heb ik al werkend, als je de file runt is de twee-na-laatste line de lijst knopen en de een-na-laatste line de lijst coördinaten.
	Ways krijgen een id, een aantal nodes (nd) en eventueel wat tags mee. Die tags zijn overbodig. Wat ik wil is dat ik per way alle takken binnenhaal, voorbeeld:
	Way: id=1, nd=3, nd=5, nd=2, nd=8, tag=highway. Wat de output moet zijn: {3, 5}, {5, 2}, {2, 8}
	De id's van de nodes zijn bijv. 45761049, 45761049, 45761948. Voor het programma waarin ik het verder gebruik moeten de id's vertaald worden naar een lijst [1, 2, 3] etc.
	Daarbij moeten dus ook de lijst met takken vertaald worden, dus {45761049, 45761049} wordt {1, 2}.
*/

public class Main {

	public static void main(String args[]) throws FileNotFoundException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		List<String> nodeID = new ArrayList<String>();							//Multidimensional arrays om bij het inlezen data in op te slaan
		List<String> coord = new ArrayList<String>();
		List<String> trail = new ArrayList<String>();
		int wayOld = 0;

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();	
			Document doc = builder.parse("Mennepark.osm");						
			doc.getDocumentElement();
			System.out.println(doc.getDocumentElement().getNodeName());			// Deze regel was als test, output = 'osm'
			
			NodeList idList = doc.getElementsByTagName("node");					// Haal alle elementen 'node' op en store indivduele nodes in element p
			for (int i = 0; i < idList.getLength(); i++) {
				Node p = (Node) idList.item(i);

				if (p.getNodeType() == Node.ELEMENT_NODE) {
					Element identifier = (Element) p;
					String id = identifier.getAttribute("id");
					nodeID.add(id);												// Voeg id toe aan de id Array, moet nog worden vertaald naar [1,2,3] lijst
					String lon = identifier.getAttribute("lon");
					String lat = identifier.getAttribute("lat");
					String co = "[" + lon + "," + lat + "]";
					coord.add(co);												// Voeg coördinaten toe aan coord Array, is verder prima zo
				}
			}

			NodeList wayList = doc.getElementsByTagName("way"); 				// Vanaf hier liep ik te klooien want ik kwam er niet uit
			for (int i = 0; i < wayList.getLength(); i++) {						// Probleem is dat ie niet alle children van way wil ophalen
				Node p = (Node) wayList.item(i);								 
				if (p.getNodeType() == Node.ELEMENT_NODE) {						 
					Element ways = (Element) p;
					if(ways.hasAttribute("nd")) {
						System.out.println(ways.getAttributeNode("nd"));
					} else {
						System.out.println(p.getNodeType());
					}
				}
			}

			NodeList refList = doc.getElementsByTagName("nd");					// Dit stukje is wat achterhaald en had ik eerder gedaan, output ervan is de laatste regel in de console
			for (int i = 0; i < refList.getLength(); i++) {						// Ik haal een lijst met alle nd's op, en vertaal ze meteen van eigen id naar een id die dat andere programma kan handelen
				Node p = (Node) refList.item(i);								// Probleem is dat er dan takken in komen te staan die er niet in horen, bijv:
				if (p.getNodeType() == Node.ELEMENT_NODE) {						// way nd 1, nd 2, nd 3 /way 	way nd 4 nd 5 /way 		geeft ouput 
					Element point = (Element) p;								// {1, 2}, {2, 3}, {3, 4}, {4, 5} i.p.v. {1, 2}, {2, 3}, {4, 5}
					String way = point.getAttribute("ref");
					int nodeRef = nodeID.indexOf(way) + 1;
					String edge = "{" + wayOld + "," + nodeRef + "}";
					if (wayOld != 0) {
						trail.add(edge);
					}
					wayOld = nodeRef;
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(nodeID);												// Output knopen (moet nog vertaald worden naar [1,2,3])
		System.out.println(coord);												// Output coordinaten (goed zo)
		System.out.println(trail);												// Output takken (op dit moment dus met takken die er niet in horen)
	}
}
