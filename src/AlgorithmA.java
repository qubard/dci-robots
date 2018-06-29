
public class AlgorithmA extends RobotManager {

	public AlgorithmA(int N_ROBOTS, int N_TRIALS) {
		super(N_ROBOTS, N_TRIALS);
	}
	
	public boolean tick() {
		if(trials < N_TRIALS) {
			for(Robot r : robots) {
				r.tick();
			}
			checkCollisions();
			
			if(no_convergence()) {
				N_FAIL++;
				init_robots();
			} else if(finished()) {
				N_SUCCESS++;
				init_robots();
			}
			return true;
		}
		return false;
	}

}
