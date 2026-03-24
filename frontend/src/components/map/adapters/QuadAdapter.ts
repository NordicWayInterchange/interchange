import { LatLng, Point, Map } from "leaflet";

function latLonToQtree(lat: number, lon: number, zoom: number) {
  const sinlat = Math.sin((lat * Math.PI) / 180);

  const x = 0.5 + lon / 360;
  const y = 0.5 - Math.log((1 + sinlat) / (1 - sinlat)) / (4 * Math.PI);

  let ix = Math.pow(2, zoom) * x;
  let iy = Math.pow(2, zoom) * y;

  let qtree = "";
  for (let i = 0; i < zoom; i++) {
    qtree = ((ix & 1) | (2 * (iy & 1))) + qtree;
    ix = ix >> 1;
    iy = iy >> 1;
  }
  return qtree;
}

function QuadToSlippy(quad: string) {
  let x = 0;
  let y = 0;
  let z = 0;

  quad.split("").forEach(function (char: string) {
    x *= 2;
    y *= 2;
    z++;

    if (char == "1" || char == "3") {
      x++;
    }

    if (char == "2" || char == "3") {
      y++;
    }
  });
  return { x: x, y: y, z: z };
}

const QuadAdapter = (map: Map) => ({
  range: ["0", "1", "2", "3"],
  encode: function (centroid: LatLng, precision: number) {
    const zoom = precision - 1;
    return latLonToQtree(centroid.lat, centroid.lng, zoom);
  },
  bbox: function (hash: string) {
    const tileSize = 256;
    const tile = QuadToSlippy(hash);

    const nwTilePoint = new Point(tile.x * tileSize, tile.y * tileSize);
    const seTilePoint = new Point(tile.x * tileSize, tile.y * tileSize);
    seTilePoint.x += tileSize;
    seTilePoint.y += tileSize;

    const nwLatLon = map.unproject(nwTilePoint, tile.z);
    const seLatLon = map.unproject(seTilePoint, tile.z);

    return {
      minlng: nwLatLon.lng,
      minlat: seLatLon.lat,
      maxlng: seLatLon.lng,
      maxlat: nwLatLon.lat,
    };
  },
  layers: function (currentHash: string, zoom: number) {
    type LayersType = {
      [key: string]: boolean;
    };

    const layers: LayersType = {};

    if (zoom > 2) layers[currentHash.substring(0, zoom - 2)] = true;
    if (zoom > 1) layers[currentHash.substring(0, zoom - 1)] = true;
    layers[currentHash.substring(0, zoom)] = true;

    return layers;
  },
  labels: function (hash: string) {
    return {
      long: hash,
      short: hash.substring(-1, 1),
    };
  },
});

export default QuadAdapter;
