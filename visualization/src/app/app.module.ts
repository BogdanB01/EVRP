import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './material.module';
import { HttpClientModule } from '@angular/common/http';
import { SnackBarService } from './services/snackbar.service';
import { InstanceService } from './services/instance.service';
import { InstancesComponent } from './components/instances/instances.component';
import { LegendComponent } from './components/legend/legend.component';


@NgModule({
  declarations: [
    AppComponent,
    InstancesComponent,
    LegendComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialModule,
    HttpClientModule
  ],
  providers: [
    SnackBarService,
    InstanceService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
