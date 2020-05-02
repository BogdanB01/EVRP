import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { DataSet, Network } from 'vis';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements AfterViewInit {
  title = 'visualization';

  @ViewChild('network', {static: false}) el: ElementRef;
  private networkInstance: any;

  ngAfterViewInit() {
    /* const container = this.el.nativeElement;
    const nodes = new DataSet<any>([
        {id: 1, label: 'Node 1', x: 0 , y: 0, group: 'depot'},
        {id: 2, label: 'Node 2', x: 10, y: 100, group: 'customer'},
    ]);

    const edges = new DataSet<any>([
        {from: 1, to: 3},
        {from: 1, to: 2},
        {from: 2, to: 4},
        {from: 2, to: 5}
    ]);


    // legend
    const x = - container.clientWidth / 2 + 50;
    const y = - container.clientWidth / 2 + 50;
    console.log(container);
    const step = 70;

    nodes.add({id: 1000, x, y, label: 'Customer', group: 'customer', value: 1 }); // fixed: true, physics: false});
    nodes.add({id: 1001, x, y: y + step, label: 'Depot', group: 'depot', value: 1 }); // fixed: true, physics: false});
    nodes.add({id: 1002, x, y: y + 2 * step,
      label: 'Recharging station', group: 'rechargingStation', value: 1 }); // , fixed: true, physics: false});

    const width = 1000;
    const height = 1000;

    const options = {
      width: width + 'px',
      height: height + 'px',
      nodes: {
        shape: 'dot'
      },
      edges: {
        smooth: false
      },
      physics: false,
      interaction: {
        dragNodes: false, // do not allow dragging nodes
        zoomView: true, // allow zooming
        dragView: false  // do not allow dragging
      },
      groups: {
        customer: {
          shape: 'triangle',
          color: '#FF9900' // orange
        },
        depot: {
          shape: 'dot',
          color: '#2B7CE9' // blue
        },
        rechargingStation: {
          shape: 'square',
          color: '#C5000B' // red
        }
      }
    };

    const data = { nodes, edges };

    this.networkInstance = new Network(container, data, options);
    */
    /* this.networkInstance.moveTo({
      position: {x: 0, y: 0},
      offset: {x: -width / 2, y: -height / 2},
      scale: 1
    }); */
  }
}
