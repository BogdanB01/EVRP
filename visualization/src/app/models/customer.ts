import { TimeWindow } from './time.window';
import { Location } from './location';

export interface Customer {
    id: number;
    name: string;
    location: Location;
    demand: number;
    timeWindow: TimeWindow;
    serviceTime: number;
}
