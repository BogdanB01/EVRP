import { TimeWindow } from './time.window';
import { Location } from './location';

export interface Depot {
    id: number;
    name: string;
    location: Location;
    timeWindow: TimeWindow;
}
