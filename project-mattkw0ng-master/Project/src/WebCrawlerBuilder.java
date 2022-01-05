import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Web crawler class to process url and build inverted index from it
 * @author matthew
 *
 */
public class WebCrawlerBuilder {
	/**
	 * Sets snowball stemmer algorithm
	 */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * The maxixmum number of sites to visit
	 */
	private final int limit;

	/**
	 * Signals when to stop adding work to the queue given the limit
	 */
	private volatile boolean shutdown; 

	/**
	 * A collection of all of the visited sites 
	 */
	private final HashSet<URL> visited;

	/**
	 * @param limit the maximum number of tasks
	 */
	public WebCrawlerBuilder(int limit) {
	
		if(limit < 1) {
			limit = 50;
		} 

		this.limit = limit;
		this.shutdown = false;
		this.visited = new HashSet<>();
	}

	/**
	 * @param index the inverted index
	 * @param url the URL to start with
	 * @param threads the number of threads to use
	 * @throws MalformedURLException if given bad url
	 */
	public void parseUrl(ThreadSafeInvertedIndex index, String url, int threads) throws MalformedURLException {
		WorkQueue workQueue = new WorkQueue(threads);
		URL base = new URL(url);
		visited.add(base);
		workQueue.executeAndTrack(new Task(index, base, workQueue));
		workQueue.finish();
		workQueue.shutdown();
	}

	/**
	 * @author matthew
	 * Runnable task to parse a URL and add additional work to the queue
	 */
	private class Task implements Runnable {
		/** The url to add or list. */
		private final URL url;
		/**
		 * The inverted index to add to
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * WorkQueue implementation
		 */
		private final WorkQueue workQueue;

		/**
		 * Initializes this task.
		 * @param index the index to add to
		 * @param url the URL to add
		 * @param workQueue the workQueue used
		 */
		public Task(ThreadSafeInvertedIndex index, URL url, WorkQueue workQueue) {
			this.index = index;
			this.url = url;
			this.workQueue = workQueue;
		}

		@Override
		public void run() {
			InvertedIndex local = new InvertedIndex();
			Stemmer stemmer = new SnowballStemmer(DEFAULT);
			String html = HtmlFetcher.fetch(url, 3);
			if(!(html == null)) {
				
				html = HtmlCleaner.stripBlockElements(html);
				int pos = 1;
				String stripped = HtmlCleaner.stripHtml(html);
				String[] words = TextParser.parse(stripped);
				
				for(String word : words) {
					word = stemmer.stem(word).toString();
					local.add(word, url.toString(), pos);
					pos++;
				}

				index.addAll(local);
				if(!shutdown) {
					synchronized (visited) {
						for(URL link : LinkParser.listLinks(url, html)) {

							if(visited.size() < limit) {
								if(!visited.contains(link)) {
									workQueue.executeAndTrack(new Task(index, link, workQueue));
									visited.add(link);
//									System.out.println("visited: "+visited.size());
								}
							} else {
//								System.out.println("REACHED LIMIT: no more new tasks - "+visited.size());
								shutdown = true;
								break;
							}
						}
					}
				}
			} 
		}
	}
}
