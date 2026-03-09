import {TextField } from "@mui/material";
import React from "react";
import SearchIcon from '@mui/icons-material/Search';

interface Props {
  searchId: string,
  setSearchId?: (value: (((prevState: string) => string) | string)) => void,
  label: string,
  searchElement: string,
}

const SearchBox = ({searchId, setSearchId, label, searchElement}: Props) => {
  return (
    <TextField
      label={
        <span style={{ display: 'flex', alignItems: 'center', gap: 4, transform: 'translateY(-1px)' }}>
          <SearchIcon fontSize="small"/>
          {`Find ${label} by ${searchElement} in table`}
        </span>
      }
      variant="standard"
      value={searchId}
      onChange={(e) => setSearchId ? setSearchId(e.target.value) : null}
      style={{ marginRight: 1, width: '330px' }}
      type="text"
      sx={textFieldSx}
    />
  );
};

const textFieldSx = {
  "& .MuiInputLabel-root.Mui-focused": {
    color: "searchBoxFocusedFontColor",
  },
  '& .MuiInput-underline:before': {
    borderBottom: '2px solid #dd7100',
  },
  '& .MuiInput-underline:after': {
    borderBottom: '2px solid #FF9600',
  },
  position: "relative",
  top: "-3px",
  bgcolor: "mainBackgroundColor",
};

export default SearchBox;
