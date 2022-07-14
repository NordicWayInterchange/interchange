// https://www.digitalocean.com/community/tutorials/react-axios-react

//TODO lage path ut i fra input, admin/serviveprovider eller admin/neigbour/cap eller sub...
// klikk på navn, se mer info

/**
 * 
 * list of service providers (assumed, will change)

[
    {
        "name": "serviceprovider1"
    },
    {
        "name": "serviceprovider2"
    }
]

 * 
 * list of subscriptions from serviceprovider1
 * 
{
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
 */


import React from 'react';
import axios from 'axios';

function responsetable(responseElement) {
    const subscriptions = responseElement.subscriptions;
    console.log(subscriptions)
    var head = <thead key='response-thead'>
                    <tr key='response-tr'> 
                        <th key='response-th-id'>id</th>
                        <th key='responde-th-path'>path</th>
                        <th key='response-th-selector'>selector</th>
                        <th key='response-th-status'>status</th>
                    </tr>
                </thead>;
    let rows = []
    for (let i = 0; i < subscriptions.length; i++) {
        let row =   <tr key={'response-tr-' + subscriptions[i].id}>
                        <td key={'responde-td-id-' + subscriptions[i].id}>{subscriptions[i].id}</td>
                        <td key={'responde-td-path-' + subscriptions[i].id}>{subscriptions[i].path}</td>
                        <td key={'responde-td-selector-' + subscriptions[i].id}>{subscriptions[i].selector}</td>
                        <td key={'responde-td-status-' + subscriptions[i].id}>{subscriptions[i].status}</td>
                    </tr>
        rows.push(row)
        console.log(subscriptions[i].path);
    }
    console.log(rows)
    return(
        <div>
            <h2>{responseElement.name}</h2>
            <table key='response-table' className='tablemain'>
                {head}
                <tbody>
                    {rows.map(row => row)}
                </tbody>
            </table>
        </div>
    )
}

export default class TestGet extends React.Component {
  state = {
    persons: [],
    response: []
  }

  componentDidMount() { 
    //var respone = null;
    axios.get(`https://jsonplaceholder.typicode.com/users`)
      .then(res => {
        const persons = res.data;
        this.setState({ persons });
      })
  }

  sendGetRequest(id) {
    axios.get('https://jsonplaceholder.typicode.com/users?id=' + id)
    .then(res =>{
        // unncomment to gather data
        //var response = res.data;


        // 4.1 example
        var response = [{ 
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
            }, {
                "id" : "3",
                "path" : "/serviceprovider1/subscriptions/2",
                "selector" : "originatingCountry = 'SE' and messageType = 'DENM'",
                "status" : "CREATED"
              } ]
          }];

        this.setState({ response });
    })

  }

  render() {
    return (
        <div>
            <table className='tablemain'>
                <thead>
                    <tr>
                        <th>Name</th>
                    </tr>
                </thead>
                <tbody>
                    {
                    this.state.persons
                        .map(person =>
                            <tr key={person.id}>
                                <td onClick={() => this.sendGetRequest(person.id)}>{person.name}</td>
                            </tr>
                        )
                    }
                </tbody>
            </table>
            
            {this.state.response.map(responsetable)}

            <br></br>
            <p>Non-interactive table:</p>
            <table className='tablemain'>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Email</th> 
                    </tr>
                </thead>
                <tbody>
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
                    </tbody>
            </table>
        </div>
    )
  }
}