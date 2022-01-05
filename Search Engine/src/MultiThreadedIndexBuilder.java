import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Builder class for InvertedIndex using a work queue
 * @author matthew
 *
 */
public class MultiThreadedIndexBuilder {

	/**
	 * Builds the inverted index based on the given path. Also stores the word count for each file that is read
	 * @param path the path to the directory of files
	 * @param index the Inverted index object that will be modified
	 * @param threads the number of threads to use
	 *
	 * @throws IOException if IO error occurs
	 */
	public static void build(Path path, ThreadSafeInvertedIndex index, int threads) throws IOException {
		WorkQueue workQueue = new WorkQueue(threads);
		List<Path> textFiles = TextFileFinder.list(path);
		for(Path file : textFiles) {
			workQueue.execute(new Task(index, file));
		}
		workQueue.finish();
		workQueue.shutdown();
	}

	/**
	 * The non-static task class that will update the shared paths and pending members in our task
	 * manager instance.
	 */
	private static class Task implements Runnable {
		/** The path to add or list. */
		private final Path path;
		/**
		 * The inverted index to add to
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * Initializes this task.
		 * @param index the index to add to
		 *
		 * @param path the path to add or list
		 */
		public Task(ThreadSafeInvertedIndex index, Path path) {
			this.index = index;
			this.path = path;
		}

		@Override
		public void run() {

			InvertedIndex local = new InvertedIndex();
			try {
				InvertedIndexBuilder.readFile(path, local);
			} catch (IOException e) {
				System.out.println("Error: could not read file");
			}

			index.addAll(local);
		}
	}
}
