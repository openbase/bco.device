/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.manager.location.core;

/*
 * #%L
 * COMA LocationManager Core
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.dc.bco.manager.location.lib.ConnectionController;
import org.dc.bco.manager.location.lib.ConnectionFactory;
import org.dc.bco.manager.location.lib.LocationController;
import org.dc.bco.manager.location.lib.LocationFactory;
import org.dc.bco.manager.location.lib.LocationManager;
import org.dc.bco.registry.device.lib.DeviceRegistry;
import org.dc.bco.registry.device.remote.DeviceRegistryRemote;
import org.dc.bco.registry.location.lib.LocationRegistry;
import org.dc.bco.registry.location.remote.LocationRegistryRemote;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.storage.registry.RegistryImpl;
import org.dc.jul.storage.registry.RegistrySynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.spatial.ConnectionConfigType.ConnectionConfig;
import rst.spatial.LocationConfigType.LocationConfig;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class LocationManagerController implements LocationManager {

    protected static final Logger logger = LoggerFactory.getLogger(LocationManagerController.class);

    private static LocationManagerController instance;
    private final LocationRegistryRemote locationRegistryRemote;
    private final DeviceRegistryRemote deviceRegistryRemote;
    private final LocationFactory locationFactory;
    private final ConnectionFactory connectionFactory;
    private final RegistryImpl<String, LocationController> locationRegistry;
    private final RegistryImpl<String, ConnectionController> connectionRegistry;
    private final RegistrySynchronizer<String, LocationController, LocationConfig, LocationConfig.Builder> locationRegistrySynchronizer;
    private final RegistrySynchronizer<String, ConnectionController, ConnectionConfig, ConnectionConfig.Builder> connectionRegistrySynchronizer;

    public LocationManagerController() throws org.dc.jul.exception.InstantiationException, InterruptedException {
        try {
            this.instance = this;
            this.locationRegistryRemote = new LocationRegistryRemote();
            this.deviceRegistryRemote = new DeviceRegistryRemote();
            this.locationFactory = LocationFactoryImpl.getInstance();
            this.connectionFactory = ConnectionFactoryImpl.getInstance();
            this.locationRegistry = new RegistryImpl<>();
            this.connectionRegistry = new RegistryImpl<>();
            this.locationRegistrySynchronizer = new RegistrySynchronizer<>(locationRegistry, locationRegistryRemote.getLocationConfigRemoteRegistry(), locationFactory);
            this.connectionRegistrySynchronizer = new RegistrySynchronizer<>(connectionRegistry, locationRegistryRemote.getConnectionConfigRemoteRegistry(), connectionFactory);
        } catch (CouldNotPerformException ex) {
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    public static LocationManagerController getInstance() throws NotAvailableException {
        if (instance == null) {
            throw new NotAvailableException(LocationManagerController.class);
        }
        return instance;
    }

    public void init() throws InitializationException, InterruptedException {
        try {
            this.locationRegistryRemote.init();
            this.locationRegistryRemote.activate();
            this.deviceRegistryRemote.init();
            this.deviceRegistryRemote.activate();
            this.locationRegistrySynchronizer.init();
            this.connectionRegistrySynchronizer.init();
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void shutdown() {
        this.locationRegistryRemote.shutdown();
        this.deviceRegistryRemote.shutdown();
        this.locationRegistrySynchronizer.shutdown();
        this.connectionRegistrySynchronizer.shutdown();
        instance = null;
    }

    @Override
    public LocationRegistry getLocationRegistry() throws NotAvailableException {
        return locationRegistryRemote;
    }

    @Override
    public DeviceRegistry getDeviceRegistry() throws NotAvailableException {
        return deviceRegistryRemote;
    }
}