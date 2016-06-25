import java.util.*;

public class VectorEngine {

	static Integer vectorLength;
	static Map<String, Vector> vectors;

	/**
	 * Defines settings based on command line arguments.
	 */
	public static void init(String[] args) {

		// Initialise a lookup table of vectors
		vectors = new HashMap<String, Vector>();

		// Attempt to parse the vector length from command line arguments
		if (args.length != 1) {
			System.out.printf("Invalid command line arguments\n");
			System.out.printf("Usage: java VectorEngine <length>\n");
			System.exit(1);
		}

		// Attempt to convert the vector length to an integer
		try {
			vectorLength = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.printf("Invalid length: vector length must be an integer\n");
			System.exit(1);
		}

		// Ensure vector length is valid
		if (vectorLength < 1) {
			System.out.printf("Invalid length: vector length must be >= 1\n");
			System.exit(1);
		}
	}

	/**
	 * Terminates the program.
	 */
	public static void byeCommand() {

		System.out.printf("bye\n");
		System.exit(0);
	}

	/**
	 * Displays help message.
	 */
	public static void helpCommand() {

		System.out.println("BYE");
		System.out.println("HELP");
		System.out.println("");
		System.out.println("SET <key> = pq <start>");
		System.out.println("SET <key> = prime <start>");
		System.out.println("SET <key> = abundant <start>");
		System.out.println("SET <key> = composite <start>");
		System.out.println("");
		System.out.println("SET <key> = random <seed>");
		System.out.println("SET <key> = uniform <value>");
		System.out.println("SET <key> = sequence <start> <step>");
		System.out.println("");
		System.out.println("SET <key> = cloned <vector>");
		System.out.println("SET <key> = sorted <vector>");
		System.out.println("SET <key> = shifted <vector> <amount>");
		System.out.println("SET <key> = reversed <vector>");
		System.out.println("");
		System.out.println("SET <key> = scalar#add <vector> <value>");
		System.out.println("SET <key> = scalar#mul <vector> <value>");
		System.out.println("SET <key> = vector#add <vector a> <vector b>");
		System.out.println("SET <key> = vector#add <vector a> <vector b>");
		System.out.println("");
		System.out.println("SHOW <key>");
		System.out.println("SHOW <key> <index>");
		System.out.println("");
		System.out.println("COMPUTE sum <key>");
		System.out.println("COMPUTE mode <key>");
		System.out.println("COMPUTE median <key>");
		System.out.println("COMPUTE minimum <key>");
		System.out.println("COMPUTE maximum <key>");
		System.out.println("COMPUTE frequency <key> <value>");
	}

	/**
	 * Returns vector with given key from map.
	 */
	public static Vector fetchVector(String key) {

		// Attempt to fetch vector from map
		Vector vector = vectors.get(key);
		if (vector == null) {
			System.out.printf("vector not found\n");
		}

		return vector;
	}

	/**
	 * Set command based on given input.
	 */
	public static void setCommand(String line) {

		String[] args = line.split(" ");

		// Ensure argument count is valid
		if (args.length < 4 || args.length > 6) {
			System.out.printf("invalid arguments\n");
			return;
		}

		Vector v = null;
		Vector v1 = null;
		Vector v2 = null;
		Vector result = null;

		int seed = 0;
		long step = 0;
		long start = 0;
		long count = 0;
		long value = 0;

		String key = args[1];
		String operation = args[3].toLowerCase();

		switch (operation) {
			case "random":
				seed = Integer.parseInt(args[4]);
				result = Vector.random(vectorLength, seed);
				break;
			case "uniform":
				value = Long.parseLong(args[4]);
				result = Vector.uniform(vectorLength, value);
				break;
			case "sequence":
				start = Long.parseLong(args[4]);
				step = Long.parseLong(args[5]);
				result = Vector.sequence(vectorLength, start, step);
				break;

			case "pq":
				start = Long.parseLong(args[4]);
				result = Vector.pq(vectorLength, start);
				break;
			case "prime":
				start = Long.parseLong(args[4]);
				result = Vector.prime(vectorLength, start);
				break;
			case "abundant":
				start = Long.parseLong(args[4]);
				result = Vector.abundant(vectorLength, start);
				break;
			case "composite":
				start = Long.parseLong(args[4]);
				result = Vector.composite(vectorLength, start);
				break;

			case "cloned":
				v = fetchVector(args[4]);
				if (v == null) return;
				result = v.cloned();
				break;
			case "sorted":
				v = fetchVector(args[4]);
				if (v == null) return;
				result = v.sorted();
				break;
			case "shifted":
				v = fetchVector(args[4]);
				if (v == null) return;
				count = Long.parseLong(args[5]);
				result = v.shifted(count);
				break;
			case "reversed":
				v = fetchVector(args[4]);
				if (v == null) return;
				result = v.reversed();
				break;

			case "scalar#add":
				v = fetchVector(args[4]);
				if (v == null) return;
				value = Long.parseLong(args[5]);
				result = v.scalarAdd(value);
				break;
			case "scalar#mul":
				v = fetchVector(args[4]);
				if (v == null) return;
				value = Long.parseLong(args[5]);
				result = v.scalarMultiply(value);
				break;
			case "vector#add":
				v1 = fetchVector(args[4]);
				v2 = fetchVector(args[5]);
				if (v1 == null || v2 == null) return;
				result = v1.vectorAdd(v2);
				break;
			case "vector#mul":
				v1 = fetchVector(args[4]);
				v2 = fetchVector(args[5]);
				if (v1 == null || v2 == null) return;
				result = v1.vectorMultiply(v2);
				break;

			default:
				System.out.printf("invalid operation\n");
				return;
		}

		// Store the result
		vectors.put(key, result);
		System.out.printf("ok\n");
	}

	/**
	 * Show command based on given input.
	 */
	public static void showCommand(String line) {

		String[] args = line.split(" ");

		// Ensure argument count is valid
		if (args.length < 2 || args.length > 3) {
			System.out.printf("invalid arguments\n");
			return;
		}

		// Attempt to fetch vector from map
		String key = args[1];
		Vector vector = fetchVector(key);
		if (vector == null) {
			return;
		}

		// Display vector
		if (args.length == 2) {
			vector.display();
			return;
		}

		int index = 0;

		// Attempt to parse vector element index
		try {
			index = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.out.printf("invalid index\n");
			return;
		}

		// Ensure index is within the valid range
		if (index < 0 || index >= vectorLength) {
			System.out.printf("index out of range\n");
			return;
		}

		// Display vector element
		vector.displayElement(index);
	}

	/**
	 * Compute command based on given input.
	 */
	public static void computeCommand(String line) {

		String[] args = line.split(" ");

		// Ensure argument count is valid
		if (args.length < 3 || args.length > 4) {
			System.out.printf("invalid arguments\n");
			return;
		}

		String operation = args[1].toLowerCase();

		// Attempt to fetch vector from map
		String key = args[2];
		Vector vector = fetchVector(key);
		if (vector == null) {
			return;
		}

		Long result = null;

		switch (operation) {
			case "sum":
				result = vector.getSum();
				break;
			case "mode":
				result = vector.getMode();
				break;
			case "median":
				result = vector.getMedian();
				break;
			case "minimum":
				result = vector.getMinimum();
				break;
			case "maximum":
				result = vector.getMaximum();
				break;
			case "frequency":
				long value = Long.parseLong(args[3]);
				result = vector.getFrequency(value);
				break;
			default:
				System.out.printf("invalid operation\n");
				return;
		}

		// Display result
		if (result == null) {
			System.out.printf("nil\n");
		} else {
			System.out.printf("%d\n", result);
		}
	}

	/**
	 * Runs computations and stores vectors based on given input.
	 */
	public static void computeEngine() {

		Scanner scan = new Scanner(System.in);

		// Display prompt
		System.out.printf("> ");

		// Read from standard input until EOF
		while (scan.hasNextLine()) {
			// Process line
			String line = scan.nextLine();
			String command = line.split(" ")[0].toLowerCase();

			switch (command) {
				case "":
					break;
				case "bye":
					byeCommand();
					break;
				case "help":
					helpCommand();
					break;
				case "set":
					setCommand(line);
					break;
				case "show":
					showCommand(line);
					break;
				case "compute":
					computeCommand(line);
					break;
				default:
					System.out.printf("invalid command\n");
			}

			System.out.printf("\n> ");
		}

		System.out.printf("\nbye\n");
	}

	/**
	 * Main function.
	 */
	public static void main(String[] args) {

		init(args);
		computeEngine();
	}
}
