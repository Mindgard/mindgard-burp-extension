package ai.mindgard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.util.List.of;
import org.json.JSONObject;
import java.util.logging.Logger;

public record MindgardToken(
    String url,
    String token
) {
    /**
     * Mindgard Token Record
     */
    public MindgardToken {}

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
    public static MindgardToken loadOrCreate() {
        File settingsFile = MindgardToken.loadFile(Constants.TOKEN_FILE_NAME);
        String fileContents;
        if (settingsFile.exists()) {
            try {
                fileContents = Files.readString(settingsFile.toPath());
                JSONObject json = new JSONObject(fileContents);
                
                //Populates with a default value if key doesn't exist.
                String url = json.optString("url", "");
                String token = json.optString("token", "");
            
                return new MindgardToken(
                    url,
                    token
                );
            } catch (IOException e) {
                Logger logger = Logger.getLogger(MindgardToken.class.getName());
                logger.severe("Error reading token file: " + e.getMessage());
                
            }
        }
        //Return defaults if file doesn't exist
         return new MindgardToken("", "");
    }


    /**
     * Saves the settings to file.
     * @param settingsFileName the filename of the settings file, typically "burp.json" but this is a constant
     * @return true is file write was succcessful.
     */
    public boolean save() {
        try {
            Files.write(MindgardSettings.loadFile(Constants.TOKEN_FILE_NAME).toPath(), of(JSON.json(this)));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
