
public class AlgorithmB extends RobotManager {
	
	private int elapsed_ticks = 0;
	
	public AlgorithmB(int N_ROBOTS, int N_TRIALS) {
		super(N_ROBOTS, N_TRIALS);
	}
	
	// has a robot done a full traversal? -> if so, flip its direction
	public boolean checkFullTraversal() {
		for(Robot r : robots) {
			if(r.getDistance() >= 360) {
				r.randomizeDirection();
			}
		}
		return false;
	}

	public boolean tick() {
		if(trials < N_TRIALS) {
			for(Robot r : robots) {
				r.tick();
			}
			elapsed_ticks++;
			
			checkCollisions();
			checkFullTraversal();
			
			if(finished()) {
				// make note of the time taken (number of ticks)
				init_robots();
				System.out.println("Average time taken: " + elapsed_ticks/trials + ", trials: " + trials);
			}
			return true;
		}
		return false;
	}

}
