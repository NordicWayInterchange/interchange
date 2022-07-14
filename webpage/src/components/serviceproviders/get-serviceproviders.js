import * as React from 'react';
import axios from 'axios';

import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Link
} from '@mui/material';

import ServiceProviderNavigation from './serviceprovider-navigation';
import ExpandableTableRow from './expandable-table-row';
import ResponseNavigation from './response-navigation';

export default class GetServiceProviders extends React.Component {
  

  constructor(props) {
    super(props);
    this.state = { 
      serviceproviders: [],
      chosenserviceprovider: -1,
      chosenresponse: "",
      response: null
    };
    
  }

  componentDidMount() { 
    if (window.location.href.substr(window.location.href.lastIndexOf('/') + 1) !== 'serviceproviders') {
      const chosenserviceprovider = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);
      this.setState({ chosenserviceprovider });
    }
    //axios.get(https://10.4.100.30:54824/serviceProvider)
    axios.get(`https://jsonplaceholder.typicode.com/users`)
      .then(res => {
        // uncomment to collect data from https
        
        //const getresponse = res.data;
        //console.log(getresponse);

        const getresponse = {
            "nameOfInterchange" : "Norge",
            "serviceProviders" : [ {
            "name" : "sp-4",
            "id" : "4",
            "numberOfCapabilities" : "0",
            "numberOfDeliveries" : "0",
            "numberOfSubcriptions" : "0",
            "status" : "UP"
            }, {
            "name" : "sp-2",
            "id" : "2",
            "numberOfCapabilities" : "0",
            "numberOfDeliveries" : "0",
            "numberOfSubcriptions" : "0",
            "status" : "UP"
            }, {
            "name" : "sp-1",
            "id" : "1",
            "numberOfCapabilities" : "0",
            "numberOfDeliveries" : "0",
            "numberOfSubcriptions" : "0",
            "status" : "UP"
            }, {
            "name" : "sp-3",
            "id" : "3",
            "numberOfCapabilities" : "0",
            "numberOfDeliveries" : "0",
            "numberOfSubcriptions" : "0",
            "status" : "UP"
            }, {
            "name" : "sp-0",
            "id" : "0",
            "numberOfCapabilities" : "0",
            "numberOfDeliveries" : "0",
            "numberOfSubcriptions" : "0",
            "status" : "UP"
            } ]
            };

        const serviceproviders = getresponse.serviceProviders;
        this.setState({ serviceproviders});
      })
  }

  getResponseNavigation() {
    const chosenserviceprovider = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);
    // Remove response if already shown
    const response = [];
    this.setState({ chosenserviceprovider, response })
  }

  responseNavigationBar(id) {
    return (
      <div key='response-navigation-div'>
        <h2 key='response-navigation-h3'>Service Provider {id}</h2>
        <button key='button-responseNavigation-subscriptions' onClick={() => this.getSubscriptions(id, "Subscriptions")}>Subscriptions</button>
        <button key='button-responseNavigation-capabilities' onClick={() => this.getCapabilities(id, "Capabilities")}>Capabilities</button>
        <button key='button-responseNavigation-deliveries' onClick={() => this.getDeliveries(id, "Deliveries")}>Deliveries</button>
        < ResponseNavigation />
        <Link fontSize="30px" href="/1">GET FREE IPHONE RIGHT NOW!!!</Link>
      </div>
    );
  }

  getSubscriptions(id) {
    axios.get('https://jsonplaceholder.typicode.com/users?id=' + id)
    .then(res =>{
        // unncomment to gather data
        //var response = res.data;
        const response = {
          "name" : "serviceprovider1",
          "version" : "1.0",
          "subscriptions" : [ {
            "id" : "1",
            "path" : "/serviceprovider1/subscriptions/1",
            "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
            "status" : "CREATED"
            }, {
            "id" : "2",
            "path" : "/serviceprovider1/subscriptions/2",
            "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
            "status" : "CREATED"
            } ]
          }  
        this.setState({ response });
    })
  }

  getCapabilities(id) {
    axios.get('https://jsonplaceholder.typicode.com/users?id=' + id)
    .then(res =>{
        // unncomment to gather data
        //var response = res.data;
        const response = {
          "name" : "sp-1",
          "capabilities" : [ {
            "id" : "1",
            "path" : "/sp-1/capabilities/1",
            "definition" : {
            "messageType" : "DENM",
            "publisherId" : "NPRA",
            "originatingCountry" : "NO",
            "protocolVersion" : "1.0",
            "quadTree" : [ "1234" ],
            "causeCode" : [ "6" ]
          }
          } ]
        };
        this.setState({ response });
    })
  }

  getDeliveries (id) {
    axios.get('https://jsonplaceholder.typicode.com/users?id=' + id)
    .then(res =>{
        // unncomment to gather data
        //var response = res.data;
        const response = {
          "version" : "1.0",
          "name" : "sp-1",
          "type": "deliveries", // kan brukes i switch TODO
          "deliveries" : [ {
            "id" : "1",
            "path" : "/sp-1/deliveries/1",
            "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
            "lastUpdatedTimestamp" : 1657188232159,
            "status" : "CREATED"
            } ]
          }
        this.setState({ response });
    })
  }

  // General request
  sendGetRequest(id) {
    //const id = this.chosenserviceprovider;
    axios.get('https://jsonplaceholder.typicode.com/users?id=' + id)
    .then(res => {
        var response = res.data;         
        this.setState({ response });
    })
  }

  responseTable(responseElement) {
    let head;
    let rows = [];
    if ("subscriptions" in responseElement) {
      const subscriptions = responseElement.subscriptions;
      head =  <TableHead>
                  <TableRow>
                    <TableCell>id</TableCell>
                    <TableCell align="right">path</TableCell>
                    <TableCell align="right">selector</TableCell>
                    <TableCell align="right">status</TableCell>
                  </TableRow>
                </TableHead>;
      for (let i = 0; i < subscriptions.length; i++) {
        let row = <TableRow
                    key={'response-tr-' + subscriptions[i].id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                  >
                    <TableCell component="th" scope="row">
                      {subscriptions[i].id}
                    </TableCell>
                    <TableCell align="right">{subscriptions[i].path}</TableCell>
                    <TableCell align="right">{subscriptions[i].selector}</TableCell>
                    <TableCell align="right">{subscriptions[i].status}</TableCell>
                  </TableRow>;
        rows.push(row);
      }          
    } else if ("capabilities" in responseElement) {
      const capabilities = responseElement.capabilities;
      head =  <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox" />
                    <TableCell>id</TableCell>
                    <TableCell align="right">path</TableCell>
                  </TableRow>
                </TableHead>;
      for (let i = 0; i < capabilities.length; i++) {
        let row = <ExpandableTableRow
                  key={'response-etr-' + capabilities[i].id}
                  expandComponent={
                    <TableCell colSpan="2" key={'response-table-expand-cell-'+ capabilities[i].id}>
                      <h3>definition</h3>
                      <small>causeCode</small> 
                        <br/> 
                        {capabilities[i].definition.causeCode.map(
                          (a, index)=>(
                            <div key={'causeCode'+index}>
                              <b>{a}</b>
                              <br/>
                            </div>)
                        )} 
                        <br/>                      
                      <small>messageType</small> 
                        <br/>
                          <b>{capabilities[i].definition.messageType}</b> 
                        <br/><br/>
                      <small>originatingCountry</small> 
                        <br/> 
                          <b>{capabilities[i].definition.originatingCountry}</b> 
                        <br/><br/>
                      <small>publisherId</small> 
                        <br/> 
                          <b>{capabilities[i].definition.publisherId}</b> 
                        <br/><br/>
                      <small>quadTree</small> 
                        <br/> 
                        {capabilities[i].definition.quadTree.map(
                          (a, index)=>(
                            <div key={'quadTree'+index}>
                              <b>{a}</b>
                              <br/>
                            </div>)
                        )}
                        <br/>  
                    </TableCell>
                  }
                  >
                    <TableCell scope="row">
                      {capabilities[i].id}
                    </TableCell>
                    <TableCell align="right">{capabilities[i].path}</TableCell>
                  </ExpandableTableRow>;          
        rows.push(row);
      }
    } else if ("deliveries" in responseElement) {
      const deliveries = responseElement.deliveries;
      head =  <TableHead>
                  <TableRow>
                    <TableCell>id</TableCell>
                    <TableCell align="right">path</TableCell>
                    <TableCell align="right">selector</TableCell>
                    <TableCell align="right">selector</TableCell>
                    <TableCell align="right">status</TableCell>
                  </TableRow>
                </TableHead>;
      for (let i = 0; i < deliveries.length; i++) {
        let row = <TableRow
                    key={'response-tr-' + deliveries[i].id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                  >
                    <TableCell component="th" scope="row">
                      {deliveries[i].id}
                    </TableCell>
                    <TableCell align="right">{deliveries[i].path}</TableCell>
                    <TableCell align="right">{deliveries[i].selector}</TableCell>
                    <TableCell align="right">{deliveries[i].lastUpdatedTimestamp}</TableCell>
                    <TableCell align="right">{deliveries[i].status}</TableCell>
                  </TableRow>;
        rows.push(row);
      
     
      }
  } else {
      rows = [];
  }
    return(
        <div key='response-div'>
            <h3 key='response-h2'>{this.state.chosenresponse}</h3>
            <TableContainer component={Paper}>
              <Table sx={{ minWidth: 650 }} aria-label="simple table">
                {head}
                <TableBody>
                    {rows.map(row => row)}
                </TableBody>
            </Table>
          </TableContainer>
        </div>
    )
  }

  render() {
    return (
        <div key='serviceproviders-div'>
          {/*
            <TableContainer component={Paper}>
              <Table sx={{ minWidth: 650 }} aria-label="simple table" className='tablemain'>
                <TableHead>
                  <TableRow>
                    <TableCell>Id</TableCell>
                    <TableCell align="right">Name</TableCell>
                    <TableCell align="right">numberOfCapabilities</TableCell>
                    <TableCell align="right">numberOfDeliveries</TableCell>
                    <TableCell align="right">numberOfSubcriptions</TableCell>
                    <TableCell align="right">Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {this.state.serviceproviders.map(serviceprovider => (
                    <TableRow
                      key={serviceprovider.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      onClick={() => this.getResponseNavigation(serviceprovider.id)}
                    >
                      <TableCell align="right">{serviceprovider.id}</TableCell>
                      <TableCell align="right">{serviceprovider.name}</TableCell>
                      <TableCell align="right">{serviceprovider.numberOfCapabilities}</TableCell>
                      <TableCell align="right">{serviceprovider.numberOfDeliveries}</TableCell>
                      <TableCell align="right">{serviceprovider.numberOfSubcriptions}</TableCell>
                      <TableCell align="right">{serviceprovider.status}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
                  */}
            {window.location.href.substr(window.location.href.lastIndexOf('/') + 1) === 'serviceproviders' ? 
            <>
              <h2>ServiceProviders</h2>
              <ServiceProviderNavigation serviceProviders={this.state.serviceproviders}></ServiceProviderNavigation>
            </> : <>
              {//this.getResponseNavigation()
              }
              <p>ServiceProvider {this.state.chosenserviceprovider}</p>
              {this.responseNavigationBar(this.state.chosenserviceprovider)}
              {this.state.response && 
              this.responseTable(this.state.response)}
            </>
            }
        </div>
    )
  }
}