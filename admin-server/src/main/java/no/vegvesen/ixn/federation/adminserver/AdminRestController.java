package no.vegvesen.ixn.federation.adminserver;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminRestController {


    /* TODO
       Certservice needs to be created. How should admin users be authenticated?
     */
    @RequestMapping(method = RequestMethod.GET, path = {"/test"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String test(String adminName) {
        return "Hello world";
    }
}
