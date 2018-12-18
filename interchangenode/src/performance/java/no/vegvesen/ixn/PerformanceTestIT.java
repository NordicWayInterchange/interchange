package no.vegvesen.ixn;

import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.Test;

import javax.jms.*;
import javax.naming.Context;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Verifies access control lists where username comes from the common name (CN) of the user certificate.
 */
public  class PerformanceTestIT extends IxnBaseIT{

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "jksLocal/king_harald.p12";
	private static final String TRUSTSTORE_JKS = "jksLocal/truststore.jks";

	private static final String NO_OUT = "king_harald";

	private static final String URI = "amqps://localhost:5671";

	private static final String DATEX2_TEXT = "\t<d2LogicalModel xmlns=\"http://datex2.eu/schema/2/2_0\" modelBaseVersion=\"2\"><exchange><supplierIdentification><country>dk</country><nationalIdentifier>Trafikman2</nationalIdentifier></supplierIdentification><target><address/><protocol/></target><subscription><operatingMode>operatingMode0</operatingMode><subscriptionStartTime>2018-12-12T20:35:45Z</subscriptionStartTime><subscriptionState>active</subscriptionState><updateMethod>allElementUpdate</updateMethod><target><address/><protocol/></target></subscription></exchange><payloadPublication xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"SituationPublication\" lang=\"da-DK\"><publicationTime>2018-12-17T07:57:03Z</publicationTime><publicationCreator><country>dk</country><nationalIdentifier>DRD-Trafikman2</nationalIdentifier></publicationCreator><situation id=\"Trafikman2/r_OTman/vejman_740_18-01282_TIC-Trafikman2/1\" version=\"0\"><situationVersionTime>2018-12-17T07:56:03.357Z</situationVersionTime><headerInformation><confidentiality>noRestriction</confidentiality><informationStatus>real</informationStatus></headerInformation><situationRecord xsi:type=\"MaintenanceWorks\" id=\"Trafikman2/r_OTman/vejman_740_18-01282_TIC-Trafikman2/1.0\" version=\"0\"><situationRecordCreationTime>2018-12-12T09:18:23.653Z</situationRecordCreationTime><situationRecordVersionTime>2018-12-17T07:56:03.357Z</situationRecordVersionTime><probabilityOfOccurrence>certain</probabilityOfOccurrence><validity><validityStatus>definedByValidityTimeSpec</validityStatus><validityTimeSpecification><overallStartTime>2018-12-12T09:18:23.653Z</overallStartTime><validPeriod><startOfPeriod>2018-12-14T05:00:00Z</startOfPeriod><endOfPeriod>2018-12-17T07:55:55Z</endOfPeriod></validPeriod></validityTimeSpecification></validity><impact><delays><delayTimeValue>300</delayTimeValue></delays></impact><generalPublicComment><comment><values><value lang=\"da-DK\">Grauballe, Grauballe Gudenåvej, fra Allinggårdsvej mod Eriksborgvej/Grønbækvej\n" +
			"ved Gudenåvej\n" +
			"vejarbejde (forsinkelse: 5 minutter), personer på vejen , hastighedsbegrænsning, indsnævring af vejbanen\n" +
			"Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>description</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>internalNote</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>other</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Grauballe Gudenåvej</value></values></comment><commentType>locationDescriptor</commentType></generalPublicComment><groupOfLocations xsi:type=\"Linear\"><locationForDisplay><latitude>56.2265549</latitude><longitude>9.63446</longitude></locationForDisplay><tpegLinearLocation><tpegDirection>unknown</tpegDirection><tpegLinearLocationType>segment</tpegLinearLocationType><to xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2264252</latitude><longitude>9.633836</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></to><from xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2265549</latitude><longitude>9.63446</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></from></tpegLinearLocation><alertCLinear xsi:type=\"AlertCMethod4Linear\"><alertCLocationCountryCode>9</alertCLocationCountryCode><alertCLocationTableNumber>9</alertCLocationTableNumber><alertCLocationTableVersion>12.0</alertCLocationTableVersion><alertCDirection><alertCDirectionCoded>negative</alertCDirectionCoded></alertCDirection><alertCMethod4PrimaryPointLocation><alertCLocation><specificLocation>11353</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4PrimaryPointLocation><alertCMethod4SecondaryPointLocation><alertCLocation><specificLocation>11354</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4SecondaryPointLocation></alertCLinear></groupOfLocations><management><lifeCycleManagement><end>true</end></lifeCycleManagement></management><roadMaintenanceType>roadworks</roadMaintenanceType></situationRecord><situationRecord xsi:type=\"GeneralObstruction\" id=\"Trafikman2/r_OTman/vejman_740_18-01282_TIC-Trafikman2/1.1\" version=\"0\"><situationRecordCreationTime>2018-12-12T09:18:23.653Z</situationRecordCreationTime><situationRecordVersionTime>2018-12-17T07:56:03.357Z</situationRecordVersionTime><probabilityOfOccurrence>certain</probabilityOfOccurrence><validity><validityStatus>definedByValidityTimeSpec</validityStatus><validityTimeSpecification><overallStartTime>2018-12-12T09:18:23.653Z</overallStartTime><validPeriod><startOfPeriod>2018-12-14T05:00:00Z</startOfPeriod><endOfPeriod>2018-12-17T07:55:55Z</endOfPeriod></validPeriod></validityTimeSpecification></validity><generalPublicComment><comment><values><value lang=\"da-DK\">Grauballe, Grauballe Gudenåvej, fra Allinggårdsvej mod Eriksborgvej/Grønbækvej\n" +
			"ved Gudenåvej\n" +
			"vejarbejde (forsinkelse: 5 minutter), personer på vejen , hastighedsbegrænsning, indsnævring af vejbanen\n" +
			"Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>description</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>internalNote</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>other</commentType></generalPublicComment><groupOfLocations xsi:type=\"Linear\"><locationForDisplay><latitude>56.2265549</latitude><longitude>9.63446</longitude></locationForDisplay><tpegLinearLocation><tpegDirection>unknown</tpegDirection><tpegLinearLocationType>segment</tpegLinearLocationType><to xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2264252</latitude><longitude>9.633836</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></to><from xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2265549</latitude><longitude>9.63446</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></from></tpegLinearLocation><alertCLinear xsi:type=\"AlertCMethod4Linear\"><alertCLocationCountryCode>9</alertCLocationCountryCode><alertCLocationTableNumber>9</alertCLocationTableNumber><alertCLocationTableVersion>12.0</alertCLocationTableVersion><alertCDirection><alertCDirectionCoded>negative</alertCDirectionCoded></alertCDirection><alertCMethod4PrimaryPointLocation><alertCLocation><specificLocation>11353</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4PrimaryPointLocation><alertCMethod4SecondaryPointLocation><alertCLocation><specificLocation>11354</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4SecondaryPointLocation></alertCLinear></groupOfLocations><management><lifeCycleManagement><end>true</end></lifeCycleManagement></management><situationRecordExtension><situationRecordExtended><safetyRelatedMessage>true</safetyRelatedMessage></situationRecordExtended></situationRecordExtension><obstructionType>peopleOnRoadway</obstructionType></situationRecord><situationRecord xsi:type=\"SpeedManagement\" id=\"Trafikman2/r_OTman/vejman_740_18-01282_TIC-Trafikman2/1.2\" version=\"0\"><situationRecordCreationTime>2018-12-12T09:18:23.653Z</situationRecordCreationTime><situationRecordVersionTime>2018-12-17T07:56:03.357Z</situationRecordVersionTime><probabilityOfOccurrence>certain</probabilityOfOccurrence><validity><validityStatus>definedByValidityTimeSpec</validityStatus><validityTimeSpecification><overallStartTime>2018-12-12T09:18:23.653Z</overallStartTime><validPeriod><startOfPeriod>2018-12-14T05:00:00Z</startOfPeriod><endOfPeriod>2018-12-17T07:55:55Z</endOfPeriod></validPeriod></validityTimeSpecification></validity><generalPublicComment><comment><values><value lang=\"da-DK\">Grauballe, Grauballe Gudenåvej, fra Allinggårdsvej mod Eriksborgvej/Grønbækvej\n" +
			"ved Gudenåvej\n" +
			"vejarbejde (forsinkelse: 5 minutter), personer på vejen , hastighedsbegrænsning, indsnævring af vejbanen\n" +
			"Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>description</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>internalNote</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>other</commentType></generalPublicComment><groupOfLocations xsi:type=\"Linear\"><locationForDisplay><latitude>56.2265549</latitude><longitude>9.63446</longitude></locationForDisplay><tpegLinearLocation><tpegDirection>unknown</tpegDirection><tpegLinearLocationType>segment</tpegLinearLocationType><to xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2264252</latitude><longitude>9.633836</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></to><from xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2265549</latitude><longitude>9.63446</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></from></tpegLinearLocation><alertCLinear xsi:type=\"AlertCMethod4Linear\"><alertCLocationCountryCode>9</alertCLocationCountryCode><alertCLocationTableNumber>9</alertCLocationTableNumber><alertCLocationTableVersion>12.0</alertCLocationTableVersion><alertCDirection><alertCDirectionCoded>negative</alertCDirectionCoded></alertCDirection><alertCMethod4PrimaryPointLocation><alertCLocation><specificLocation>11353</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4PrimaryPointLocation><alertCMethod4SecondaryPointLocation><alertCLocation><specificLocation>11354</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4SecondaryPointLocation></alertCLinear></groupOfLocations><management><lifeCycleManagement><end>true</end></lifeCycleManagement></management><complianceOption>mandatory</complianceOption><speedManagementType>observeSpeedLimit</speedManagementType><speedManagementExtension/></situationRecord><situationRecord xsi:type=\"RoadOrCarriagewayOrLaneManagement\" id=\"Trafikman2/r_OTman/vejman_740_18-01282_TIC-Trafikman2/1.3\" version=\"0\"><situationRecordCreationTime>2018-12-12T09:18:23.653Z</situationRecordCreationTime><situationRecordVersionTime>2018-12-17T07:56:03.357Z</situationRecordVersionTime><probabilityOfOccurrence>certain</probabilityOfOccurrence><validity><validityStatus>definedByValidityTimeSpec</validityStatus><validityTimeSpecification><overallStartTime>2018-12-12T09:18:23.653Z</overallStartTime><validPeriod><startOfPeriod>2018-12-14T05:00:00Z</startOfPeriod><endOfPeriod>2018-12-17T07:55:55Z</endOfPeriod></validPeriod></validityTimeSpecification></validity><generalPublicComment><comment><values><value lang=\"da-DK\">Grauballe, Grauballe Gudenåvej, fra Allinggårdsvej mod Eriksborgvej/Grønbækvej\n" +
			"ved Gudenåvej\n" +
			"vejarbejde (forsinkelse: 5 minutter), personer på vejen , hastighedsbegrænsning, indsnævring af vejbanen\n" +
			"Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>description</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>internalNote</commentType></generalPublicComment><generalPublicComment><comment><values><value lang=\"da-DK\">Hastighedsbegrænsning 20 km/t.\n" +
			"Forvent forlænget rejsetid I kortere perioder.</value></values></comment><commentType>other</commentType></generalPublicComment><groupOfLocations xsi:type=\"Linear\"><locationForDisplay><latitude>56.2265549</latitude><longitude>9.63446</longitude></locationForDisplay><tpegLinearLocation><tpegDirection>unknown</tpegDirection><tpegLinearLocationType>segment</tpegLinearLocationType><to xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2264252</latitude><longitude>9.633836</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></to><from xsi:type=\"TpegNonJunctionPoint\"><pointCoordinates><latitude>56.2265549</latitude><longitude>9.63446</longitude></pointCoordinates><name><descriptor><values><value/></values></descriptor><tpegOtherPointDescriptorType>other</tpegOtherPointDescriptorType></name></from></tpegLinearLocation><alertCLinear xsi:type=\"AlertCMethod4Linear\"><alertCLocationCountryCode>9</alertCLocationCountryCode><alertCLocationTableNumber>9</alertCLocationTableNumber><alertCLocationTableVersion>12.0</alertCLocationTableVersion><alertCDirection><alertCDirectionCoded>negative</alertCDirectionCoded></alertCDirection><alertCMethod4PrimaryPointLocation><alertCLocation><specificLocation>11353</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4PrimaryPointLocation><alertCMethod4SecondaryPointLocation><alertCLocation><specificLocation>11354</specificLocation></alertCLocation><offsetDistance><offsetDistance>0</offsetDistance></offsetDistance></alertCMethod4SecondaryPointLocation></alertCLinear></groupOfLocations><management><lifeCycleManagement><end>true</end></lifeCycleManagement></management><complianceOption>mandatory</complianceOption><roadOrCarriagewayOrLaneManagementType>narrowLanes</roadOrCarriagewayOrLaneManagementType></situationRecord></situation></payloadPublication></d2LogicalModel>";


	@Test
	public void testSendReceive2000() throws Exception {
		Context context = setContext(URI, NO_OUT, "onramp");
		Connection connection = createConnection(context);
		Destination receive = (Destination) context.lookup("receiveQueue");
		Destination send = (Destination) context.lookup("sendQueue");
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(receive);
		MessageProducer producer = session.createProducer(send);

		String start = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		for (int i = 0; i < 2000; i++) {
			JmsTextMessage message = (JmsTextMessage) session.createTextMessage(DATEX2_TEXT);
			message.getFacade().setUserId("king_harald");
			message.setStringProperty("who", "Norwegian Public Roads Administration");
			message.setStringProperty("how", "Datex2");
			message.setStringProperty("what", "Conditions");
			message.setDoubleProperty("lat", 63.0d + (i / 100000d));
			message.setDoubleProperty("lon", 10.0d + (i / 100000d));
			message.setStringProperty("where1", "NO");
			message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

			producer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
			Message received = consumer.receive();
			if (received.getStringProperty("when") != null) {
				int delay = -1;
				try {
					delay = (int) ZonedDateTime.parse(received.getStringProperty("when")).until(ZonedDateTime.now(), ChronoUnit.MILLIS);
				} catch (Exception ignored) {
				}
				System.out.println(delay);
			}
		}
		int totalRunTime = (int) ZonedDateTime.parse(start).until(ZonedDateTime.now(), ChronoUnit.MILLIS);
		System.out.println("total duration " + totalRunTime);
	}


}
