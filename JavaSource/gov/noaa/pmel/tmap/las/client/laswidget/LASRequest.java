package gov.noaa.pmel.tmap.las.client.laswidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class LASRequest {
    Document document;
	public LASRequest() {
		super();
	}
	public LASRequest(String xml) {
		document = XMLParser.parse(xml);
	}
	/**
	 * Replaces the top level <link match=...> element in the LASRequest.
	 * @param operation
	 * @param id
	 */
	public void setOperation(String operation, String style) {
		if ( style.contains("7") ) {
			NodeList l = document.getDocumentElement().getElementsByTagName("link");
			Element op = null;
			if ( l != null && l.getLength() > 0 ) {
				op = (Element) l.item(0);
			}
			if ( op != null ) {
				op.setAttribute("match", "/lasdata/operations/operation[@ID='"+operation+"']");
			} else {
				op = document.createElement("link");
				document.getDocumentElement().appendChild(op);
				op.setAttribute("match", "/lasdata/operations/operation[@ID='"+operation+"']");
			}
		}
	}
	/**
	 * Replaces the value of a Property element in the named PropertyGroup of the LASRequest.
	 * If the property is not found a new Property element will be created.
	 * @param group
	 * @param property
	 * @param value
	 */
	public void setProperty(String group_name, String property_name, String property_value) {
		NodeList l = document.getDocumentElement().getElementsByTagName("properties");
		Element properties;
		if ( l != null && l.getLength() > 0 ) {
			properties = (Element) l.item(0);
		} else {
			properties = document.createElement("properties");
			document.getDocumentElement().appendChild(properties);
		}
		Element group = null;
		NodeList groups = properties.getChildNodes();
		for ( int i = 0; i < groups.getLength(); i++ ) {
			Node child = groups.item(i);
			if ( child instanceof Element ) {
				Element g = (Element) child;
				if ( g.getNodeName().equals(group_name) ) {
					group = g;
				}
			}
		}
		if ( group != null ) {
			NodeList props = group.getChildNodes();
			Element prop = null;
			for ( int i = 0; i < props.getLength(); i++ ) {
				Node child = props.item(i);
				if ( child instanceof Element ) {
					Element p = (Element) child;
					if ( p.getNodeName().equals(property_name)) {
						prop = p;
						Text text = document.createTextNode(property_value);
						p.replaceChild(text, p.getFirstChild());
					}
				}
			}
			// Property not found.   Create it.
			if ( prop == null ) {
				prop = document.createElement(property_name);
				Text text = document.createTextNode(property_value);
				prop.appendChild(text);
				group.appendChild(prop);
			}
		} else {
			group = document.createElement(group_name);
			Element property = document.createElement(property_name);
			Text text = document.createTextNode(property_value);
			property.appendChild(text);
			group.appendChild(property);
			properties.appendChild(group);
		}
	}
	  /**
     * Sets a range using a range element to <region> section of the LASRequest.
     * If no Region with this region_ID is found, one will be created.
     * If a Range along the desired axis already exists it will be replaced.
     * @param axis
     * @param lo
     * @param hi
     * @param region
     */
	public void setRange(String axis_type, String lo, String hi, int index) {
		NodeList regions = document.getElementsByTagName("region");
		if ( index >= 0 && index < regions.getLength() ) {
			
			Element region = (Element) regions.item(index);
			
			Element axis = null;
			
			NodeList ranges = region.getElementsByTagName("range");
			for ( int i = 0; i < ranges.getLength(); i++ ) {
				Element range = (Element) ranges.item(i);
				String type = range.getAttribute("type");
				if ( type.equals(axis_type) ) {
					axis = range;
				}
			}

			NodeList points = region.getElementsByTagName("point");
			for ( int i = 0; i < points.getLength(); i++ ) {
				Element point = (Element) points.item(i);
				String type = point.getAttribute("type");
				if ( type.equals(axis_type) ) {
					axis = point;
				}
			}
			
			if ( axis != null ) {
				region.removeChild(axis);
			}
			
			if ( lo.equals(hi) ) {
				Element point = document.createElement("point");
				point.setAttribute("type", axis_type);
				point.setAttribute("v", lo);
				region.appendChild(point);
			} else {
				Element range = document.createElement("range");
				range.setAttribute("type", axis_type);
				range.setAttribute("low", lo);
				range.setAttribute("high", hi);
				region.appendChild(range);
			}

		} else {
			Element region = document.createElement("region");
			if ( lo.equals(hi) ) {
				Element point = document.createElement("point");
				point.setAttribute("type", axis_type);
				point.setAttribute("v", lo);
				region.appendChild(point);
			} else {
				Element range = document.createElement("range");
				range.setAttribute("type", axis_type);
				range.setAttribute("low", lo);
				range.setAttribute("high", hi);
				region.appendChild(range);
			}
			Element args = (Element) document.getDocumentElement().getElementsByTagName("args");
			args.appendChild(region);
		}
	}
    /**
     * Adds a <link match=.../> element to the <args> section of the LASRequest.
     * This will add a new dataset-variable pair to the LASRequest.
     * Note that the order in which variables appear in an LASRequest is important as differencing 
     * products (as of 2007-10-24) always subtract the second variable from the first.
     * @param dsID
     * @param varID
     * @param region - the region to which this variable belongs.
     */
    public void addVariable(String dsID, String varID, int region_index) {
    	Element link = link(dsID, varID);
    	NodeList l = document.getDocumentElement().getElementsByTagName("args");
    	Element args = (Element) l.item(0);
    	NodeList regions = document.getElementsByTagName("region");
		if ( region_index >= 0 && region_index < regions.getLength() ) {
			Element region = (Element) regions.item(region_index);
			args.insertBefore(link, region);
		} else {
			args.appendChild(link);
		}
    }
    /**
	 * Adds a Constraint element of type 'variable' to the <args> section of the LASRequest.
	 * Constraints of type 'variable' contain dataset-variable xpath information as the left hand side.
	 * @param dataset
	 * @param variable
	 * @param op
	 * @param value
	 */
	public void addVariableConstraint(String dataset, String variable, String op, String value, String cid) {
		Element constraint = document.createElement("constraint");
		constraint.setAttribute("type", "variable");
		if ( cid != null ) {
			constraint.setAttribute("id", cid);
		}
		constraint.setAttribute("op", op);
		Element link = link(dataset, variable);
		Element v = document.createElement("v");
		Text text = document.createTextNode(value);
		v.appendChild(text);
		constraint.appendChild(link);
		constraint.appendChild(v);
		Element args = getArgsElement();
		args.appendChild(constraint);
	}
	/**
	 * Removes all Constraint elements defined in the <args> section of the LASRequest.
     * No 'data options' will be applied before creating the product.
	 */
	public void removeConstraints() {
		Element args = getArgsElement();
		NodeList constraints = args.getElementsByTagName("constraint");
		List<Node> remove = new ArrayList<Node>();
		for(int i = 0; i < constraints.getLength(); i++ ) {
			Node constraint = constraints.item(i);
			remove.add(constraint);
		}
		for ( int i = 0; i < remove.size(); i++ ) {
			args.removeChild(remove.get(i));
		}
	}
	/**
	 * Removes all <link match=...> elements defined in the <args> section of the LASRequest.
	 * Clears out all dataset-variable pairs defined in the LASRequest.
	 */
	public void removeVariables() {
		Element args = getArgsElement();
		NodeList variables = args.getElementsByTagName("link");
		List<Node> remove = new ArrayList<Node>();
		for (int i = 0; i < variables.getLength(); i++) {
			Node var = variables.item(i);
			if ( var.getParentNode().getNodeName().equals("args") ) {
			    remove.add(var);
			}
		}
		for (int i = 0; i < remove.size(); i++ ) {
			args.removeChild(remove.get(i));
		}
	}
	public String getVariable(int index) {
		Element args = getArgsElement();
		NodeList variables = args.getElementsByTagName("link");
	    for (int i = 0; i < variables.getLength(); i++) {
		    Element var = (Element) variables.item(i);
		    // ByTagName gets children, grandchildren and below.  Check that it is a child of "args".
			if ( var.getParentNode().getNodeName().equals("args") ) {
				if ( i == index ) {
					return getVariableId(var.getAttribute("match"));
				}				
			}	
		}
		return null;
	}
	public String getDataset(int index) {
		Element args = getArgsElement();
		NodeList variables = args.getElementsByTagName("link");
		Element var = (Element) variables.item(index);
		if ( var != null ) {
			return getDatasetId(var.getAttribute("match"));
		} else {
			return null;
		}
	}
	public List<Map<String, String>> getVariableConstraints() {
		List<Map<String, String>> vcs = new ArrayList<Map<String, String>>();
		NodeList l = document.getDocumentElement().getElementsByTagName("args");
		Element args = (Element) l.item(0);
		NodeList constraints = args.getElementsByTagName("constraint");
		for (int i = 0; i < constraints.getLength(); i++) {
			Map<String, String> c = new HashMap<String, String>();
			Element constraint = (Element) constraints.item(i);
			c.put("op", constraint.getAttribute("op"));
			c.put("type", constraint.getAttribute("type"));
			String id = constraint.getAttribute("id");
			if ( id != null ) {
				c.put("id", id);
			}
			
			
			NodeList vl = constraint.getElementsByTagName("v");
			Element v = (Element) vl.item(0);
			Text text = (Text) v.getFirstChild();
			String value = text.getData();
			c.put("value", value);
			
			NodeList ll = constraint.getElementsByTagName("link");
			Element link = (Element) ll.item(0);
			String match = link.getAttribute("match");
			
			c.put("dsID", getDatasetId(match));
			c.put("varID", getVariableId(match));
			
			vcs.add(c);
		}
		return vcs;
	}
	public String getRangeHi(String axis_type, int index) {
		Element args = getArgsElement();
		NodeList regions = args.getElementsByTagName("region");
		if ( regions.getLength() > index ) {
			Element region = (Element) regions.item(index);
			NodeList ranges = region.getElementsByTagName("range");
			for(int i = 0; i < ranges.getLength(); i++ ) {
				Element range = (Element) ranges.item(i);
				if ( range.getAttribute("type").equals(axis_type) ) {
					return range.getAttribute("high");
				}
			}
			NodeList points = region.getElementsByTagName("point");
			for ( int i = 0; i < points.getLength(); i++ ) {
				Element point = (Element) points.item(i);
				if ( point.getAttribute("type").equals(axis_type) ) {
					return point.getAttribute("v");
				}
			}
		} 
		return null;
	}
	public String getRangeLo(String axis_type, int index) {
		Element args = getArgsElement();
		NodeList regions = args.getElementsByTagName("region");
		if ( regions.getLength() > index ) {
			Element region = (Element) regions.item(index);
			NodeList ranges = region.getElementsByTagName("range");
			for(int i = 0; i < ranges.getLength(); i++ ) {
				Element range = (Element) ranges.item(i);
				if ( range.getAttribute("type").equals(axis_type) ) {
					return range.getAttribute("low");
				}
			}
			NodeList points = region.getElementsByTagName("point");
			for ( int i = 0; i < points.getLength(); i++ ) {
				Element point = (Element) points.item(i);
				if ( point.getAttribute("type").equals(axis_type) ) {
					return point.getAttribute("v");
				}
			}
		} 
		return null;
	}
	public String toString() {
		String xml = document.toString();
		xml = xml.replaceAll("\n", "");
		// Get rid of the doc declaration
		xml = xml.substring(xml.indexOf("<lasRequest"), xml.length());
		return xml;
	}
	private Element getArgsElement() {
		NodeList l = document.getDocumentElement().getElementsByTagName("args");
		return (Element) l.item(0);
	}
	private Element link(String dsid, String varid) {
		Element link = document.createElement("link");
    	link.setAttribute("match", "/lasdata/datasets/"+dsid+"/variables/"+varid);
    	return link;
	}
	private String getVariableId(String match) {
		return match.substring(match.lastIndexOf("/")+1, match.length());
	}
	private String getDatasetId(String match) {
		String ds = match.substring(match.indexOf("/lasdata/datasets/"), match.indexOf("/variables/"));
		return ds.substring(ds.lastIndexOf("/")+1, ds.length());
	}
}