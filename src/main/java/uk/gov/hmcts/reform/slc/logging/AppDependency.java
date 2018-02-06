package uk.gov.hmcts.reform.slc.logging;

final class AppDependency {

    /**
     * Service bus message receiver dependency.
     * Used to track dependency of how long in millis did it take to...
     */
    static final String SERVICE_BUS_DEPENDENCY = "ServiceBus";

    /**
     * .. receive message from service bus queue.
     */
    static final String SERVICE_BUS_MESSAGE_RECEIVED = "MessageReceived";

    /**
     * .. release lock and remove message from service bus queue.
     */
    static final String SERVICE_BUS_MESSAGE_COMPLETED = "MessageCompleted";

    /**
     * .. release lock and send message to dead letter pool.
     */
    static final String SERVICE_BUS_DEAD_LETTERED = "MessageDeadLettered";

    /**
     * Pdf service client.
     * Used to track dependency of how long in millis did it take to...
     */
    static final String PDF_GENERATOR_DEPENDENCY = "PdfGenerator";

    /**
     * .. generate pdf.
     */
    static final String GENERATE_PDF_FROM_HTML_COMMAND = "GeneratePdfFromHtml";

    static final String FTP_DEPENDENCY = "FtpClient";

    static final String FTP_FILE_UPLOAD_COMMAND = "FtpFileUpload";

    private AppDependency() {
        // utility class constructor
    }
}
