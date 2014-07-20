package medhanie.parallel.shortestpath;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkPool {
	private Lock M = new ReentrantLock();
	private static int count = 0;
	BlockingQueue<Integer> stream = new LinkedBlockingQueue<Integer>();

	public int getwork() {
		int workcount;
		M.lock();
		workcount = count - 1;
		count = workcount;
		M.unlock();
		try {
			if (workcount == -1 * Application.numworkers)/* Terminate Workers */
			{
				for (int i = 1; i < Application.numworkers; i++) {
					stream.put(-1);
				}
			} else {
				return stream.take(); /* read from Work Pool */
			}
		} catch (InterruptedException ex) {
		}
		return -1; // this is used when last worker is terminating after
					// processing for loop
	}

	public void putwork(int item) {
		M.lock();
		count++; /* Increment Work Pool counter */
		M.unlock();
		try {
			stream.put(item);
		} catch (InterruptedException e) {
		}
	}
}
