import { Customer } from './customer';
import { RechargingStation } from './recharging.station';
import { Depot } from './depot';

export interface Instance {
    name: string;
    location: string;
    customers?: Array<Customer>;
    rechargingStations?: Array<RechargingStation>;
    depot?: Depot;
    numNodes: number;
    numCustomers: number;
    rechargingRate: number;
    vehicleAverageVelocity: number;
    vehicleCapacity: number;
    vehicleEnergyCapacity: number;
    vehicleEnergyConsumption: number;
}
