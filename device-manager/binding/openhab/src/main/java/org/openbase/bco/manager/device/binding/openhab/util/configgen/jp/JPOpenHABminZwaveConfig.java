package org.openbase.bco.manager.device.binding.openhab.util.configgen.jp;

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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPDirectory;
import org.openbase.jps.tools.FileHandler;
import java.io.File;

/**
 *
 * @author mpohling
 */
public class JPOpenHABminZwaveConfig extends AbstractJPDirectory {
    private static final String[] COMMAND_IDENTIFIERS = {"--hambin-zwave"};

    public JPOpenHABminZwaveConfig() {
        super(COMMAND_IDENTIFIERS, FileHandler.ExistenceHandling.Must, FileHandler.AutoMode.On);
    }

    @Override
    protected File getPropertyDefaultValue() throws JPNotAvailableException {
        return new File(JPService.getProperty(JPOpenHABDistribution.class).getValue(), "etc/zwave");
    }

    @Override
    public String getDescription() {
        return "Define the habmin zwave config folder location.";
    }
}