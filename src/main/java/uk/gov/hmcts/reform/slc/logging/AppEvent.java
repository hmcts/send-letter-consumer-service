package uk.gov.hmcts.reform.slc.logging;

final class AppEvent {

    /**
     * Used to track event of message receive from service bus queue.
     */
    static final String MESSAGE_RECEIVED = "MessageReceived";

    /**
     * Used to track event of successful message handler process.
     */
    static final String MESSAGE_HANDLED_SUCCESSFULLY = "MessageHandleSuccess";

    /**
     * Used to track event of failed message handler process.
     */
    static final String MESSAGE_HANDLED_UNSUCCESSFULLY = "MessageHandleFailure";

    /**
     * Used to track event of successful message map.
     */
    static final String MESSAGE_MAPPED_SUCCESSFULLY = "MessageMapSuccess";

    /**
     * Used to track event of attempt to map an empty message.
     */
    static final String MESSAGE_MAPPED_EMPTY = "MessageMapEmpty";

    /**
     * Used to track event of attempt to map an invalid message..
     */
    static final String MESSAGE_MAPPED_INVALID = "MessageMapInvalid";

    /**
     * Used to track event of failed message map.
     */
    static final String MESSAGE_MAPPED_UNSUCCESSFULLY = "MessageMapFailure";

    private AppEvent() {
        // utility class constructor
    }
}
