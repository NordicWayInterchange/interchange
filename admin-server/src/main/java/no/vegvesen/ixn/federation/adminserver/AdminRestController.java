package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.NeighbourApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminRestController {

    private final TypeTransformer typeTransformer = new TypeTransformer();

    private final NeighbourRepository neighbourRepository;

    private final CertService certService;

    @Autowired
    public AdminRestController(NeighbourRepository neighbourRepository, CertService certService){
        this.neighbourRepository = neighbourRepository;
        this.certService = certService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/neighbours", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NeighbourApi> getNeighbours(@PathVariable("adminUser") String adminUser){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminUser);
        List<Neighbour> neighbourList = neighbourRepository.findAll();
        return typeTransformer.neighbourListToNeighbourApiList(neighbourList);
    }
}
