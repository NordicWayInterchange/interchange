import React from 'react';
import {BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

import Home from './components/home';
import Monitoring from './components/monitoring';
import Qpid from './components/qpid';
import Neighbours from './components/neighbours';
import ServiceProviders from './components/serviceproviders';
import Signing from './components/signing';



const Main = () => {
  return (
    <div>
      <h1>Admin Interface</h1>
      <Router>
        <div>
          <div className ="topnav">
              <a href="/">Home</a>
              <a href="/monitoring">Monitoring</a>
              <a href="/qpid">Qpid</a>
              <a href="/neighbours">Neighbours</a>
              <a href="/serviceproviders">ServiceProviders</a>
              <a href="/signing">Signing</a>
          </div>
        <hr />
          <Routes>
              <Route exact path='/' element={< Home />}></Route>
              <Route exact path='/monitoring' element={< Monitoring />}></Route>
              <Route exact path='/qpid' element={< Qpid />}></Route>
              <Route exact path='/neighbours' element={< Neighbours />}></Route>
              <Route exact path='/serviceproviders' element={< ServiceProviders />}></Route>
              <Route exact path='/signing' element={< Signing />}></Route>
          </Routes>
        </div>
      </Router>
    </div>
  );
}

export default Main;