package uk.gov.hmcts.reform.slc.services.steps.getpdf.duplex;

import org.junit.Test;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;

public class DuplexPreparatorTest {

    @Test
    public void should_add_blank_page_if_total_number_of_pages_is_odd() throws Exception {
        // given
        byte[] before = toByteArray(getResource("single_page.pdf"));

        // when
        byte[] after = new DuplexPreparator().prepare(before);

        // then
        assertThat(after).isNotEqualTo(before);
    }

    @Test
    public void should_not_add_a_blank_page_if_total_number_of_pages_is_even() throws Exception {
        // given
        byte[] before = toByteArray(getResource("two_pages.pdf"));

        // when
        byte[] after = new DuplexPreparator().prepare(before);

        // then
        assertThat(after).isEqualTo(before);
    }
}
