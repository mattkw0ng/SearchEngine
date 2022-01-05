import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * An alternative implemention of the {@MessageServlet} class but using the Bootstrap CSS framework.
 */
public class FavoriteServlet extends HttpServlet {

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

//	/** Template for individual message HTML. **/
//	private final String searchText;
	
	/** Template for individual message HTML. **/
	private final String linkText;



	/**
	 * Initializes this search page

	 * @throws IOException if unable to read templates
	 */
	public FavoriteServlet() throws IOException {
		super();

		// load templates
		headTemplate = Files.readString(Path.of("html", "favorites-head.html"), StandardCharsets.UTF_8);
		footTemplate = Files.readString(Path.of("html", "default-foot.html"), StandardCharsets.UTF_8);
//		searchText = Files.readString(Path.of("html", "history-text.html"), StandardCharsets.UTF_8);
		linkText = Files.readString(Path.of("html", "favorites-text.html"), StandardCharsets.UTF_8);

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
		
		Map<String, String> favorites = HistoryServlet.getSessionAttribute(request, "favorite");
		
		values.clear();
		
		if (favorites.isEmpty()) {
			out.printf("    <p class=\"info text-secondary\">No favorites yet, <a href=\"/home\">try searching</a> first and the click on the <i class=\"far fa-star favorite\"></i>!</p>%n");
		} else {
			out.printf("    <p class=\"info text-secondary\">Favorites: </p>%n");
			log.info("searches: "+favorites.size());
			Integer i = 1;
			for (String result : favorites.keySet()) {
				values.put("action", "/favorites");
				values.put("number", i.toString());
				values.put("result", URLDecoder.decode(result, StandardCharsets.UTF_8));
				String formatted = replacer.replace(linkText);
				out.println(formatted);
				i++;
			}
		}

		out.println(foot);
		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	@Override
	/**
	 * edit favorite websites
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		log.info("MessageServlet ID " + this.hashCode() + " handling POST request.");
		
		String remove = request.getParameter("favorite");
		SearchServlet.addSessionElement(request, "favorite", URLEncoder.encode(remove,StandardCharsets.UTF_8), true);

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

