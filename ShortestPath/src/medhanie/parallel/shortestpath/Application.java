package medhanie.parallel.shortestpath;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

public class Application {

	public static final int numworkers = Runtime.getRuntime()
			.availableProcessors();
	public static final int n = 4000;
	public static final int infinity = 32000;
	private static WorkPool workpool = new WorkPool();
	private static int weight[][] = new int[n][n];
	private static int mindist[] = new int[n];
	private static boolean inflag[] = new boolean[n];
	private static Lock L[] = new ReentrantLock[n];

	private static StringBuffer output = new StringBuffer();

	public static void initializeMinimumDistance() {
		for (int i = 0; i < n; i++) {
			mindist[i] = infinity;
		}
	}

	public static void initializeLock() {
		for (int i = 0; i < n; i++) {
			L[i] = new ReentrantLock();
		}
	}

	public static void initializeInFlag() {
		for (int i = 1; i < n; i++) {
			inflag[i] = false;
		}
	}

	public static void initializeAll() {
		initializeMinimumDistance();
		initializeInFlag();
		initializeLock();
		inflag[0] = true;
		mindist[0] = 0;
		workpool = new WorkPool();
		workpool.putwork(0);

	}

	public static void main(String[] args) {
		int point[][] = new int[n][2];
		int k, temp = 0, dist = 0;
		Random rand = new Random(500);

		/* initialization */
		for (int i = 0; i < n; i++) {
			L[i] = new ReentrantLock();
			temp = rand.nextInt(1000);
			point[i][0] = temp;
			temp = rand.nextInt(1000);
			point[i][1] = temp;
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++) {
				if (i == j) {
					weight[i][j] = 0;
				} else {
					temp = point[i][0] - point[j][0];
					dist = temp * temp;
					temp = point[i][1] - point[j][1];
					dist = dist + temp * temp;
					weight[i][j] = dist;
					weight[j][i] = dist;
				}
			}
		}

		// initialization for sequential

		initializeMinimumDistance();
		initializeInFlag();
		mindist[0] = 0;
		inflag[0] = true;

		/***************** Sequential Version ****************/

		SequentialWorker seq = new SequentialWorker(workpool, weight, mindist,
				inflag, L);
		long time = System.currentTimeMillis();
		seq.runSequential();
		long sequentialTime = (System.currentTimeMillis() - time);
		output.append("Sequential Execution Time:\t" + sequentialTime);
		output.append("\n________________________________\n");

		/***************** Unpotimized Parallel version ****************/
		initializeAll();
		time = System.currentTimeMillis();
		Runnable unoptimized = new UnoptimizedWorker(workpool, weight, mindist,
				inflag, L);
		Thread[] threads = new Thread[numworkers];
		for (int i = 0; i < numworkers; i++) {
			Thread thread = new Thread(unoptimized);
			threads[i] = thread;
			threads[i].start();
		}
		for (int i = 0; i < numworkers; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		long unopParallelTimeVersion = (System.currentTimeMillis() - time);
		
	
		/***************** Optimized Parallel version ****************/

		initializeAll();

		time = System.currentTimeMillis();
		Runnable optimized = new Worker(workpool, weight, mindist, inflag, L);
		Thread[] opThreads = new Thread[numworkers];
		for (int i = 0; i < numworkers; i++) {
			Thread thread = new Thread(optimized);
			opThreads[i] = thread;
			opThreads[i].start();
		}
		for (int i = 0; i < numworkers; i++) {
			try {
			opThreads[i].join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		long parallelTimeVersion = (System.currentTimeMillis() - time);
		
		
		/****************** Output ***************************/

		output.append("\nUnoptimized Parallel Execution Time: "
				+ unopParallelTimeVersion);
		output.append("\nProcessors: " + numworkers);
		double speedup = (double) sequentialTime / unopParallelTimeVersion;
		output.append(String.format("\n%s %.2f", "Speedup: ", speedup));
		output.append(String.format("\n%s %.2f %s", "Efficiency: ",
				((double) speedup / numworkers) * 100, "%"));
		output.append("\n________________________________\n");

		output.append("\nOptimized Parallel Execution Time: "
				+ parallelTimeVersion);
		output.append("\nProcessors: " + numworkers);
		speedup = (double) sequentialTime / parallelTimeVersion;
		output.append(String.format("\n%s %.2f", "Speedup: ", speedup));
		output.append(String.format("\n%s %.2f %s", "Efficiency: ",
				((double) speedup / numworkers) * 100, "%"));

		JOptionPane.showMessageDialog(null, output.toString());

	}
}
