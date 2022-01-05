import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * An alternative implemention of the {@MessageServlet} class but using the Bootstrap CSS framework.
 */
public class SearchServlet extends HttpServlet {

	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202020;

	/** The logger to use for this servlet. */
	private static Logger log = Log.getRootLogger();

	/** The thread-safe data structure to use for storing messages. */
	private final CopyOnWriteArrayList<String> messages;

	/** Template for starting HTML (including <head> tag). **/
	private final String headTemplate;

	/** Template for ending HTML (including <foot> tag). **/
	private final String footTemplate;

	/** Template for individual message HTML. **/
	private final String textTemplate;
	
	/** Template for individual message HTML. **/
	private final String altTextTemplate;
	
	/** Template for individual message HTML. **/
	private final String historyTextTemplate;

	/** The index to store data **/
	private final InvertedIndex invertedIndex;

	/**
	 * Initializes this search page
	 * @param invertedIndex the index to store data
	 * @throws IOException if unable to read templates
	 */
	public SearchServlet(InvertedIndex invertedIndex) throws IOException {
		super();
		messages = new CopyOnWriteArrayList<>();

		// load templates
		headTemplate = Files.readString(Path.of("html", "default-head.html"), StandardCharsets.UTF_8);
		footTemplate = Files.readString(Path.of("html", "default-foot.html"), StandardCharsets.UTF_8);
		textTemplate = Files.readString(Path.of("html", "default-text.html"), StandardCharsets.UTF_8);
		altTextTemplate = Files.readString(Path.of("html", "default-include-word-count.html"), StandardCharsets.UTF_8);
		historyTextTemplate = Files.readString(Path.of("html", "history-text.html"), StandardCharsets.UTF_8);
		this.invertedIndex = invertedIndex;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		log.info("MessageServlet ID " + this.hashCode() + " handling GET request.");
		HttpSession session = request.getSession(true);

		//handles redirection
		if(request.getParameter("link") != null) {
			String link = request.getParameter("link");

			SearchServlet.addSessionElement(request, "visited", link);

			response.sendRedirect(link);
			return;
		}

		// used to substitute values in our templates
		Map<String, String> values = new HashMap<>();
		values.put("thread", Thread.currentThread().getName());

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

		if (messages.isEmpty()) {
			out.printf("<h3 class=\"info text-secondary\">Try searching!</h3>%n");
		}
		else if (messages.size() == 1) { //unlucky
			out.println(messages.get(0));
		} 
		else if (messages.size() == 2) { //no results
			out.println(messages.get(0));
			out.printf("<p class=\"info text-secondary\">No results.</p>%n");
		}
		else {

			findFavorites(session);
			// could be multiple threads, but the data structure handles synchronization
			for (String message : messages) {
				out.println(message);
			}
		}

		out.println(foot);
		out.flush();
		//messages.clear(); //clear for clean page

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		log.info("MessageServlet ID " + this.hashCode() + " handling POST request.");

		//favorite a link
		if(request.getParameter("favorite") != null) {
			String urlEncoded = URLEncoder.encode(request.getParameter("favorite"), StandardCharsets.UTF_8);
			SearchServlet.addSessionElement(request, "favorite", urlEncoded);
		}
		else if(request.getParameter("search").equals("") && request.getParameter("lucky") == null){
			//Special case: print all websites
			Instant start = Instant.now();
			Map<String, Integer> allSites = invertedIndex.getWordCount();
			// used to substitute values in our templates
			Map<String, String> values = new HashMap<>();
			messages.clear();
			
			Integer number = 1;
			for(String link : allSites.keySet()) {
				values.put("action", "/home");
				values.put("number", number.toString());
				values.put("result", link);
				values.put("count", allSites.get(link).toString());
				
				// generate html from template
				StringSubstitutor replacer = new StringSubstitutor(values);
				String formatted = replacer.replace(altTextTemplate);
				
				messages.add(formatted);
				number ++;
			}
			Duration elapsed = Duration.between(start, Instant.now());
			double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
			String timeNote = "<footnote class=\"title text-secondary blockquote-footer\"> Recieved " + (number-1) + " results in " + seconds + " seconds</footnote>";
			messages.add(0,timeNote);
			String searchMessageTitle = "<h3 class=\"title text-secondary\">Showing all indexed sites...</h3>";
			messages.add(0,searchMessageTitle);
		}
		else if(request.getParameter("search").toLowerCase().equals("-view index") && request.getParameter("lucky") == null){
			//Special case: view all words in index
			Instant start = Instant.now();
			// used to substitute values in our templates
			Map<String, String> values = new HashMap<>();
			messages.clear();
			
			Integer number = 1;
			for(String word : invertedIndex.get()) {
				values.put("number", number.toString());
				values.put("result", word);
				values.put("message", "Locations: " + invertedIndex.get(word).size());
				
				// generate html from template
				StringSubstitutor replacer = new StringSubstitutor(values);
				String formatted = replacer.replace(historyTextTemplate);
				
				messages.add(formatted);
				number ++;
			}
			Duration elapsed = Duration.between(start, Instant.now());
			double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
			String timeNote = "<footnote class=\"title text-secondary blockquote-footer\"> Recieved " + (number-1) + " results in " + seconds + " seconds</footnote>";
			messages.add(0,timeNote);
			String searchMessageTitle = "<h3 class=\"title text-secondary\">Showing all indexed sites...</h3>";
			messages.add(0,searchMessageTitle);
		}
		else {
			String query = request.getParameter("search");
			// avoid xss attacks using apache commons text
			query = StringEscapeUtils.escapeHtml4(query);
			log.info("Search: " + query);
			String queryEncoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

			SearchServlet.addSessionElement(request, "history", queryEncoded);

			messages.clear();

			// used to substitute values in our templates
			Map<String, String> values = new HashMap<>();

			Collection<String> queryCollection = QueryParserInterface.cleanLine(query);
			Integer number = 1;
			boolean partialSearch = request.getParameter("searchType").equals("Partial");

			Instant start = Instant.now();
			Collection<InvertedIndex.SearchResult> searchResults = invertedIndex.search(queryCollection, !partialSearch);
			Duration elapsed = Duration.between(start, Instant.now());
			double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();

			if(request.getParameter("lucky") != null) {
				if(searchResults.isEmpty()) {
					String unluckyMessage = "<h3 class=\"title text-secondary\"> Sorry, no search results were found for \"" + query +"\"</h3>";
					messages.add(unluckyMessage);
				} else {
					String link = searchResults.iterator().next().getWhere();
					SearchServlet.addSessionElement(request, "visited", link);

					response.sendRedirect(link);
					return;
				}
			}
			else {
				for(InvertedIndex.SearchResult result: searchResults) {
					values.put("action", "/home");
					values.put("number", number.toString());
					values.put("result", result.getWhere());

					// generate html from template
					StringSubstitutor replacer = new StringSubstitutor(values);
					String formatted = replacer.replace(textTemplate);

					// keep in mind multiple threads may access at once
					// but we are using a thread-safe data structure here to avoid any issues
					messages.add(formatted);
					number ++;
				}

				String searchMessageTitle = "<h3 class=\"title text-secondary\">"+ request.getParameter("searchType") +" search results for \"" + query + "\"...</h3>";
				String timeNote = "<footnote class=\"title text-secondary blockquote-footer\"> Recieved " + (number-1) + " results in " + seconds + " seconds</footnote>";
				messages.add(0,timeNote);
				messages.add(0,searchMessageTitle);

			}
		}

		//		response.addCookie(cookie);
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
	}

	/* ---------------- Helper Methods ------------------- */

	/**
	 * default method for adding to session
	 * @param request the http request
	 * @param attribute the attribute to add to 
	 * @param value the value to add
	 * @throws ServletException if exception occurs
	 * @throws IOException if exception occurs
	 */
	public static void addSessionElement(HttpServletRequest request, String attribute, String value)
			throws ServletException, IOException {
		addSessionElement(request, attribute, value, false);
	}

	/**
	 * A method to add an element to a session attribute
	 * @param request the http request
	 * @param attribute the attribute to add to 
	 * @param value the value to add
	 * @param remove if the element should be removed
	 * @throws ServletException if exception occurs
	 * @throws IOException if exception occurs
	 */
	@SuppressWarnings("unchecked")
	public static void addSessionElement(HttpServletRequest request, String attribute, String value, boolean remove)
			throws ServletException, IOException {

		HttpSession session = request.getSession(true);
		if(session.getAttribute(attribute) == null) {
			session.setAttribute(attribute, new LinkedHashMap<String,String>());
		} 

		LinkedHashMap<String, String> elements = null;
		try {
			elements = (LinkedHashMap<String, String>) session.getAttribute(attribute);
			if(remove) {
				log.info("removing element");
				elements.remove(value);
				return;
			} else {
				log.info("adding element");
				elements.put(value, getDate());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param session the http session
	 */
	@SuppressWarnings("unchecked")
	private void findFavorites(HttpSession session) {
		if(session == null) {
			return;
		} else if (session.getAttribute("favorite") == null) {
			return;
		}

		LinkedHashMap<String, String> elements = null;
		try {
			elements = (LinkedHashMap<String, String>) session.getAttribute("favorite");
			synchronized(messages) {
				for(String message: messages) {
					for(String element: elements.keySet()) {
						String decoded = URLDecoder.decode(element, StandardCharsets.UTF_8);
						if(message.contains(decoded)) {
							messages.remove(message);
							message = message.replace("class=\"far fa-star favorite\"", "class=\"fas fa-star favorite\"");
							messages.add(2, message);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
