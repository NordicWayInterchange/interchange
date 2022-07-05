// https://www.digitalocean.com/community/tutorials/react-axios-react

import React from 'react';
import axios from 'axios';

export default class TestGet extends React.Component {
  state = {
    persons: []
  }

  componentDidMount() { //TODO lage path ut i fra input
    axios.get(`https://jsonplaceholder.typicode.com/users`)
      .then(res => {
        const persons = res.data;
        this.setState({ persons });
      })
  }

  render() { //TODO sette opp table
    return (
      <table>
        <tr>
           <th>Name</th>
            <th>Username</th>
            <th>Email</th> 
        </tr>
        {
          this.state.persons
            .map(person =>
                <tr key={person.id}>
                    <td>{person.name}</td>
                    <td>{person.username}</td>
                    <td>{person.email}</td>
                </tr>
            )
        }
      </table>
    )
  }
}