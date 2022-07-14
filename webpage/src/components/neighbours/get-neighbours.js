import React from 'react';
import axios from 'axios';

function responsetable(responseElement) {
    const deliveries = responseElement.deliveries;
    var head = <thead key='response-thead'>
                    <tr key='response-tr'> 
                        <th key='response-th-id'>id</th>
                        <th key='responde-th-path'>path</th>
                        <th key='response-th-selector'>selector</th>
                        <th key='response-th-lastUpdatedTimestamp'>lastUpdatedTimestamp</th>
                        <th key='response-th-status'>status</th>
                    </tr>
                </thead>;
    let rows = []
    for (let i = 0; i < deliveries.length; i++) {
        let row =   <tr key={'response-tr-' + deliveries[i].id}>
                        <td key={'responde-td-id-' + deliveries[i].id}>{deliveries[i].id}</td>
                        <td key={'responde-td-path-' + deliveries[i].id}>{deliveries[i].path}</td>
                        <td key={'responde-td-selector-' + deliveries[i].id}>{deliveries[i].selector}</td>
                        <td key={'responde-td-lastUpdatedTimestamp-' + deliveries[i].id}>{deliveries[i].lastUpdatedTimestamp}</td>
                        <td key={'responde-td-status-' + deliveries[i].id}>{deliveries[i].status}</td>
                    </tr>
        rows.push(row)
    }
    return(
        <div key='response-div'>
            <h2 key='response-h2'>{responseElement.name}</h2>
            <table key='response-table' className='tablemain'>
                {head}
                <tbody key='response-tbody'>
                    {rows.map(row => row)}
                </tbody>
            </table>
        </div>
    )
}

export default class GetNeighbours extends React.Component {
  state = {
    neighbours: [],
    response: []
  }

  componentDidMount() { 
    axios.get(`https://jsonplaceholder.typicode.com/users`)
      .then(res => {
        // unncomment to collect data
        //const neighbours = res.data;

        // Probably not right format
        const neighbours = [{
            "id": 1,
            "name" : "neighbour1"
            }, {
            "id": 2,
            "name" : "neighbour2"
            }, {
            "id": 3,
            "name" : "neighbour3"
        }];


        this.setState({ neighbours });
      })
  }

  sendGetRequest(id) {
    axios.get('https://jsonplaceholder.typicode.com/users?id=' + id)
    .then(res =>{
        // unncomment to gather data
        //var response = res.data;


        // 3.4 example (deliveries?)
        var response = [{
            "version" : "1.0",
            "name" : "neighbour1",
            "deliveries" : [ {
                "id" : "1",
                "path" : "/neighbour1/deliveries/1",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "lastUpdatedTimestamp" : 1651829285366,
                "status" : "REQUESTED"
            }, {
                "id" : "2",
                "path" : "/neighbour1/deliveries/2",
                "selector" : "originatingCountry = 'NO' and messageType = 'DENM'",
                "lastUpdatedTimestamp" : 1651829285367,
                "status" : "REQUESTED"
            } ]
          }];

        this.setState({ response });
    })

  }

  render() {
    return (
        <div key='neighbours-div'>
            <table className='tablemain' key='neighbours-table'>
                <thead key='neighbours-thead'>
                    <tr key='neighbours-tr'>
                        <th key='neighbours-th-name'>Name</th>
                    </tr>
                </thead>
                <tbody key='neighbours-tbody'>
                    {
                    this.state.neighbours
                        .map(neighbour =>
                            <tr key={neighbour.id}>
                                <td key={'neighbours-td-name-' + neighbour.id} onClick={() => this.sendGetRequest(neighbour.id)}>{neighbour.name}</td>
                            </tr>
                        )
                    }
                </tbody>
            </table>
            
            {this.state.response.map(responsetable)}
        </div>
    )
  }
}