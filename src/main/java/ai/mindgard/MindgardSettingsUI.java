package ai.mindgard;

import ai.mindgard.sandbox.Dataset;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class MindgardSettingsUI extends JPanel implements MindgardSettings {
    private static final int DEFAULT_PROMPT_REPEATS = 1;
    private static final int DEFAULT_PARALLELISM = 1;
    private final JFileChooser customDatasetField = new JFileChooser();
    private File customDataset;
    private String selector = "$";
    private String testName = "burp-suite-test";
    private String dataset = null;
    private String systemPrompt = "Please answer the question: ";
    private String exclude;
    private String include;
    private Integer promptRepeats;
    private Integer parallelism;

    Map<Component, Object> originalValues = new HashMap<>();
    List<JLabel> changedLabels = new ArrayList<>();
    private void setupField(JComponent field, JLabel label) {
        if (field instanceof JTextField textField) {
            originalValues.put(textField, textField.getText());
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { checkChanged(); }
                public void removeUpdate(DocumentEvent e) { checkChanged(); }
                public void changedUpdate(DocumentEvent e) { checkChanged(); }
                private void checkChanged() {
                    String original = (String) originalValues.get(textField);
                    if (!textField.getText().equals(original)) {
                        label.setForeground(Color.decode("#CC5500"));
                        changedLabels.add(label);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                }
            });
        } else if (field instanceof JComboBox comboBox) {
            originalValues.put(comboBox, comboBox.getSelectedItem());
            comboBox.addActionListener(e -> {
                Object original = originalValues.get(comboBox);
                if (!Objects.equals(comboBox.getSelectedItem(), original)) {
                    label.setForeground(Color.decode("#CC5500"));
                    changedLabels.add(label);
                } else {
                    label.setForeground(Color.BLACK);
                }
            });
        } else if (field instanceof JTextArea textArea) {
            originalValues.put(textArea, textArea.getText());
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { checkChanged(); }
                public void removeUpdate(DocumentEvent e) { checkChanged(); }
                public void changedUpdate(DocumentEvent e) { checkChanged(); }
                private void checkChanged() {
                    String original = (String) originalValues.get(textArea);
                    if (!textArea.getText().equals(original)) {
                        label.setForeground(Color.decode("#CC5500"));
                        changedLabels.add(label);
                    } else {
                        label.setForeground(Color.BLACK);
                    }
                }
            });
        } else if (field instanceof JLabel labelField) {
            originalValues.put(labelField, labelField.getText());
            labelField.addPropertyChangeListener("text", e -> {
                String original = (String) originalValues.get(labelField);
                if (!labelField.getText().equals(original)) {
                    label.setForeground(Color.decode("#CC5500"));
                    changedLabels.add(label);
                } else {
                    label.setForeground(Color.BLACK);
                }
            });
        }
    }

    public MindgardSettingsUI() {
        super(new SpringLayout());

        try (Stream<String> lines = Files.lines(MindgardSettings.file("burp.json").toPath())) {
            Settings settings = JSON.fromJson(lines.collect(joining()), Settings.class);
            selector = Optional.ofNullable(settings.selector()).orElse(selector);
            testName = Optional.ofNullable(settings.testName()).orElse(testName);
            dataset = Optional.ofNullable(settings.dataset()).orElse(dataset);
            systemPrompt = Optional.ofNullable(settings.systemPrompt()).orElse(systemPrompt);
            customDataset = Optional.ofNullable(settings.customDatasetFilename()).map(File::new).orElse(customDataset);
            exclude = Optional.ofNullable(settings.exclude()).orElse(exclude);
            include = Optional.ofNullable(settings.include()).orElse(include);
            promptRepeats = Optional.ofNullable(settings.promptRepeats()).orElse(DEFAULT_PROMPT_REPEATS);
            parallelism = Optional.ofNullable(settings.parallelism()).orElse(DEFAULT_PARALLELISM);
        } catch (IOException e) {

        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 5);

        JLabel selectorLabel = new JLabel("Selector:");
        inputPanel.add(selectorLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField selectorField = new JTextField(selector, 20);
        inputPanel.add(selectorField, gbc);
        setupField(selectorField, selectorLabel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel testNameLabel = new JLabel("Test Name:");
        inputPanel.add(testNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField testNameField = new JTextField(testName, 20);
        inputPanel.add(testNameField, gbc);
        setupField(testNameField, testNameLabel);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel datasetLabel = new JLabel("Domain:");
        inputPanel.add(datasetLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        JComboBox<Dataset> datasetField = new JComboBox<>(Dataset.values());
        datasetField.setSelectedIndex(Dataset.indexOfName(dataset));
        inputPanel.add(datasetField, gbc);
        setupField(datasetField, datasetLabel);


        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        JLabel systemPromptLabel = new JLabel("System Prompt:");
        inputPanel.add(systemPromptLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        JTextArea systemPromptField = new JTextArea(systemPrompt, 5, 20);
        inputPanel.add(systemPromptField, gbc);
        setupField(systemPromptField, systemPromptLabel);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        JLabel customDatasetLabel = new JLabel("Custom Dataset:");
        inputPanel.add(customDatasetLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        var customDatasetPathLabel = new JLabel("No file selected");
        Optional.ofNullable(customDataset).map(Object::toString).ifPresent(customDatasetPathLabel::setText);

        inputPanel.add(customDatasetPathLabel, gbc);
        customDatasetPathLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        var datasetButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        var chooseButton = new JButton("Select File…");

        chooseButton.addActionListener(actionEvent -> {
            Optional.ofNullable(customDataset).ifPresent(customDatasetField::setSelectedFile);

            int result = customDatasetField.showOpenDialog(MindgardSettingsUI.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = customDatasetField.getSelectedFile();
                customDatasetPathLabel.setText(selectedFile.getAbsolutePath());
                customDataset = selectedFile;
            }
        });
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0,5,0,0);

        datasetButtons.add(chooseButton, constraints);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;

        var clearButton = new JButton("Remove Dataset");

        clearButton.addActionListener(actionEvent -> {
            customDatasetPathLabel.setText("No file selected");
            customDataset = null;
            customDatasetField.setSelectedFile(null);
        });

        datasetButtons.add(clearButton, constraints);
        inputPanel.add(datasetButtons,gbc);
        setupField(customDatasetPathLabel, customDatasetLabel);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0;
        JLabel excludeAttacksLabel = new JLabel("Exclude attack(s):");
        inputPanel.add(excludeAttacksLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        JTextField excludeAttacksField = new JTextField(exclude, 20);
        excludeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        inputPanel.add(excludeAttacksField, gbc);
        setupField(excludeAttacksField, excludeAttacksLabel);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0;
        JLabel includeAttacksLabel = new JLabel("Include attack(s):");
        inputPanel.add(includeAttacksLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        JTextField includeAttacksField = new JTextField(include, 20);
        includeAttacksField.setToolTipText("e.g. AntiGPT,PersonGPT");
        inputPanel.add(includeAttacksField, gbc);
        setupField(includeAttacksField, includeAttacksLabel);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0;
        JLabel promptRepeatsLabel = new JLabel("Prompt Repeats:");
        inputPanel.add(promptRepeatsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.weightx = 1.0;
        JFormattedTextField promptRepeatsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        promptRepeatsField.setValue(promptRepeats);
        promptRepeatsField.setToolTipText("e.g. 3");
        inputPanel.add(promptRepeatsField, gbc);
        setupField(promptRepeatsField, promptRepeatsLabel);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.weightx = 0;
        JLabel parallelismLabel = new JLabel("Parallelism:");
        inputPanel.add(parallelismLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.weightx = 1.0;
        JFormattedTextField parallelismField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        parallelismField.setValue(parallelism);
        parallelismField.setToolTipText("e.g. 1");
        inputPanel.add(parallelismField, gbc);
        setupField(parallelismField, parallelismLabel);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        JLabel parallelismLabelDescription = new JLabel("(Parallelism controls the maximum number of attacks we will launch against your model at once. If your model is stateful or otherwise would be confused by overlapping attacks, leave this at the default of 1.)");
        parallelismLabelDescription.setForeground(Color.decode("#CC5500"));
        inputPanel.add(parallelismLabelDescription, gbc);

        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((actionEvent) -> {
            selector = selectorField.getText();
            testName = testNameField.getText();
            systemPrompt = systemPromptField.getText();
            dataset = ((Dataset)datasetField.getSelectedItem()).getDatasetName();
            customDataset = customDatasetField.getSelectedFile();
            exclude = excludeAttacksField.getText();
            include =includeAttacksField.getText();
            try {
                promptRepeats = ((Number) promptRepeatsField.getFormatter()
                        .stringToValue(promptRepeatsField.getText()))
                        .intValue();

                parallelism = ((Number) parallelismField.getFormatter()
                        .stringToValue(parallelismField.getText()))
                        .intValue();
            } catch (ParseException ignored) {}
            save();

            JOptionPane.showMessageDialog(
                    this,
                    "Mindgard settings updated successfully!" + "\n" +
                            "You can view the result of your test at https://sandbox.mindgard.ai/models-assessment" + "\n" +
                            "with the model name: " + testName,
                    "Mindgard Extension",
                    JOptionPane.INFORMATION_MESSAGE
            );

            changedLabels.forEach(label -> {
                label.setForeground(Color.BLACK);
            });
        });
        buttonPanel.add(saveButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(300, 120));
    }

    public String selector() {
        return selector;
    }
    public String testName() {
        return testName;
    }

    @Override
    public String dataset() {
        return dataset;
    }

    @Override
    public String systemPrompt() {
        return systemPrompt;
    }

    @Override
    public String customDatasetFilename() {
        return Optional.ofNullable(customDataset).map(File::getAbsolutePath).orElse(null);
    }

    @Override
    public String exclude() {
        return exclude;
    }

    @Override
    public String include() {
        return include;
    }

    @Override
    public Integer promptRepeats() { return promptRepeats; }

    @Override
    public Integer parallelism() { return parallelism; }
}
