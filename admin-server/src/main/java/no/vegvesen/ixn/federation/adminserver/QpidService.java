package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.qpid.Exchange;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Collections;

import java.util.List;

@Service
public class QpidService {

    private final QpidClient qpidClient;

    private static final Logger logger = LoggerFactory.getLogger(QpidClient.class);

    private final AdminServerErrorAdvice adminServerErrorAdvice = new AdminServerErrorAdvice();


    @Autowired
    public QpidService(QpidClient qpidClient) {
        this.qpidClient = qpidClient;
    }

    public boolean exchangeExists(String exchangeName){
        return qpidClient.exchangeExists(exchangeName);
    }

    public boolean queueExists(String queueName){
        return qpidClient.queueExists(queueName);
    }

    public boolean bindingExists(String exchangeName, String queueName){
        QpidDelta delta = qpidClient.getQpidDelta();
        try{
          boolean exists = delta.exchangeHasBindingToQueue(exchangeName, queueName);
          return exists;
        }
        catch(Exception e){
            return false;
        }
    }

    public List<Exchange> getExchanges() {
        try {
            return qpidClient.getAllExchanges();
        } catch (JsonProcessingException e) {
            ResponseEntity<String> response = adminServerErrorAdvice.handleJsonProcessingException(e);
            logger.error("JSON processing error: {}", response.getBody());
            return Collections.emptyList();
        }
    }

}
