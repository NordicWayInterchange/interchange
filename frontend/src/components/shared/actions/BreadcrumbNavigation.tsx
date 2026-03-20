import { Breadcrumbs, ButtonProps, Typography } from "@mui/material";
import React from "react";
import { Box } from "@mui/system";
import NavigateBeforeIcon from "@mui/icons-material/NavigateBefore";
import NextLink from 'next/link';

interface Props extends ButtonProps {
  text: string;
}
export const BreadcrumbNavigation = (props: Props) => {
  const { text} = props;

  const getUrl = () => {
    return text === 'Subscriptions' ? "/subscriptions" :
           text === 'Deliveries' ? '/deliveries' :
           text === 'Private channels' ? '/private-channels' :
             '/capabilities';
  };

  return (
    <Box sx={{ width: "100%", py: 2, ml: -.5 }}>
      <Box sx={{ display: "flex", alignItems: "center" }}>
        <NextLink href={getUrl()} passHref>
          <Typography
            sx={{
              display: "flex",
              alignItems: "center",
              textDecoration: "none",
              color: "#444f55", cursor: "pointer",
              mr: 1
            }}
          >
            <NavigateBeforeIcon />
          </Typography>
        </NextLink>

        <NextLink href={getUrl()} passHref>
          <Breadcrumbs sx={{ ml: -1.25 }}>
            <Typography
              sx={{
                cursor: "pointer",
                textDecoration: "underline",
                textDecorationThickness: "1px",
                "&:hover": {
                  textDecorationThickness: "2px"
                }
              }}
            >
              {text}
            </Typography>
          </Breadcrumbs>
        </NextLink>
      </Box>
    </Box>
  );
};