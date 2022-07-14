import React from 'react';

const Users = [
    {
      id: 1,
      selected: false,
      common_name: "Leanne Graham",
      email: "Sincere@april.biz",
      state_ctr_reg: "OSLO",
      org: "NO",
      org_unit: "hildegard",
      city_locality: "NO",
      country: "NORWAY",
    },
    {
      id: 2,
      selected: false,
      common_name: "Ervin Howell",
      email: "Shanna@melissa.tv",
      state_ctr_reg: "OSLO",
      org: "anastasia.net",
      org_unit: "anastasia",
      city_locality: "NO",
      country: "NORWAY",
    },
    {
      id: 3,
      selected: false,
      common_name: "Clementine Bauch",
      email: "Nathan@yesenia.net",
      state_ctr_reg: "OSLO",
      org: "ramiro.info",
      org_unit: "ramiro",
      city_locality: "NO",
      country: "NORWAY",
    },
    {
      id: 4,
      selected: false,
      common_name: "Patricia Lebsack",
      email: "Julianne.OConner@kory.org",
      state_ctr_reg: "OSLO",
      org: "kale.biz",
      org_unit: "kale",
      city_locality: "NO",
      country: "NORWAY",
    },
    {
      id: 5,
      selected: false,
      common_name: "Chelsey Dietrich",
      email: "Lucio_Hettinger@annie.ca",
      state_ctr_reg: "OSLO",
      org: "demarco.info",
      org_unit: "demarco",
      city_locality: "NO",
      country: "NORWAY",
    },
  ];

  class SelectTableComponent extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        List: Users,
        MasterChecked: false,
        SelectedList: [],
      };
    }
  
    // Select/ UnSelect Table rows
    onMasterCheck(e) {
      let tempList = this.state.List;
      // Check/ UnCheck All Items
      tempList.map((user) => (user.selected = e.target.checked));
  
      //Update State
      this.setState({
        MasterChecked: e.target.checked,
        List: tempList,
        SelectedList: this.state.List.filter((e) => e.selected),
      });
    }
  
    // Update List Item's state and Master Checkbox State
    onItemCheck(e, item) {
      let tempList = this.state.List;
      tempList.map((user) => {
        if (user.id === item.id) {
          user.selected = e.target.checked;
        }
        return user;
      });
  
      //To Control Master Checkbox State
      const totalItems = this.state.List.length;
      const totalCheckedItems = tempList.filter((e) => e.selected).length;
  
      // Update State
      this.setState({
        MasterChecked: totalItems === totalCheckedItems,
        List: tempList,
        SelectedList: this.state.List.filter((e) => e.selected),
      });
    }
  
    // Event to get selected rows(Optional)
    getSelectedRows() {
 
      this.setState({
        SelectedList: this.state.List.filter((e) => e.selected),
      });

    }

    //Sign the request 
    sign_certificate() {

    }
  
 

    render() {
      return (
            
        <div className="container">
      
        <h2>Certificate Signing Request</h2>
          <div className="row">
            <div className="col-md-12">
              <table className="tablemain">
                
                <thead>
                  <tr>
                    <th scope="col">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        checked={this.state.MasterChecked}
                        id="mastercheck"
                        onChange={(e) => this.onMasterCheck(e)}
                      />
                    </th>
                    <th scope="col">Common Name</th>
                    <th scope="col">Organization</th>
                    <th scope="col">Organizational Unit</th>
                    <th scope="col">City/Locality</th>
                    <th scope="col">State/County/Region</th>
                    <th scope="col">Country</th>
                    <th scope="col">Email adress</th>
                    
                  </tr>
                </thead>
                <tbody>
                  {this.state.List.map((user) => (
                    <tr key={user.id} className={user.selected ? "selected" : ""}>
                      <th scope="row">
                        <input
                          type="checkbox"
                          checked={user.selected}
                          className="form-check-input"
                          id="rowcheck{user.id}"
                          onChange={(e) => this.onItemCheck(e, user)}
                        />
                      </th>
                      <td>{user.common_name}</td>
                      <td>{user.org}</td>
                      <td>{user.org_unit }</td>
                      <td>{user.city_locality}</td>
                      <td>{user.state_ctr_reg}</td>
                      <td>{user.country}</td>
                      <td>{user.email}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <button
                className="btn btn-primary"
                onClick={() => this.getSelectedRows()}
              >
                Get Selected Items {this.state.SelectedList.length} 
              </button>
              <div className="row">
                <b>All Row Items:</b>
              
            
              </div>
              <div className="row">
                <button 
                onClick= {() => alert('Signed')}> 
                    Sign Certificate
                </button>
            
              </div>
            </div>
          </div>
        </div>
      );
    }
  }

                //Selected row items
//             <code>{JSON.stringify(this.state.SelectedList)}</code> 

                //Get selected items
// <code>{JSON.stringify(this.state.List)}</code>

  export default SelectTableComponent;