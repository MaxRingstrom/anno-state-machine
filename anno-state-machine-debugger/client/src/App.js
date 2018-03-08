import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  state = {
    version: ''
  };

  componentDidMount() {
    document.title = "Anno State Machine Debugger";
    this.callApi()
      .then(res => this.setState({ version: res.version }))
      .catch(err => console.log(err));
  }

  callApi = async () => {
    const response = await fetch('/api/version');
    const body = await response.json();

    if (response.status !== 200) throw Error(body.message);

    return body;
  };

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Anno State Machine Debugger</h1>
        </header>
        <div className="App-BuildInfo"><p>Backend version: {this.state.version}</p></div>
      </div>
    );
  }
}

export default App;
