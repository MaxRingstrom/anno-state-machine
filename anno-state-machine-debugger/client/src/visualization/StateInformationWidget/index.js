import React, { Component } from "react";

class StateInformationWidget extends Component {
    constructor(props) {
        super(props);
        this.state = {state : props.state, connections : props.connections}
    }

    componentWillReceiveProps(newProps) {
        this.setState({state : newProps.state, connections : newProps.connections});
        console.log("new props: " + newProps);
        console.log("newProps.state: " + newProps.state);
    }

    render() {
        return (
        <div><h1>State information:</h1>
        <p>{this.state.state.label}</p>
        <h2>Connections:</h2>
        {
                this.state.connections.map(function(connection) {
                    return <p>{connection.label}</p>
                })
            }
        </div>
            );
    }
}

export default StateInformationWidget;