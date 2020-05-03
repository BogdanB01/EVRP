import { Route } from './route';

export interface Solution {
    routes: Array<Route>;
    cost: number;
    numberOfRoutes: number;
    totalDistance: number;
    timeTaken: number;
}
