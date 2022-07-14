import * as React from 'react';
import axios from 'axios';
import https from 'https';
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

import ExpandableTableRow from './expandable-table-row';
import ResponseNavigation from './response-navigation';

export default class GetServiceProviders extends React.Component {
  constructor(props) {
    super(props);
    this.state = { 
      serviceproviders: [],
      chosenserviceprovider: [],
      chosenresponse: "",
      response: null
    };
    
  }


  componentDidMount() {
    const httpsAgent = new https.Agent({
      rejectUnauthorized: false,
      cert: "-----BEGIN CERTIFICATE-----MIIDBzCCAe8CCQCAA1Z+8Uj3DDANBgkqhkiG9w0BAQsFADBEMSAwHgYDVQQDDBdjYS5ib3V2ZXRpbnRlcmNoYW5nZS5ldTETMBEGA1UECgwKTm9yZGljIFdheTELMAkGA1UEBhMCTk8wHhcNMjIwNzA4MTEzNTEwWhcNMjMwNzA4MTEzNTEwWjBHMSMwIQYDVQQDDBpsb2NhbC5ib3V2ZXRpbnRlcmNoYW5nZS5ldTETMBEGA1UECgwKTm9yZGljIFdheTELMAkGA1UEBhMCTk8wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDhpSayrefOkkJ0yZRq5Q+gT/DJOzp2FGtjD6thP/oSUMxwDPQDycA27mC3snAotcojQpugxImQz5luKUQSjn6USXoXSoPa0iu/EwydCNY84RMhkwNFkmo7rCCD1mVZjvexM3MqiSEga3tkaWJPO0MbSy8GaI/JsIiGiotMNreiSsdU1PLLYiowNBw5J/tB4axZeKBRfHguP/RgzpOnrJVyfeOVgWsfKE/Xp8Qae3FimYrgtCL08ofuo2HsyG6hMRVMiKjy2jyHhtHAVWWlBfvJqQE5l2SrDQJvD7lRLSscY4q5Lh/+0baYXmCxnikWALAKvOcj3PY27oIgizAkxie1AgMBAAEwDQYJKoZIhvcNAQELBQADggEBAAJRwUfeKW2Ozl2g5GZQUMrO9Ua1KS8oT8aDy0VnHGhkerw0TC04n9DuxlHocPFF+oVEw1CnF3iGNXR1v5Lk17faHBzLOHbWFxsB3E4LVn6JDNyNmW2xZtE2OiZ8YbTqhWc70Vs4H2RvUdCibktrjkPBwPT7BLI0i3mEQbXIFf/lp1qT8TqGzTdTQPamJyaDKJAoX/c6uLvelONBVdQjrzPUbBp2jV/Ik5D/x56YHTrZ807hfG0mDY2e8P4FT3oO5KK43Z3mAF2jif0xpR3EZDWG40luUYBya/PeL6f8TsuBIfLkCI7+TQVmSMo3AYYT0zy6n9f2+Df4CIMBvwH7J0M=-----END CERTIFICATE-----",
      key: "-----BEGIN PRIVATE KEY-----MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDhpSayrefOkkJ0yZRq5Q+gT/DJOzp2FGtjD6thP/oSUMxwDPQDycA27mC3snAotcojQpugxImQz5luKUQSjn6USXoXSoPa0iu/EwydCNY84RMhkwNFkmo7rCCD1mVZjvexM3MqiSEga3tkaWJPO0MbSy8GaI/JsIiGiotMNreiSsdU1PLLYiowNBw5J/tB4axZeKBRfHguP/RgzpOnrJVyfeOVgWsfKE/Xp8Qae3FimYrgtCL08ofuo2HsyG6hMRVMiKjy2jyHhtHAVWWlBfvJqQE5l2SrDQJvD7lRLSscY4q5Lh/+0baYXmCxnikWALAKvOcj3PY27oIgizAkxie1AgMBAAECggEAHQVttbtyPfpHu9eVVC5D1T2S3X7VpQw5R5NjWm2tDx//O0YfOEGBjE8ad2Xm0QWYivJtK787ZjmdJTL/g1d6Zj4RCt/Vl5ZCdB/SFnxleY0FNeM/n3oWWbhosiwn+t3Nc6gHXugmL5JDW4XGVCRuHBuOX8eg/rLKzGsTQWkkMyYnIl2EXBw7R7EC8Q9yumt7EAIMJX7VBOsH/shHcUNaRMNemGz11POxo+kfpVoGeYti5X/cjM1AzslykL3gM8eYG6qfWgqkKj3vC2hxgoyBqmOYaU9casOTerHy0OIeIsdcpXdE8+bU+5ounU+ZHwud9z31XQj+5iRYF393N8yHQQKBgQD43XzqIrhsRXYDlqr+CfVECKbziIG7URKi8EJJQrlnd2hzF9YJtcah0emUUmcWQ+IvwIr1W2QoClmt8taOKnREsrdeEzC7+6PucQuX/rJkkoP0I9RGbe9UVi4f/bnoT/Mp5m7xTjkPf+hfE2kJUB3a4Tgsvym3DVqOrN0vHt5P5QKBgQDoHT4iqwOZSp2zfvjpHdvHmyosHZg6VebMazmFmhsh1CifitVv3v5og7M0qCtSap5RC7pxVpAeGzEH4vh/3PF7boQmEuYWMIa7ixMbZovU9qiQqBFc699xGnKTf/I+L282EMxKQwXm5H2XsrtpvO85TP6XAs1txzpowDLTlFrbkQKBgQD33ss6/MgJdwm7O0cmbc6vK5r1t0HXy0EvrvOmumGmD0WmVNhnpXio1kW66HQqtgREIZeGF7fjNaPq4JMY2GojvolqltyUb2fw5wFwK0uNZm0tLr6QQ5D7xDZ+wJ15KKB+XDil+Y5VxMUyZUfZ8cU6q9Xio6gt3YwTzVMkSq+MjQKBgF0jBsCwcFQ9LYRNyaD4uyyf5U8oXOHjgCW8TKuAR6zOTbKe6b/m8ZyMZAcYB7IdWANbOmep/VAnRwhMGbxssnT9xobkhHCY7icoyfTvs+IzSzYoSjdH6jOfEiR/bXtoWy6NlPujYyXfv0l0jWEDT9ZlP67mj/mhvJ01qe9eEwsRAoGAGpXEZzHqABeJd06hlks2p1x0wbX5hsARxaxPV9njMk0f8G7EppreYcx6tutgPAjpVo7NdPKpd/FSCZEZfjvv8wzeuCwkIuGh98KKKTySMSYyBRn/eB99Vm9lCM7jhL5IZQ/0+2glCWE2uusqpudWtiwzAKdIWLeYRzuuBX5A4XM=-----END PRIVATE KEY-----",
      passphrase: "password",
    });
    console.log(httpsAgent);
    axios.get('https://local.bouvetinterchange.eu:38799/admin/serviceProvider')//, {httpsAgent})
      .then(res => {
        // uncomment to collect data from https
        const getresponse = res.data;
          /*
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
            */
            console.log(getresponse);
        const serviceproviders = getresponse.serviceProviders;
        this.setState({ serviceproviders });
      })
  }

  getResponseNavigation(id) {
    const chosenserviceprovider = [id];
    // Remove response if already shown
    const chosenresponse = "";
    const response = [];
    this.setState({ chosenserviceprovider, chosenresponse, response })
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
          {!this.state.response ? 
          <div>hello</div>:<div>bye</div>}

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

            {this.state.chosenserviceprovider.map(a => this.responseNavigationBar(a))}

            {this.state.response && 
            this.responseTable(this.state.response)}
        </div>
    )
  }
}