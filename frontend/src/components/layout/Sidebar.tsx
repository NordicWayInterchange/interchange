import {
  Box,
  CssBaseline,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Toolbar,
  ListItemIcon,
} from "@mui/material";
import Link from "next/link";
import React from "react";
import { useRouter } from "next/router";
import Navbar from "./Navbar";
import HouseIcon from "@mui/icons-material/House";
import CellTowerIcon from "@mui/icons-material/CellTower";
import SubscriptionsIcon from "@mui/icons-material/Subscriptions";
import SettingsIcon from "@mui/icons-material/Settings";
import { styled } from "@mui/material/styles";
import { IPages } from "@/interface/IPages";
import LocalPostOfficeIcon from '@mui/icons-material/LocalPostOffice';
import PersonIcon from '@mui/icons-material/Person';
import ArticleIcon from '@mui/icons-material/Article';
import LockIcon from '@mui/icons-material/Lock';
import TableRowsIcon from '@mui/icons-material/TableRows';
import Version from "@/components/shared/Version";

const MAIN_PAGES: Array<IPages> = [
  {
    text: "Home",
    url: "/",
    icon: <HouseIcon /> },
  {
    text: "Subscriptions",
    url: "/subscriptions",
    icon: <SubscriptionsIcon /> },
  {
    text: "Network capabilities",
    url: "/network-capabilities",
    icon: <CellTowerIcon />,
  },
  {
    text: "My capabilities",
    url: "/capabilities",
    icon: <PersonIcon />,
  },
  {
    text: "Deliveries",
    url: "/deliveries",
    icon: <LocalPostOfficeIcon />,
  },
  {
    text: "Private channels",
    url: "/private-channels",
    icon: <LockIcon />,
  },
  {
    text: "Bi-queues",
    url: "/bi-queues",
    icon: <TableRowsIcon />,
  },

];

const SECONDARY_PAGES: Array<IPages> = [
  {
    text: "Certificate",
    url: "/certificate",
    icon: <SettingsIcon />,
  },
  {
    text: "User help",
    url: "/generated/USERHELP.html",
    icon: <ArticleIcon />
  }
];

export default function Sidebar() {
  const router = useRouter();

  const mapPages = (pages: Array<IPages>) => {
    return pages.map((page: IPages, key: number) => (
      <Link
        href={page.url}
        key={key}
        style={{
          textDecoration: "none",
          color: "inherit",
        }}
        target={page.text === "User help" ? "_blank" : ""}
        rel={page.text === "User help" ?"noopener noreferrer" : ""}
      >
        <ListItem
          sx={{
            borderRadius: 100,
            my: 2,
            backgroundColor:
              router.asPath === page.url ? "sidebarActiveColor" : null,
            border: "1px solid transparent",
            "&:hover": {
              backgroundColor: "sidebarActiveColor",
              border: "1px solid",
              borderColor: "sidebarBorderColor",
            },
          }}
          disablePadding
        >
          <StyledListItemButton disableRipple>
            <ListItemIcon sx={{color: "inherit"}}>{page.icon}</ListItemIcon>
            <ListItemText primary={page.text} />
          </StyledListItemButton>
        </ListItem>
      </Link>
    ));
  };

  return (
    <Box sx={{ display: "flex" }}>
      <CssBaseline />
      <Navbar />
      <StyledDrawer
        sx={{ display: { xs: "none", sm: "block" } }}
        variant="permanent"
        anchor="left"
      >
        <Toolbar />

        <Box sx={{ padding: 2 }}>
          <List>{mapPages(MAIN_PAGES)}</List>
        </Box>

        <Box sx={{ marginTop: "auto", padding: 2 }}>
          <List>
            {SECONDARY_PAGES.map((page) => (
                <ListItem
                    sx={{
                      borderRadius: 100,
                      my: 2,
                      backgroundColor:
                          router.asPath === page.url ? "sidebarActiveColor" : null,
                      border: "1px solid transparent",
                      "&:hover": {
                        backgroundColor: "sidebarActiveColor",
                        border: "1px solid",
                        borderColor: "sidebarBorderColor",
                      },
                    }}
                    disablePadding
                >
                <ListItemButton
                    key={page.text}
                    component="a"
                    href={page.url}
                    target="_self"
                >
                  <ListItemIcon sx={{ color: "text.primary"}}>
                    {page.icon}
                  </ListItemIcon>
                  <ListItemText primary={page.text}/>
                </ListItemButton>
                </ListItem>
            ))}
          </List>
        </Box>
        <Version></Version>
      </StyledDrawer>
    </Box>
  );
}

const StyledListItemButton = styled(ListItemButton)(({}) => ({
  "&:hover": {
    backgroundColor: "transparent",
    borderRadius: 100,
  },
}));

const StyledDrawer = styled(Drawer)(({}) => ({
  width: 360,
  flexShrink: 0,
  "& .MuiDrawer-paper": {
    width: 360,
    boxSizing: "border-box",
  },
}));
