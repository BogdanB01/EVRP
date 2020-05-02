import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Instance } from '../models/instance';
import { APP_CONSTANTS } from '../app.constants';

@Injectable()
export class InstanceService {
    constructor(private httpClient: HttpClient) {}

    public getInstances(): Observable<Instance[]> {
        return this.httpClient.get<Instance[]>(`${APP_CONSTANTS.ENDPOINT}/instances`);
    }

    public getInstanceByName(name: string): Observable<Instance> {
        const params = new HttpParams({
            fromObject: {
                name
            }
        });
        return this.httpClient.get<Instance>(`${APP_CONSTANTS.ENDPOINT}/instances/filter`, { params });
    }
}
