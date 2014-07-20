package medhanie.parallel.shortestpath;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class SequentialWorker  {
	private WorkPool workpool;
	private int weight[][];
	private int mindist[];
	private boolean inflag[];
	private Lock L[];

	public SequentialWorker(WorkPool p, int weight[][], int mindist[], boolean inflag[],
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
}