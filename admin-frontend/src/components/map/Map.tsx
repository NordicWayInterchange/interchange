import { CircularProgress } from "@mui/material";
import dynamic from "next/dynamic";
import { useMemo } from "react";
import { Box } from "@mui/system";
import { styled } from "@mui/material/styles";

type Props = {
  quadtreeCallback?: (value: string[]) => void;
  quadtree: string[];
  interactive?: boolean;
};

export default function Map(props: Props) {
  const { quadtree } = props;

  const DynamicMap = useMemo(
    () =>
      dynamic(() => import("./DynamicMap"), {
        loading: () => (
          <StyledBox>
            <CircularProgress />
          </StyledBox>
        ),
        ssr: false,
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [quadtree]
  );

  return <DynamicMap {...props} />;
}

const StyledBox = styled(Box)(({}) => ({
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  height: "100%",
}));
