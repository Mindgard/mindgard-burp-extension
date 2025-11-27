package ai.mindgard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.List.of;

public interface MindgardSettings {
    String selector();

    String projectID();

    String dataset();

    String systemPrompt();

    String customDatasetFilename();

    String exclude();

    String include();

    Integer promptRepeats();

    Integer parallelism();

    record Settings(String selector, String projectID, String dataset, String systemPrompt, String customDatasetFilename, String exclude, String include, Integer promptRepeats, Integer parallelism) implements MindgardSettings {}

    static File file(String name) {
        String directory = System.getProperty("user.home") + File.separator + ".mindgard";
        new File(directory).mkdirs();
        return new File(directory + File.separator + name);
    }

    /**
     * Save the user's extension settings
     */
    default boolean save(String settingsFileName) {
        // Validate project ID before saving
        boolean valid = true;
        Exception validationException = null;
        try {
            ai.mindgard.sandbox.wsapi.SandboxConnectionFactory validator = new ai.mindgard.sandbox.wsapi.SandboxConnectionFactory();
            valid = validator.validateProject(projectID());
        } catch (Exception e) {
            valid = false;
            validationException = e;
        }
        if (!valid) {
            String message = (validationException == null)
                ? "Project ID is invalid. Please go to " + Constants.FRONTEND_URL + "/results to create a new project or find the ID of an existing project."
                : "There was a problem validating the project ID: " + validationException.getMessage();
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(null,
                    message,
                    "Mindgard Settings Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );
            });
            return false;
        }

        try {
            Files.write(MindgardSettings.file(settingsFileName).toPath(), of(JSON.json(new Settings(selector(), projectID(), dataset(), systemPrompt(), customDatasetFilename(), exclude(), include(), promptRepeats(), parallelism()))));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
