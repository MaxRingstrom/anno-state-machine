import React, { Component } from "react";

class StateInformationWidget extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
        <div><h1>State information:</h1>
        <h2>Incoming connections:</h2>
        <h2>Outgoing connections:</h2>
        </div>
            );
    }
}

export default StateInformationWidget;