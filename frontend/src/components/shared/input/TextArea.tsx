import {
  FormControl,
  StandardTextFieldProps,
  TextField as MuiTextField,
} from "@mui/material";
import React from "react";

interface Props extends StandardTextFieldProps {}

const TextArea = (props: Props) => {
  const { name, label, onChange, value, rows = 4, disabled = true } = props;
  return (
    <FormControl fullWidth>
      <MuiTextField
        sx={{ backgroundColor: "white" }}
        value={value}
        name={name}
        label={label}
        disabled={disabled}
        multiline
        rows={rows}
        onChange={onChange}
      />
    </FormControl>
  );
};

export default TextArea;
