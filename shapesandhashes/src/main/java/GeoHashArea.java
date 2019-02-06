import ch.hsr.geohash.GeoHash;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public class GeoHashArea {

	private final Set<GeoHash> area = new HashSet<>();

	public GeoHashArea(Collection<GeoHash> area) {
		this.area.addAll(area);
	}

	public Set<GeoHash> getNeigbours() {
		Set<GeoHash> neighbours = new HashSet<>();
		Map<Integer, Set<GeoHash>> precisionGeoHashMap = new HashMap<>();
		for (GeoHash areaHash : area) {
			for (GeoHash neighbour : areaHash.getAdjacent()) {
				if (!area.contains(neighbour)) {
					int precision = neighbour.getCharacterPrecision();
					Set<GeoHash> geoHashes = precisionGeoHashMap.computeIfAbsent(precision, k -> new HashSet<>());
					geoHashes.add(neighbour);
					neighbours.add(neighbour);
				}
			}
		}

		//Neighbour must not be part of the lowest precision area
		Integer lowestPrecision = precisionGeoHashMap.keySet().iterator().next();
		if (lowestPrecision != null) {
			for (GeoHash geoHash : precisionGeoHashMap.get(lowestPrecision)) {
				neighbours.removeIf(covered -> covered != geoHash && covered.within(geoHash));
			}
		}

		// Neighbour must not be part of the original area
		for (GeoHash geoHash : area) {
			neighbours.removeIf(covered -> covered != geoHash && covered.within(geoHash));
		}

		return neighbours;
	}

	static class LonLat {
		final public double lon, lat;

		LonLat(double lon, double lat) {
			this.lon = lon;
			this.lat = lat;
		}
	}

	public static void main(String[] args) {
		List<LonLat> eventArea = getEventArea();
		System.out.println("Distance " + getDistance(eventArea.get(0), eventArea.get(eventArea.size() - 1)));
		Set<GeoHash> eventHashes = new HashSet<>();
		for (LonLat lonLat : eventArea) {
			GeoHash eventHash = GeoHash.withCharacterPrecision(lonLat.lat, lonLat.lon, 6);
			eventHashes.add(eventHash);
		}
		System.out.println("hashes: " + eventHashes.size());
		for (GeoHash eventHash : eventHashes) {
			System.out.println(eventHash.toBase32());
		}

	}

	private static double getDistance(LonLat from, LonLat to) {
		GeodesicData g = Geodesic.WGS84.Inverse(from.lat, from.lon, to.lat, to.lon);
		return g.s12;
	}


	private static List<LonLat> getEventArea() {
		List<LonLat> area = new LinkedList<>();
		area.add(new LonLat(7.404180141463237, 59.28536476928409));
		area.add(new LonLat(7.404001624338982, 59.28554455081058));
		area.add(new LonLat(7.403828270582126, 59.285692834978654));
		area.add(new LonLat(7.403692552864854, 59.28579507832624));
		area.add(new LonLat(7.403414431296033, 59.28597590490725));
		area.add(new LonLat(7.403086490141508, 59.28615407871466));
		area.add(new LonLat(7.402852064042737, 59.286264959699146));
		area.add(new LonLat(7.402609055634177, 59.286363793181785));
		area.add(new LonLat(7.40226322447992, 59.28648491108043));
		area.add(new LonLat(7.40152602718505, 59.28672501349313));
		area.add(new LonLat(7.401248402706455, 59.286831192593496));
		area.add(new LonLat(7.401050669850849, 59.286919874150605));
		area.add(new LonLat(7.400965043763296, 59.28696372925053));
		area.add(new LonLat(7.400840886210298, 59.28703950300287));
		area.add(new LonLat(7.400658548322759, 59.28717508161358));
		area.add(new LonLat(7.400554952095735, 59.28727235334072));
		area.add(new LonLat(7.40044381364851, 59.287394614413735));
		area.add(new LonLat(7.400352238854203, 59.28753732308469));
		area.add(new LonLat(7.4002881736294475, 59.28769805982436));
		area.add(new LonLat(7.400267441657316, 59.287803892173166));
		area.add(new LonLat(7.400254386525168, 59.28793208992252));
		area.add(new LonLat(7.400216979949769, 59.28856434310454));
		area.add(new LonLat(7.4001732560272835, 59.28875305852403));
		area.add(new LonLat(7.400129175074481, 59.28886789181169));
		area.add(new LonLat(7.400059378683966, 59.28899284969411));
		area.add(new LonLat(7.400017549183908, 59.2890536141577));
		area.add(new LonLat(7.399870221488085, 59.28921694956669));
		area.add(new LonLat(7.399656570006577, 59.289408768498994));
		area.add(new LonLat(7.399463421590609, 59.289555525829655));
		area.add(new LonLat(7.399234338382258, 59.28970765908379));
		area.add(new LonLat(7.399069676050215, 59.289804412103265));
		area.add(new LonLat(7.398855297256056, 59.289913758495615));
		area.add(new LonLat(7.398635685615387, 59.290013418207096));
		area.add(new LonLat(7.39842280673125, 59.29009877269382));
		area.add(new LonLat(7.39820775033363, 59.290177325370394));
		area.add(new LonLat(7.398024302506926, 59.29023834353462));
		area.add(new LonLat(7.397702335228331, 59.29033532998464));
		area.add(new LonLat(7.397365391325838, 59.29042205721705));
		area.add(new LonLat(7.397149820376784, 59.29047144836902));
		area.add(new LonLat(7.396638519020503, 59.29057706686825));
		area.add(new LonLat(7.395868398474016, 59.29072601593525));
		area.add(new LonLat(7.393789021515859, 59.291146542524295));
		area.add(new LonLat(7.393338533784236, 59.29123245285808));
		area.add(new LonLat(7.393144539174263, 59.29126263305174));
		area.add(new LonLat(7.392766360037599, 59.291310149070796));
		area.add(new LonLat(7.3925611248122385, 59.291331263060904));
		area.add(new LonLat(7.392342822848467, 59.29134720782647));
		area.add(new LonLat(7.391964042227304, 59.291366530569924));
		area.add(new LonLat(7.391483014630891, 59.291376417834705));
		area.add(new LonLat(7.391102310680122, 59.29136739700298));
		area.add(new LonLat(7.390780489308923, 59.291346593578645));
		area.add(new LonLat(7.390467231817118, 59.2913104198972));
		area.add(new LonLat(7.390114509973063, 59.291256865335036));
		area.add(new LonLat(7.389731727438266, 59.291180169173124));
		area.add(new LonLat(7.388762651086884, 59.29095411695558));
		area.add(new LonLat(7.387331286429653, 59.290598881096365));
		area.add(new LonLat(7.386091243102385, 59.29029690418417));
		area.add(new LonLat(7.383760187697648, 59.28974938613034));
		area.add(new LonLat(7.383119288708409, 59.28959453749664));
		area.add(new LonLat(7.382666185982477, 59.28947084420941));
		area.add(new LonLat(7.381588535058936, 59.28913688775113));
		area.add(new LonLat(7.381286108488462, 59.28906362989182));
		area.add(new LonLat(7.381028427180712, 59.28901717399645));
		area.add(new LonLat(7.38068576775336, 59.288970767867795));
		area.add(new LonLat(7.378874042256789, 59.288775137476655));
		area.add(new LonLat(7.37868778406021, 59.28876228118459));
		area.add(new LonLat(7.378308820105523, 59.28875034760878));
		area.add(new LonLat(7.37803196737982, 59.28875389300586));
		area.add(new LonLat(7.377778773886875, 59.288766676739634));
		area.add(new LonLat(7.377529439830896, 59.28878816474068));
		area.add(new LonLat(7.37717452603792, 59.288833108520635));
		area.add(new LonLat(7.376925211676403, 59.28887723347532));
		area.add(new LonLat(7.3765967101356695, 59.288950516161655));
		area.add(new LonLat(7.376383302162245, 59.28901055264377));
		area.add(new LonLat(7.376199100457623, 59.28907005366659));
		area.add(new LonLat(7.376047401821767, 59.28912533541032));
		area.add(new LonLat(7.375886930430549, 59.289192185541594));
		area.add(new LonLat(7.375724596283672, 59.28926731322023));
		area.add(new LonLat(7.375420316641981, 59.28942174473691));
		area.add(new LonLat(7.37510427530207, 59.28959631630834));
		area.add(new LonLat(7.374807268682413, 59.28978499506391));
		area.add(new LonLat(7.374715916415886, 59.28985645518475));
		area.add(new LonLat(7.374566950833833, 59.290002985449206));
		area.add(new LonLat(7.374425937070969, 59.29016901336878));
		area.add(new LonLat(7.374059787009021, 59.29065709919087));
		area.add(new LonLat(7.373822142591808, 59.2909430669242));
		area.add(new LonLat(7.373553659978696, 59.29124462436249));
		area.add(new LonLat(7.373073138971856, 59.29175275829499));
		area.add(new LonLat(7.37293674838687, 59.29190679198571));
		area.add(new LonLat(7.372867483055184, 59.292006969030815));
		area.add(new LonLat(7.372802518270886, 59.29213111864377));
		area.add(new LonLat(7.372258583110082, 59.29337045332823));
		area.add(new LonLat(7.372143074762409, 59.29356873603124));
		area.add(new LonLat(7.371987401389673, 59.293773130470825));
		area.add(new LonLat(7.371179822024234, 59.29473755401737));
		area.add(new LonLat(7.37045723631064, 59.29555059618053));
		area.add(new LonLat(7.370317073309683, 59.295730650595104));
		area.add(new LonLat(7.370197664277656, 59.295898399477274));
		area.add(new LonLat(7.370098051269426, 59.29606749496542));
		area.add(new LonLat(7.369974547216451, 59.29634737641564));
		area.add(new LonLat(7.369771649082985, 59.2969719676277));
		area.add(new LonLat(7.369283438183453, 59.298336424513714));
		area.add(new LonLat(7.3692009819621065, 59.298621880936125));
		area.add(new LonLat(7.369184915492733, 59.29876288999588));
		area.add(new LonLat(7.3691863251248675, 59.29884901226893));
		area.add(new LonLat(7.36920623757712, 59.29898059714088));
		area.add(new LonLat(7.369236330480868, 59.29907481273714));
		area.add(new LonLat(7.369301383709043, 59.29921031983838));
		area.add(new LonLat(7.3693701411063195, 59.29931529107334));
		area.add(new LonLat(7.3694453217543145, 59.299412884539436));
		area.add(new LonLat(7.369534363288164, 59.29950687537011));
		area.add(new LonLat(7.369617126914456, 59.29958254888056));
		area.add(new LonLat(7.369727827516341, 59.29967276507066));
		area.add(new LonLat(7.369807324840876, 59.299728494867544));
		area.add(new LonLat(7.370007657537498, 59.29985528644658));
		area.add(new LonLat(7.371406396525762, 59.30067045566829));
		area.add(new LonLat(7.371698692154584, 59.30084937795063));
		area.add(new LonLat(7.371798824378237, 59.300919218468074));
		area.add(new LonLat(7.371924902868621, 59.30102341530307));
		area.add(new LonLat(7.37204481246516, 59.301147180130556));
		area.add(new LonLat(7.3721453610787515, 59.30129271222556));
		area.add(new LonLat(7.372221923251025, 59.301475071118965));
		area.add(new LonLat(7.372254882330168, 59.301609858461994));
		area.add(new LonLat(7.372334207565881, 59.30207286331324));
		area.add(new LonLat(7.372373402204255, 59.30243998024665));
		area.add(new LonLat(7.372375084353078, 59.30265571779653));
		area.add(new LonLat(7.372363001631477, 59.3027610668317));
		area.add(new LonLat(7.3723386289393105, 59.30286695473741));
		area.add(new LonLat(7.372212195230817, 59.30323811648825));
		area.add(new LonLat(7.37218735407154, 59.30336174377164));
		area.add(new LonLat(7.372184723067579, 59.30345330962046));
		area.add(new LonLat(7.372200006179688, 59.30357731572956));
		area.add(new LonLat(7.372220413345968, 59.3036621214219));
		area.add(new LonLat(7.37248502812763, 59.304560099647006));
		area.add(new LonLat(7.372496573007358, 59.30467486678384));
		area.add(new LonLat(7.372489506826839, 59.30474227174112));
		area.add(new LonLat(7.372458057423433, 59.3048498173228));
		area.add(new LonLat(7.372410299091387, 59.30495459876598));
		area.add(new LonLat(7.372310889806938, 59.30509764451809));
		area.add(new LonLat(7.37199186968315, 59.305478118368626));
		area.add(new LonLat(7.371877916139182, 59.30565205346817));
		area.add(new LonLat(7.371169816672929, 59.30703946589551));
		area.add(new LonLat(7.370801504683109, 59.307788157814976));
		area.add(new LonLat(7.370235774586719, 59.30896678129714));
		area.add(new LonLat(7.370112241155595, 59.30918894308428));
		area.add(new LonLat(7.370004948470668, 59.30934775747786));
		area.add(new LonLat(7.369927801349573, 59.30944747060745));
		area.add(new LonLat(7.36981313752533, 59.3095586824523));
		area.add(new LonLat(7.369671285020951, 59.309674696065564));
		area.add(new LonLat(7.368755780525803, 59.31033234993608));
		area.add(new LonLat(7.368176287032808, 59.31073715618531));
		area.add(new LonLat(7.367241982551883, 59.311417684115156));
		area.add(new LonLat(7.366557650939374, 59.31190315875858));
		area.add(new LonLat(7.363861423777809, 59.313783147006596));
		area.add(new LonLat(7.363428116118851, 59.31410320807123));
		area.add(new LonLat(7.363292428822757, 59.31421516048979));
		area.add(new LonLat(7.363144557176813, 59.314352188497445));
		area.add(new LonLat(7.363047289352017, 59.31445413986201));
		area.add(new LonLat(7.362964965013273, 59.31455261406594));
		area.add(new LonLat(7.362784729243011, 59.31479301307425));
		area.add(new LonLat(7.362477420741353, 59.31531224081924));
		area.add(new LonLat(7.362342337560863, 59.315505939173036));
		area.add(new LonLat(7.362275148553583, 59.31558792796428));
		area.add(new LonLat(7.362068272059708, 59.31579876831906));
		area.add(new LonLat(7.361873628710283, 59.31595693791465));
		area.add(new LonLat(7.361787205452198, 59.31601589981677));
		return area;
	}
}
