package org.dc.bco.manager.scene.remote;

/*
 * #%L
 * COMA AgentManager Remote
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
import org.dc.jul.extension.rsb.com.AbstractConfigurableRemote;
import org.dc.bco.manager.scene.lib.Scene;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.rsb.com.RPCHelper;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.homeautomation.control.scene.SceneConfigType.SceneConfig;
import rst.homeautomation.control.scene.SceneDataType.SceneData;
import rst.homeautomation.state.ActivationStateType.ActivationState;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine
 * Threepwood</a>
 */
public class SceneRemote extends AbstractConfigurableRemote<SceneData, SceneConfig> implements Scene {
    
    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneData.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(ActivationState.getDefaultInstance()));
    }

    public SceneRemote() {
        super(SceneData.class, SceneConfig.class);
    }

    @Override
    public void notifyDataUpdate(SceneData data) throws CouldNotPerformException {
       
    }

    public void setActivationState(ActivationState.State activationState) throws CouldNotPerformException {
        setActivationState(ActivationState.newBuilder().setValue(activationState).build());
    }

    @Override
    public void setActivationState(ActivationState activation) throws CouldNotPerformException {
        RPCHelper.callRemoteMethod(activation, this);
    }

    @Override
    public SceneConfig applyConfigUpdate(SceneConfig config) throws CouldNotPerformException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
