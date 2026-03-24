import Box from "@mui/material/Box";
import React, { ReactNode } from "react";
import Sidebar from "./Sidebar";
import { useSession } from "next-auth/react";
import { CircularProgress, CssBaseline, Toolbar } from "@mui/material";
import Navbar from "@/components/layout/Navbar";

type LayoutProps = {
  children: ReactNode;
};

export default function Layout({ children }: LayoutProps) {
  const { status: authStatus } = useSession();

  if (authStatus == "authenticated") {
    return (
      <Box sx={{ display: "flex" }}>
        <Sidebar />
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            bgcolor: "mainBackgroundColor",
            p: 5,
            height: "100vh",
            width: "100vw",
            overflow: "auto",
          }}
        >
          <Toolbar />
          {children}
        </Box>
      </Box>
    );
  }

  return (
    <Box
      component="main"
      sx={{
        bgcolor: "mainBackgroundColor",
        height: "100vh",
        width: "100vw",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        overflow: "auto",
      }}
    >
      <CssBaseline />
      <Navbar />
      {authStatus == "unauthenticated" ? children : <CircularProgress />}
    </Box>
  );
}
