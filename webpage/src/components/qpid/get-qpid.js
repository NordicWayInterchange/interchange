// https://www.digitalocean.com/community/tutorials/react-axios-react

import React from 'react';
import axios from 'axios';
import _ from 'lodash';
import OutGoingdata from './data.json';
import IncomingData from './incoming.json';
import queueData from './queue.json';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

const StyledTableCell = styled(TableCell)(({ theme }) => ({
  [`&.${tableCellClasses.head}`]: {
    backgroundColor: theme.palette.common.black,
    color: theme.palette.common.white,
  },
  [`&.${tableCellClasses.body}`]: {
    fontSize: 14,
  },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
  '&:nth-of-type(odd)': {
    backgroundColor: theme.palette.action.hover,
  },
  // hide last border
  '&:last-child td, &:last-child th': {
    border: 0,
  },
}));
export default class getQpid extends React.Component {
  state = {
    persons: []
  }
  componentDidMount() {
/*   var url = "http://localhost:8666/api/latest/exchange/default/local.bouvetinterchange.eu/outgoingExchange";
    var url2= "http://localhost:8555/api/latest/exchange/default/local.bouvetinterchange.eu/outgoingExchange";
    var url3="https://jsonkeeper.com/b/FQF8"
    var user = 'qpid-admin';
    var pass = 'supersecret';
    var basicAuth = 'Basic ' + btoa(user + ':' + pass);
/**
  axios.get(url,{
    auth: {
      username: user,
      password: pass
    }
  })
/*axios.get(url, { headers: {"Authorization" : `Bearer cXBpZC1hZG1pbjpzdXBlcnNlY3JldA==`} })*/
  /*axios({
    method: 'get',
    url: url,
    responseType: 'json',
    auth: {
      username: user,
      password: pass
    }
  })*/
  
  
 /* .then(({ data })=> {
      console.log(data);
      this.setState(
        { array: data.stats }
      );
    })
    .catch((err)=> {})
}*/
}


render() {
  //console.log(this.state.array);
  return(
    <div>
      <h3>Information</h3>
      <TableContainer component={Paper}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Id</TableCell>
            <TableCell>Messages In</TableCell>
            <TableCell>Messages Dropped</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          <StyledTableRow>
            <StyledTableCell>{OutGoingdata.name}</StyledTableCell>
            <StyledTableCell>{OutGoingdata.id}</StyledTableCell>
            <StyledTableCell>{OutGoingdata.statistics.messagesIn}</StyledTableCell>
            <StyledTableCell>{OutGoingdata.statistics.messagesDropped}</StyledTableCell>
          </StyledTableRow>
          <StyledTableRow>
            <StyledTableCell>{IncomingData.name}</StyledTableCell>
            <StyledTableCell>{IncomingData.id}</StyledTableCell>
            <StyledTableCell>{IncomingData.statistics.messagesIn}</StyledTableCell>
            <StyledTableCell>{IncomingData.statistics.messagesDropped}</StyledTableCell>
          </StyledTableRow>
          <StyledTableRow>
            <StyledTableCell>{queueData.name}</StyledTableCell>
            <StyledTableCell>{queueData.id}</StyledTableCell>
            <StyledTableCell>{queueData.statistics.persistentDequeuedBytes}</StyledTableCell>
            <StyledTableCell>{queueData.statistics.persistentDequeuedBytes}</StyledTableCell>
          </StyledTableRow>
        </TableBody>
      </Table>
      </TableContainer>
    </div>
  );
}
};
