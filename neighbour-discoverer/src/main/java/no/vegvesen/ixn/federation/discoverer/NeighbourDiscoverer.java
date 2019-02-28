package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.Model.DataType;
import no.vegvesen.ixn.federation.model.Model.Interchange;
import no.vegvesen.ixn.federation.model.Model.Subscription;
import no.vegvesen.ixn.federation.model.Repository.InterchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
public class NeighbourDiscoverer{

	private InterchangeRepository interchangeRepository;
	private DNSFacade dnsFacade;

	private Logger logger = LoggerFactory.getLogger(NeighbourDiscoverer.class);
	private Timestamp from;

	@Autowired
	NeighbourDiscoverer(DNSFacade dnsFacade, InterchangeRepository interchangeRepository){
		this.dnsFacade = dnsFacade;
		this.interchangeRepository = interchangeRepository;
		from = Timestamp.from(Instant.now());
	}

	//@Scheduled(fixedRate = 5000)
	public void checkForChanges(){

		// Every 5 seconds; print element from database that is older than 5 seconds.

		Instant now = Instant.now();
		Timestamp nowTimestamp = Timestamp.from(now);

		logger.info("Querying for all interchanges edited between " + from.toString() + " and " + nowTimestamp.toString());

		try{
			List<Interchange> interchanges = interchangeRepository.findOlderThan(from, nowTimestamp);

			for(Interchange i : interchanges){
				logger.info("Interchange: " + i.getName());
			}

		}catch (Exception e){
			logger.info("Error: " + e.getClass().getName());
			logger.info("Found no interchanges last edited before this time.");
		}

		from = nowTimestamp;
	}

	public void POSTtoInterchange(String json, String url){
		try {
			URL obj = new URL(url);
			HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
			postConnection.setRequestMethod("POST");
			postConnection.setRequestProperty("Content-Type", "application/json");
			postConnection.setDoOutput(true);
			OutputStream os = postConnection.getOutputStream();
			os.write(json.getBytes());
			os.flush();
			os.close();

			// Log response code.
			int responseCode = postConnection.getResponseCode();
			logger.info("POST Response Code :  " + responseCode);
			logger.info("POST Response Message : " + postConnection.getResponseMessage());

		}catch(Exception e){
			logger.info("Error in sending JSON. " + e.getClass().getName());
		}
	}

	public void GETfromInterchange(String url){
		try {
			URL urlForGetRequest = new URL(url);
			String readLine = null;
			HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuffer response = new StringBuffer();

				while ((readLine = in.readLine()) != null) {
					response.append(readLine);
				}
				in.close();
				// print response as json.
				System.out.println("JSON String Result " + response.toString());
			}

		}catch(Exception e){
				logger.info("Exception: " + e.getClass().getName());
		}
	}


	// Possibly redundant?
	public void getCapabilities(String ixnName){
		// Select a given interchange from the database
		// get hostname and port nr for the interchange
		// send a GET request, receive a Subscription object.

		Interchange interchange = interchangeRepository.findByName(ixnName);
		if(interchange != null){
			// get capabilities from this node

			String url = interchange.getHostname() + ":8090/" + ixnName + "/capabilities";
			logger.info("Getting capabilities from URL: " + url);

			GETfromInterchange(url);
		}
	}

	@Scheduled(fixedRate = 8000, initialDelay = 3000)
	public void checkForNewInterchanges(){

		// dnsFacade.getNeighbours() returns all neighbours found in the DNS lookup.
		// check if there are any neighbours we have not yet discovered.
		// If we find a new neighbour, post our information to the neighbour.

		// TODO: REMOVE MOCK
		List<Interchange> neighbours = dnsFacade.mockGetNeighbours();

		for(Interchange i : neighbours){
			if(interchangeRepository.findByName(i.getName()) == null){

				logger.info("New interchange not in the database: " + i.getName());
				interchangeRepository.save(i);

				try {

					// Interchange object to represent the current node (aka "me").
					// TODO: get the representation of the current node("me") from local database instead of this.
					Interchange ixnA = new Interchange();
					ixnA.setHostname("http://localhost");
					ixnA.setPortNr("8080");
					ixnA.setName("ixn-a");

					DataType dataTypeCapability = new DataType("datex2", "1.0","obstruction");
					ixnA.setCapabilities(Collections.singleton(dataTypeCapability));

					DataType dataTypeSubscription = new DataType("datex2", "1.0", "obstruction");
					Subscription subscription = new Subscription("SE", dataTypeSubscription, "", "");
					ixnA.setSubscriptions(Collections.singleton(subscription));

					// convert object to JSON
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = ow.writeValueAsString(ixnA);

					// create URL for the new interchange.
					String url = i.getHostname() + ":" + i.getPortNr() + "/updateCapabilities";

					POSTtoInterchange(json, url);

					logger.info("Posting capabilities for ixn-a to " + i.getName() + " on url " + url);
					logger.info("Json: " + json);

				}catch(Exception e){
					logger.info("Error creating capability json object. " + e.getClass().getName());
				}

			}else{
				logger.info("Found interchange " + i.getName() + " in the database.");
			}
		}
	}

	public static void main(String[] args){


	}
}
