import React, { Component } from "react";
import Graph from "vis-react";
import StateInformationWidget from "../StateInformationWidget"
import './index.css'

class VisWidget extends Component {
  constructor(props) {
    super(props);
    this.events.stabilizationIterationsDone = this.events.stabilizationIterationsDone.bind(this)
    this.events.select = this.events.select.bind(this)
    var graph = JSON.parse('{"nodes":[{"id":0,"label":"GLOBAL","color":"#aaffff"},{"id":1,"label":"Init"},{"id":2,"label":"LoadingContent"},{"id":3,"label":"UpAndRunning"},{"id":4,"label":"Done"},{"id":5,"label":"Error"},{"id":6,"label":"Finish"}],"edges":[{"from":2,"to":3,"arrows":"to","label":"ContentLoaded (onContentLoaded)","color":"#000000","id":0},{"from":1,"to":2,"arrows":"to","label":"Start (startLoadingContent)","color":"#000000","id":1},{"from":4,"to":3,"arrows":"to","label":"CheckBoxCheckStateChanged (onUserNoLongerReadyToContinue)","color":"#ff0000","id":2},{"from":4,"to":6,"arrows":"to","label":"Next (onNext)","color":"#000000","id":3},{"from":3,"to":4,"arrows":"to","label":"CheckBoxCheckStateChanged (onUserReadyToContinue)","color":"#ff0000","id":4}]}');
    console.log(graph);
    this.state = {
      graph: graph,
      selectedState : {label : "No selection"},
      selectedEdges : [],
      options: {
        physics: {
          forceAtlas2Based: {
            gravitationalConstant: -26,
            centralGravity: 0.005,
            springLength: 230,
            springConstant: 0.18,
            avoidOverlap: 1.5
          },
          maxVelocity: 146,
          solver: "forceAtlas2Based",
          timestep: 0.35,
          stabilization: {
            enabled: true,
            iterations: 1000,
            updateInterval: 25
          }
        },

        edges: {
          font: {
            color: "#343434",
            size: 11, // px
            face: "arial",
            background: "none",
            strokeWidth: 5, // px
            strokeColor: "#ffffff",
            align: "top"
          },
          smooth: {
            enabled: true, //setting to true enables curved lines
            type: "dynamic",
            "forceDirection": "none",
            roundness: 0.75
          }
        }
      },
      graphConstructCount : 0
    };
  }

  componentDidMount() {}

  events = {
    select: function(event) {
        var { nodes, edges } = event;

        if (nodes.length === 1) {
            console.log(this.state.graph.nodes[nodes[0]]);
            this.setState({selectedState : this.state.graph.nodes[nodes[0]]});
        } else {
          this.setState({selectedState : {label : "No selection"}});
        }

        if (edges.length >= 0) {
            var that = this;
            this.setState({selectedEdges : edges});
            var connections = new Array();
            edges.forEach(function(edgeId) {
                console.log(that.state.graph.edges[edgeId]);
                connections.push(that.state.graph.edges[edgeId]);
            });
            this.setState({selectedEdges : connections});
        } else {
          this.setState({selectedEdges : {}});
        }
    },
    stabilizationIterationsDone: function(event) {
        console.log("stabilizationIterationsDone");
        this.setState({options: {
            physics: { enabled : false },
        edges :{smooth : {type:"dynamic"}}}});
    }
  }

  handleModelInputChanged(event) {
      try {
        var newGraph = JSON.parse(event.target.value);
      } catch (error) {
        console.log("Not a complete state machine json");
        this.setState({ selectedState: { label: "No selection"}, selectedEdges: []})
      }
      if (newGraph) {
        console.log(newGraph);
        this.setState({graph:{nodes:[], edges:[]}})
        this.setState( {
            graph: newGraph,
            options: {
              physics: {
                  enabled:false,
                forceAtlas2Based: {
                  gravitationalConstant: -26,
                  centralGravity: 0.005,
                  springLength: 230,
                  springConstant: 0.18,
                  avoidOverlap: 1.5
                },
                maxVelocity: 146,
                solver: "forceAtlas2Based",
                timestep: 0.35,
                stabilization: {
                  enabled: true,
                  iterations: 1000,
                  updateInterval: 25
                }
              },
      
              edges: {
                font: {
                  color: "#343434",
                  size: 11, // px
                  face: "arial",
                  background: "none",
                  strokeWidth: 5, // px
                  strokeColor: "#ffffff",
                  align: "top"
                },
                smooth: {
                  enabled: true, //setting to true enables curved lines
                  type: "dynamic",
                  "forceDirection": "none",
                  roundness: 0.75
                }
              }
            },
            graphConstructCount : 0
          });
      }
  }

  render() {
    return (
      <div className='Graph-Page'>
        <div class="Left-Pane">
          <h2>State machine JSON:</h2>
          <textarea onChange={this.handleModelInputChanged.bind(this)} />
        </div>
        <div className="Center-Pane" >
          <Graph graph={this.state.graph} graphConstructCount={this.state.graphConstructCount} options={this.state.options} events={this.events}/>
        </div>
        <div className="State-Info">
          <StateInformationWidget state={this.state.selectedState} connections={this.state.selectedEdges}/>
        </div>
      </div>
    );
  }
}

export default VisWidget;