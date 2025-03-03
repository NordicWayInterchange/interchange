package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.exceptions.HandleJsonProcessingException;
import no.vegvesen.ixn.federation.qpid.Exchange;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import no.vegvesen.ixn.federation.qpid.Queue;
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

    public boolean bindingExists(String exchangeName, String queueName){
        QpidDelta delta = qpidClient.getQpidDelta();
        try{
            return delta.exchangeHasBindingToQueue(exchangeName, queueName);
        }
        catch(Exception e){
            return false;
        }
    }

    public List<Exchange> getAllExchanges() {
        try {
            return qpidClient.getAllExchanges();
        } catch (JsonProcessingException e) {
            throw new HandleJsonProcessingException("JSON processing error: {}", e);
        }
    }

    public List<Queue> getAllQueues() {
        try {
            return qpidClient.getAllQueues();
        } catch (JsonProcessingException e) {
            throw new HandleJsonProcessingException("JSON processing error: {}", e);
        }
    }

}
