package org.openbase.bco.manager.device.binding.openhab.util.configgen.items;

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
import java.util.List;
import org.openbase.bco.manager.device.binding.openhab.util.configgen.GroupEntry;
import org.openbase.bco.registry.location.remote.LocationRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.ProtobufVariableProvider;
import org.openbase.jul.extension.rst.processing.MetaConfigPool;
import org.openbase.jul.extension.rst.processing.MetaConfigVariableProvider;
import org.openbase.jul.processing.StringProcessor;
import rst.configuration.MetaConfigType.MetaConfig;
import rst.homeautomation.device.DeviceClassType.DeviceClass;
import rst.homeautomation.service.ServiceConfigType.ServiceConfig;
import rst.homeautomation.service.ServiceTemplateConfigType.ServiceTemplateConfig;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.homeautomation.unit.UnitTemplateConfigType.UnitTemplateConfig;
import rst.homeautomation.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ServiceItemEntry extends AbstractItemEntry {

    public static final String SERVICE_TEMPLATE_BINDING_TYPE = "OPENHAB_BINDING_TYPE";
    public static final String SERVICE_TEMPLATE_BINDING_ICON = "OPENHAB_BINDING_ICON";
    public static final String SERVICE_TEMPLATE_BINDING_COMMAND = "OPENHAB_BINDING_COMMAND";
    public static final String SERVICE_TEMPLATE_BINDING_CONFIG = "OPENHAB_BINDING_CONFIG";
    public static final String SERVICE_TEMPLATE_BINDING_LABEL_DESCRIPTOR = "OPENHAB_SERVICE_LABEL_DESCRIPTOR";
    public static final String OPENHAB_BINDING_ITEM_ID = "OPENHAB_BINDING_ITEM_ID";
    public static final String UNIT_VISIBLE_IN_GUI = "UNIT_VISIBLE_IN_GUI";
    public static final String OPENHAB_BINDING_DEVICE_ID = "OPENHAB_BINDING_DEVICE_ID";

    private final MetaConfigPool configPool;

    public ServiceItemEntry(final DeviceClass deviceClass, final MetaConfig unitHostMetaConfig, final UnitConfig unitConfig, final ServiceConfig serviceConfig, final LocationRegistryRemote locationRegistryRemote) throws InstantiationException {
        super();
        try {
            UnitConfig locationUnitConfig = locationRegistryRemote.getLocationConfigById(unitConfig.getPlacementConfig().getLocationId());

            configPool = new MetaConfigPool();
            configPool.register(new MetaConfigVariableProvider("BindingServiceConfig", serviceConfig.getBindingConfig().getMetaConfig()));
            configPool.register(new MetaConfigVariableProvider("ServiceMetaConfig", serviceConfig.getMetaConfig()));
            configPool.register(new MetaConfigVariableProvider("UnitLocationMetaConfig", locationUnitConfig.getMetaConfig()));
            configPool.register(new MetaConfigVariableProvider("UnitMetaConfig", unitConfig.getMetaConfig()));
            configPool.register(new MetaConfigVariableProvider("DeviceMetaConfig", unitHostMetaConfig));
            configPool.register(new MetaConfigVariableProvider("DeviceBindingConfig", deviceClass.getBindingConfig().getMetaConfig()));
            configPool.register(new MetaConfigVariableProvider("DeviceClassMetaConfig", deviceClass.getMetaConfig()));
            configPool.register(new ProtobufVariableProvider(locationUnitConfig));
            configPool.register(new ProtobufVariableProvider(unitConfig));
            configPool.register(new ProtobufVariableProvider(serviceConfig));

            try {
                configPool.register(new MetaConfigVariableProvider("ServiceTemplateMetaConfig", lookupServiceTemplate(deviceClass, unitConfig, serviceConfig).getMetaConfig()));
            } catch (final NotAvailableException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not load service template meta config for Service[" + serviceConfig.getServiceTemplate().getType().name() + "] of Unit[" + unitConfig.getId() + "]", ex), logger, LogLevel.ERROR);
            }

            try {
                this.itemId = configPool.getValue(OPENHAB_BINDING_ITEM_ID);
            } catch (final NotAvailableException ex) {
                throw new NotAvailableException("itemId", ex);
            }

            try {
                this.label = configPool.getValue(SERVICE_TEMPLATE_BINDING_LABEL_DESCRIPTOR);
            } catch (NotAvailableException ex) {
                this.label = unitConfig.getLabel();
            }

            try {
                this.commandType = configPool.getValue(SERVICE_TEMPLATE_BINDING_COMMAND);
            } catch (NotAvailableException ex) {
                this.commandType = getDefaultCommand(serviceConfig.getServiceTemplate());
            }

            try {
                this.icon = configPool.getValue(SERVICE_TEMPLATE_BINDING_ICON);
            } catch (NotAvailableException ex) {
                this.icon = "";
            }

            this.groups.add("Unit" + StringProcessor.transformUpperCaseToCamelCase(unitConfig.getType().name()));
            this.groups.add("Service" + StringProcessor.transformUpperCaseToCamelCase(serviceConfig.getServiceTemplate().getType().name()));

            try {
                // just add location group if unit is visible.
                if (Boolean.parseBoolean(configPool.getValue(UNIT_VISIBLE_IN_GUI))) {
                    this.groups.add(GroupEntry.generateGroupID(unitConfig.getPlacementConfig(), locationRegistryRemote));
                }
            } catch (Exception ex) {
                this.groups.add(GroupEntry.generateGroupID(unitConfig.getPlacementConfig(), locationRegistryRemote));
            }

            try {
                itemHardwareConfig = generateItemHardwareConfig(unitConfig, serviceConfig);
            } catch (CouldNotPerformException ex) {
                throw new NotAvailableException("itemHardwareConfig", ex);
            }

            this.calculateGaps();
        } catch (Exception ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    private String generateItemHardwareConfig(final UnitConfig unitConfig, final ServiceConfig serviceConfig) throws CouldNotPerformException {
        try {
            String config = "";

            config += configPool.getValue(SERVICE_TEMPLATE_BINDING_TYPE);
            config += "=\"";
            config += configPool.getValue(SERVICE_TEMPLATE_BINDING_CONFIG);
            config += "\"";
            return config;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not generate item hardware config of Unit[" + unitConfig.getId() + "] !", ex);
        }
    }

    /**
     * Lookups the service template of the given ServiceType out of the unit
     * template.
     *
     * @param unitTemplate to lookup the service template.
     * @param serviceType the service type to resolve the template.
     * @return the related service template for the given service type.
     * @throws NotAvailableException
     */
    private ServiceTemplateConfig lookupServiceTemplate(final DeviceClass deviceClass, final UnitConfig unitConfig, final ServiceConfig serviceConfig) throws NotAvailableException {
        List<UnitTemplateConfig> unitTemplateConfigList = deviceClass.getUnitTemplateConfigList();
        for (UnitTemplateConfig unitTemplateConfig : unitTemplateConfigList) {
            if (unitTemplateConfig.getId().equals(unitConfig.getUnitTemplateConfigId())) {
                List<ServiceTemplateConfig> serviceTemplateList = unitTemplateConfig.getServiceTemplateConfigList();
                for (ServiceTemplateConfig serviceTemplate : serviceTemplateList) {
                    if (serviceTemplate.getServiceType().equals(serviceConfig.getServiceTemplate().getType())) {
                        return serviceTemplate;
                    }
                }
            }
        }
        throw new NotAvailableException("service template for ServiceType[" + serviceConfig.getServiceTemplate().getType().name() + "]");
    }

    private UnitTemplateConfig lookupUnitTemplateConfig(final DeviceClass deviceClass, final UnitConfig unitConfig) throws NotAvailableException {
        return lookupUnitTemplateConfig(deviceClass, unitConfig.getType());
    }

    private UnitTemplateConfig lookupUnitTemplateConfig(final DeviceClass deviceClass, final UnitType unitType) throws NotAvailableException {

        for (UnitTemplateConfig template : deviceClass.getUnitTemplateConfigList()) {
            if (template.getType() == unitType) {
                return template;
            }
        }
        throw new NotAvailableException("unit template config for UnitType[" + unitType.name() + "]");
    }
}
