import React, { useEffect, useState } from 'react';
import axios from 'axios';
//import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

function RenderBindings (props) {
  const [binding, setBindingState]=useState(null)
  useEffect(()=>{
    axios.get("http://localhost:8181/remote_qpid/api/latest/queue/default/remote.bouvetinterchange.eu/"+props.input+"/getPublishingLinks",{
    auth: {
      username: "qpid-admin",
      password: "supersecret"
    }
  }).then(res=> {
      const binding = res.data;
      setBindingState(binding)
    })
    .catch((err)=> {})
  },[props.input])
  
  return (
    binding && binding[0]?.arguments 
      ? <TableCell>{binding[0].arguments["x-filter-jms-selector"]}</TableCell>
      : <TableCell>null</TableCell>
  );
}

export default class getQpid extends React.Component {
  state = {
    outgoing: {},
    incoming: {},
    allExchanges: {},
    allQueues: {}
  }
  componentDidMount() {
    var allQueues   = "http://localhost:8181/remote_qpid/api/latest/queue/default/remote.bouvetinterchange.eu/";
    var allExchanges= "http://localhost:8181/remote_qpid/api/latest/exchange/default/remote.bouvetinterchange.eu/";
    var user = 'qpid-admin';
    var pass = 'supersecret';
    //###############################################
    // Gets data from all exchanges
    //###############################################
    axios.get(allExchanges,{
      auth: {
        username: user,
        password: pass
      }
    }).then(res=> {
        const allExchanges = res.data;
        this.setState({ allExchanges });
      })
      .catch((err)=> {})

    //###############################################
    // Gets data from all queues
    //###############################################
    axios.get(allQueues,{
      auth: {
        username: user,
        password: pass
      }
    }).then(res=> {
        const allQueues = res.data;
        this.setState({ allQueues });
      })
      .catch((err)=> {})
     
  }

  render() {
    return(
        <div className="tablemain">
      
        {Object.keys(this.state.allExchanges).length > 0 ?
        <><h3>Exchanges</h3>
        <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Id</TableCell>
                  <TableCell>Numbers of bindings</TableCell>
                  <TableCell>Messages In</TableCell>
                  <TableCell>Messages Dropped</TableCell>
                  <TableCell>Bytes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>

                {Object.keys(this.state.allExchanges).map((id,index) => {
                  return (
                  <React.Fragment key={"exchange"+index}>
                    <TableRow>
                      <TableCell>{this.state.allExchanges[id].name}</TableCell>
                      <TableCell>{this.state.allExchanges[id].id}</TableCell>
                      <TableCell>{this.state.allExchanges[id].statistics.bindingCount}</TableCell>
                      <TableCell>{this.state.allExchanges[id].statistics.messagesIn}</TableCell>
                      <TableCell>{this.state.allExchanges[id].statistics.messagesDropped}</TableCell>
                      <TableCell>{this.state.allExchanges[id].statistics.bytesIn}</TableCell>
                    </TableRow>
                  </React.Fragment>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer></>
          : <p>No contact with qpid, have you started a systemtest?</p>
        }

        {Object.keys(this.state.allQueues).length > 0 && 
        <><h3>Queues</h3>
        <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Id</TableCell>
                  <TableCell>Binding Arg</TableCell>
                  <TableCell>Queue Depth</TableCell>
                  <TableCell>Queue Depth (Bytes)</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                  {Object.keys(this.state.allQueues).map((id,index) => {
                    return (
                    <React.Fragment key={"queue"+index}>
                    <TableRow>
                      <TableCell>{this.state.allQueues[id].name}</TableCell>
                      <TableCell>{this.state.allQueues[id].id}</TableCell>
                      <RenderBindings input={this.state.allQueues[id].name}/>
                      <TableCell>{this.state.allQueues[id].statistics.queueDepthMessages}</TableCell>
                      <TableCell>{this.state.allQueues[id].statistics.queueDepthBytes}</TableCell>
                    </TableRow>
                    </React.Fragment>
                        )
                    })}
              </TableBody>
            </Table>
          </TableContainer></>
        }


      </div>
    );
  }
}; 
