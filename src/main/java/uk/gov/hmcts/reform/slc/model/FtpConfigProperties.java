package uk.gov.hmcts.reform.slc.model;

public class FtpConfigProperties {

    public final String hostname;
    public final int port;
    public final String fingerprint;
    public final String username;
    public final String publicKey;
    public final String privateKey;
    public final String targetFolder;
    public final String reportsFolder;

    private FtpConfigProperties(Builder builder) {
        this.hostname = builder.hostname;
        this.port = builder.port;
        this.fingerprint = builder.fingerprint;
        this.username = builder.username;
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
        this.targetFolder = builder.targetFolder;
        this.reportsFolder = builder.reportsFolder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String hostname;
        private int port;
        private String fingerprint;
        private String username;
        private String publicKey;
        private String privateKey;
        private String targetFolder;
        private String reportsFolder;

        private Builder() {
        }

        public FtpConfigProperties build() {
            return new FtpConfigProperties(this);
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder fingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder targetFolder(String targetFolder) {
            this.targetFolder = targetFolder;
            return this;
        }

        public Builder reportsFolder(String reportsFolder) {
            this.reportsFolder = reportsFolder;
            return this;
        }
    }
}
