import { Typography } from "@mui/material";
import { Box } from "@mui/system";
import React, { useMemo } from "react";
import { POSITIONS } from "@/components/map/controls/Positions";
import { styled } from "@mui/material/styles";

type MapControlsProps = {
  controlsHash: string[];
  quadtree: string[];
};

export default function CustomControls({
  controlsHash,
  quadtree,
}: MapControlsProps) {
  let hash: string[];

  if (quadtree?.length && !controlsHash?.length) {
    hash = quadtree;
  } else {
    hash = controlsHash;
  }

  const inputBar = useMemo(
    () => (
      <StyledBox>
        <Typography noWrap={true}>{hash && hash.join()}</Typography>
      </StyledBox>
    ),
    [hash]
  );
  return (
    <div className={POSITIONS.topright}>
      <div className="leaflet-control leaflet-bar">{inputBar}</div>
    </div>
  );
}

const StyledBox = styled(Box)(({}) => ({
  width: 400,
  height: 40,
  backgroundColor: "white",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  padding: "10px",
}));
