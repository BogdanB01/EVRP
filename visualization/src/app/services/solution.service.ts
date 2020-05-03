import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONSTANTS } from '../app.constants';
import { Algorithm } from '../models/algorithm';
import { Solution } from '../models/solution';

@Injectable()
export class SolutionService {
    constructor(private httpClient: HttpClient) {}

    public getSolutionByNameAndAlgorithm(name: string, algorithm: Algorithm): Observable<Solution> {
        const params = new HttpParams({
            fromObject: {
                name,
                algorithm
            }
        });
        return this.httpClient.get<Solution>(`${APP_CONSTANTS.ENDPOINT}/solutions/filter`, { params });
    }
}
