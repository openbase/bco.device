package org.dc.bco.manager.app.core.preset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.dc.bco.manager.app.core.AbstractApp;
import org.dc.bco.manager.location.remote.LocationRemote;
import org.dc.bco.registry.location.lib.LocationRegistry;
import org.dc.bco.registry.location.remote.CachedLocationRegistryRemote;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.schedule.GlobalExecutionService;
import rst.spatial.LocationConfigType;
import rst.vision.HSVColorType;

/*
 * #%L
 * COMA AppManager Core
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
/**
 * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class PartyLightTileFollowerApp extends AbstractApp {
    
    private Map<String, LocationRemote> locationRemoteMap;
    private LocationRegistry locationRegistry;
    
    public PartyLightTileFollowerApp() throws InstantiationException, InterruptedException {
        super(true);
        try {
            this.locationRegistry = CachedLocationRegistryRemote.getRegistry();
            this.locationRemoteMap = new HashMap<>();
            
            LocationRemote locationRemote;
            // init tile remotes
            for (LocationConfigType.LocationConfig locationConfig : locationRegistry.getLocationConfigs()) {
                if (!locationConfig.getType().equals(LocationConfigType.LocationConfig.LocationType.TILE)) {
                    continue;
                }
                locationRemote = new LocationRemote();
                locationRemoteMap.put(locationConfig.getId(), locationRemote);
                locationRemote.init(locationConfig);
                locationRemote.activate();
            }
            
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }
    
    @Override
    public void shutdown() throws InterruptedException {
        // shutdown tile remotes
        locationRemoteMap.values().stream().forEach((locationRemote) -> {
            locationRemote.shutdown();
        });
        super.shutdown();
    }
    
    private double brightness = 50;
    private HSVColorType.HSVColor[] colors = {
        HSVColorType.HSVColor.newBuilder().setHue(0).setSaturation(100).setValue(brightness).build(),
        HSVColorType.HSVColor.newBuilder().setHue(290).setSaturation(100).setValue(brightness).build(),
        HSVColorType.HSVColor.newBuilder().setHue(30).setSaturation(100).setValue(brightness).build(),
    };
    
    private Future<Void> tileFollowerFuture;
    
    @Override
    protected void execute() throws CouldNotPerformException, InterruptedException {

        // verify
        if (!locationRegistry.getLocationConfigById(getConfig().getLocationId()).getType().equals(LocationConfigType.LocationConfig.LocationType.TILE)) {
            throw new InvalidStateException("App location is not a tile!");
        }

        // execute
        if (tileFollowerFuture != null) {
            logger.warn(this + " is already executing!");
            return;
        }
        tileFollowerFuture = GlobalExecutionService.submit(new TileFollower());
        
    }
    
    @Override
    protected void stop() throws CouldNotPerformException, InterruptedException {
        if (tileFollowerFuture != null) {
            tileFollowerFuture.cancel(true);
            tileFollowerFuture = null;
        }
    }
    
    public class TileFollower implements Callable<Void> {
        
        private List<String> processedLocations = new ArrayList<>();
        
        @Override
        public Void call() throws CouldNotPerformException, InterruptedException {
            logger.info("Execute " + this);
            if (locationRemoteMap.isEmpty()) {
                throw new CouldNotPerformException("No Locations found!");
            }
            
            LocationRemote locationRemote;
            
            int colorIndex = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // apply updates for next iteration
                    colorIndex = ++colorIndex % colors.length;
                    System.out.println("color index:"+colorIndex);
                    processedLocations.clear();

                    // select inital room
                    locationRemote = locationRemoteMap.get(getConfig().getLocationId());
                    
                    processRoom(locationRemote, colors[colorIndex]);
                    
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Skip animation run!", ex), logger);
                }
                
                logger.info("#########################");
                
            }
            return null;
        }
        
        public void processRoom(final LocationRemote locationRemote, final HSVColorType.HSVColor color) throws CouldNotPerformException, InterruptedException {
            logger.info("Set " + locationRemote + " to " + color + "...");
            try {
                
                try {
                    locationRemote.setColor(color).get(1, TimeUnit.SECONDS);
                } catch (TimeoutException ex) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not set color!", ex), logger);
                }

                // mark as prcessed        
                processedLocations.add(locationRemote.getId());
                
                Thread.sleep(3000);

                // process neighbors
                for (String neighborsId : locationRemote.getNeighborLocationIds()) {
                    // skip if already processed
                    if (processedLocations.contains(neighborsId)) {
                        continue;
                    }

                    // process remote 
                    processRoom(locationRemoteMap.get(neighborsId), color);
                }
            } catch (CouldNotPerformException | ExecutionException ex) {
                throw new CouldNotPerformException("Could not process room of " + locationRemote);
            }
        }
    }
}
