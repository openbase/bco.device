/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.coma.agm.core;

import de.citec.agm.remote.AgentRegistryRemote;
import org.dc.jps.core.JPService;
import de.citec.jul.storage.registry.ActivatableEntryRegistrySynchronizer;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.printer.LogLevel;
import de.citec.jul.storage.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.homeautomation.control.agent.AgentConfigType.AgentConfig;
import rst.homeautomation.state.ActivationStateType.ActivationState;

/**
 *
 * @author mpohling
 */
public class AgentManager {

    protected static final Logger logger = LoggerFactory.getLogger(AgentManager.class);

    private final AgentFactory factory;
    private final Registry<String, Agent> agentRegistry;
    private final AgentRegistryRemote agentRegistryRemote;
    private final ActivatableEntryRegistrySynchronizer<String, Agent, AgentConfig, AgentConfig.Builder> registrySynchronizer;

    public AgentManager() throws InstantiationException, InterruptedException {
        try {
            this.factory = AgentFactoryImpl.getInstance();
            this.agentRegistry = new Registry<>();

            agentRegistryRemote = new AgentRegistryRemote();

            this.registrySynchronizer = new ActivatableEntryRegistrySynchronizer<String, Agent, AgentConfig, AgentConfig.Builder>(agentRegistry, agentRegistryRemote.getAgentConfigRemoteRegistry(), factory) {

                @Override
                public boolean activationCondition(final AgentConfig config) {
                    return config.getActivationState().getValue() == ActivationState.State.ACTIVE;
                }
            };

            agentRegistryRemote.init();
            agentRegistryRemote.activate();
            registrySynchronizer.init();
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws de.citec.jul.exception.CouldNotPerformException
     */
    public static void main(String[] args) throws InterruptedException, CouldNotPerformException {

        /* Setup JPService */
        JPService.setApplicationName(AgentManager.class);
        JPService.parseAndExitOnError(args);

        /* Start main app */
        logger.info("Start " + JPService.getApplicationName() + "...");
        try {
            new AgentManager();
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, logger, LogLevel.ERROR);
        }
        logger.info(JPService.getApplicationName() + " successfully started.");
    }
}
