import * as React from 'react';
import {
    TableCell,
    TableRow
  } from '@mui/material';
  
import {
    IconButton
} from '@material-ui/core';

import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@material-ui/icons/KeyboardArrowUp';
    
const ExpandableTableRow = ({ children, expandComponent, ...otherProps }) => {
    const [isExpanded, setIsExpanded] = React.useState(false);
    return (
      <>
        <TableRow {...otherProps}>
          <TableCell padding="checkbox">
            <IconButton onClick={() => setIsExpanded(!isExpanded)}>
              {isExpanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
            </IconButton>
          </TableCell>
          {children}
        </TableRow>
        {isExpanded && (
          <TableRow>
            <TableCell padding="checkbox" />
            {expandComponent}
          </TableRow>
        )}
      </>
    );
  };

export default ExpandableTableRow;