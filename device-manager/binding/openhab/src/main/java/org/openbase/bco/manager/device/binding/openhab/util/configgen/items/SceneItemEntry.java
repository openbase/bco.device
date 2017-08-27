package org.openbase.bco.manager.device.binding.openhab.util.configgen.items;

/*
 * #%L
 * BCO Manager Device Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.openbase.jul.processing.StringProcessor;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class SceneItemEntry extends AbstractItemEntry {

    public SceneItemEntry(final UnitConfig sceneUnitConfig) throws org.openbase.jul.exception.InstantiationException, InterruptedException {
        super(sceneUnitConfig, null);
        try {
            this.itemId = generateItemId(sceneUnitConfig);
            this.icon = "";
            this.commandType = "Switch";
            this.label = sceneUnitConfig.getLabel();
            this.itemHardwareConfig = "rsb=\"bco.manager.scene:" + sceneUnitConfig.getId() + "\"";
            groups.add(ItemIdGenerator.generateUnitGroupID(UnitType.SCENE));
            groups.add(ItemIdGenerator.generateUnitGroupID(sceneUnitConfig.getPlacementConfig().getLocationId()));
            calculateGaps();
        } catch (CouldNotPerformException ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    private String generateItemId(final UnitConfig sceneUnitConfig) throws CouldNotPerformException {
        return StringProcessor.transformToIdString("Scene")
                + ITEM_SEGMENT_DELIMITER
                + ScopeGenerator.generateStringRepWithDelimiter(sceneUnitConfig.getScope(), ITEM_SUBSEGMENT_DELIMITER);
    }
}
