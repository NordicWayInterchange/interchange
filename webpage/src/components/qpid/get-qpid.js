// https://www.digitalocean.com/community/tutorials/react-axios-react

import React from 'react';
import axios from 'axios';
import _ from 'lodash';

export default class getQpid extends React.Component {
  state = {
    persons: []
  }
  componentDidMount() {
    var url = "http://localhost:8181/qpid/api/latest/exchange/default/local.bouvetinterchange.eu/outgoingExchange";
    var url2= "http://localhost:8555/api/latest/exchange/default/local.bouvetinterchange.eu/outgoingExchange";
    var url3="https://jsonkeeper.com/b/FQF8"
    var user = 'qpid-admin';
    var pass = 'supersecret';
    var basicAuth = 'Basic ' + btoa(user + ':' + pass);

  axios.get(url,{
    auth: {
      username: user,
      password: pass
    }
  })

  
  
  .then(({ data })=> {
      console.log(data);
      this.setState(
        { array: data.stats }
      );
    })
    .catch((err)=> {})
}

render() {
  console.log(this.state.array);
  return(
    <div>
      <h3>Information123</h3>
      <ul className="list-group">
         {this.renderStats()}
      </ul>
    </div>
  );
}

renderStats() {
  console.log(this.state.array);
  return _.map(this.state.array, stats => {
    return (
      <li className="list-group-item" key={stats.name}>
          {stats.name}
          <ul className="list-group">
             Stats:
             {this.renderIngredients(stats)}
          </ul>
      </li>
    );
  });
}

renderIngredients(stats) {
  return _.map(stats.Information, info => {
      return (
        <li className="list-group-item" key={info.name}>
            <p>Name: {info.name}, Amount: {info.amount}</p>
        </li>
      );
  });
}
}; 
