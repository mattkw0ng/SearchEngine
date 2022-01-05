import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * An alternative implemention of the {@MessageServlet} class but using the Bootstrap CSS framework.
 */
public class HistoryServlet extends HttpServlet {

	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202020;

	/** The title to use for this webpage. */
	private static final String TITLE = "Fancy Messages";

	/** The logger to use for this servlet. */
	private static Logger log = Log.getRootLogger();

	/** Template for starting HTML (including <head> tag). **/
	private final String headTemplate;

	/** Template for ending HTML (including <foot> tag). **/
	private final String footTemplate;

	/** Template for individual message HTML. **/
	private final String searchText;
	
	/** Template for individual message HTML. **/
	private final String linkText;



	/**
	 * Initializes this search page

	 * @throws IOException if unable to read templates
	 */
	public HistoryServlet() throws IOException {
		super();

		// load templates
		headTemplate = Files.readString(Path.of("html", "history-head.html"), StandardCharsets.UTF_8);
		footTemplate = Files.readString(Path.of("html", "default-foot.html"), StandardCharsets.UTF_8);
		searchText = Files.readString(Path.of("html", "history-text.html"), StandardCharsets.UTF_8);
		linkText = Files.readString(Path.of("html", "default-text-disabled.html"), StandardCharsets.UTF_8);

	}
	
	@SuppressWarnings({ "javadoc", "unchecked" })
	public static Map<String, String> getSessionAttribute(HttpServletRequest request, String attribute) {
		HttpSession session = request.getSession(true);
		if(session.getAttribute(attribute) == null) {
			session.setAttribute(attribute, new LinkedHashMap<String, String>());
		} 

		LinkedHashMap<String, String> elements = null;
		try {
			elements = (LinkedHashMap<String, String>) session.getAttribute(attribute);
			if(elements != null) {
				return elements;
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
		//else return empty list to avoid null pointer exceptions
		return Collections.emptyMap();
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		log.info("MessageServlet ID " + this.hashCode() + " handling GET request.");

		// used to substitute values in our templates
		Map<String, String> values = new HashMap<>();
		values.put("title", TITLE);
		values.put("thread", Thread.currentThread().getName());
		values.put("updated", getDate());

		// setup form
		values.put("method", "POST");
		values.put("action", request.getServletPath());

		// generate html from template
		StringSubstitutor replacer = new StringSubstitutor(values);
		String head = replacer.replace(headTemplate);
		String foot = replacer.replace(footTemplate);

		// output generated html
		PrintWriter out = response.getWriter();
		out.println(head);
		
//		HttpSession session = request.getSession(false);
		Map<String, String> history = getSessionAttribute(request, "history");
		Map<String, String> visited = getSessionAttribute(request, "visited");
		boolean bothEmpty = history.isEmpty() && visited.isEmpty();
		
		values.clear();

		if (bothEmpty) {
			out.printf("    <p class=\"info text-secondary\">Nothing yet, <a href=\"/home\">try searching!</a></p>%n");
		} else {
			out.printf("    <p class=\"info text-secondary\">Searches: </p>%n");
			log.info("searches: "+history.size());
			Integer i = 1;
			for (String result : history.keySet()) {
				values.put("message", "Accessed: " + history.get(result));
				values.put("number", i.toString());
				values.put("result", URLDecoder.decode(result, StandardCharsets.UTF_8));
				String formatted = replacer.replace(searchText);
				out.println(formatted);
				i++;
			}
			if(visited != null) {
				out.printf("    <p class=\"info text-secondary\">Visits: </p>%n");
				i = 1;
				for (String result : visited.keySet()) {
					values.put("date", visited.get(result));
					values.put("number", i.toString());
					values.put("result", URLDecoder.decode(result, StandardCharsets.UTF_8));
					String formatted = replacer.replace(linkText);
					out.println(formatted);
					i++;
				}
			}
		}

		out.println(foot);
		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
	}
	

	@Override
	/** 
	 * Clears search history
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		log.info("MessageServlet ID " + this.hashCode() + " handling POST request.");
		
		Map<String, String> history = getSessionAttribute(request, "history");
		Map<String, String> visited = getSessionAttribute(request, "visited");
		
		if(!history.isEmpty()) {
			history.clear();
		}
		if(!visited.isEmpty()) {
			visited.clear();
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
	}

	/**
	 * Returns the date and time in a long format. For example: "12:00 am on Saturday, January 01
	 * 2000".
	 *
	 * @return current date and time
	 */
	private static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}
