import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { InstanceService } from 'src/app/services/instance.service';
import { SnackBarService } from 'src/app/services/snackbar.service';
import { Instance } from 'src/app/models/instance';
import { MatSelectChange } from '@angular/material/select';
import { DataSet, Network } from 'vis';
import { APP_CONFIG } from 'src/app/app.config';
import { Customer } from 'src/app/models/customer';
import { RechargingStation } from 'src/app/models/recharging.station';
import { Depot } from 'src/app/models/depot';

@Component({
    selector: 'app-instances',
    templateUrl: './instances.component.html',
    styleUrls: ['./instances.component.css']
})
export class InstancesComponent implements OnInit {

    @ViewChild('instance', { static: false }) el: ElementRef;
    private networkInstance: any;

    selectedInstance;
    instances: Array<Instance> = [];
    instance: Instance;

    constructor(private instanceService: InstanceService,
                private snackbarService: SnackBarService) {
    }

    ngOnInit(): void {
        this.instanceService.getInstances().subscribe(instances => {
            this.instances = instances;
        }, err => {
            this.snackbarService.showSnackBar('Could not load instances');
        });
    }

    getSelectedInstance(select: MatSelectChange): void {
        if (select.value !== undefined) {
            this.instanceService.getInstanceByName(select.value).subscribe(instance => {
                this.instance = instance;
                this.draw();
            }, err => {
                this.snackbarService.showSnackBar('Could not load instance with name: ' + select.value);
            });
        }
    }

    private draw(): void {
        const container = this.el.nativeElement;
        const nodes = new DataSet<any>([]);
        this.addNodes(nodes);

        const edges = new DataSet<any>([]);
        const options = {
            width: '100%',
            height: '800px',
            physics: false,
            interaction: {
                dragNodes: false,
                zoomView: true,
                dragView: true
            },
            groups: APP_CONFIG.groups
        };
        const data = { nodes, edges };
        this.networkInstance = new Network(container, data, options);
        this.networkInstance.moveTo({
            position: {x: 0, y: 0},
            offset: {x: -500, y: -500},
            scale: 0.75
        });

        console.log(container);
    }

    private addNodes(nodes: DataSet<any>): void {
        if (this.instance === undefined) {
            return;
        }
        this.instance.customers.forEach(customer => {
            const node = {
                id: customer.name,
                label: customer.name,
                x: customer.location.x * 10,
                y: customer.location.y * 10,
                group: 'customer',
                title: this.createToolTipForCustomer(customer)
            };
            nodes.add(node);
        });
        const depot = {
            id: this.instance.depot.name,
            label: this.instance.depot.name,
            x: this.instance.depot.location.x * 10,
            y: this.instance.depot.location.y * 10,
            group: 'depot',
            size: 50,
            title: this.creatToolTipForDepot(this.instance.depot)
        };
        nodes.add(depot);
        this.instance.rechargingStations.forEach(rc => {
            const node = {
                id: rc.name,
                label: rc.name,
                x: rc.location.x * 10,
                y: rc.location.y * 10,
                group: 'rechargingStation',
                title: this.createToolTipForRechargingStation(rc)
            };
            nodes.add(node);
        });
    }

    private createToolTipForCustomer(customer: Customer) {
        const tooltip = {
            location: {
                x: customer.location.x,
                y: customer.location.y
            },
            timeWindow: {
                start: customer.timeWindow.start,
                end: customer.timeWindow.end
            },
            demand: customer.demand
        };
        return JSON.stringify(tooltip);
    }

    private createToolTipForRechargingStation(rc: RechargingStation) {
        const tooltip = {
            location: {
                x: rc.location.x,
                y: rc.location.y
            },
            timeWindow: {
                start: rc.timeWindow.start,
                end: rc.timeWindow.end
            },
            rechargingRate: rc.rechargingRate
        };
        return JSON.stringify(tooltip);
    }

    private creatToolTipForDepot(depot: Depot) {
        const tooltip = {
            location: {
                x: depot.location.x,
                y: depot.location.y
            },
            timeWindow: {
                start: depot.timeWindow.start,
                end: depot.timeWindow.end
            }
        };
        return JSON.stringify(tooltip);
    }
}
