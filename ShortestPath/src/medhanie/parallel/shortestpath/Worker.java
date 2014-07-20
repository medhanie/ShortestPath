package medhanie.parallel.shortestpath;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Worker implements Runnable {
	private WorkPool workpool;
	private int weight[][];
	private int mindist[];
	private boolean inflag[];
	private Lock L[];

	public Worker(WorkPool p, int weight[][], int mindist[], boolean inflag[],
			Lock L[]) {
		this.workpool = p;
		this.weight = weight;
		this.mindist = mindist;
		this.inflag = inflag;
		this.L = L;
	}

	public void runSequential() {
		Queue<Integer> queue = new LinkedList<Integer>();
		Integer vertex;
		int w, newDist;
		queue.add(new Integer(0));
		while (!queue.isEmpty()) {
			vertex = (Integer) queue.remove();
			inflag[vertex] = false;
			for (w = 0; w < Application.n; w++) {
				if (weight[vertex][w] < Application.infinity) {
					newDist = mindist[vertex] + weight[vertex][w];
					if (newDist < mindist[w]) {
						mindist[w] = newDist;
						if (!inflag[w]) {
							inflag[w] = true;
							queue.add(w);
						}
					}
				}
			}
		}
	}

	public void run() {
		int vertex;
		int w, newdist;
		vertex = workpool.getwork(); /* Get vertex no. to analyze */
		while (vertex != -1) {
			inflag[vertex] = false; /* Vertex removed from WP */
			for (w = 0; w < Application.n; w++) {
				if (weight[vertex][w] < Application.infinity) {
					newdist = mindist[vertex] + weight[vertex][w];
					// Here we add the extra if before we Lock
					// if (newdist < mindist[w]) {
					// L[w].lock(); /* mutual exclusion on mindist[w] */
					// System.out.println("Hello");
					if (newdist < mindist[w]) {
						L[w].lock();
						if (newdist < mindist[w]) {
							mindist[w] = newdist; /* Update dist to w */

							L[w].unlock();
							if (!inflag[w]) { /* If w not in Work Pool */
								inflag[w] = true;
								workpool.putwork(w); /* Put w into Work Pool */
							}
						} else
							L[w].unlock();
					}
				}
			}
			vertex = workpool.getwork(); /* Get new vertex number */
		}
	}
}
