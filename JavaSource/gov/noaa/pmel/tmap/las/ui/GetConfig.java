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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.json.XMLTokener;

/** 
 * MyEclipse Struts
 * Creation date: 01-05-2007
 * 
 * XDoclet definition:
 * @struts.action validate="true"
 */
public class GetConfig extends ConfigService {
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
	private static Logger log = LoggerFactory.getLogger(GetConfig.class.getName());
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		
		String query = request.getQueryString();
		if ( query != null ) {
			try{
				query = URLDecoder.decode(query, "UTF-8");
				log.info("START: "+request.getRequestURL()+"?"+query);
			} catch (UnsupportedEncodingException e) {
				// Don't care we missed a log message.
			}			
		} else {
			log.info("START: "+request.getRequestURL());
		}
		
		
		LASConfig lasConfig = (LASConfig)servlet.getServletContext().getAttribute(LASConfigPlugIn.LAS_CONFIG_KEY); 
		String format = request.getParameter("format");
		if ( format == null ) {
			format = "json";
		}

		try {

			
			writeResponse(response, lasConfig, format);
			
						
			// IOExceptiono, JSONException and JDOM Exception are expected.
		} catch (Exception e) {
			sendError(response, "config", format, e.toString());
		}
		
		if ( query != null ) {
			log.info("END:   "+request.getRequestURL()+"?"+query);						
		} else {
			log.info("END:   "+request.getRequestURL());
		}

		return null;
	}
	public void writeResponse(HttpServletResponse response, LASConfig lasConfig, String format) throws IOException, JSONException {
		PrintWriter respout = response.getWriter();
		if (format.equals("xml")) {
			response.setContentType("application/xml");
			respout.print(lasConfig.write());
		} else {
			response.setContentType("application/json");
			JSONObject json_response = new JSONObject(new XMLTokener(lasConfig.write()));
			log.debug(json_response.toString(3));
			json_response.write(respout);
		}
		
	}
}