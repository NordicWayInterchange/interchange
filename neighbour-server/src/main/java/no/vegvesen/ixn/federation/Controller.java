package idaberge.springbootrestapi;

import idaberge.springbootrestapi.Model.Capability;
import idaberge.springbootrestapi.Model.Interchange;
import idaberge.springbootrestapi.Model.Subscription;
import idaberge.springbootrestapi.Model.DataType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@RestController
public class Controller{

	Logger logger = LoggerFactory.getLogger(Controller.class);

	List<Interchange> neighbours = new ArrayList<>();
	List<Subscription> subscriptions = new ArrayList<>();
	List<Capability> neighbourCapabilities = new ArrayList<>();

	String exchangeURL = "http://localhost:8081/api/latest/exchange/default/qpid.test.io/nwEx";
	String queueURL = "http://localhost:8081/api/latest/queue/default/qpid.test.io";

	public void callQpid(String urlString, String message, String command) throws Exception{
		String url = urlString + command;

		logger.info("URL: " + url);
		URL sourceURL = new URL(url);
		HttpURLConnection source = (HttpURLConnection) sourceURL.openConnection();

		// HTTP Basic Authentication.
		String userpass = "interchange:12345678";
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
		source.setRequestProperty ("Authorization", basicAuth);

		source.setRequestMethod("POST");
		source.setRequestProperty("User-Agent", "Java client");
		source.setDoOutput(true);
		source.setRequestProperty("Content-Type", "application/json");

		DataOutputStream wr = new DataOutputStream(source.getOutputStream());
		wr.writeBytes(message);

		InputStream is;

		try {
			is = source.getInputStream();
		} catch (IOException ioe) {
			if (source instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection) source;
				int statusCode = httpConn.getResponseCode();
				if (statusCode != 200) {
					is = httpConn.getErrorStream();
				}
			}
		}

		logger.info("Response code: " + source.getResponseCode());
		logger.info("Closing connection...");
		wr.close();
	}

	public void updateBinding(String binding, String queueName) throws Exception{

		JSONObject json = new JSONObject();
		json.put("destination", queueName);
		json.put("bindingKey", "where1");
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", binding);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		logger.info("Json string: " + jsonString);
		callQpid(exchangeURL, jsonString, "/bind");
	}

	public void createQueue(Interchange interchange) throws Exception{
		JSONObject json = new JSONObject();
		json.put("name", interchange.getId());
		json.put("durable", true);

		String jsonString = json.toString();
		logger.info("Creating queue:" + jsonString);

		callQpid(queueURL, jsonString, "/");

		logger.info("Creating binding for queue..");
		String binding = createBinding(interchange);
		updateBinding(binding, interchange.getId());
	}

	public String createBinding(Interchange interchange){

		String binding = "";

		logger.info("Number of subscriptions: " + interchange.getSubscriptions().size());

		for(Subscription subscription : interchange.getSubscriptions()) {

			binding += "(where1='" + subscription.getCountry() + "' AND ("; // opening parentesis for how/version/what

			for (DataType elem : subscription.getDataSets()) {
				binding = binding + "how='" + elem.getHow() +
						"' AND version='" + elem.getVersion() +
						"' AND ("; // opening parenthesis for what

				for (String situationRecord : elem.getWhat()) {
					binding = binding + "what='" + situationRecord + "'";

					if (elem.getWhat().indexOf(situationRecord) != elem.getWhat().size() - 1) {
						binding += " OR ";
					} else {
						binding += ")"; // closing parentesis for what
					}
				}

				if (subscription.getDataSets().indexOf(elem) != subscription.getDataSets().size() - 1) {
					binding += " OR ";
				}
			}

			binding += ")"; // closing parenthesis for how/version/what

			if(interchange.getSubscriptions().indexOf(subscription) != interchange.getSubscriptions().size()-1 ){
				// Subscrption is not the last element, add OR

				int index = interchange.getSubscriptions().indexOf(subscription);
				logger.info("Country: " + interchange.getSubscriptions().get(index).getCountry()
						+ ", index: " + index );
				binding += ") OR";
			}else{
				binding += ")";
			}

		}
		logger.info("Queue binding:" + binding);

		return binding;
	}


	@PostMapping("/updateSubscription")
	public Interchange updateSubscription(@RequestBody Interchange interchange)throws Exception{

		// Check if we already have information about this interchange; if it is already in the list.

		if(!neighbours.contains(interchange)) {
			// We have not seen this interchange before. Add it to the list of neighbours.
			createInterchange(interchange);
		}else {
			String binding = createBinding(interchange);
			updateBinding(binding, interchange.getId());
		}

		return interchange;
	}

	@GetMapping("/neighbours")
	public List<Interchange> getAllSubscriptions()throws Exception{
		return neighbours;
	}

	@PostMapping("/interchange")
	public Interchange createInterchange(@RequestBody Interchange interchange)throws Exception{
		if(!neighbours.contains(interchange)){
			// we have a new interchange node, add it to list of neighbours and create a queue for it.
			neighbours.add(interchange);
			createQueue(interchange);
		}
		return interchange;
	}


	@PostMapping("/neighbourCapabilities")
	public Capability createNewCapability(@RequestBody Capability capability){
		neighbourCapabilities.add(capability);
		return capability;
	}

	@GetMapping("/capabilities")
	public List<Capability> getNeighbourCapabilities(){ return neighbourCapabilities; }

}
