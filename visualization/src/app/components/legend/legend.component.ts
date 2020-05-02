import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { DataSet, Network } from 'vis';
import { APP_CONFIG } from 'src/app/app.config';

@Component({
    selector: 'app-legend',
    templateUrl: './legend.component.html',
    styleUrls: ['./legend.component.css']
})
export class LegendComponent implements AfterViewInit {

    @ViewChild('legend', {static: false}) el: ElementRef;
    private networkInstance: any;

    ngAfterViewInit() {
      const container = this.el.nativeElement;
      const x = - container.clientWidth / 2 + 50;
      const y = - container.clientWidth / 2 + 50;

      const step = 150;
      const nodes = new DataSet<any>([
        {id: 1000, x, y, label: 'Customer', group: 'customer', value: 1 },
        {id: 1001, x, y: y + step, label: 'Depot', group: 'depot', value: 1 },
        {id: 1002, x, y: y + 2 * step, label: 'Recharging station', group: 'rechargingStation', value: 1 }
      ]);

      const edges = new DataSet<any>([]);

      const width = 200;
      const height = 600;

      const options = {
        width: width + 'px',
        height: height + 'px',
        interaction: {
          dragNodes: false, // do not allow dragging nodes
          zoomView: false, // do allow zooming
          dragView: false  // do not allow dragging
        },
        groups: APP_CONFIG.groups
      };

      const data = { nodes, edges };

      this.networkInstance = new Network(container, data, options);
    }
}

