import React, { useState } from "react";
import { MapContainer, TileLayer } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import QuadtreeGenerator from "./quadtree/QuadtreeGenerator";
import CustomControls from "./controls/CustomControls";

type Props = {
  quadtreeCallback?: (value: string[]) => void;
  quadtree: string[];
  interactive?: boolean;
};

const SIZE = "100%";

export default function DynamicMap(props: Props) {
  const { quadtreeCallback, quadtree, interactive } = props;
  const [controlsHash, setControlsHash] = useState<string[]>([]);

  const controlsCallback = (hash: string) => {
    setControlsHash([hash]);
  };

  return (
    <MapContainer
      center={[0, 0]}
      zoom={3}
      maxBounds={[
        [-90, -180],
        [90, 180],
      ]}
      scrollWheelZoom={true}
      doubleClickZoom={false}
      style={{ height: SIZE, width: SIZE }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <QuadtreeGenerator
        controlsCallback={controlsCallback}
        quadtree={quadtree}
        quadtreeCallback={quadtreeCallback}
        interactive={interactive}
      />
      {interactive && (
        <CustomControls controlsHash={controlsHash} quadtree={quadtree} />
      )}
    </MapContainer>
  );
}
