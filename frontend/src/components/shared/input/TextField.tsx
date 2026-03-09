import {
  FormControl,
  StandardTextFieldProps,
  TextField as MuiTextField,
} from "@mui/material";
import React from "react";
import { styled } from "@mui/material/styles";

interface Props extends StandardTextFieldProps {}

const TextField = (props: Props) => {
  return (
    <FormControl fullWidth>
      <StyledTextField {...props} />
    </FormControl>
  );
};

const StyledTextField = styled(MuiTextField)(({}) => ({
  "& .MuiInputBase-input": { background: "white" },
}));

export default TextField;
