package ai.mindgard;

import java.awt.*;
import javax.swing.*;

import ai.mindgard.DeviceCodeFlow.DeviceCodeData;

public class LoginTab extends JPanel {

    private int loginRow = 0;
    public JPanel loginPanel;
    public JTextField urlField;
    public JTextField audienceField;
    public JTextField clientIDField;
    public JPanel loginButtonPanel;
    private JLabel loginStatusLabel;
    private Log logger;

    public LoginTab(MindgardSettingsManager mgsm, MindgardSettingsUI ui, Log logger) {
        this.logger = logger;

        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints loginGBC = new GridBagConstraints();
        loginGBC.gridx = 0;
        loginGBC.gridy = 0;
        loginGBC.weightx = 1.0;
        loginGBC.weighty = 0.0;
        loginGBC.insets = new Insets(0, 0, 10, 5);
        loginGBC.fill = GridBagConstraints.HORIZONTAL;
        loginGBC.anchor = GridBagConstraints.NORTHWEST;

        var settings = mgsm.getSettings();

        urlField = new JTextField(settings.url(), 20);
        JLabel urlFieldLabel = addLoginRow(loginPanel, loginGBC, "Mindgard URL:", urlField);
        ui.setupUIChangeTracking(urlField, urlFieldLabel);

        // Audience
        audienceField = new JTextField(settings.audience(), 20);
        JLabel audienceFieldLabel = addLoginRow(loginPanel, loginGBC, "Audience:", audienceField);
        ui.setupUIChangeTracking(audienceField, audienceFieldLabel);

        // Client ID
        clientIDField = new JTextField(settings.clientID(), 20);
        JLabel clientIDFieldLabel = addLoginRow(loginPanel, loginGBC, "Client ID:", clientIDField);
        ui.setupUIChangeTracking(clientIDField, clientIDFieldLabel);

        // Add login status label
        loginStatusLabel = new JLabel();
        updateLoginStatus(mgsm);
        GridBagConstraints statusGBC = (GridBagConstraints) loginGBC.clone();
        statusGBC.gridx = 0;
        statusGBC.gridy = loginRow++;
        statusGBC.gridwidth = 2;
        statusGBC.anchor = GridBagConstraints.WEST;
        loginPanel.add(loginStatusLabel, statusGBC);

        loginButtonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener((actionEvent) -> {
            loginButton.setEnabled(false); // Disable button
            if (ui.hasUnsavedChanges()) {
                JOptionPane.showMessageDialog(
                    this,
                    "You have unsaved changes. Please save your settings before logging in.",
                    "Unsaved Changes",
                    JOptionPane.WARNING_MESSAGE
                );
                loginButton.setEnabled(true);
                return;
            }
            var auth = new MindgardAuthentication(mgsm);
            DeviceCodeData deviceCode;
            logger.log("Initiating login to Mindgard tenant");
            try {
                deviceCode = auth.get_device_code(logger);
            } catch (Exception e) {
                logger.log("Andrew "+ e.getMessage());
                loginButton.setEnabled(true);
                return;
            }
            try {
                Desktop desktop = Desktop.getDesktop();
                logger.log("Opening browser to: " + deviceCode.verification_uri_complete());
                String url = deviceCode.verification_uri_complete();
                desktop.browse(new java.net.URI(url));
                JOptionPane.showMessageDialog(this, "Confirm that you see " + deviceCode.user_code());

                // Run validate_login in a background thread with timeout
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    private Exception error = null;

                    @Override
                    protected Void doInBackground() {
                        Thread timeoutThread = new Thread(() -> {
                            try {
                                Thread.sleep(60000); // 60 seconds
                                if (!isDone()) {
                                    cancel(true);
                                }
                            } catch (InterruptedException ignored) {
                            }
                        });
                        timeoutThread.start();
                        try {
                            auth.validate_login(deviceCode);
                        } catch (Exception e) {
                            error = e;
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        loginButton.setEnabled(true); // Re-enable button
                        if (isCancelled()) {
                            JOptionPane.showMessageDialog(
                                    LoginTab.this,
                                    "Login to Mindgard timed out - please try again.",
                                    "Mindgard Login",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (error != null) {
                            JOptionPane.showMessageDialog(
                                    LoginTab.this,
                                    "Login failed: " + error.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(
                                    LoginTab.this,
                                    "Logged in successfully to " + mgsm.getSettings().url());
                            updateLoginStatus(mgsm);
                        }
                    }
                };
                worker.execute();
            } catch (Exception e) {
                loginButton.setEnabled(true); // Re-enable button on browser error
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to open browser: " + e.getMessage(),
                        "Error",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        loginButtonPanel.add(loginButton);
    }

    // Helper to update login status label
    public void updateLoginStatus(MindgardSettingsManager mgsm) {
        if (mgsm.validLogin()) {
            loginStatusLabel.setText("You are logged in to " + mgsm.getSettings().url());
        } else {
            loginStatusLabel.setText("You do not have an active login for this tenant, please log in.");
        }
    }

    /**
     * Adds a row to the login panel with a label and corresponding field.
     * 
     * @param panel     The panel to which the row will be added
     * @param baseGbc   The base GridBagConstraints to use for layout
     * @param labelText The text for the label
     * @param field     The input field component
     * @return The JLabel that was added to the panel
     */
    private JLabel addLoginRow(JPanel panel, GridBagConstraints baseGbc,
            String labelText, JComponent field) {

        GridBagConstraints gbcLabel = (GridBagConstraints) baseGbc.clone();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = loginRow;
        gbcLabel.weightx = 0.0;
        gbcLabel.gridwidth = 1;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbcLabel.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        panel.add(label, gbcLabel);

        GridBagConstraints gbcField = (GridBagConstraints) baseGbc.clone();
        gbcField.gridx = 1;
        gbcField.gridy = loginRow;
        gbcField.weightx = 1.0;
        gbcField.gridwidth = 1;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.anchor = GridBagConstraints.WEST;

        panel.add(field, gbcField);

        loginRow++;
        return label;
    }
}
