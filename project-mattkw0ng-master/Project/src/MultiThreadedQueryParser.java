import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeMap;

/**
 * @author matthew
 * A class to parse a file usig a work queue
 */
public class MultiThreadedQueryParser implements QueryParserInterface{

	/**
	 * Work queue for this class
	 */
	private final int threads;

	/**
	 * collection of all the search results
	 */
	private final TreeMap<String, Collection<InvertedIndex.SearchResult>> allResults;

	/**
	 * The thread safe inverted index
	 */
	private final ThreadSafeInvertedIndex safeIndex;


	/**
	 * Initializes QueryParser
	 * @param index The inverted index to use
	 * @param threads the number of threads to use
	 */
	public MultiThreadedQueryParser(ThreadSafeInvertedIndex index, int threads) {
		this.allResults = new TreeMap<>();
		this.safeIndex = index;
		this.threads = 5; 
		if (threads < 0) {
			throw new RuntimeException("Invalid thread count");
		}
	}


	/**
	 * Method to parse a file and search the queries as well as return a collections of the queries
	 * @param path the path to the query file
	 * @param exact determines whether to conduct exact or partial search
	 * @throws IOException if IO error occurs
	 */
	@Override
	public void parseFile(Path path, boolean exact) throws IOException {
		WorkQueue workQueue = new WorkQueue(threads);
		try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
			String line;
			while((line = reader.readLine()) != null) {
				workQueue.execute(new Task(line, exact));
			}
		}
		
		workQueue.finish();
		workQueue.shutdown();
	}
	
	@Override
	public void parseLine(String line, boolean exact) {
		Collection<String> query = QueryParserInterface.cleanLine(line);

		String joined = TextParser.clean(query.toString());
		
		synchronized (allResults) {
			if(joined.length() == 0 || allResults.containsKey(joined)) {
				return;
			}
		}
		
		Collection<InvertedIndex.SearchResult> result = safeIndex.search(query, exact);
		
		synchronized (allResults) {
			allResults.put(joined, result);
		}
		
	}

	@Override
	public void writeToFile(Path file) throws IOException {
		synchronized (allResults) {
			SimpleJsonWriter.asSearchResults(allResults, file);
		}
	}

	/**
	 * The non-static task class that will update the shared paths and pending members in our task
	 * manager instance.
	 */
	private class Task implements Runnable {
		/** The line to process */
		private final String line;

		/**
		 * The type of search
		 */
		private boolean exact;

		/**
		 * Initializes this task.
		 *
		 * @param line the line to process
		 * @param exact the type of search to do
		 */
		public Task(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			parseLine(line, exact);
		}
	}
}
