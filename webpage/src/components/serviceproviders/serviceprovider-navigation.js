import * as React from 'react';

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
import {
    BrowserRouter as Router, 
    Routes, 
    Route
} from 'react-router-dom';

function handleClick (id) {
    window.location.href = 'serviceproviders/' + id;
}

const ServiceProviderNavigation = ({ children, serviceProviders, ...otherProps }) => {

    return (
        <>
        {serviceProviders.map(serviceprovider => (<Link key={serviceprovider.id} to={'serviceproviders/' + serviceprovider.id}/>))}

        <Routes>
            <Route exact path="serviceproviders/:id"/>
        </Routes>

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
                {serviceProviders.map(serviceprovider => (
                <TableRow
                    key={serviceprovider.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    onClick={() => handleClick(serviceprovider.id)}
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
        </>
    );
  };

export default ServiceProviderNavigation;