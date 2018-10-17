package org.openbase.bco.manager.device.core;

/*
 * #%L
 * BCO Manager Device Core
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.dal.control.layer.unit.AbstractHostUnitController;
import org.openbase.bco.dal.lib.layer.unit.UnitController;
import org.openbase.bco.manager.device.lib.DeviceController;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.schedule.SyncObject;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.device.DeviceDataType.DeviceData;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractDeviceController extends AbstractHostUnitController<DeviceData, DeviceData.Builder> implements DeviceController {

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DeviceData.getDefaultInstance()));
    }

    private final SyncObject configUpdateLock = new SyncObject("ApplyConfigUpdateLock");

    public AbstractDeviceController(final Class deviceClass) throws InstantiationException {
        super(deviceClass, DeviceData.newBuilder());
    }

    @Override
    public UnitConfig applyConfigUpdate(UnitConfig config) throws CouldNotPerformException, InterruptedException {
        synchronized (configUpdateLock) {
            UnitConfig unitConfig = super.applyConfigUpdate(config);

            // update unit controller registry if device is active.
            for (final String removedUnitId : getRemovedUnitIds()) {
                DeviceManagerImpl.getDeviceManager().getUnitControllerRegistry().remove(removedUnitId);
            }

            for (final UnitController newUnitController : getNewUnitController()) {
                DeviceManagerImpl.getDeviceManager().getUnitControllerRegistry().register(newUnitController);
            }

            return unitConfig;
        }
    }

    @Override
    public void shutdown() {
        try {
            // skip if registry is shutting down anyway.
            if (!DeviceManagerImpl.getDeviceManager().getUnitControllerRegistry().isShutdownInitiated()) {
                DeviceManagerImpl.getDeviceManager().getUnitControllerRegistry().removeAll(getHostedUnitControllerList());
            }
        } catch (InvalidStateException ex) {
            // registry already shutting down during removal which will clear the entries anyway.
        } catch (MultiException ex) {
            logger.warn("Could not deregister all unit controller during shutdown!", ex);
        }
        super.shutdown();
    }
}
