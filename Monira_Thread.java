
import java.util.concurrent.atomic.AtomicLong;

public class Monira_Thread extends Thread {

    private static final AtomicLong safeStaticCounter =
            new AtomicLong(0);

    private static long unsafeStaticCounter = 0;

    private long instanceCounter = 0;

    private final int increments;
    private final boolean threadSafe;

    public Monira_Thread(int increments,
                         boolean threadSafe,
                         String threadName) {
        super(threadName);
        this.increments = increments;
        this.threadSafe = threadSafe;
    }

    @Override
    public void run() {
        for (int i = 0; i < increments; i++) {

            if (threadSafe) {
                safeStaticCounter.incrementAndGet();
            } else {
                unsafeStaticCounter++;
            }

            instanceCounter++;
        }
    }

    public long getInstanceCounter() {
        return instanceCounter;
    }

    public static void main(String[] args)
            throws InterruptedException {

        if (args.length < 3) {
            System.out.println(
                "Usage: java Monira_Thread <threads> <increments> <true|false>"
            );
            return;
        }

        int numberOfThreads =
                Integer.parseInt(args[0]);

        int incrementsPerThread =
                Integer.parseInt(args[1]);

        boolean threadSafe =
                Boolean.parseBoolean(args[2]);

        if (numberOfThreads <= 0 ||
                incrementsPerThread < 0) {
            System.out.println(
                "Threads must be > 0 and increments >= 0."
            );
            return;
        }

        safeStaticCounter.set(0);
        unsafeStaticCounter = 0;

        Monira_Thread[] tasks =
                new Monira_Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            tasks[i] = new Monira_Thread(
                incrementsPerThread,
                threadSafe,
                "Worker-" + (i + 1)
            );
        }

        for (Monira_Thread task : tasks) {
            task.start();
        }

        for (Monira_Thread task : tasks) {
            task.join();
        }

        long nonStaticTotal = 0;

        for (Monira_Thread task : tasks) {
            nonStaticTotal += task.getInstanceCounter();
        }

        long staticCount;

        if (threadSafe) {
            staticCount = safeStaticCounter.get();
        } else {
            staticCount = unsafeStaticCounter;
        }

        long expectedCount =
                (long) numberOfThreads * incrementsPerThread;

        long absoluteDifference =
                Math.abs(nonStaticTotal - staticCount);

        double percentageDifference;

        if (nonStaticTotal == 0) {
            percentageDifference =
                    (staticCount == 0) ? 0.0 : Double.NaN;
        } else {
            percentageDifference =
                    absoluteDifference * 100.0 / nonStaticTotal;
        }

        System.out.println("Mode: " +
                (threadSafe
                ? "THREAD-SAFE (AtomicLong)"
                : "UNSYNCHRONIZED"));

        System.out.println(
                "Number of threads: " + numberOfThreads);

        System.out.println(
                "Increments per thread: " + incrementsPerThread);

        System.out.println(
                "Expected count: " + expectedCount);

        System.out.println(
                "Static count: " + staticCount);

        System.out.println(
                "Non-static total: " + nonStaticTotal);

        System.out.println(
                "Absolute difference: " + absoluteDifference);

        if (Double.isNaN(percentageDifference)) {
            System.out.println(
                    "Percentage difference: undefined");
        } else {
            System.out.printf(
                    "Percentage difference: %.4f%%%n",
                    percentageDifference);
        }
    }
}
