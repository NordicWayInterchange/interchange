import * as React from 'react';
import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import CssBaseline from '@mui/material/CssBaseline';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import Divider from '@mui/material/Divider';
import ListItem from '@mui/material/ListItem';

import { NavLink } from 'react-router-dom';

export default function PermanentDrawerLeft() {
  return (
    <Box sx={{ display: 'inline-flex' }}>
      <CssBaseline />
      <div>
        <Drawer
          PaperProps={{
            sx: {
              backgroundColor: "#11133C",
              position: 'fixed',
            }
          }}
          variant="permanent"
          anchor="left"
        >
          <Toolbar />
          <Divider />
          <List>
            <ListItem key="Home">
              <NavLink to="/" className={({ isActive }) => (isActive ? "link-active" : "link")}>
                Home
              </NavLink>
            </ListItem>
            <ListItem key="Monitoring">
              <NavLink to="/monitoring" className={({ isActive }) => (isActive ? "link-active" : "link")}>
                Monitoring
              </NavLink>
            </ListItem>

            <ListItem key="Qpid">
              <NavLink to="/qpid" className={({ isActive }) => (isActive ? "link-active" : "link")}>
                Qpid
              </NavLink>
            </ListItem>

            <ListItem key="Neighbours" >
              <NavLink to="/neighbours" className={({ isActive }) => (isActive ? "link-active" : "link")}>
                Neighbours
              </NavLink>
            </ListItem>

            <ListItem key="Serviceproviders" >
              <NavLink to="/serviceproviders" className={({ isActive }) => (isActive ? "link-active" : "link")}>
                ServiceProviders
              </NavLink>
            </ListItem>

            <ListItem key="Signing" >
              <NavLink to="/signing" className={({ isActive }) => (isActive ? "link-active" : "link")}>
                Signing
              </NavLink>
            </ListItem>
          </List>
        </Drawer>
      </div>

    </Box>
  );
}