package ai.mindgard.sandbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public enum Dataset {
    Default(null),
    CustomerService("BadCustomer"),
    Finance("BadFinance"),
    Legal("BadLegal"),
    Medical("BadMedical"),
    Injection("SqlInjection"),
    XSS("Xss");


    private final String datasetName;

    Dataset(String datasetName) {
        this.datasetName = datasetName;
    }

    public static int indexOfName(String dataset) {
        // not worth a map at the moment
        for (int i = 0; i < Dataset.values().length; i++) {
            if (Objects.equals(dataset, Dataset.values()[i].getDatasetName())) {
                return i;
            }
        }
        return 0;
    }

    public static Optional<List<String>> fromFile(String fileName) {
        if (fileName == null) {
            return Optional.empty();
        }
        try (Stream<String> lines = Files.lines(new File(fileName).toPath())) {
            return Optional.of(lines.toList());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public String getDatasetName() {
        return datasetName;
    }

}
