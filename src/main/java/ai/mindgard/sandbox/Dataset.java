package ai.mindgard.sandbox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public enum Dataset {
    Default(null, "Default"),
    CustomerService("BadCustomer", "CustomerService"),
    Finance("BadFinance", "Finance"),
    Legal("BadLegal", "Legal"),
    Medical("BadMedical", "Medical"),
    Injection("SqlInjection", "Injection"),
    XSS("Xss", "XSS"),
    GuardrailBypassDeepset("GuardrailBypassDeepset", "Bypass - Deepset (experimental)"),
    GuardrailBypassLlmGuard("GuardrailBypassLlmGuard", "Bypass - LlmGuard (experimental)"),
    GuardrailBypassMetaPromptGuardV1("GuardrailBypassMetaPromptGuardV1", "Bypass - MetaPromptGuardV1 (experimental)"),
    GuardrailBypassProtectAiV1("GuardrailBypassProtectAiV1", "Bypass - ProtectAiV1 (experimental)"),
    GuardrailBypassProtectAiV2("GuardrailBypassProtectAiV2", "Bypass - ProtectAiV2 (experimental)"),
    GuardrailBypassVijilPromptInjection("GuardrailBypassVijilPromptInjection", "Bypass - VijilPromptInjection (experimental)"),
    Toxicity("Toxicity", "Toxicity"),
    ToxicityDiscrimination("ToxicityDiscrimination", "Toxicity - Discrimination"),
    ToxicityHarassment("ToxicityHarassment", "Toxicity - Harassment"),
    ToxicityHateSpeetch("ToxicityHateSpeech", "Toxicity - Hate Speech"),
    ToxicityProfanity("ToxicityProfanity", "Toxicity - Profanity"),
    InformationDisorder("InformationDisorder", "Disinformation and Misinformation"),
    InformationDisorderMisinformation("InformationDisorderMisinformation", "Misinformation"),
    InformationDisorderDisinformation("InformationDisorderDisinformation", "Disinformation"),
    Cybersecurity("Cybersecurity", "Cybersecurity"),
    CybersecurityCloudAttacks("CybersecurityCloudAttacks", "Cybersecurity - Cloud Attacks"),
    CybersecurityControlSystemAttacks("CybersecurityControlSystemAttacks", "Cybersecurity - Control System Attacks"),
    CybersecurityCryptographicAttacks("CybersecurityCryptographicAttacks", "Cybersecurity - Cryptographic Attacks"),
    CybersecurityCyberCrime("CybersecurityCyberCrime", "Cybersecurity - Cyber Crime"),
    CybersecurityEvasionTechniques("CybersecurityEvasionTechniques", "Cybersecurity - Evasion Techniques"),
    CybersecurityHardwareAttacks("CybersecurityHardwareAttacks", "Cybersecurity - Hardware Attacks"),
    CybersecurityIntrustionTechniques("CybersecurityIntrustionTechniques", "Cybersecurity - Intrustion Techniques"),
    CybersecurityIotAttacks("CybersecurityIotAttacks", "Cybersecurity - IoT Attacks"),
    CybersecurityMalwareAttacks("CybersecurityMalwareAttacks", "Cybersecurity - Malware Attacks"),
    CybersecurityNetworkAttacks("CybersecurityNetworkAttacks", "Cybersecurity - Network Attacks"),
    CybersecurityWebApplicationAttacks("CybersecurityWebApplicationAttacks", "Cybersecurity - Web Application Attacks"),
    Harmful("Harmful", "Harmful Information"),
    HarmfulViolence("HarmfulViolence", "Harmful - Violence"),
    HarmfulSexuallyExplicit("HarmfulSexuallyExplicit", "Harmful - Sexually Explicit"),
    HarmfulSelfHarm("HarmfulSelfHarm", "Harmful - Self Harm"),
    HarmfulIllegal("HarmfulIllegal", "Harmful - Illegal"),
    HarmfulDangerousContent("HarmfulDangerousContent", "Harmful - Dangerous Content"),
    BusinessRisk("BusinessRisk", "Business Risk"),
    BusinessRiskPii("BusinessRiskPii", "Business Risk - PII"),
    BusinessRiskCopyright("BusinessRiskCopyright", "Business Risk - Copyright");

    private final String datasetName;
    private final String displayName;

    Dataset(String datasetName, String displayName) {
        this.datasetName = datasetName;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
