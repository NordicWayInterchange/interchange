import { IconButton, Tooltip } from "@mui/material";
import { Box } from "@mui/system";
import React from "react";
import { styled } from "@mui/material/styles";
import Link from "next/link";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";

export default function UserAssistance() {

  return (
    <Box
      position="absolute"
      top={6}
      right={-4}
      sx={{
        transform: "translate(50%, -50%)"
      }}
    >
      <Tooltip title={
        <span style={{ fontSize: ".88rem" }}>
          Do you need help with filling out this form? Please visit our  <CustomLink
          href="https://github.com/NordicWayInterchange/interchange/blob/federation-master/GLOSSARY.md"
          target="_blank"
          rel="noopener noreferrer"
        >
         glossary
      </CustomLink>
        </span>
      } arrow placement="right">
        <IconButton size="small">
          <InfoOutlinedIcon fontSize="small" />
        </IconButton>
      </Tooltip>
    </Box>
  );
}


const CustomLink = styled(Link)(({ theme }) => ({
  gap: theme.spacing(1),
  color: "#FF9600",
  textDecoration: "none",
  fontWeight: 600,
  transition: "color 0.3s ease, transform 0.3s ease",
  "&:hover": {
    color: "#FF9600",
    textDecoration: "underline",
    transform: "scale(1.05)"
  }
}));