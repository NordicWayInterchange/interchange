package no.vegvesen.ixn.federation.capability;

import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.message.AMQMessageHeader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class JmsSelectorFilterTest {

    @Test
    public void testNotLike() throws ParseException {
        assertThatNoException().isThrownBy(
                () -> new JMSSelectorFilter("entry NOT LIKE '%aa%'")
        );
        assertThatNoException().isThrownBy(
                () -> new JMSSelectorFilter("NOT (entry LIKE '%aa%')")
        );
        assertThatNoException().isThrownBy(
                () -> new JMSSelectorFilter("NOT entry LIKE '%aa%'")
        );

        String name = "entry";
        String value = "aaaa";
        SimpleStringFilterable filterable = new SimpleStringFilterable(name, value);
        assertThat(new JMSSelectorFilter("entry NOT LIKE '%aa%'").matches(filterable)).isFalse();
        assertThat(new JMSSelectorFilter("entry like '%aaa%'").matches(filterable)).isTrue();
        assertThat(new JMSSelectorFilter("NOT (entry LIKE '%aa%')").matches(filterable)).isFalse();
        assertThat(new JMSSelectorFilter("(entry LIKE '%aaa%')").matches(filterable)).isTrue();
        assertThat(new JMSSelectorFilter("NOT entry LIKE '%aa%'").matches(filterable)).isFalse();

        /*
        Fra SQL:
        federation=# select count(*) from app_quad where quadrant_app (not like '%12%');
        ERROR:  type "like" does not exist
        LINE 1: ...ct count(*) from app_quad where quadrant_app (not like '%12%...
         */

        assertThatExceptionOfType(ParseException.class).isThrownBy(
                () -> new JMSSelectorFilter("entry (not like '%aa%')")

        );
        assertThat(new JMSSelectorFilter("entry in ('aaaa','bbb','ccc')").matches(filterable)).isTrue();
        assertThat(new JMSSelectorFilter("entry in ('%aa%','bbb','ccc')").matches(filterable)).isTrue();

    }


    private static class SimpleStringFilterable implements Filterable {
        private final String variableName;
        private final String variableValue;

        public SimpleStringFilterable(String variableName, String variableValue) {

            this.variableName = variableName;
            this.variableValue = variableValue;
        }
        @Override
        public AMQMessageHeader getMessageHeader() {
            return null;
        }

        @Override
        public boolean isPersistent() {
            return false;
        }

        @Override
        public boolean isRedelivered() {
            return false;
        }

        @Override
        public Object getConnectionReference() {
            return null;
        }

        @Override
        public long getMessageNumber() {
            return 0;
        }

        @Override
        public long getArrivalTime() {
            return 0;
        }

        @Override
        public Object getHeader(String name) {
            System.out.println("Looking up " + name);
            if (name.equals(variableName)) {
                return variableValue;
            }
            return null;
        }

        @Override
        public String getReplyTo() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public byte getPriority() {
            return 0;
        }

        @Override
        public String getMessageId() {
            return null;
        }

        @Override
        public long getTimestamp() {
            return 0;
        }

        @Override
        public String getCorrelationId() {
            return null;
        }

        @Override
        public long getExpiration() {
            return 0;
        }
    }
}
