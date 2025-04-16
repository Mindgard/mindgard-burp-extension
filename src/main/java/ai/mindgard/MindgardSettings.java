package ai.mindgard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.List.of;

public interface MindgardSettings {
    String selector();

    String testName();

    String dataset();

    String systemPrompt();

    String customDatasetFilename();

    String exclude();

    String include();

    Integer promptRepeats();

    record Settings(String selector, String testName, String dataset, String systemPrompt, String customDatasetFilename, String exclude, String include, Integer promptRepeats) implements MindgardSettings {}

    static File file(String name) {
        String directory = System.getProperty("user.home") + File.separator + ".mindgard";
        new File(directory).mkdirs();
        return new File(directory + File.separator + name);
    }

    default void save() {
        try {
            Files.write(MindgardSettings.file("burp.json").toPath(), of(JSON.json(new Settings(selector(), testName(), dataset(), systemPrompt(), customDatasetFilename(), exclude(), include(), promptRepeats()))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
