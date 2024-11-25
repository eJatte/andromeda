package andromeda.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {
    public static String readFile(String path) {
        StringBuilder builder = new StringBuilder();

        try (var inputStream = new FileInputStream(path)) {
            var reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file!" + System.lineSeparator() + path);
        }

        return builder.toString();
    }
}
