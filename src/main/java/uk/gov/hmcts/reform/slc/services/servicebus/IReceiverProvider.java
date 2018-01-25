package uk.gov.hmcts.reform.slc.services.servicebus;

import com.microsoft.azure.servicebus.IMessageReceiver;

import java.util.function.Supplier;

@FunctionalInterface
public interface IReceiverProvider extends Supplier<IMessageReceiver> {
}
