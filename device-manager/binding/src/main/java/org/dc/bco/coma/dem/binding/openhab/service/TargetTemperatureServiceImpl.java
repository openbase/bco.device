/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.coma.dem.binding.openhab.service;

import org.dc.bco.coma.dem.binding.openhab.OpenHABCommandFactory;
import org.dc.bco.coma.dem.lib.Device;
import org.dc.bco.dal.lib.layer.service.TargetTemperatureService;
import org.dc.bco.dal.lib.layer.unit.Unit;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;

/**
 *
 * @author thuxohl
 * @param <ST> Related service type.
 */
public class TargetTemperatureServiceImpl<ST extends TargetTemperatureService & Unit> extends OpenHABService<ST> implements TargetTemperatureService {
    
    public TargetTemperatureServiceImpl(final ST unit) throws InstantiationException {
        super(device, unit);
    }
    
    @Override
    public void setTargetTemperature(final Double value) throws CouldNotPerformException {
        executeCommand(OpenHABCommandFactory.newDecimalCommand(value));
    }

    @Override
    public Double getTargetTemperature() throws CouldNotPerformException {
        return unit.getTargetTemperature();
    }

}
