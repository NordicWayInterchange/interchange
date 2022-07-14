import React from 'react';
import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';

import Home from './components/home';
import Monitoring from './components/monitoring';
import Qpid from './components/qpid';
import Neighbours from './components/neighbours';
import ServiceProviders from './components/serviceproviders';
import Signing from './components/signing';
import PermanentDrawerLeft from './components/sidebar';




const Main = () => {
  return (
    <>
    <Router>
    <div>
        <PermanentDrawerLeft>
        </PermanentDrawerLeft>
    
          <div className="routes-padding">
          <h1>Admin Interface</h1>
          <Routes >
              <Route exact path='/' element={< Home />}></Route>
              <Route exact path='/monitoring' element={< Monitoring />}></Route>
              <Route exact path='/qpid' element={< Qpid />}></Route>
              <Route exact path='/neighbours' element={< Neighbours />}></Route>
              <Route exact path='/serviceproviders/*' element={< ServiceProviders />}></Route>
              <Route exact path='/signing' element={< Signing />}></Route>
          </Routes>
          </div>
          
  
      
    </div>
<div>

</div>
</Router>
</>
  );

}


export default Main;