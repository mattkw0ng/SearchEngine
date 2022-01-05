import java.time.Duration;
import java.time.Instant;
import java.nio.file.Path;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;

/**
 * Class responsible for running this project based on the provided command-line arguments.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2020
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line arguments. This includes
	 * (but is not limited to) how to build or search an inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */

	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		ArgumentParser parser = new ArgumentParser(args); //parser for argument parsing

		ThreadSafeInvertedIndex threadSafe = null;
		InvertedIndex index;
		QueryParserInterface qParser = null;
		int threads = 5;

		if (parser.hasFlag("-threads") || parser.hasFlag("-url")) {
			threadSafe = new ThreadSafeInvertedIndex();
			index = threadSafe;
			try {
				threads = parser.getPositiveInteger("-threads", 5);
			} catch (NumberFormatException e){
				System.out.println("Error: invalid thread count");
			}
			qParser = new MultiThreadedQueryParser(threadSafe, threads);
		}
		else {
			index = new InvertedIndex();
			qParser = new QueryParser(index);
		}

		//-- project 4 url processing --
		if(parser.hasFlag("-url")) {
			String seed = parser.getString("-url");

			int limit = 50;
			if(parser.hasFlag("-limit")) {
				limit = Integer.parseInt(parser.getString("-limit"));
			}	

			WebCrawlerBuilder webCrawler = new WebCrawlerBuilder(limit);
			try {
				webCrawler.parseUrl(threadSafe, seed, threads);
			} catch (MalformedURLException e) {
				System.out.println("Error: Invalid URL");
			}
		}

		if(parser.hasFlag("-path")) {
			Path path = parser.getPath("-path");
			try {
				if (threadSafe != null) {
					try {
						MultiThreadedIndexBuilder.build(path ,threadSafe , threads);
					} catch (IOException e) {
						System.out.println("Error: thread interruption occured when parsing file");
					}
				}
				else {
					InvertedIndexBuilder.build(path,index);
				}
			}catch(IOException e) {
				System.out.println("Unable to build from path: " + path.toString());
			} catch(NullPointerException e) {
				System.out.println("Error: No path was given");
			}
		}

		if(parser.hasFlag("-index")) {
			System.out.println("printing to file");
			try {
				Path path = parser.getPath("-index", Path.of("index.json"));
				index.writeToFile(path.toString());
			}catch (InvalidPathException e) {
				System.out.println("Invalid path: " + parser.getString("-index","index.json"));
			}catch (IOException e) {
				System.out.println("Unable to print to path: " + parser.getString("-index","index.json"));
			}

		}

		//project 2: additional flags

		if(parser.hasFlag("-counts")) {
			Path wordCountFile = parser.getPath("-counts", Path.of("counts.json"));
			try {
				index.writeWordCount(wordCountFile);
			} catch (IOException e) {
				System.out.println("Error: Could not print to file: " + wordCountFile.toString());
			}
		}

		if(parser.hasFlag("-query")) {
			Path queryFile = parser.getPath("-query");
			try {
				qParser.parseFile(queryFile, parser.hasFlag("-exact"));
			} catch (IOException e) {
				System.out.println("Error: could not parse file - " + queryFile);
			} catch (NullPointerException e) {
				System.out.println("Error: no query file provided / no path to build on");
			}
		}

		if(parser.hasFlag("-results")) {
			Path resultFile = parser.getPath("-results",Path.of("results.json"));
			try {
				qParser.writeToFile(resultFile);
			} catch (IOException e) {
				System.out.println("Error: could not write to file - " + resultFile.toString());
			} catch (NullPointerException e) {
				System.out.println("Error: no query file provided / no path to build on");
			}
		}
		
		if(parser.hasFlag("-port")) {
			int port = parser.getPositiveInteger("-port", 8080);
			try {
				MessageServer.runServer(index, port);
			} catch (Exception e) {
				System.out.println("Error when running server");
				e.printStackTrace();
			}
		}

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
