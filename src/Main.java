import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Ensure that Java compiler also known as javac of version 22.0.1 and above is installed and added to environment variables
 * <P>To run code, navigate to the main.java directory path in cmd just like <b>cd C:\IntelliJ IDEA\Projects\File Path Calculator\src</b></P>
 * <p>Now, let's assume that you're in the directory. Then, run this command: javac Main.java</p>
 * <p>This will compile it to a .class file whereby it'll generate the Main.class file if not exists in same directory in which Main.java file is stored</p>
 * <p>Lastly, run: java Main</p>
 * */
public class Main {
    private volatile static ExecutorService executorService;

    public static void main(String[] args) {
        if (executorService == null) executorService = Executors.newFixedThreadPool(Math.max(2, getFreeMemorySize()));
        // Enter the absolute paths
        var file1 = new File("C:\\IntelliJ IDEA\\Projects\\File Path Calculator\\files\\file1.txt");
        var file2 = new File("C:\\IntelliJ IDEA\\Projects\\File Path Calculator\\files\\file2.txt");
        var totalLength = saveFilePathsLength(file1, file2);
        // Let's free up our JVM memory
        gc();
        System.out.println("Total path length => " + totalLength);
    }

    /**
     * @param files takes in many file parameters
     */
    private static int saveFilePathsLength(File... files) {
        // No file(s) was present
        if (files == null || files.length == 0) return 0;
        Future<Integer> finalPathLengthFuture = executorService.submit(() -> {
            var finalPathLength = 0;
            for (var file : files) {
                try {
                    if (file == null || !file.exists()) continue;
                    // We want only files...
                    if (file.isDirectory() || !file.isFile()) continue;
                    // Below code retrieves the length of the file complete/absolute path.
                    finalPathLength += file.getAbsolutePath().trim().length();
                } catch (SecurityException ignored) {
                    // We do nothing here...
                    continue;
                }
            }
            return finalPathLength;
        });
        try {
            return !finalPathLengthFuture.isDone() ? finalPathLengthFuture.get(2, TimeUnit.MINUTES) : finalPathLengthFuture.get();
        } catch (Exception ex) {
            return 0;
        }
    }

    private static int getFreeMemorySize() {
        return Runtime.getRuntime().availableProcessors();
    }

    private synchronized static void gc() {
        // Close all background processes
        if (executorService != null) {
            if (!executorService.isShutdown() || !executorService.isTerminated()) {
                // Forcefully shut it down...
                executorService.shutdownNow();
            }
            executorService = null;
        }
        Runtime.getRuntime().gc();
        System.gc();
    }
}