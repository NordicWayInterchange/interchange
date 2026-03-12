import { Button, ButtonProps } from "@mui/material";
import React from "react";

interface Props extends ButtonProps {
  text: string;
}

const ButtonComponent = (props: Props) => {
  const { text, variant = "contained", onClick, color = "primary" } = props;
  return (
    <Button color={color} onClick={onClick} variant={variant}>
      {text}
    </Button>
  );
};

export default ButtonComponent;
