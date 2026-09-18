
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

/**
 * Educational exhaustive string-combination generator.
 *
 * The input string remains one contiguous block. The remaining positions are
 * filled exhaustively from a configured character alphabet. Results are
 * streamed directly to disk instead of being stored in memory.
 */
public final class Main {

    // Full character alphabet used for general string-combination generation.
    private static final char[] TEST_ALPHABET =
            "abcdefghijklmnopqrstuvwxyz".concat(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ").concat(
            "0123456789").concat(
            "!#$@%").toCharArray();
    private static final Path OUTPUT_DIR = Paths.get("output");

    private Main() {}

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("====================================");
            System.out.println("   STRING COMBINATION GENERATOR");
            System.out.println("====================================");

            System.out.print("Enter base string: ");
            String base = scanner.nextLine();
            if (base.isEmpty()) {
                System.out.println("Base string cannot be empty.");
                return;
            }

            System.out.print("Enter total length: ");
            int totalLength;
            try {
                totalLength = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Length must be an integer.");
                return;
            }

            if (totalLength < base.length()) {
                System.out.println("Total length cannot be smaller than the base string length.");
                return;
            }

            int free = totalLength - base.length();
            int placements = free + 1; // contiguous base block can start here
            BigInteger perPlacement = BigInteger.valueOf(TEST_ALPHABET.length).pow(free);
            BigInteger rawTotal = perPlacement.multiply(BigInteger.valueOf(placements));

            System.out.println();
            System.out.println("Base length: " + base.length());
            System.out.println("Free positions: " + free);
            System.out.println("Test alphabet: " + new String(TEST_ALPHABET));
            System.out.println("Alphabet size: " + TEST_ALPHABET.length);
            System.out.println("Contiguous placements: " + placements);
            System.out.println("Combinations to generate: " + rawTotal.toString());
            System.out.println();

            Files.createDirectories(OUTPUT_DIR);
            String safeName = base.replaceAll("[^a-zA-Z0-9_-]", "_");
            if (safeName.isBlank()) safeName = "input";
            Path output = OUTPUT_DIR.resolve(safeName + "_combinations.txt");

            System.out.println("Generating...");
            Instant start = Instant.now();
            BigInteger written = generate(base, totalLength, output, rawTotal);
            Duration elapsed = Duration.between(start, Instant.now());

            System.out.println();
            System.out.println("Generation completed.");
            System.out.println("Generated: " + written);
            System.out.println("Output: " + output);
            System.out.printf("Time: %.3f seconds%n", elapsed.toMillis() / 1000.0);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    private static BigInteger generate(String base, int totalLength, Path output,
                                       BigInteger total) throws IOException {
        int free = totalLength - base.length();
        BigInteger written = BigInteger.ZERO;
        final int barWidth = 40;
        final long refreshNanos = 500_000_000L; // refresh twice per second
        final long startedNanos = System.nanoTime();
        long nextRefresh = startedNanos;

        try (BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            // Each start position keeps the input string contiguous.
            for (int baseStart = 0; baseStart <= free; baseStart++) {
                int[] digits = new int[free];
                boolean done = false;

                while (!done) {
                    String value = buildValue(base, totalLength, baseStart, digits);
                    writer.write(value);
                    writer.newLine();
                    written = written.add(BigInteger.ONE);

                    long now = System.nanoTime();
                    if (now >= nextRefresh || written.equals(total)) {
                        printProgressDisplay(written, total, barWidth, startedNanos, now);
                        nextRefresh = now + refreshNanos;
                    }

                    done = !increment(digits);
                }
            }
        }

        printProgressDisplay(written, total, barWidth, startedNanos, System.nanoTime());
        System.out.println();
        return written;
    }

    private static void printProgressDisplay(BigInteger written, BigInteger total, int width,
                                             long startedNanos, long nowNanos) {
        if (total.signum() <= 0) return;

        BigInteger scaled = written.multiply(BigInteger.valueOf(10_000)).divide(total);
        int hundredths = scaled.min(BigInteger.valueOf(10_000)).intValue();
        double percent = hundredths / 100.0;
        int filled = (int) Math.min(width, (long) hundredths * width / 10_000L);

        StringBuilder bar = new StringBuilder(width);
        for (int i = 0; i < width; i++) {
            bar.append(i < filled ? '\u2588' : '\u2591');
        }

        double elapsedSeconds = Math.max((nowNanos - startedNanos) / 1_000_000_000.0, 0.001);
        double speed = written.doubleValue() / elapsedSeconds;
        double remaining = Math.max(0.0, total.subtract(written).doubleValue());
        long etaSeconds = speed > 0.0 ? (long) Math.ceil(remaining / speed) : 0L;
        long elapsedWholeSeconds = (long) elapsedSeconds;

        // ANSI: save cursor, rewrite the same 5-line progress area, then restore cursor.
        // [2K clears the whole current line so shorter updates do not leave old text behind.
        System.out.print("\u001B[s");
        System.out.print("\u001B[2K\r[" + bar + "] " + String.format(java.util.Locale.ROOT, "%6.2f%%", percent) + "\n");
        System.out.print("\u001B[2KGenerated: " + String.format("%,d", written) + " / " + String.format("%,d", total) + "\n");
        System.out.print("\u001B[2KSpeed: " + String.format("%,.0f", speed) + " strings/sec\n");
        System.out.print("\u001B[2KElapsed: " + formatDuration(elapsedWholeSeconds) + "\n");
        System.out.print("\u001B[2KETA: " + formatDuration(etaSeconds));
        System.out.print("\u001B[u");
        System.out.flush();
    }

    private static String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86_400;
        long hours = (totalSeconds % 86_400) / 3_600;
        long minutes = (totalSeconds % 3_600) / 60;
        long seconds = totalSeconds % 60;
        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String buildValue(String base, int totalLength, int baseStart, int[] digits) {
        StringBuilder sb = new StringBuilder(totalLength);
        int generatedIndex = 0;

        for (int pos = 0; pos < totalLength; ) {
            if (pos == baseStart) {
                sb.append(base);
                pos += base.length();
            } else {
                sb.append(TEST_ALPHABET[digits[generatedIndex++]]);
                pos++;
            }
        }
        return sb.toString();
    }

    // Base-N counter over TEST_ALPHABET. Returns false after the final value.
    private static boolean increment(int[] digits) {
        if (digits.length == 0) return false;
        for (int i = digits.length - 1; i >= 0; i--) {
            if (digits[i] + 1 < TEST_ALPHABET.length) {
                digits[i]++;
                return true;
            }
            digits[i] = 0;
        }
        return false;
    }
}
