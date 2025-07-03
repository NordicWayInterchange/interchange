import {TextField} from "@mui/material";
import React from "react";

interface Props {
    searchId: string,
    setSearchId?: (value: (((prevState: string) => string) | string)) => void
}

const SearchBox = ({searchId, setSearchId}: Props) => {
    return (
        <TextField
            label="Search by ID"
            variant="outlined"
            value={searchId}
            onChange={(e) => setSearchId ? setSearchId(e.target.value) : null}
            style={{ marginBottom: 16, marginTop: -20 }}
            type="text"
        />
    );
};

export default SearchBox;
