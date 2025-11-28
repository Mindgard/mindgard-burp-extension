package ai.mindgard;

/**
 * Manages Mindgard credentials and URIs based on provided settings or defaults.
 */
public class MindgardServerConfiguration {
    private MindgardSettings settings;

    /**
     * @param settings the settings object to base the config from
     */
    public MindgardServerConfiguration(MindgardSettings settings) {
        this.settings = settings;
    }

    /**
     * @return the mindgard base URL, defaulted if not overridden in settings
     */
    public String getMindgardUrl() {
        return this.settings != null ? this.settings.mindgardUrl() : Constants.MINDGARD_URL;
    }

    /**
     * @return the mindgard audience, defaulted if not overridden in settings
     */
    public String getMindgardAudience() {
        return this.settings != null ? this.settings.mindgardAudience() : Constants.AUDIENCE;
    }

    /**
     * @return the mindgard client ID, defaulted if not overridden in settings
     */
    public String getMindgardClientID() {
        return this.settings != null ? this.settings.mindgardClientID() : Constants.CLIENT_ID;
    }

    /**
     * @return the mindgard API subdomain, defaulted if not overridden in settings
     */
    public String getMindgardApiUrl() throws java.net.URISyntaxException {
        return addSubdomainToURI(getMindgardUrl(), "api");
    }

    /**
     * @return the mindgard login subdomain, defaulted if not overridden in settings
     */
    public String getMindgardLoginUrl() throws java.net.URISyntaxException {
        return addSubdomainToURI(getMindgardUrl(), "login");
    }

    /**
     * Helper method to add a subdomain to a given URI.
     *
     * @param sourceUri the original URI
     * @param subdomain the subdomain to add
     * @return the new URI with the subdomain added
     */
    private String addSubdomainToURI(String sourceUri, String subdomain) throws java.net.URISyntaxException {
        java.net.URI uri = new java.net.URI(sourceUri);
        String host = uri.getHost();

        if (host == null) {
            throw new IllegalArgumentException("Invalid or relative url :" + getMindgardUrl());
        }

        String newHost = subdomain + "." + host;
        java.net.URI newUri = new java.net.URI(
                uri.getScheme(),
                uri.getUserInfo(),
                newHost,
                uri.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment());

        return newUri.toString();
    }
}
