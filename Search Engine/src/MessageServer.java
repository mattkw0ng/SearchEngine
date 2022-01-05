import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Demonstrates how to create a simple message board using Jetty and servlets, as well as how to
 * initialize servlets when you need to call its constructor.
 */
public class MessageServer {

	/**
	 * Sets up a Jetty server with different servlet instances.
	 *
	 * @param index the index to build website on
	 * @param port the port to host server on
	 * @throws Exception if unable to start and run server
	 */
	public static void runServer(InvertedIndex index, int port) throws Exception {
		System.out.println("starting server");
		// type of handler that supports sessions
	    ServletContextHandler servletContext = null;

	    // turn on sessions and set context
	    servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
	    servletContext.setContextPath("/");
	    servletContext.addServlet(new ServletHolder(new SearchServlet(index)), "/home");
	    servletContext.addServlet(new ServletHolder(new HistoryServlet()), "/history");
	    servletContext.addServlet(new ServletHolder(new FavoriteServlet()), "/favorites");
	    
		
		Server server = new Server(port);

		//ServletHandler handler = new ServletHandler();

		// must use servlet holds when need to call a constructor`
		//handler.addServletWithMapping(new ServletHolder(new SearchServlet(index)), "/home");
		//handler.addServletWithMapping(new ServletHolder(new HistoryServlet()), "/history");
		
		// setup handler order
	    //HandlerList handlers = new HandlerList();
	    //handlers.setHandlers(new Handler[] {handler, servletContext});

		server.setHandler(servletContext);
		server.start();
		server.join();
	}
}
