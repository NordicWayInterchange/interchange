// Tutorial = https://www.digitalocean.com/community/tutorials/react-react-select

import Select from 'react-select';
import styled from 'styled-components';
import React, { useState } from 'react';

import './get-requests';

const Tab = styled.button`
  font-size: 20px;
  padding: 10px 60px;
  cursor: pointer;
  opacity: 0.6;
  background: white;
  border: 0;
  outline: 0;
  ${({ active }) =>
    active &&
    `
    border-bottom: 2px solid black;
    opacity: 1;
  `}
`;

const ButtonGroup = styled.div`
  display: flex;
`;

const types = ['Capability', 'Delivery', 'Subscriptions', 'Se alle capabilities'];
function TabGroup() {
  const [active, setActive] = useState(types[0]);
  return (
    <>
      <ButtonGroup>
        {types.map(type => (
          <Tab
            key = {type}
            active = {active === type}
            onClick = {() => setActive(type)}
          >
            {type}
          </Tab>
        ))}
      </ButtonGroup>
      <p />
      <p> You are on page: {active} </p>
    </>
  );
}

const pods = [
  { label: 'Pod1', value: 'Pod1' },
  { label: 'Pod2', value: 'Pod2' },
  { label: 'Pod3', value: 'Pod3' },
  { label: 'Pod4', value: 'Pod4' },
  { label: 'Pod5', value: 'Pod5' },
  { label: 'Pod6', value: 'Pod6' },
];

const varsling = [
    { label: 'Varsel1', value: 'Varsel1' },
    { label: 'Varsel2', value: 'Varsel2' },
    { label: 'Varsel3', value: 'Varsel3' },
    { label: 'Varsel4', value: 'Varsel4' },
    { label: 'Varsel5', value: 'Varse5l' },
    { label: 'Varsel6', value: 'Varsel6' },
  ];

function Monitoring() {
    return (
        <div>
            <h2>Monitoring</h2>
            <Select
                options = {pods}
                isMulti
                onChange = {opt => console.log(opt)}
            />
            <Select
                options = {varsling}
                isMulti
                onChange = {opt => console.log(opt)}
            />
            <TabGroup 
                button = {types}
            />
        </div>
    );
}

export default Monitoring;