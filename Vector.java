import java.util.stream.LongStream;
import java.util.*;

public class Vector {

    private Long sum;
    private Long mode;
    private Long median;
    private Long minimum;
    private Long maximum;

    public static Long lastMode;
    public static Long lastSum;

    // is sorted
    private boolean stable;
    // is reversed
    private boolean reversed;
    // is uniform
    private boolean uniform;
    private boolean random;

    private final int length;
    private final long[] elements;

    // prime and pq cache size
    public static final int cacheLimitPr = 1024 * 1024 * 5;

    public static final int cacheMaxPr  = 1024 * 1024 * 30;
    public static final int cacheMaxPq  = 1024 * 1024 * 30;
    public static final int cacheMaxAb  = 1024 * 1024 * 20;

    public static int cacheSizePr = cacheLimitPr;
    public static int cacheSizePq = cacheLimitPr;

    // abundant cache size
    public static final int cacheLimitAb = 1 * 1500 * 1000;
    public static int cacheSizeAb = cacheLimitAb;

    public static Integer rlength;

    // our caches
    public static boolean[] primeCache;
    public static boolean[] pqCache;
    public static long[] abundantCache;

    public static final int rtThreadCount = Runtime.getRuntime().availableProcessors();

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

        this.uniform = false;
        this.stable = false;
        this.reversed = false;
        this.random = false;

        this.length = length;
        this.elements = new long[length];

        Vector.setRange(length);
    }

    public static void setRange(int value) {
        if (Vector.rlength == null) {
            Vector.rlength = value;
        }
    }

    public static int getRange() {
        return (Vector.rlength == null) ? 0 : Vector.rlength;
    }

    /**
     * Checks if the value is stored in our prime cache
     */
    public static boolean isPrimeValueCached(long number) {
        return number > 0 && number < (long)Vector.cacheSizePr && Vector.primeCache != null;
    }

    /**
     * Gets the prime flag from our cache
     */
    public static boolean getStateFromPrimeCache(long number) {
        return ! Vector.primeCache[(int) number];
    }

    /**
     * Checks the prime cache, and falls back to naive checking if its outside of its limits
     */
    public static boolean isPrimeCached(long number) {
        if (Vector.isPrimeValueCached(number)) {
            return Vector.getStateFromPrimeCache(number);
        } else {
            return Vector.isPrime(number);
        }
    }

    /**
     * Checks if the value is stored in our abundant cache
     */
    public static boolean isAbundantValueCached(long number) {
        return Vector.abundantCache != null && number > 0 && number < (long)Vector.cacheSizeAb;
    }

    /**
     * Compares the stored sum to the number in our cache
     */
    public static boolean getStateFromAbundantCache(long number) {
        return Vector.abundantCache[(int)number] > number;
    }

    /**
     * Checks the abundant cache, and falls back to naive checking if its outside of its limits
     */
    public static boolean isAbundantCached(long number) {
        if (Vector.isAbundantValueCached(number)) {
            return Vector.getStateFromAbundantCache(number);
        }

        if (Vector.primeCache == null) {
            Vector.initializePrimeCache(number, Vector.rlength);
        }

        return Vector.isAbundant(number);
    }

    /**
     * Checks if the value is stored in our pq cache
     */
    public static boolean isPQValueCached(long number) {
        return Vector.pqCache != null && number > 0 && number < (long)Vector.cacheSizePq;
    }

    /**
     * Returns the pq flag from the cache, true = is semiprime, false = not
     */
    public static boolean getStateFromPQCache(long number) {
        return Vector.pqCache[(int) number];
    }


    /**
     * Checks the pq cache, and falls back to naive checking if its outside of its limits
     */
    public static boolean isPQCached(long number) {
        if (Vector.isPQValueCached(number)) {
            return Vector.getStateFromPQCache(number);
        }

        return Vector.isPQ(number);
    }


    /**
     * This does the heavy lifting and initalizes the abundant cache
     */
    public static void initializeAbundantCache(long start, int length) {
        long capacity = (((start + ((long)length / 5)) / (500 * 1000))+1) * (500 * 1000);

        // a rough average for abundants is 1 found every 4-6 values
        if ((int)capacity < Vector.cacheSizeAb && Vector.abundantCache != null) {
            return;
        }

        Vector.cacheSizeAb = Math.min( Math.max(Vector.cacheSizeAb, (int)capacity), Vector.cacheMaxAb );
        Vector.abundantCache = new long[Vector.cacheSizeAb+1];

        for (long i = 2; i <= Vector.cacheSizeAb; i++) {
            for (long j = i*2; j <= Vector.cacheSizeAb; j += i) {
                Vector.abundantCache[(int) j] += i;
            }
        }
    }

    /**
     * This does the heavy lifting and initializes the prime cache
     */
    // reference: https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes#Algorithm_and_variants
    public static void initializePrimeCache(long start, int length) {
        long capacity = start + ((long)length*10);

        if (capacity < (long)Vector.cacheSizePr && Vector.primeCache != null) {
            return;
        }

        Vector.cacheSizePr = Math.min( Math.max(Vector.cacheSizePr, (int)capacity), Vector.cacheMaxPr);
        Vector.primeCache = new boolean[Vector.cacheSizePr+1];

        Vector.primeCache[2] = false;
        Vector.primeCache[3] = false;

        final long limit = (long) Math.sqrt((double) Vector.cacheSizePr);
        // generate a boolean array with all the primes set to false
        for (long i = 2; i < limit; i++) {
            if (! Vector.primeCache[(int) i]) {
                for (long j = (i*i); j <= Vector.cacheSizePr; j+=i) {
                    Vector.primeCache[(int) j] = true;
                }
            }
        }
    }

    /**
     * This does the heavy lifting and initializes the pq cache
     * uses initializePrimeCache as a helper because we can get the semiprimes from that
     */
    // reference: http://codility-lessons.blogspot.com.au/2015/03/lesson-9-countsemiprimes.html
    public static void initializePQCache(long start, int length) {
        // setup the prime cache
        Vector.initializePrimeCache(start, length);

        if (Vector.pqCache != null && Vector.cacheSizePq == Vector.cacheSizePr) {
            return;
        }

        Vector.pqCache = new boolean[Vector.cacheSizePq+1];

        final long limit = (long) Math.sqrt(Vector.cacheSizePq);

        for (long i = 2; i < limit; i++ ) {
            // not a prime
            if (! Vector.primeCache[(int) i]) {
                for (long j = i; (j * i) <= Vector.cacheSizePq; j++) {
                    // found a prime
                    if (!Vector.primeCache[(int) j]) {
                        // set the pq cache index j*i to is semiprime
                        Vector.pqCache[(int) (j * i)] = true;
                    }
                }
            }
        }
    }

    public static void bucketSort(long[] in, long[] out) {
        bucketSort(in, out, 2000, false);
    }

    /**
     *  Quick dirty limited bucket sort implementation for random arrays
     */
    public static void bucketSort(long[] in, long[] out, long upper, boolean modeFlag) {
        long[] buckets = new long[(int)upper+1];

        for (long i = 0; i < in.length; i++) {
            buckets[(int)in[(int)i]]++;
        }

        long max = 0L;
        long mode = -1L;
        long sum = 0;

        for (long i = 0, idx = 0; i < buckets.length; i++) {
            for (long j = 0; j < buckets[(int)i]; j++, idx++) {
                out[(int)idx] = i;
            }

            sum += (buckets[(int)i] * i);

            if (modeFlag) {
                if (max != buckets[(int) i]) {
                    if (buckets[(int) i] > max) {
                        max = buckets[(int) i];
                        mode = i;
                    }
                } else {
                    mode = -1L;
                }
            }
        }

        Vector.lastMode = (modeFlag) ? mode : null;
        Vector.lastSum = sum;
    }

    /**
     * Returns new vector with elements generated at random up to 100.
     */
    public static Vector random(int length, long seed) {

        Vector vector = new Vector(length);
        Random random = new Random(seed);

        long min = 100;
        long max = -1;
        long sum = 0;

        for (int i = 0; i < length; i++) {
            sum += vector.elements[i] = (long) random.nextInt(101);
            if (vector.elements[i] < min || vector.elements[i] > max) {
                min = min > vector.elements[i] ? vector.elements[i] : min;
                max = max < vector.elements[i] ? vector.elements[i] : max;
            }
        }

        vector.minimum = min;
        vector.maximum = max;
        vector.sum = sum;

        vector.random = true;

        return vector;
    }

    /**
     * Returns new vector with all elements set to given value.
     */
    public static Vector uniform(int length, long value) {
        /*
            length 1, value 1 => [1]
            length 2, value 2 => [2 2]
            length 3, value 3 => [3 3 3]
            length 4, value 4 => [4 4 4 4]
        */
        Vector vector = new Vector(length);
        // fills elements with a static value
        Arrays.fill(vector.elements, value);

        // things we can we cache by default
        // min / max / median / mode are identical
        vector.minimum = vector.maximum = vector.median = vector.mode = vector.elements[0];
        // sum is simply the length * value
        vector.sum = (long)length * value;
        vector.uniform = true;
        vector.stable = true;

        return vector;
    }

    /**
     * Returns new vector with elements in sequence from given start and step.
     */
    public static Vector sequence(int length, long start, long step) {
        /*
            length 1, start 1, step 1  => [1]
            length 2, start 2, step 2  => [2 4]
            length 3, start 3, step 3  => [3 6 9]
            length 4, start 4, step 4  => [4 8 12 16]
            length 5, start 5, step -1 => [5 4 3 2 1]
        */
        Vector vector = new Vector(length);

        long sum = 0;

        // use the current index to calculate the values offset from start
        for (int i = 0; i < length; i++) {
            sum += vector.elements[i] = start + (step * (long) i);
        }

        // things we can we cache by default
        // we know its ordered so we can grab the min/max
        vector.maximum = vector.elements[(step >= 0) ? length-1 : 0];
        vector.minimum = vector.elements[(step >= 0) ? 0 : length-1];
        vector.median  = vector.elements[length/2];
        vector.stable = true;
        vector.sum = sum;

        if (step < 0) {
            vector.reversed = true;
            // inverse median
            vector.median  = vector.elements[length % 2 == 0 ? (length/2)-1 : length/2];
        }

        // mode is either element 0 or -1 since a sequence has only one instance of each value
        vector.mode = (length > 1) ? -1 : vector.elements[0];
        //vector.sum  = sum;

        return vector;
    }

    /**
     * Returns whether the number is semiprime.
     */
    // reference: https://rosettacode.org/wiki/Semiprime
    public static boolean isPQ(long number) {
        if (number < 4 || Vector.isPrimeCached(number)) {
            return false;
        }

        long n = number;
        long start = 2;
        long factors = 0;

        while ( factors < 3 && n != 1 ) {
            if ( n % start == 0 ) {
                n /= start;
                factors++;
            } else {
                start++;
            }
        }

        return (factors == 2);
    }

    /**
     * Returns new vector with elements generated from the
     * pq number sequence starting from the specified value.
     */
    public static Vector pq(int length, long start) {
        /*
            length 4, start 1  => [4 6 9 10]
            length 4, start 4  => [4 6 9 10]
            length 4, start -1 => [4 6 9 10]
            length 4, start 42 => [46 49 51 55]
        */
        Vector vector = new Vector(length);

        // check if the prime cache is initialized
        Vector.initializePQCache(start, length);

        int idx = 0;
        long sum = 0;

        for (long current = (start >= 4) ? start : 4; idx < length ; current++) {
            if (Vector.isPQCached(current)) {
                sum += vector.elements[idx] = current;
                idx++;
            }
        }

        // set these up
        vector.minimum = vector.elements[0];
        vector.maximum = vector.elements[length-1];
        vector.mode    = (length > 1) ? -1 : vector.elements[0];
        vector.median  = vector.elements[length/2];
        vector.sum     = sum;

        vector.stable = true;

        return vector;
    }

    /**
     * Returns whether the number is prime.
     */
    // cant be divisible by 2, at i^2 the factors cross over so we can sqrt number
    public static boolean isPrime(long number) {
        if (number < 2) {
            return false;
        }

        if (number == 2 || number == 3) {
            return true;
        }

        if (number % 2 == 0 || number % 3 == 0) {
            return false;
        }

        for (long i = 5; (i*i) <= number; i+=6) {
            if (number % i == 0 || number % (i+2) == 0) {
                return false;
            }
        }

        return true;
    }
    /**
     * Returns new vector with elements generated from the
     * prime number sequence starting from the specified value.
     */
    public static Vector prime(int length, long start) {
        // check if the prime cache is initialized
        Vector.initializePrimeCache(start, length);

        Vector vector = new Vector(length);

        int idx = 0;
        long sum = 0;

        for (long current = (start < 2) ? 2 : start; idx < length; current++) {
            if (Vector.isPrimeCached(current)) {
                sum += vector.elements[idx] = current;
                idx++;
            }
        }

        // set these up
        vector.minimum = vector.elements[0];
        vector.maximum = vector.elements[length-1];
        vector.mode    = (length > 1) ? -1 : vector.elements[0];
        vector.median  = vector.elements[length / 2];
        vector.sum     = sum;

        vector.stable = true;

        return vector;
    }

    /**
     * Returns whether the number is abundant.
     */
    public static boolean isAbundant(long number) {
        long sum = 1;

        // known by default
        if (number < 12) {
            return false;
        }

        boolean[] checked = new boolean[(int)(number/2)+1];

        if (number % 2 == 0) {
            for (long i = 2; i <= number / 2; i+=2) {
                if (! checked[(int) i]) {
                    sum += (number % i == 0) ? i : 0;
                    checked[(int) i] = true;
                }
            }
        }

        for (long i = 3; i <= number / 2; i+=2) {
            if (! Vector.isPrimeCached(i)) {
                continue;
            }

            for (long j = i; j <= number / 2; j += i) {
                if (checked[(int) j]) {
                    continue;
                }

                checked[(int) j] = true;
                if (number % j == 0) {
                    sum += j;
                }
            }

            if (sum > number) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns new vector with elements generated from the
     * abundant number sequence starting from the specified value.
     */
    public static Vector abundant(int length, long start) {
        /*
            length 4, start 0  => [12 18 20 24]
            length 4, start 12 => [12 18 20 24]
            length 4, start -1 => [12 18 20 24]
            length 4, start 42 => [42 48 54 56]
        */
        Vector vector = new Vector(length);

        Vector.initializeAbundantCache(start, length);

        //abc
        int idx = 0;
        long sum = 0;

        for (long current = (start < 12) ? 12 : start; idx < length ; current++) {
            if (Vector.isAbundantCached(current)) {
                sum += vector.elements[idx] = current;
                idx++;
            }
        }

        // set these up
        vector.minimum = vector.elements[0];
        vector.maximum = vector.elements[length-1];
        vector.mode    = (length > 1) ? -1 : vector.elements[0];
        vector.median  = vector.elements[length / 2];
        vector.sum     = sum;

        vector.stable = true;

        return vector;
    }

    /**
     * Returns whether the number is composite.
     */
    // composite is a positive integer that is not a prime or 1
    public static boolean isComposite(long number) {
        return ! (number <= 1 || Vector.isPrimeCached(number));
    }

    /**
     * Returns new vector with elements generated from the
     * composite number sequence starting from the specified value.
     */
    public static Vector composite(int length, long start) {
        /*
            length 4, start 0  => [4 6 8 9]
            length 4, start 4  => [4 6 8 9]
            length 4, start -1 => [4 6 8 9]
            length 4, start 42 => [42 44 45 46]
        */

        Vector.initializePrimeCache(start, length);

        Vector vector = new Vector(length);

        int idx = 0;
        long sum = 0;

        for (long current = (start < 4) ? 4 : start; idx < length ; current++) {
            if (Vector.isComposite(current)) {
                sum += vector.elements[idx] = current;
                idx++;
            }
        }

        // set these up
        vector.minimum = vector.elements[0];
        vector.maximum = vector.elements[length-1];
        vector.mode    = (length > 1) ? -1 : vector.elements[0];
        vector.median  = vector.elements[length / 2];
        vector.stable  = true;
        vector.sum     = sum;

        return vector;
    }

    // ===========================================================================
    // VECTOR OPERATIONS
    // ===========================================================================

    /**
     * Returns new vector that is a copy of the current vector.
     */
    public Vector cloned() {
        /*
            [1 1 1 1] => [1 1 1 1]
            [1 2 3 4] => [1 2 3 4]
            [4 3 2 1] => [4 3 2 1]
        */
        Vector clone = new Vector(this.length);

        System.arraycopy(this.elements, 0, clone.elements, 0, this.length);

        clone.stable = this.stable;
        clone.reversed = this.reversed;
        clone.random = this.random;
        clone.uniform = this.uniform;

        clone.sum = this.sum;
        clone.median = this.median;
        clone.mode = this.mode;
        clone.minimum = this.minimum;
        clone.maximum = this.maximum;

        return clone;
    }

    /**
     * Returns new vector with elements ordered from smallest to largest.
     */
    public Vector sorted() {
        /*

            [1 1 1 1] => [1 1 1 1]
            [1 2 3 4] => [1 2 3 4]
            [4 3 2 1] => [1 2 3 4]
        */
        // clone this instance

        if (this.stable) {
            if (this.reversed) {
                if (this.elements[0] < this.elements[length-1]) {
                    this.reversed = false;
                    return this.sorted();
                }
                return this.reversed();
            }
            return this.cloned();
        }

        if (this.random) {
            Vector vector = new Vector(this.length);

            vector.stable = true;
            vector.reversed = false;
            vector.random = true;
            vector.uniform = false;

            if ((this.minimum != null && this.minimum >= 0) &&
                    ((this.maximum != null && this.maximum <= (20*1000*1000)) ||
                            (this.maximum == null && this.getMaximum() <= 20*1000*1000))) {

                if (this.mode == null) {
                    Vector.bucketSort(this.elements, vector.elements, Math.max(this.maximum, 100),
                                      true);
                    vector.mode = Vector.lastMode;
                    Vector.lastMode = null;
                } else {
                    Vector.bucketSort(this.elements, vector.elements, Math.max(this.maximum, 100),
                                      false);
                }

                vector.sum = Vector.lastSum;
                Vector.lastSum = null;

                vector.minimum = vector.elements[0];
                vector.maximum = vector.elements[this.length - 1];
                vector.median = vector.elements[this.length / 2];

                return vector;
            }
        }

        Vector vector = this.cloned();

        if (!this.uniform) {
            // and sort the clones elements
            if (this.length > (75*1000)) {
                Arrays.parallelSort(vector.elements);
            } else {
                Arrays.sort(vector.elements);
            }
        }

        vector.stable = true;
        vector.reversed = false;

        return vector;
    }

    /**
     * Returns new vector with elements ordered in reverse.
     */
    public Vector reversed() {
        if (this.uniform) {
            return this.cloned();
        }

        Vector vector = new Vector(this.length);

        if (this.length % 2 == 1) {
            vector.elements[this.length/2] = this.elements[this.length/2];
        }

        // write forwards as we read this instance backwards
        for (int i = 0; i < this.length/2; i++) {
            vector.elements[i] = this.elements[(this.length - 1) - i];
            vector.elements[(this.length - 1) - i] = this.elements[i];
        }

        vector.reversed = !(this.reversed);
        vector.stable = this.stable;
        vector.random = this.random;
        vector.uniform = this.uniform;

        vector.sum = this.sum;
        vector.median = this.median;
        vector.mode = this.mode;
        vector.minimum = this.minimum;
        vector.maximum = this.maximum;

        return vector;
    }

    /**
     * Returns new vector with elements shifted right by a given number of positions.
     */
    public Vector shifted(long amount) {
        Vector vector = new Vector(this.length);
        /*
            [1 2 3 4] 0 => [1 2 3 4]
            [1 2 3 4] 1 => [4 1 2 3]
            [1 2 3 4] 2 => [3 4 1 2]
            [1 2 3 4] 3 => [2 3 4 1]
            [1 2 3 4] 4 => [1 2 3 4]
            [1 2 3 4] 5 => [4 1 2 3]
        */

        if (amount >= 0) {
            // select the offset element from this instance at index (i+amount) % length
            for (int i = 0; i < this.length; i++) {
                vector.elements[(i + (int) amount) % this.length] = this.elements[i];
            }
        } else {
            // fixes negative shifting
            Vector rev = this.reversed();
            Vector temp = new Vector(this.length);

            for (int i = 0; i < this.length; i++) {
                temp.elements[(i + Math.abs((int) amount)) % this.length] = rev.elements[i];
            }

            vector = temp.reversed();
        }

        vector.minimum = (this.minimum != null) ? this.minimum : null;
        vector.maximum = (this.maximum != null) ? this.maximum : null;
        vector.median  = (this.median  != null) ? this.median  : null;
        vector.sum     = (this.sum     != null) ? this.sum     : null;
        vector.mode    = (this.mode    != null) ? this.mode    : null;

        vector.uniform = this.uniform;
        vector.random  = this.random;
        vector.stable = (amount == 0) && this.stable;
        vector.reversed = (amount == 0) && this.reversed;

        return vector;
    }

    /**
     * Returns new vector, adding scalar to each element.
     */
    public Vector scalarAdd(long scalar) {
        /*
            [1 1 1 1] + 1  => [2 2 2 2]
            [1 2 3 4] + 4  => [5 6 7 8]
            [2 2 2 2] + -1 => [1 1 1 1]
        */
        Vector vector = new Vector(this.length);

        // add scalar to each element of this instance
        for (int i = 0; i < this.length; i++) {
            vector.elements[i] = this.elements[i] + scalar;
        }

        vector.minimum = (this.minimum != null) ? this.minimum + scalar : null;
        vector.maximum = (this.maximum != null) ? this.maximum + scalar : null;
        vector.median  = (this.median  != null) ? this.median  + scalar : null;
        vector.sum     = (this.sum     != null) ? this.sum + ((this.length) * scalar) : null;
        vector.mode    = (this.mode    != null) ? ((this.mode != -1) ? this.mode + scalar : -1) : null;

        vector.uniform  = this.uniform;
        vector.stable   = this.stable;
        vector.reversed = this.reversed;
        vector.random   = (vector.minimum != null && vector.minimum >= 0) && this.random;

        return vector;
    }

    /**
     * Returns new vector, multiplying scalar to each element.
     */
    public Vector scalarMultiply(long scalar) {
        /*
            [1 2 3 4] x 0  => [0 0 0 0]
            [1 2 3 4] x 1  => [1 2 3 4]
            [1 2 3 4] x 2  => [2 4 6 8]
            [1 2 3 4] x 10 => [10 20 30 40]
            [1 2 3 4] x -1 => [-1 -2 -3 -4]
        */
        Vector vector = new Vector(this.length);

        // multiply each element by scalar
        for (int i = 0; i < this.length; i++) {
            vector.elements[i] = this.elements[i] * scalar;
        }

        vector.minimum = (this.minimum != null) ? this.minimum * scalar : null;
        vector.maximum = (this.maximum != null) ? this.maximum * scalar : null;
        vector.median  = (this.median  != null) ? this.median  * scalar : null;
        vector.sum     = (this.sum     != null) ? this.sum     * scalar : null;

        if (scalar == 0) {
            vector.mode = 0L;
        } else {
            vector.mode = (this.mode    != null) ? ((this.mode != -1) ? this.mode * scalar : -1) : null;
        }

        vector.uniform  = (scalar == 0) || this.uniform;
        vector.stable   = (scalar == 0) || this.stable;
        vector.reversed = (scalar == 0) || this.reversed;
        vector.random   = (scalar == 0) || ((vector.minimum != null && vector.minimum >= 0) && this.random);

        return vector;
    }

    /**
     * Returns new vector, adding elements with the same index.
     */
    public Vector vectorAdd(Vector other) {
        /*
            [1 2 3 4] + [0 0 0 0]     => [1 2 3 4]
            [1 2 3 4] + [4 4 4 4]     => [5 6 7 8]
            [1 2 3 4] + [1 2 3 4]     => [2 4 6 8]
            [2 2 2 2] + [-1 -1 -1 -1] => [2 2 2 2]
        */

        Vector vector = new Vector(this.length);
        // add v1[index] + v2[index] expected that: v2.length == v1.length
        for (int i = 0; i < this.length; i++) {
            vector.elements[i] = this.elements[i] + other.elements[i];
        }

        if (other.uniform) {
            vector.minimum = (this.minimum != null) ? this.minimum + other.elements[0] : null;
            vector.maximum = (this.maximum != null) ? this.maximum + other.elements[0] : null;
            vector.median  = (this.median  != null) ? this.median  + other.elements[0] : null;
        }

        if (this.uniform && other.uniform) {
            vector.uniform = true;
        }

        if ((this.stable && other.stable) && ! (this.reversed || other.reversed)) {
            vector.stable = true;
            vector.minimum = vector.elements[0];
            vector.maximum = vector.elements[this.length-1];
            vector.median  = vector.elements[length/2];
        }

        if (this.length < (250*1000) && (this.random || other.random)  ||
                (vector.maximum != null && vector.minimum != null) && vector.maximum < vector.minimum) {
            vector.minimum = null;
            vector.maximum = null;
            getMinimum();

            vector.sum = null;
            vector.median = null;
            vector.mode = null;
        }

        if ((this.reversed && other.reversed) ||
                (this.reversed && other.uniform ) ||
                (this.uniform  && other.reversed)) {
            vector.reversed = true;
        }

        if ((this.random || other.random) && ((vector.minimum != null && vector.minimum >= 0) &&
                (vector.maximum != null && vector.maximum <= 20*1000*1000))) {
            vector.random = true;
        }


        return vector;
    }

    /**
     * Returns new vector, multiplying elements with the same index.
     */
    public Vector vectorMultiply(Vector other) {
        /*
            [1 2 3 4] x [0 0 0 0]     => [0 0 0 0]
            [1 2 3 4] x [1 1 1 1]     => [1 2 3 4]
            [1 2 3 4] x [1 2 3 4]     => [1 4 9 16]
            [2 2 2 2] x [-1 -1 -1 -1] => [-2 -2 -2 -2]
        */

        Vector vector = new Vector(this.length);

        // multiply v1[index] * v2[index] expected that: v2.length == v1.length

        for (int i = 0; i < this.length; i++) {
            vector.elements[i] = this.elements[i] * other.elements[i];
        }


        if ((this.reversed && other.reversed) ||
                (this.reversed && other.uniform && other.elements[0] > 0) ||
                (this.uniform  && other.reversed && this.elements[0] > 0)) {
            vector.reversed = true;
        }

        if (this.uniform || other.uniform) {
            if (this.uniform && other.uniform) {
                vector.uniform = true;
            }

            if ((this.uniform && this.elements[0] == 0) || (other.uniform && other.elements[0] == 0)) {
                vector.mode = 0L;
                vector.uniform = true;
            } else if (this.uniform && this.minimum < 0 && other.stable && other.minimum >= 0) {
                vector.reversed = true;
            } else if (other.uniform && other.minimum < 0 && this.stable && this.minimum >= 0) {
                vector.reversed = true;
            }

            // oops caught a non-reversed reverse flagged corner case
            if (vector.reversed && vector.minimum != null && vector.maximum != null &&
                    vector.minimum == vector.elements[0] && vector.maximum == vector.elements[length-1]) {
                vector.reversed = !(vector.reversed);
            }

            if ((other.uniform || this.uniform) && !vector.uniform) {
                vector.minimum = (this.minimum != null && other.minimum != null) ? (other.uniform ?
                        this.minimum * other.elements[0] : other.minimum * this.elements[0]) : null;
                vector.maximum = (this.maximum != null && other.minimum != null) ? (other.uniform ?
                        this.maximum * other.elements[0] : other.maximum * this.elements[0]) : null;

                if (vector.reversed && vector.maximum < vector.minimum) {
                    if (other.uniform && other.minimum < 0) {
                        vector.minimum = this.maximum * other.elements[0];
                        vector.maximum = this.minimum * other.elements[0];
                        vector.stable = true;

                    } else if (this.uniform && this.minimum < 0) {
                        vector.minimum = other.maximum * this.elements[0];
                        vector.maximum = other.minimum * this.elements[0];
                        vector.stable = true;
                    }
                }
                if (vector.stable) {
                    if (vector.reversed) {
                        vector.median = vector.elements[length % 2 == 0 ? length / 2 - 1 : length / 2];
                    } else {
                        vector.median = vector.elements[length / 2];
                    }
                } else {
                    if (this.median != null && other.median != null) {
                        if ((this.median >= 0 && other.elements[0] > 0 && other.uniform) ||
                                (other.median >= 0 && this.elements[0] > 0 && this.uniform)) {
                            vector.median = (other.uniform) ? this.median * other.elements[0] : other.median * this.elements[0];
                        } else {
                            vector.median = null;
                        }
                    }
                }
            }
        }

        if (vector.reversed && (vector.minimum != null && vector.maximum != null) &&
                (vector.minimum == vector.elements[length-1] && vector.maximum == vector.elements[0] &&
                        vector.minimum > vector.maximum)) {
            vector.reversed = false;

            Long m = vector.minimum;
            vector.minimum = null;
            vector.minimum = vector.maximum;

            vector.maximum = null;
            vector.maximum = m;

            if (vector.minimum >= 0 && vector.maximum > vector.minimum) {
                vector.random = true;
                vector.stable = true;
            }

            return vector;
        }

        if ((this.stable && other.stable) && (!this.reversed && !other.reversed)) {
            vector.median  = vector.elements[length/2];
            vector.minimum = vector.elements[0];
            vector.maximum = vector.elements[this.length-1];

            if (vector.minimum < vector.maximum) {
                vector.stable = true;
                vector.reversed = false;
            } else {
                vector.median = null;
                vector.reversed = false;
                vector.stable = false;
                vector.minimum = null;
                vector.maximum = null;
                if ( this.length < 50000) {
                    this.getMinimum();
                    this.getMaximum();
                }
            }
        }

        if ((this.random || other.random) &&
                ((this.minimum != null && this.minimum >= 0) && (other.minimum != null && other.minimum >= 0) &&
                        (this.maximum != null && other.maximum != null && (this.maximum * other.maximum) <= (10*1000*1000)))) {

            vector.random = true;
        }

        vector.mode = null;

        return vector;
    }

    // ===========================================================================
    // VECTOR COMPUTATIONS
    // ===========================================================================

    /**
     * Returns the sum of all elements.
     */
    public Long getSum() {
        /*
            [0 0 0 0] => 0
            [1 1 1 1] => 4
            [1 2 3 4] => 10
        */

        // already cached it
        if (this.sum != null) {
            return this.sum;
        }

        if (this.length == 1) {
            this.sum = this.elements[0];

            return this.sum;
        }

        this.sum = 0L;

        // cost of creating threads plus summing is less than summing single threaded about here
        long minParallel = 100*1000;

        this.sum = (this.length < minParallel) ? SumHelper.getSum(this.elements) :
                SumHelper.getParallelSum(this.elements);

        return this.sum;
    }

    public Long getMode() {
        if (this.minimum != null && this.maximum != null && (this.reversed || this.stable)) {

            if (this.minimum > this.maximum && this.maximum == this.elements[0] && this.minimum == this.elements[length-1]) {
                Long tmp = this.minimum;
                this.minimum = null;
                Long min2 = this.getMinimum();

                if (min2 == this.maximum && tmp > min2) {
                    this.maximum = tmp;
                    this.minimum = min2;
                }

                this.mode = null;

                if (this.minimum >= 0) {
                    this.random = true;
                }
            }
        }

        // already cached it
        if (this.mode != null) {
            return this.mode;
        }

        // we already know the mode, so shortcut
        if (this.length == 1) {
            this.mode = this.elements[0];
            return this.mode;
        }

        // shortcut once more
        if (this.length == 2) {
            if (this.elements[0] == this.elements[1]) {
                this.mode = this.elements[0];
            } else {
                this.mode = (long)-1;
            }

            return this.mode;
        }

        if (this.random && this.minimum == null) {
            if (this.getMinimum() < 0) {
                this.random = false;
                this.mode = null;
                this.maximum = null;
                this.median = null;
                return getMode();
            }
        }

        // random can only be 0-100, dohoho, 24x faster than a hashmap for 10m elements!
        if ((this.random && this.minimum >= 0) || (this.minimum == null && this.getMinimum() >= 0)) {
            if (this.maximum != null && this.stable && this.maximum != this.elements[length-1]) {
                this.maximum = null;
            }
            this.getMaximum();
            int upper = (int)Math.max(this.maximum,1000);

            if (upper >= 20*1000*1000) {
                this.random = false;
                this.mode = null;
                getMode();
            }

            long[] freq = new long[(int)upper+1];
            long maxCount = 0;
            long currentMode = -1L;

            // count the frequency
            for (long l = 0; l < this.length; l++) {
                freq[(int) this.elements[(int) l]]++;
            }

            for ( int i = 0; i <= upper; i++ ) {
                if (maxCount <= freq[i]) {
                    if (freq[i] != maxCount) {
                        currentMode = i;
                        maxCount = freq[i];
                    } else {
                        currentMode = -1L;
                    }
                }
            }

            this.mode = currentMode;

            return this.mode;
        }

        // do it the long way with a frequency map
        Map<Long,Integer> fm = new HashMap<>();

        int maxCount = 0;
        long currentMode = 0;

        // create a hash map for each unique value and store their frequency in it (memory is cheap!)
        for ( int i = 0; i < this.length; i++ ) {
            long l = this.elements[i];
            fm.put( l, fm.get(l) == null ? 1 : fm.get(l)+1 );
            Integer tmp = fm.get(l);

            if (maxCount <= tmp) {
                if (maxCount != tmp) {
                    currentMode = l;
                    maxCount = tmp;
                } else {
                    currentMode = -1L;
                }
            }
        }

        this.mode = currentMode;

        return this.mode;
    }


    /**
     * Returns the upper median.
     */
    public Long getMedian() {
        /*
            [1] => 1
            [1 2] => 2
            [1 2 3] => 2
            [1 1 1 1] => 1
        */
        // already cached it
        if (this.median != null) {
            return this.median;
        }

        if (length == 1) {
            this.median = this.elements[0];
            return this.median;
        }

        Vector vector;

        if (!this.stable) {
            // get a sorted clone
            vector = this.sorted();
        } else {
            vector = this;
        }

        // get the upper median, which is index length/2 for even, and length/2+1 for odd
        if (this.reversed) {
            this.median  = vector.elements[length % 2 == 0 ? (length/2)-1 : length/2];
        } else {
            this.median = vector.elements[length / 2];
        }

        return this.median;
    }

    /**
     * Returns the smallest value in the vector.
     */
    public Long getMinimum() {
        /*
            [1 1 1 1] => 1
            [1 2 3 4] => 1
            [4 3 2 1] => 1
        */
        // cached
        if (this.minimum != null) {
            return this.minimum;
        }

        if (length == 1) {
            this.minimum = this.elements[0];
            return this.minimum;
        }

        // a bit faster than naive approach of sorting and picking lowest element
        this.minimum = LongStream.of(this.elements).parallel().min().getAsLong();

        return this.minimum;
    }

    /**
     * Returns the largest value in the vector.
     */
    public Long getMaximum() {
        /*
            [1 1 1 1] => 1
            [1 2 3 4] => 4
            [4 3 2 1] => 4
        */

        // cached
        if (this.maximum != null) {
            return this.maximum;
        }

        if (length == 1) {
            this.maximum = this.elements[0];
            return this.maximum;
        }

        // a bit faster than naive approach of sorting and picking highest element
        this.maximum = LongStream.of(this.elements).parallel().max().getAsLong();

        return this.maximum;
    }

    /**
     * Returns the frequency of the value in the vector.
     */
    public long getFrequency(long value) {
        /*
            [1 2 3 4] 0 => 0
            [1 2 3 4] 1 => 1
            [1 1 1 1] 1 => 4
        */

        // cost of creating threads plus counting is less than counting single threaded about here
        long minParallel = 1400*1000;

        return (this.length < minParallel) ? FrequencyHelper.getFrequency(this.elements, value) :
                FrequencyHelper.getParallelFrequency(this.elements, value);
    }

    // ===========================================================================
    // DISPLAY OPERATIONS
    // ===========================================================================

    /**
     * Displays the vector.
     */
    public void display() {
        // 100k elements - 1.6s for printing each, avg 0.45s for printing one string
        StringBuilder sb = new StringBuilder();

        for ( int i = 0; i < this.length; i++ ) {
            sb.append(String.format("%d ", this.elements[i]));
        }

        // trim tailing space
        sb.setLength(sb.length()-1);
        System.out.println(sb.toString());
    }

    /**
     * Displays the element at the specified index.
     */
    public void displayElement(int index) {

        System.out.printf( "%d\n", this.elements[index] );
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

// reference: http://eddmann.com/posts/parallel-summation-in-java/
// helper class for threading sums
class SumHelper extends Thread {
    private long[] elements;
    private long sumPart;
    private int start, end;

    public SumHelper(long[] elements, int start, int end) {
        this.elements = elements;
        this.start = start;
        this.end = (end > elements.length) ? elements.length : end;
    }

    public long getPartialSum() {
        return sumPart;
    }

    public void run() {
        sumPart = SumHelper.getSum(this.elements, this.start, this.end);
    }

    public static long getSum(long[] elements) {
        return getSum(elements, 0, elements.length);
    }

    public static long getSum(long[] elements, int start, int end) {
        long sum = 0;

        for (int i = start; i < end; i++) {
            sum += elements[i];
        }

        return sum;
    }

    public static long getParallelSum(long[] elements) {
        return SumHelper.getParallelSum(elements, Vector.rtThreadCount);
    }

    public static long getParallelSum(long[] elements, int threadCount) {
        int length = (int) Math.ceil((double)elements.length / threadCount);

        SumHelper[] sumThreads = new SumHelper[threadCount];

        for (int i = 0; i < threadCount; i++) {
            sumThreads[i] = new SumHelper(elements, i * length, (i+1) * length);
            sumThreads[i].start();
        }

        try {
            for (SumHelper thr : sumThreads) {
                thr.join();
            }
        } catch (InterruptedException e) {
            //System.out.printf("caught an InterruptedException\n");
        }

        long sum = 0;

        for (SumHelper thr : sumThreads) {
            sum += thr.getPartialSum();
        }

        return sum;
    }
}


// single threaded frequency becomes slower at around 3m elements
class FrequencyHelper extends Thread {
    private long[] elements;
    private long countPart;
    private long value;
    private int start, end;

    public FrequencyHelper(long[] elements, long value, int start, int end) {
        this.elements = elements;
        this.start = start;
        this.end = (end > elements.length) ? elements.length : end;
        this.value = value;
    }

    public long getPartialFrequency() {
        return countPart;
    }

    public void run() {
        countPart = FrequencyHelper.getFrequency(this.elements, this.value, this.start, this.end);
    }

    public static long getFrequency(long[] elements, long value) {
        return getFrequency(elements, value, 0, elements.length);
    }

    public static long getFrequency(long[] elements, long value, int start, int end) {
        long count = 0;

        for (int i = start; i < end; i++) {
            count = (elements[i] == value) ? count + 1 : count;
        }

        return count;
    }

    public static long getParallelFrequency(long[] elements, long value) {
        return FrequencyHelper.getParallelFrequency(elements, value, Vector.rtThreadCount);
    }

    public static long getParallelFrequency(long[] elements, long value, int threadCount) {
        int length = (int) Math.ceil((double)elements.length / threadCount);

        FrequencyHelper[] freqThreads = new FrequencyHelper[threadCount];

        for (int i = 0; i < threadCount; i++) {
            freqThreads[i] = new FrequencyHelper(elements, value, i * length, (i+1) * length);
            freqThreads[i].start();
        }

        try {
            for (FrequencyHelper thr : freqThreads) {
                thr.join();
            }
        } catch (InterruptedException e) {
            //System.out.printf("caught an InterruptedException\n");
        }

        long count = 0;

        for (FrequencyHelper thr : freqThreads) {
            count += thr.getPartialFrequency();
        }

        return count;
    }
}
