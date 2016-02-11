/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.manager.scene.core;

/*
 * #%L
 * COMA SceneManager Core
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

import org.dc.bco.manager.scene.lib.Scene;
import org.dc.bco.manager.scene.lib.SceneController;
import org.dc.bco.manager.scene.lib.SceneFactory;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.homeautomation.control.scene.SceneConfigType.SceneConfig;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class SceneFactoryImpl implements SceneFactory {

    protected final Logger logger = LoggerFactory.getLogger(SceneFactoryImpl.class);
    private static SceneFactoryImpl instance;

    public synchronized static SceneFactoryImpl getInstance() {

        if (instance == null) {
            instance = new SceneFactoryImpl();
        }
        return instance;
    }

    private SceneFactoryImpl() {

    }

    @Override
    public SceneController newInstance(final SceneConfig config) throws org.dc.jul.exception.InstantiationException {
        SceneController scene;
        try {
            if (config == null) {
                throw new NotAvailableException("sceneConfig");
            }
            logger.info("Creating scene [" + config.getId() + "]");
            scene = new SceneControllerImpl();
            scene.init(config);
        } catch (CouldNotPerformException | SecurityException | IllegalArgumentException | InterruptedException ex) {
            throw new org.dc.jul.exception.InstantiationException(Scene.class, config.getId(), ex);
        }
        return scene;
    }
}
