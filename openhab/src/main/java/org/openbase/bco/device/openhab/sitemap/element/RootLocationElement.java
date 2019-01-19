package org.openbase.bco.device.openhab.sitemap.element;

/*-
 * #%L
 * BCO Openhab Device Manager
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import org.openbase.bco.device.openhab.sitemap.SitemapBuilder;
import org.openbase.bco.device.openhab.sitemap.SitemapBuilder.SitemapIconType;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.extension.type.processing.LabelProcessor;
import org.openbase.type.domotic.service.ServiceConfigType.ServiceConfig;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import sun.net.www.content.text.Generic;

import java.util.*;

public class RootLocationElement extends LocationElement {

    public RootLocationElement() throws InstantiationException {
        super();
        try {
            init(Registries.getUnitRegistry().getRootLocationConfig());
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    public void serialize(final SitemapBuilder sitemap) throws CouldNotPerformException {
        super.serialize(sitemap);

        sitemap.openTextContext("Settings", SitemapIconType.SETTINGS);

        // open compositions
        sitemap.openFrameContext("Ansichten", SitemapIconType.NONE);


        // list location power consumption
        sitemap.openTextContext("Energieverbrauch", SitemapIconType.ENERGY);
        for (UnitConfig unitConfig : Registries.getUnitRegistry().getUnitConfigs(UnitType.LOCATION)) {
//            sitemap.append(new GenericUnitSitemapElement(unitConfig, ServiceType.POWER_CONSUMPTION_STATE_SERVICE));
        }
        sitemap.closeContext();

        // list battery and tamper states
        sitemap.openTextContext("Wartung", SitemapIconType.PRESSURE);

        sitemap.openFrameContext("Battery Level");
        for (UnitConfig unitConfig : Registries.getUnitRegistry().getUnitConfigs(UnitType.BATTERY)) {
            sitemap.append(new GenericUnitSitemapElement(unitConfig, true));
        }
        sitemap.closeContext();

        sitemap.openFrameContext("Manipulation");
        for (UnitConfig unitConfig : Registries.getUnitRegistry().getUnitConfigs(UnitType.TAMPER_DETECTOR)) {
            sitemap.append(new GenericUnitSitemapElement(unitConfig, true));
        }
        sitemap.closeContext();


        sitemap.closeContext();

        // close compositions
        sitemap.closeContext();


        sitemap.openFrameContext("Debug");

        sitemap.openTextContext("Units", SitemapIconType.STATUS);
        final Map<UnitType, List<UnitConfig>> unitTypeUnitConfigMap = new TreeMap<>();

        // load unit configs
        for (UnitConfig unitConfig : Registries.getUnitRegistry().getUnitConfigs()) {

            // filter devices
            if (unitConfig.getUnitType() == UnitType.DEVICE) {
                continue;
            }

            // filter system user
            if (unitConfig.getUnitType() == UnitType.USER && unitConfig.getUserConfig().getSystemUser()) {
                continue;
            }

            // filter system auth groups
            if (unitConfig.getUnitType() == UnitType.AUTHORIZATION_GROUP) {
                continue;
            }

            if (!unitTypeUnitConfigMap.containsKey(unitConfig.getUnitType())) {
                unitTypeUnitConfigMap.put(unitConfig.getUnitType(), new ArrayList<>());
            }
            unitTypeUnitConfigMap.get(unitConfig.getUnitType()).add(unitConfig);
        }

        for (List<UnitConfig> unitConfigListValue : unitTypeUnitConfigMap.values()) {
            // sort by name
            Collections.sort(unitConfigListValue, Comparator.comparing(o -> LabelProcessor.getBestMatch(o.getLabel(), "?")));
        }

        boolean absoluteLabel;
        for (UnitType unitType : unitTypeUnitConfigMap.keySet()) {

            if(unitType == UnitType.USER) {
                absoluteLabel = false;
            } else {
                absoluteLabel = true;
            }

            if (unitTypeUnitConfigMap.get(unitType).isEmpty()) {
                continue;
            }

            sitemap.openFrameContext(unitType.name());
            for (UnitConfig unitConfig : unitTypeUnitConfigMap.get(unitType)) {
                sitemap.append(new GenericUnitSitemapElement(unitConfig, absoluteLabel));
            }
            sitemap.closeContext();
        }
        sitemap.closeContext();

        sitemap.openTextContext("Services", SitemapIconType.STATUS);
        final Map<ServiceType, List<UnitConfig>> serviceTypeUnitConfigMap = new TreeMap<>();

        // load unit configs
        for (UnitConfig unitConfig : Registries.getUnitRegistry().getUnitConfigs()) {
            for (ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                if (!serviceTypeUnitConfigMap.containsKey(serviceConfig.getServiceDescription().getServiceType())) {
                    serviceTypeUnitConfigMap.put(serviceConfig.getServiceDescription().getServiceType(), new ArrayList<>());
                }
                serviceTypeUnitConfigMap.get(serviceConfig.getServiceDescription().getServiceType()).add(unitConfig);
            }
        }

        for (List<UnitConfig> unitConfigListValue : serviceTypeUnitConfigMap.values()) {
            // sort by name
            Collections.sort(unitConfigListValue, Comparator.comparing(o ->  LabelProcessor.getBestMatch(o.getLabel(), "?")));
        }

        for (ServiceType serviceType : serviceTypeUnitConfigMap.keySet()) {

            if (serviceTypeUnitConfigMap.get(serviceType).isEmpty()) {
                continue;
            }

            sitemap.openFrameContext(serviceType.name());
            for (UnitConfig unitConfig : serviceTypeUnitConfigMap.get(serviceType)) {
                sitemap.append(new GenericUnitSitemapElement(unitConfig, serviceType, true));
            }
            sitemap.closeContext();
        }
        sitemap.closeContext();
        sitemap.closeContext();
        sitemap.closeContext();
    }
}
