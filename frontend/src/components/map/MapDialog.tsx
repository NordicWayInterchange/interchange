import {
  AppBar,
  Dialog,
  IconButton,
  Slide,
  Toolbar,
} from "@mui/material";
import Map from "./Map";
import React, { useEffect, useState } from "react";
import { TransitionProps } from "@mui/material/transitions";
import CloseIcon from "@mui/icons-material/Close";
import { Box } from "@mui/system";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

type Props = {
  quadtreeCallback?: (value: string[]) => void;
  quadtree: string[];
  open: boolean;
  interactive?: boolean;
  onClose: () => void;
};

const Transition = React.forwardRef(function Transition(
  props: TransitionProps & {
    children: React.ReactElement;
  },
  ref: React.Ref<unknown>
) {
  return <Slide direction="up" ref={ref} {...props} />;
});

export default function MapDialog(props: Props) {
  const {
    onClose,
    open,
    quadtreeCallback,
    quadtree,
    interactive = true,
  } = props;

  const [discard, setDiscard] = useState(false);

  const handleDiscard = () => {
    quadtreeCallback && quadtreeCallback([]);
    setDiscard((current) => !current);
  };

  /*
  Wait for handleDiscard before closing dialog
  */
  useEffect(() => {
    onClose();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [discard]);

  return (
    <Dialog
      fullScreen
      TransitionComponent={Transition}
      open={open}
      onClose={onClose}
    >
      <AppBar
        sx={{
          position: "relative",
          bgcolor: "navbarBackgroundColor",
        }}
      >
        <Toolbar>
          <Box sx={{ flex: 1 }}>
            <IconButton
              edge="start"
              color="inherit"
              onClick={handleDiscard}
              aria-label="close"
            >
              <CloseIcon />
            </IconButton>
          </Box>
          {interactive && (
            <StyledButton autoFocus color="inherit" onClick={onClose}>
              SAVE
            </StyledButton>
          )}
        </Toolbar>
      </AppBar>
      <Map
        quadtree={quadtree}
        quadtreeCallback={quadtreeCallback}
        interactive={interactive}
      />
    </Dialog>
  );
}
