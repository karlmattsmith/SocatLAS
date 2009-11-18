/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package gov.noaa.pmel.tmap.las.ui;

import gov.noaa.pmel.tmap.las.jdom.LASConfig;
import gov.noaa.pmel.tmap.las.product.server.LASConfigPlugIn;
import gov.noaa.pmel.tmap.las.ui.json.JSONUtil;
import gov.noaa.pmel.tmap.las.util.Category;
import gov.noaa.pmel.tmap.las.util.Constants;
import gov.noaa.pmel.tmap.las.util.ContainerComparator;
import gov.noaa.pmel.tmap.las.util.Dataset;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/** 
 * MyEclipse Struts
 * Creation date: 01-05-2007
 * 
 * XDoclet definition:
 * @struts.action validate="true"
 */
public class GetFullCategories extends ConfigService {
	/*
	 * Generated Methods
	 */

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	private static Logger log = LogManager.getLogger(GetFullCategories.class.getName());
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		String format = request.getParameter("format");
		if ( format == null ) {
			format = "json";
		}
		String catid = request.getParameter("catid");
		log.info("Starting: getCategories.do?catid="+catid+"&format="+format);	
		
		
		ArrayList<Category> categories = new ArrayList<Category>();
		try {
			// Handle the case where the request is for local categories on a confluence server (the made up parent category is the same
			// as null).
			if ( catid != null && catid.contains(Constants.NAME_SPACE_SPARATOR) ) {
			    if ( catid.equals(lasConfig.getTopLevelCategoryID())) catid=null;
			}
			categories = lasConfig.getFullCategories(catid);


			// Check to see if there's anything good in the top level category.
			Category cat = categories.get(0);
			if (!cat.hasVariableChildren() && !cat.hasCategoryChildren() ) {
				sendError(response, "categories", format, "No categories found.");
			} else {

			Collections.sort(categories, new ContainerComparator("name"));
			
			writeResponse(response, categories, format);
			}
						
			// IOExceptiono, JSONException and JDOM Exception are expected.
		} catch (Exception e) {
			sendError(response, "categories", format, e.toString());
		}     
		log.info("Finished: getCategories.do?catid="+catid+"&format="+format);
		
		return null;
	}
	public void writeResponse(HttpServletResponse response, ArrayList<Category> categories, String format) throws IOException, JSONException {
		PrintWriter respout = response.getWriter();
		if (format.equals("xml")) {
			response.setContentType("application/xml");
			respout.print(Util.toXML(categories, "categories"));
		} else {
			response.setContentType("application/json");
			JSONObject json_response = Util.toJSON(categories, "categories");
			log.debug(json_response.toString(3));
			json_response.write(respout);
		}
		
	}
}