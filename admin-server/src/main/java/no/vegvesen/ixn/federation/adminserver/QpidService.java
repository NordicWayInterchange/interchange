package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.qpid.Exchange;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QpidService {

    private final QpidClient qpidClient;

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

    public List<Exchange> exchangeList() throws JsonProcessingException {
        return qpidClient.getAllExchanges();
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
}
