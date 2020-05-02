import { TimeWindow } from './time.window';
import { Location } from './location';

export interface RechargingStation {
    id: number;
    name: string;
    location: Location;
    timeWindow: TimeWindow;
    rechargingRate: number;
}
