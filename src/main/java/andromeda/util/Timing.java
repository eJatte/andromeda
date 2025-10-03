package andromeda.util;

public class Timing {

    private long start, end;
    private float elapsed_ms = 0;

    public void start() {
        start = System.nanoTime();
    }

    public void end() {
        end = System.nanoTime();
        float elapsed = end - start;
        elapsed_ms += elapsed / 1e6f;
    }

    public void print(String text) {
        System.out.printf("%s took %fms%n", text, elapsed_ms);
    }
}
