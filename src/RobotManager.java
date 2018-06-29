import java.util.ArrayList;

public abstract class RobotManager {

	protected ArrayList<Robot> robots = new ArrayList<Robot>();
	
	private int N_ROBOTS;
	
	protected int N_FAIL = 0;
	protected int N_SUCCESS = 0;
	
	protected int N_TRIALS = 0;
	
	protected int trials = 0;
	
	public RobotManager(int N_ROBOTS, int N_TRIALS) {
		this.N_ROBOTS = N_ROBOTS;
		this.N_TRIALS = N_TRIALS;
	}
	
	public int getNRobots() {
		return N_ROBOTS;
	}
	
	public void init_robots() {
		robots.clear();
		int spacing = (int) Math.floor(360/N_ROBOTS);
		for(int i = 0; i < N_ROBOTS; i++) {
			robots.add(new Robot(i*spacing, 1.0f));
		}
		trials++;
		if(N_SUCCESS > 0 || N_FAIL > 0) {
			System.out.println("Success rate with: " + N_ROBOTS +  " robots is " + 100*(float)N_SUCCESS/trials + "% , trials: " + trials + ", successes: " + N_SUCCESS);
		}
	}
	
	public void add(Robot r) {
		this.robots.add(r);
	}
	
	public ArrayList<Robot> getRobots() {
		return this.robots;
	}
	
	public boolean finished() {
		return robots.size() == 1;
	}
	
	public boolean no_convergence() {
		if(robots.size() == 1) {
			return false;
		}
		
		for(int i = 0; i < robots.size()-1; i++) {
			if(robots.get(i).isClockwise() != robots.get(i+1).isClockwise()) {
				return false; // not going in the same direction	
			}
		}
		return true;
	}
	
	// Check for collisions and merge robots
	public void checkCollisions() {
		ArrayList<Robot> to_remove = new ArrayList<Robot>();
		ArrayList<Robot> to_add = new ArrayList<Robot>();
		for(Robot a : robots) {
			for(Robot b : robots) {
				if(a != b && !(to_remove.contains(a) || to_remove.contains(b)) && a.collided(b)) {
					to_remove.add(a);
					to_remove.add(b);
					to_add.add(new Robot(a.getAngle(), 1.0f));
				}
			}
		}
		
		for(Robot r : to_remove) {
			if(robots.contains(r)) {
				robots.remove(r);
			}
		}
		
		for(Robot r : to_add) {
			robots.add(r);
		}
	}
	
	abstract boolean tick();
	
}
