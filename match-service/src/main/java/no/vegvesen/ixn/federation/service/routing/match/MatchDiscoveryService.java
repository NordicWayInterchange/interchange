package no.vegvesen.ixn.federation.service.routing.match;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchDiscoveryService {

    private final ServiceProviderRepository serviceProviderRepository;
    public final MatchRepository matchRepository;
    private final QpidClient qpidClient;

    private Logger logger = LoggerFactory.getLogger(MatchDiscoveryService.class);

    @Autowired
    public MatchDiscoveryService(MatchRepository matchRepository, ServiceProviderRepository serviceProviderRepository, QpidClient qpidClient) {
        this.matchRepository = matchRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.qpidClient = qpidClient;
    }

    public void syncLocalSubscriptionAndSubscriptionsToCreateMatch(List<ServiceProvider> serviceProviders, List<Neighbour> neighbours) {
        for (ServiceProvider serviceProvider : serviceProviders) {
            List<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions();
            String serviceProviderName = serviceProvider.getName();
            for (LocalSubscription localSubscription : localSubscriptions) {
                for (Neighbour neighbour : neighbours) {
                    for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                        if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED) && localSubscription.getStatus().equals(LocalSubscriptionStatus.CREATED)) {
                            if (Objects.equals(localSubscription.getSelector(),subscription.getSelector()) &&
                                    Objects.equals(localSubscription.getConsumerCommonName(),subscription.getConsumerCommonName())) {
                                if (matchRepository.findBySubscriptionIdAndAndLocalSubscriptionId(subscription.getId(), localSubscription.getId()) == null) {
                                    Match newMatch = new Match(localSubscription, subscription);
                                    matchRepository.save(newMatch);
                                    logger.info("Saved new Match {}", newMatch);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void syncMatchesToDelete() {
        List<Match> existingMatches = matchRepository.findAll();
        Set<Match> matchesToDelete = new HashSet<>();
        for (Match match : existingMatches) {
            if (match.subscriptionIsTearDown()) {
                if ( match.getSubscription().getEndpoints().isEmpty()) {
                    logger.info("Removing Match {}", match);
                    matchesToDelete.add(match);
                }
            } else {
                if (match.localSubscriptionIsTearDown()) {
                    logger.info("Removing Match {}", match);
                    matchesToDelete.add(match);
                }
            }
        }
        matchRepository.deleteAll(matchesToDelete);
    }

    public void createBindingsWithMatches() {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        QpidDelta delta = qpidClient.getQpidDelta();
        for (ServiceProvider serviceProvider : serviceProviders) {
            for (LocalSubscription localSubscription : serviceProvider.wantedNonRedirectSubscriptions()) {
                if (!localSubscription.getLocalEndpoints().isEmpty()) {
                    List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                    for (Match match : matches) {
                        Subscription subscription = match.getSubscription();
                        if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)) {
                            for (Endpoint endpoint : subscription.getEndpoints()) {
                                if (endpoint.hasShard()) {
                                    Exchange exchange = delta.findByExchangeName(endpoint.getShard().getExchangeName());
                                    if (exchange != null) {
                                        for (String queueName : localSubscription.getLocalEndpoints().stream().map(LocalEndpoint::getSource).collect(Collectors.toSet())) {
                                            Queue queue = delta.findByQueueName(queueName);
                                            if (queue != null && !exchange.isBoundTo(queue.getName())) {
                                                String exchangeName = exchange.getName();
                                                logger.debug("Adding bindings from queue {} to exchange {}", queueName, exchangeName);
                                                Binding binding = new Binding(exchangeName, queueName, new Filter(localSubscription.getSelector()));
                                                qpidClient.addBinding(exchangeName, binding);
                                                exchange.addBinding(binding);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}