package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.napcore.properties.CertGeneratorProperties;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class NapRestController {

    private final ServiceProviderRepository serviceProviderRepository;
    private final NeighbourRepository neighbourRepository;
    private final CertService certService;
    private final NapCoreProperties napCoreProperties;
    private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
    private Logger logger = LoggerFactory.getLogger(NapRestController.class);
    private TypeTransformer typeTransformer = new TypeTransformer();


    private CertSigner certSigner;

    @Autowired
    public NapRestController(
            ServiceProviderRepository serviceProviderRepository,
            NeighbourRepository neighbourRepository,
            CertService certService,
            NapCoreProperties napCoreProperties,
            CertSigner certSigner) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
        this.certService = certService;
        this.napCoreProperties = napCoreProperties;
        this.certSigner = certSigner;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/nap/{actorCommonName}/x509/csr", produces = MediaType.APPLICATION_JSON_VALUE)
    public CertificateSignResponse addCsrRequest(@PathVariable String actorCommonName, @RequestBody CertificateSignRequest signRequest) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("CSR - signing new cert for actor {}", actorCommonName);
        List<String> certs;
        String csr = new String(Base64.getDecoder().decode(signRequest.getCsr()));
        //TODO make a single, better runtimeException to signal that something went wrong with signing.
        try {
            certs = certSigner.sign(csr,actorCommonName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        List<String> encodedCerts = certs.stream().map(s -> Base64.getEncoder().encodeToString(s.getBytes())).collect(Collectors.toList());
        return new CertificateSignResponse(encodedCerts);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/nap/{actorCommonName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Subscription addSubscription(@PathVariable String actorCommonName, @RequestBody SubscriptionRequest subscriptionRequest) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Subscription - Received POST from Service Provider: {}", actorCommonName);

        if (Objects.isNull(subscriptionRequest)) {
            throw new SubscriptionRequestException("Bad api object for Subscription Request, Subscription has no selector.");
        }

        LocalSubscription localSubscription = typeTransformer.transformNapSubscriptionToLocalSubscription(subscriptionRequest, napCoreProperties.getName());
        if (JMSSelectorFilterFactory.isValidSelector(localSubscription.getSelector())) {
            localSubscription.setStatus(LocalSubscriptionStatus.REQUESTED);
        } else {
            localSubscription.setStatus(LocalSubscriptionStatus.ILLEGAL);
        }

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        serviceProvider.addLocalSubscription(localSubscription);

        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProvider);
        logger.debug("Updated Service Provider: {}", savedServiceProvider.toString());

        LocalSubscription savedSubscription = savedServiceProvider
                .getSubscriptions()
                .stream()
                .filter(subscription -> subscription.equals(localSubscription))
                .findFirst()
                .get();

        return typeTransformer.transformLocalSubscriptionToNapSubscription(savedSubscription);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/nap/{actorCommonName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Subscription> getSubscriptions(@PathVariable String actorCommonName) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Listing subscription for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);

        return typeTransformer.transformLocalSubscriptionsToNapSubscriptions(serviceProvider.getSubscriptions());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/nap/{actorCommonName}/subscriptions/{subscriptionId}")
    public Subscription getSubscription(@PathVariable String actorCommonName, @PathVariable String subscriptionId) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Getting subscription {} for service provider {}", subscriptionId, actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        LocalSubscription localSubscription = serviceProvider.getSubscriptions()
                .stream()
                .filter(s -> s.getId().equals(Integer.parseInt(subscriptionId)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Could not find subscription with ID %s for service provider %s",subscriptionId,actorCommonName)));

        return typeTransformer.transformLocalSubscriptionToNapSubscription(localSubscription);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/nap/{actorCommonName}/subscriptions/{subscriptionId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteSubscription(@PathVariable String actorCommonName, @PathVariable String subscriptionId) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Service Provider {}, DELETE subscription {}", actorCommonName, subscriptionId);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.removeLocalSubscription(Integer.parseInt(subscriptionId));

        ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
        logger.debug("Updated Service Provider: {}", saved.toString());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/nap/{actorCommonName}/subscriptions/capabilities")
    public List<Capability> getMatchingSubscriptionCapabilities(@PathVariable String actorCommonName, @RequestParam(required = false) String selector) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List network capabilities for serivce provider {}",actorCommonName);

        Set<CapabilitySplit> allCapabilities = getAllNeighbourCapabilities();
        allCapabilities.addAll(getAllLocalCapabilities());
        if (selector != null) {
            if (!selector.isEmpty()) {
                allCapabilities = getAllMatchingCapabilities(selector, allCapabilities);
            }
        }

        return typeTransformer.transformCapabilitiesToGetMatchingCapabilitiesResponse(allCapabilities);
    }

    private ServiceProvider getOrCreateServiceProvider(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }
        return serviceProvider;
    }

    private Set<CapabilitySplit> getAllMatchingCapabilities(String selector, Set<CapabilitySplit> allCapabilities) {
        return CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, selector);
    }

    private Set<CapabilitySplit> getAllLocalCapabilities() {
        Set<CapabilitySplit> capabilities = new HashSet<>();
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider otherServiceProvider : serviceProviders) {
            capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
        }
        return capabilities;
    }

    private Set<CapabilitySplit> getAllNeighbourCapabilities() {
        Set<CapabilitySplit> capabilities = new HashSet<>();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for (Neighbour neighbour : neighbours) {
            capabilities.addAll(neighbour.getCapabilities().getCapabilities());
        }
        return capabilities;
    }
}
