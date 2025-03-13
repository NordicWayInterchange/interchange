package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.qpid.Exchange;
import no.vegvesen.ixn.federation.adminserver.qpid.QpidAdminClient;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QpidService {

    private final QpidAdminClient qpidAdminClient;

    @Autowired
    public QpidService(QpidAdminClient qpidAdminClient) {
        this.qpidAdminClient = qpidAdminClient;
    }

    public boolean exchangeExists(String exchangeName) {
        return qpidAdminClient.exchangeExists(exchangeName);
    }

    public boolean queueExists(String queueName) {
        return qpidAdminClient.queueExists(queueName);
    }

    public boolean bindingExists(String exchangeName, String queueName) {
        Exchange exchange = qpidAdminClient.getExchange(exchangeName);
        if (exchange != null) {
            return exchange.isBoundToQueue(queueName);
        } else {
            return false;
        }
    }

    public List<Exchange> getAllExchanges() {
        try {
            return qpidAdminClient.getAllExchanges();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Queue> getAllQueues() {
        try {
            return qpidAdminClient.getAllQueues();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
