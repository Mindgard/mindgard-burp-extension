package ai.mindgard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.util.List.of;
import org.json.JSONObject;
import java.util.logging.Logger;

public record MindgardSettings(
    String selector, 
    String projectID, 
    String dataset, 
    String systemPrompt, 
    String customDatasetFilename, 
    String exclude, 
    String include, 
    Integer promptRepeats, 
    Integer parallelism
) {
    /**
     * loadSettingsFile
     * @param name the file name to be created
     * @return a new file in .mindgard with passed name in the user's home directory.
     */
    public static File loadFile(String name) {
        String directory = System.getProperty("user.home") + File.separator + ".mindgard";
        new File(directory).mkdirs();
        return new File(directory + File.separator + name);
    }

    /**
     * loadOrCreate
     * Loads the settings from file, or creates a new settings record with default values if the file doesn't exist.
     * @param filename the name of the settings file - typically "burp.json"
     * @return a settings record containing the contents of the settings file, or defaults if it doesn't exist
     */
    public static MindgardSettings loadOrCreate(String filename) {
        File settingsFile = MindgardSettings.loadFile(Constants.SETTINGS_FILE_NAME);
        String fileContents;
        if (settingsFile.exists()) {
            try {
                fileContents = Files.readString(settingsFile.toPath());
                JSONObject json = new JSONObject(fileContents);
                
                //Populates with a default value if key doesn't exist.
                String selector = json.optString("selector", "");
                String projectID = json.optString("projectID", "");
                String dataset = json.optString("dataset", "");
                String systemPrompt = json.optString("systemPrompt", "");
                String customDatasetFilename = json.optString("customDatasetFilename", "");
                String exclude = json.optString("exclude", "");
                String include = json.optString("include", "");
                Integer promptRepeats = json.has("promptRepeats") ? json.optInt("promptRepeats") : 1;
                Integer parallelism = json.has("parallelism") ? json.optInt("parallelism") : 1;
            
                return new MindgardSettings(
                    selector,
                    projectID,
                    dataset,
                    systemPrompt,
                    customDatasetFilename,
                    exclude,
                    include,
                    promptRepeats,
                    parallelism
                );
            } catch (IOException e) {
                Logger logger = Logger.getLogger(MindgardSettings.class.getName());
                logger.severe("Error reading settings file: " + e.getMessage());
                
            }
        }
        //Return defaults if file doesn't exist
         return new MindgardSettings(
                    "", "", "", "", "", "", "", 1, 1
        );
    }


    /**
     * Saves the settings to file.
     * @param settingsFileName the filename of the settings file, typically "burp.json" but this is a constant
     * @return true is file write was succcessful.
     */
    public boolean save(String settingsFileName) {
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
            Files.write(MindgardSettings.loadFile(settingsFileName).toPath(), of(JSON.json(this)));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
