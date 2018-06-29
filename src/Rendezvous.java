import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.apache.commons.cli.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Rendezvous {

	// The window handle
	private long window;
	
	private long totalElapsed = 0;
	
	private long lastTime = System.currentTimeMillis();
	
	private long elapsedTime = 0;
	
	private RobotManager manager;
	
	private int TICK_RATE; // simulation speed in milliseconds
	
	private boolean RENDER_MODE;
	
	public Rendezvous(RobotManager manager, boolean RENDER_MODE, int TICK_RATE) {
		this.manager = manager;
		this.RENDER_MODE = RENDER_MODE;
		this.TICK_RATE = TICK_RATE;
		if(!RENDER_MODE) {
			this.TICK_RATE = 0;
		}
	}
	
	public void run() {
		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init_robots() {
		manager.init_robots();
	}
	
	private void init() {
		init_robots();
		
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		// Create the window
		window = glfwCreateWindow(800, 800, "Robot Rendezvous", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			} else if(key == GLFW_KEY_R && action == GLFW_RELEASE) {
				init_robots();
			}
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		if(RENDER_MODE) {
			glfwShowWindow(window);
		}
	}
	
	void drawFilledCircle(float x, float y, float radius){
		int i;
		int triangleAmount = 100; //# of triangles used to draw circle
		
		float twicePi = (float) (2.0f * Math.PI);
		
		glBegin(GL_TRIANGLE_FAN);
			glVertex2f(x, y); // center of circle
			for(i = 0; i <= triangleAmount;i++) { 
				glVertex2f(
			            (float)(x + (radius * Math.cos(i *  twicePi / triangleAmount))), 
				    (float)(y + (radius * Math.sin(i * twicePi / triangleAmount)))
				);
			}
		glEnd();
	}
	
	void drawHollowCircle(float x, float y, float radius, long elapsed){
		int i;
		int lineAmount = 100; //# of triangles used to draw circle
		
		float twicePi = (float) (2.0f * Math.PI);
		
		glBegin(GL_LINE_LOOP);	
			for(i = 0; i <= lineAmount; i++) { 
				glColor3f((float)(Math.cos(twicePi*(double)elapsed/10000)),(1-(float)Math.sin(twicePi*i/lineAmount)),(float)Math.sin(twicePi*i/lineAmount));
				glVertex2f(
				    (float)(x + (radius * Math.cos(i *  twicePi / lineAmount))), 
				    (float)(y + (radius* Math.sin(i * twicePi / lineAmount)))
				);
			}
		glEnd();
	}
	
	void drawHollowCircle(float x, float y, float radius){
		int i;
		int lineAmount = 100; //# of triangles used to draw circle
		
		float twicePi = (float) (2.0f * Math.PI);
		
		glBegin(GL_LINE_LOOP);	
			for(i = 0; i <= lineAmount; i++) { 
				glVertex2f(
				    (float)(x + (radius * Math.cos(i *  twicePi / lineAmount))), 
				    (float)(y + (radius* Math.sin(i * twicePi / lineAmount)))
				);
			}
		glEnd();
	}
	
	private void render(long elapsed) {
		glColor3f(1,1,1);
		
		float radius = 0.5f;
		drawHollowCircle(0f, 0f, radius, elapsed);	
		
		for(Robot r : manager.getRobots()) {
			drawHollowCircle(radius*(float)Math.cos(Math.toRadians(r.getAngle())), radius*(float)Math.sin(Math.toRadians(r.getAngle())), Math.min(0.05f, 1.0f/manager.getNRobots()));	
		}
	}

	private void loop() {
		GL.createCapabilities();

		glClearColor(0.1f, 0.1f, 0.1f, 0.0f);

		glEnable(GL_LINE_SMOOTH);
		glLineWidth(15f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(false);
		
		while ( !glfwWindowShouldClose(window) ) {
			if(RENDER_MODE) {
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			}
			
			totalElapsed += System.currentTimeMillis()-lastTime;
			elapsedTime += System.currentTimeMillis()-lastTime;
			lastTime = System.currentTimeMillis();
			
			if(elapsedTime >= TICK_RATE) {
				elapsedTime -= TICK_RATE;
				if(!manager.tick()) { // tick all of the robots
					System.exit(0);
				}
			}
			
			if(RENDER_MODE) {
				render(totalElapsed%10000);
				
				glfwSwapBuffers(window); // swap the color buffers
		
				glfwPollEvents();
			}
		}
	}

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption(new Option("render", true, "Enable render mode"));
		options.addOption(new Option("n_robots", true, "The number of robots"));
		options.addOption(new Option("rate", true, "The render tick rate"));
		options.addOption(new Option("n_trials", true, "The number of trials"));
		options.addOption(new Option("algorithm", true, "Algorithm chosen"));
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			RobotManager algorithm = Boolean.parseBoolean(cmd.getOptionValue("algorithm")) ? new AlgorithmB(Integer.parseInt(cmd.getOptionValue("n_robots")), 
					Integer.parseInt(cmd.getOptionValue("n_trials"))) : new AlgorithmA(Integer.parseInt(cmd.getOptionValue("n_robots")), 
							Integer.parseInt(cmd.getOptionValue("n_trials")));
			new Rendezvous(
					algorithm,
					Boolean.parseBoolean(cmd.getOptionValue("render")), 
					cmd.hasOption("rate") ? Integer.parseInt(cmd.getOptionValue("rate")) : 25).run();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}