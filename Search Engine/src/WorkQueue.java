import java.util.LinkedList;

/**
 * A simple work queue implementation 
 */
public class WorkQueue {
	/**
	 * Keeps track of pending work
	 */
	private Integer pending;

	/**
	 * Pool of worker threads that will wait in the background until work is available.
	 */
	private final PoolWorker[] workers;

	/** Queue of pending work requests. */
	private final LinkedList<Runnable> queue;

	/** Used to signal the queue should be shutdown. */
	private volatile boolean shutdown;

	/** The default number of threads to use when not specified. */
	public static final int DEFAULT = 5;
	
	/**
	 * Optional tracker for the number of tasks performed
	 */
	private int tracker;

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with the specified number of threads.
	 *
	 * @param threads number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		if(threads < 1) {
			threads = 5;
		}
		this.tracker = 0;
		this.queue = new LinkedList<Runnable>();
		this.workers = new PoolWorker[threads];
		this.pending = 0;

		shutdown = false;
		// start the threads so they are waiting in the background
		for (int i = 0; i < threads; i++) {
			workers[i] = new PoolWorker();
			workers[i].start();
		}
	}

	/**
	 * Adds a work request to the queue. A thread will process this request when available.
	 *
	 * @param task work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable task) {
		synchronized (queue) {
			queue.addLast(task);
			queue.notifyAll();
			incrementPending();
		}
	}
	
	/**
	 * Adds a work request to the queue. Also keeps track of the total number of tasks performed
	 * Will call finish() when the tracker reaches the limit
	 *
	 * @param task work request (in the form of a {@link Runnable} object)
	 */
	public void executeAndTrack(Runnable task){
		synchronized (queue) {
			queue.addLast(task);
			this.tracker ++;
			incrementPending();
			queue.notifyAll();
//			System.out.println("tracker: "+tracker);
		}
	}
	
	

	/**
	 * Waits for all pending work to be finished.
	 */
	public synchronized void finish() {
		try {
			while(pending>0) {
				this.wait();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Waits for all pending work to be finished AND waits for the tracker to reach the limit.
	 * @param limit the maximum number of tasks to perform
	 *
	 */
	public synchronized void finish(int limit) {
		System.out.println("starting to wait");
		try {
			while(tracker < limit || pending > 0) {
				this.wait();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		System.out.println("done waiting");
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished, but threads in-progress
	 * will not be interrupted.
	 */
	public void shutdown() {
		// safe to do unsynchronized due to volatile keyword
		shutdown = true;

		synchronized (queue) {
			queue.notifyAll();
		}
	}

	/**
	 * safely increments the pending variable
	 */
	private synchronized void incrementPending() {
		pending++;
	}

	/**
	 * safely decrements the pending variable
	 */
	private synchronized void decrementPending() {
		pending--;

		if (pending == 0) {
			this.notifyAll();
		}
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Waits until work is available in the work queue. When work is found, will remove the work from
	 * the queue and run it. If a shutdown is detected, will exit instead of grabbing new work from
	 * the queue. These threads will continue running in the background until a shutdown is requested.
	 */
	private class PoolWorker extends Thread {

		@Override
		public void run() {
			Runnable task = null;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						try {
							queue.wait();
						} catch (InterruptedException ex) {
							System.out.println("Warning: Work queue interrupted while waiting.");
							Thread.currentThread().interrupt();
						}
					}

					// exit while for one of two reasons:
					// (a) queue has work, or (b) shutdown has been called

					if (shutdown) {
						break;
					} else {
						task = queue.removeFirst();
					}
				}

				try {
					task.run();
				} catch (RuntimeException ex) {
					// catch runtime exceptions to avoid leaking threads
					ex.printStackTrace();
					System.out.println("Warning: Work queue encountered an exception while running in: "+Thread.currentThread());
				} finally {
					decrementPending();
				}
			}
		}
	}
}


