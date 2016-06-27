package org.openbase.bco.manager.device.binding.openhab.execution;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

import org.openbase.bco.dal.lib.layer.unit.UnitController;
import org.openbase.bco.dal.lib.layer.unit.UnitControllerRegistry;
import org.openbase.bco.manager.device.binding.openhab.transform.OpenhabCommandTransformer;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.processing.StringProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Scope;
import rst.homeautomation.openhab.OpenhabCommandType;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate;

/**
 *
 * @author Divine Threepwood
 */
public class OpenHABCommandExecutor {

    public static final String ITEM_SUBSEGMENT_DELIMITER = "_";
    public static final String ITEM_SEGMENT_DELIMITER = "__";

    private static final Logger logger = LoggerFactory.getLogger(OpenHABCommandExecutor.class);

    private final UnitControllerRegistry unitControllerRegistry;

    public OpenHABCommandExecutor(UnitControllerRegistry unitControllerRegistry) {
        this.unitControllerRegistry = unitControllerRegistry;
    }

    private class OpenhabCommandMetaData {

        private final OpenhabCommandType.OpenhabCommand command;
        private final ServiceTemplate.ServiceType serviceType;
        private final String unitId;
        private final String locationId;

        public OpenhabCommandMetaData(OpenhabCommand command) throws CouldNotPerformException {
            this.command = command;

            try {
                String[] nameSegment = command.getItem().split(ITEM_SEGMENT_DELIMITER);
                try {
                    locationId = nameSegment[1].replace(ITEM_SUBSEGMENT_DELIMITER, Scope.COMPONENT_SEPARATOR);
                } catch (IndexOutOfBoundsException | NullPointerException ex) {
                    throw new CouldNotPerformException("Could not extract location id out of item name!");
                }
                try {
                    this.unitId = (Scope.COMPONENT_SEPARATOR + locationId + Scope.COMPONENT_SEPARATOR + nameSegment[2] + Scope.COMPONENT_SEPARATOR + nameSegment[3] + Scope.COMPONENT_SEPARATOR).toLowerCase();
                } catch (IndexOutOfBoundsException | NullPointerException ex) {
                    throw new CouldNotPerformException("Could not extract unit id out of item name!");
                }
                try {
                    serviceType = ServiceTemplate.ServiceType.valueOf(StringProcessor.transformToUpperCase(nameSegment[4]));
                } catch (IndexOutOfBoundsException | NullPointerException ex) {
                    throw new CouldNotPerformException("Could not extract service type out of item name!", ex);
                }
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not extract meta data out of openhab command because Item[" + command.getItem() + "] not compatible!", ex);
            }
        }

        public OpenhabCommand getCommand() {
            return command;
        }

        public ServiceTemplate.ServiceType getServiceType() {
            return serviceType;
        }

        public String getUnitId() {
            return unitId;
        }

        public String getLocationId() {
            return locationId;
        }
    }

    public void receiveUpdate(OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        logger.info("receiveUpdate [" + command.getItem() + "=" + command.getType() + "]");
        OpenhabCommandMetaData metaData = new OpenhabCommandMetaData(command);
        Object serviceData = OpenhabCommandTransformer.getServiceData(command, metaData.getServiceType());
        UnitController unitController = unitControllerRegistry.get(metaData.getUnitId());
        unitController.applyUpdate(metaData.getServiceType(), serviceData);
    }
}