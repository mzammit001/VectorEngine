import java.util.*;

public class Vector {

	private Long sum;
	private Long mode;
	private Long median;
	private Long minimum;
	private Long maximum;

	private final int length;
	private final long[] elements;

	// ===========================================================================
	// INITIALIZATION
	// ===========================================================================

	/**
	 * Constructs new vector with the given
	 * length and all elements set to zero.
	 */
	public Vector(int length) {

		this.sum = null;
		this.mode = null;
		this.median = null;
		this.minimum = null;
		this.maximum = null;

		this.length = length;
		this.elements = new long[length];
	}

	/**
	 * Returns new vector with elements generated at random up to 100.
	 */
	public static Vector random(int length, int seed) {

		Vector vector = new Vector(length);
		Random random = new Random(seed);

		for (int i = 0; i < length; i++) {
			vector.elements[i] = (long) random.nextInt(101);
		}

		return vector;
	}

	/**
	 * Returns new vector with all elements set to given value.
	 */
	public static Vector uniform(int length, long value) {

		/*
			to do

			length 1, value 1 => [1]
			length 2, value 2 => [2 2]
			length 3, value 3 => [3 3 3]
			length 4, value 4 => [4 4 4 4]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector with elements in sequence from given start and step.
	 */
	public static Vector sequence(int length, long start, long step) {

		/*
			to do

			length 1, start 1, step 1  => [1]
			length 2, start 2, step 2  => [2 4]
			length 3, start 3, step 3  => [3 6 9]
			length 4, start 4, step 4  => [4 8 12 16]
			length 5, start 5, step -1 => [5 4 3 2 1]
		*/

		return new Vector(length);
	}

	/**
	 * Returns whether the number is semiprime.
	 */
	public static boolean isPQ(long number) {

		/*
			to do
		*/

		return true;
	}

	/**
	 * Returns new vector with elements generated from the
	 * pq number sequence starting from the specified value.
	 */
	public static Vector pq(int length, long start) {

		/*
			to do

			length 4, start 1  => [4 6 9 10]
			length 4, start 4  => [4 6 9 10]
			length 4, start -1 => [4 6 9 10]
			length 4, start 42 => [46 49 51 55]
		*/

		return new Vector(length);
	}

	/**
	 * Returns whether the number is prime.
	 */
	public static boolean isPrime(long number) {

		/*
			to do
		*/

		return true;
	}

	/**
	 * Returns new vector with elements generated from the
	 * prime number sequence starting from the specified value.
	 */
	public static Vector prime(int length, long start) {

		/*
			to do

			length 4, start 1  => [2 3 5 7]
			length 4, start 2  => [2 3 5 7]
			length 4, start -1 => [2 3 5 7]
			length 4, start 42 => [43 47 53 59]
		*/

		return new Vector(length);
	}

	/**
	 * Returns whether the number is abundant.
	 */
	public static boolean isAbundant(long number) {

		/*
			to do
		*/

		return true;
	}

	/**
	 * Returns new vector with elements generated from the
	 * abundant number sequence starting from the specified value.
	 */
	public static Vector abundant(int length, long start) {

		/*
			to do

			length 4, start 0  => [12 18 20 24]
			length 4, start 12 => [12 18 20 24]
			length 4, start -1 => [12 18 20 24]
			length 4, start 42 => [42 48 54 56]
		*/

		return new Vector(length);
	}

	/**
	 * Returns whether the number is composite.
	 */
	public static boolean isComposite(long number) {

		/*
			to do
		*/

		return true;
	}

	/**
	 * Returns new vector with elements generated from the
	 * composite number sequence starting from the specified value.
	 */
	public static Vector composite(int length, long start) {

		/*
			to do

			length 4, start 0  => [4 6 8 9]
			length 4, start 4  => [4 6 8 9]
			length 4, start -1 => [4 6 8 9]
			length 4, start 42 => [42 44 45 46]
		*/

		return new Vector(length);
	}

	// ===========================================================================
	// VECTOR OPERATIONS
	// ===========================================================================

	/**
	 * Returns new vector that is a copy of the current vector.
	 */
	public Vector cloned() {

		/*
			to do

			[1 1 1 1] => [1 1 1 1]
			[1 2 3 4] => [1 2 3 4]
			[4 3 2 1] => [4 3 2 1]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector with elements ordered from smallest to largest.
	 */
	public Vector sorted() {

		/*
			to do

			[1 1 1 1] => [1 1 1 1]
			[1 2 3 4] => [1 2 3 4]
			[4 3 2 1] => [1 2 3 4]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector with elements ordered in reverse.
	 */
	public Vector reversed() {

		/*
			to do

			[1 1 1 1] => [1 1 1 1]
			[1 2 3 4] => [4 3 2 1]
			[4 3 2 1] => [1 2 3 4]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector with elements shifted right by a given number of positions.
	 */
	public Vector shifted(long amount) {

		/*
			to do

			[1 2 3 4] 0 => [1 2 3 4]
			[1 2 3 4] 1 => [4 1 2 3]
			[1 2 3 4] 2 => [3 4 1 2]
			[1 2 3 4] 3 => [2 3 4 1]
			[1 2 3 4] 4 => [1 2 3 4]
			[1 2 3 4] 5 => [4 1 2 3]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector, adding scalar to each element.
	 */
	public Vector scalarAdd(long scalar) {

		/*
			to do

			[1 1 1 1] + 1  => [2 2 2 2]
			[1 2 3 4] + 4  => [5 6 7 8]
			[2 2 2 2] + -1 => [1 1 1 1]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector, multiplying scalar to each element.
	 */
	public Vector scalarMultiply(long scalar) {

		/*
			to do

			[1 2 3 4] x 0  => [0 0 0 0]
			[1 2 3 4] x 1  => [1 2 3 4]
			[1 2 3 4] x 2  => [2 4 6 8]
			[1 2 3 4] x 10 => [10 20 30 40]
			[1 2 3 4] x -1 => [-1 -2 -3 -4]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector, adding elements with the same index.
	 */
	public Vector vectorAdd(Vector other) {

		/*
			to do

			[1 2 3 4] + [0 0 0 0]     => [1 2 3 4]
			[1 2 3 4] + [4 4 4 4]     => [5 6 7 8]
			[1 2 3 4] + [1 2 3 4]     => [2 4 6 8]
			[2 2 2 2] + [-1 -1 -1 -1] => [2 2 2 2]
		*/

		return new Vector(length);
	}

	/**
	 * Returns new vector, multiplying elements with the same index.
	 */
	public Vector vectorMultiply(Vector other) {

		/*
			to do

			[1 2 3 4] x [0 0 0 0]     => [0 0 0 0]
			[1 2 3 4] x [1 1 1 1]     => [1 2 3 4]
			[1 2 3 4] x [1 2 3 4]     => [1 4 9 16]
			[2 2 2 2] x [-1 -1 -1 -1] => [-2 -2 -2 -2]
		*/

		return new Vector(length);
	}

	// ===========================================================================
	// VECTOR COMPUTATIONS
	// ===========================================================================

	/**
	 * Returns the sum of all elements.
	 */
	public Long getSum() {

		/*
			to do

			[0 0 0 0] => 0
			[1 1 1 1] => 4
			[1 2 3 4] => 10
		*/

		// Calculate and store the sum when unknown
		if (this.sum == null) {

			// to do

		}

		return this.sum;
	}

	/**
	 * Returns the most frequently occuring element
	 * or -1 if there is no such unique element.
	 */
	public Long getMode() {

		/*
			to do

			[1]       => 1
			[2 2]     => 2
			[2 4 4]   => 4
			[1 2 3 4] => -1
		*/

		return this.mode;
	}

	/**
	 * Returns the upper median.
	 */
	public Long getMedian() {

		/*
			to do

			[1] => 1
			[1 2] => 2
			[1 2 3] => 2
			[1 1 1 1] => 1
		*/

		return this.median;
	}

	/**
	 * Returns the smallest value in the vector.
	 */
	public Long getMinimum() {

		/*
			to do

			[1 1 1 1] => 1
			[1 2 3 4] => 1
			[4 3 2 1] => 1
		*/

		return this.minimum;
	}

	/**
	 * Returns the largest value in the vector.
	 */
	public Long getMaximum() {

		/*
			to do

			[1 1 1 1] => 1
			[1 2 3 4] => 4
			[4 3 2 1] => 4
		*/

		return this.maximum;
	}

	/**
	 * Returns the frequency of the value in the vector.
	 */
	public long getFrequency(long value) {

		/*
			to do

			[1 2 3 4] 0 => 0
			[1 2 3 4] 1 => 1
			[1 1 1 1] 1 => 4
		*/

		return -1;
	}

	// ===========================================================================
	// DISPLAY OPERATIONS
	// ===========================================================================

	/**
	 * Displays the vector.
	 */
	public void display() {

		/*
			to do

			Display each element in the vector seperated by a space.
		*/
	}

	/**
	 * Displays the element at the specified index.
	 */
	public void displayElement(int index) {

		/*
			to do

			Display the element at the given index.
		*/
	}

	// ===========================================================================
	// ACCESSOR METHODS
	// ===========================================================================

	/**
	 * Returns the vector length.
	 */
	public int getLength() {

		return this.length;
	}

	/**
	 * Returns the vector elements.
	 */
	public long[] getElements() {

		return this.elements;
	}
}
