package uk.gov.hmcts.reform.slc.supplier;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

@Component
public class CurrentDateTimeSupplier implements Supplier<ZonedDateTime> {
    @Override
    public ZonedDateTime get() {
        return ZonedDateTime.now();
    }
}
