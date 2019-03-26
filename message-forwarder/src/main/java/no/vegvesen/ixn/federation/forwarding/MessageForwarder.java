package no.vegvesen.ixn.federation.forwarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MessageForwarder {

    private Logger logger = LoggerFactory.getLogger(MessageForwarder.class);

    //Call the rest api and get the list of queues
    //Or, use the database to get the neighbours, then check if I already have a connection



}
