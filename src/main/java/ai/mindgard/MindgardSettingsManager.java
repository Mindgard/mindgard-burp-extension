package ai.mindgard;

import ai.mindgard.sandbox.wsapi.SandboxConnectionFactory;

public class MindgardSettingsManager {
    private MindgardSettings mindgardSettings;
    private MindgardToken mindgardToken;

    /**
     * When created, loads the current settings file.
     */
    public MindgardSettingsManager() {
        this.mindgardSettings = MindgardSettings.loadOrCreate();
        this.mindgardToken = MindgardToken.loadOrCreate();
    }

    /**
     * Saves new settings contents to the settings file.
     * Writes first, as validation is part of writing before local state is updated
     * 
     * @param newSettings the new settings
     */
    public void setSettings(MindgardSettings newSettings) {
        validateSettings(newSettings);
        newSettings.save(Constants.SETTINGS_FILE_NAME);
        this.mindgardSettings = newSettings;
    }

    /**
     * Gets the settings from disk
     * 
     * @return the populated MindgardSettings record
     */
    public MindgardSettings getSettings() {
        return this.mindgardSettings;
    }

    public void setToken(String token) {
        this.mindgardToken = new MindgardToken(
                mindgardSettings.url(),
                token
        );
        this.mindgardToken.save();
    }

    /**
     * Gets the mindgard token
     *
     * @return the mindgard token
     */
    public MindgardToken getToken() {
        return this.mindgardToken;
    }

    /**
     * Determines whether the current state of the settings indicates a valid login
     *
     * @return whether or not the current state should be deemed "logged in"
     */
    public Boolean validLogin() {
        if (mindgardToken.token().equals("")) {
            return false;
        }

        return mindgardToken.url().equals(mindgardSettings.url());
    }

    /**
     * Validates settings before saving
     * Throws error message if project ID doesn't exist in the user's tenancy.
     * 
     * @param newSettings the new settings to be validated
     */
    private void validateSettings(MindgardSettings newSettings) {
        // Validate project ID before saving
        try {
            SandboxConnectionFactory validator = new SandboxConnectionFactory();
            validator.validateProject(newSettings.projectID(), newSettings.getAPIUrl(), this);
        } catch (Exception e) {
            String message = "Project ID is invalid. Please go to " + newSettings.url()
                    + "/results to create a new project or find the ID of an existing project.";
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(null,
                        message,
                        "Mindgard Settings Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}
