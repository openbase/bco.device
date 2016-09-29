package org.openbase.bco.manager.scene.visual;

/*
 * #%L
 * COMA SceneManager Visualisation
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
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import org.openbase.bco.dal.visual.util.StatusPanel;
import org.openbase.bco.registry.location.remote.LocationRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.spatial.LocationConfigType;
import rst.spatial.LocationConfigType.LocationConfig;
import rst.spatial.LocationRegistryDataType.LocationRegistryData;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class LocationSelectorPanel extends javax.swing.JPanel {

    protected static final Logger logger = LoggerFactory.getLogger(LocationSelectorPanel.class);

    private final ObservableImpl<LocationConfigHolder> locationConfigHolderObservable;
    private boolean enableAllLocation;
    private LocationRegistryRemote locationRegistryRemote;
    private LocationConfigHolder selectedLocationConfigHolder;
    private StatusPanel statusPanel;
    private boolean init = false;

    /**
     * Creates new form LocationSelectorPanel
     *
     */
    public LocationSelectorPanel() {
        initComponents();
        setEnable(false);
        locationConfigHolderObservable = new ObservableImpl<>();
    }

    public void init(boolean enableAllLocation) throws InitializationException, InterruptedException, CouldNotPerformException {
        this.locationRegistryRemote = new LocationRegistryRemote();
        this.enableAllLocation = enableAllLocation;

        statusPanel = StatusPanel.getInstance();
        statusPanel.setStatus("Init location manager connection...", StatusPanel.StatusType.INFO, true);
        locationRegistryRemote.init();

        statusPanel.setStatus("Connecting to location manager...", StatusPanel.StatusType.INFO, true);
        locationRegistryRemote.activate();
        statusPanel.setStatus("Connection established.", StatusPanel.StatusType.INFO, 3);

        init = true;

        initDynamicComponents();
        setEnable(true);
    }

    private void setEnable(final boolean value) {
        this.setEnabled(value);
        locationComboBox.setEnabled(value);
    }

    private void initDynamicComponents() {
        locationRegistryRemote.addDataObserver(new Observer<LocationRegistryData>() {

            @Override
            public void update(final Observable<LocationRegistryData> source, LocationRegistryData data) throws Exception {
                updateDynamicComponents();
            }
        });
        updateDynamicComponents();
    }

    private void updateDynamicComponents() {
        if (!init) {
            return;
        }

        if (locationComboBox.isEnabled()) {
            if (!(locationComboBox.getSelectedItem() instanceof LocationConfigHolder)) {
                selectedLocationConfigHolder = ALL_LOCATION;
            } else {
                try {
                    selectedLocationConfigHolder = (LocationConfigHolder) locationComboBox.getSelectedItem();
                } catch (Exception ex) {
                    if (enableAllLocation) {
                        selectedLocationConfigHolder = ALL_LOCATION;
                    }
                    ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                }
            }
        } else {
            if (enableAllLocation) {
                selectedLocationConfigHolder = ALL_LOCATION;
            }
        }

        try {
            ArrayList<LocationConfigHolder> locationConfigHolderList = new ArrayList<>();
            if (enableAllLocation) {
                locationConfigHolderList.add(ALL_LOCATION);
            }
            for (LocationConfig config : locationRegistryRemote.getLocationConfigs()) {
                locationConfigHolderList.add(new LocationConfigHolder(config));
            }
            Collections.sort(locationConfigHolderList);
            locationComboBox.setModel(new DefaultComboBoxModel(locationConfigHolderList.toArray()));

            int selectedLocationIndex;
            if (!enableAllLocation && selectedLocationConfigHolder == null) {
                selectedLocationIndex = 0;
            } else {
                selectedLocationIndex = Collections.binarySearch(locationConfigHolderList, selectedLocationConfigHolder);
            }
            if (selectedLocationIndex >= 0) {
                locationComboBox.setSelectedItem(locationConfigHolderList.get(selectedLocationIndex));
            }
            locationComboBox.setEnabled(locationConfigHolderList.size() > 0);
        } catch (CouldNotPerformException ex) {
            locationComboBox.setEnabled(false);
            ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        locationComboBox = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Location"));

        locationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(locationComboBox, 0, 408, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(locationComboBox)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        //no change id the same location has been selected
        if (locationComboBox.getSelectedIndex() == -1 || (selectedLocationConfigHolder != null && selectedLocationConfigHolder.getConfig().getId().equals(((LocationConfigHolder) locationComboBox.getSelectedItem()).getConfig().getId()))) {
            return;
        }

        selectedLocationConfigHolder = (LocationConfigHolder) locationComboBox.getSelectedItem();
        try {
//            logger.info("Notify observer with new location");
            locationConfigHolderObservable.notifyObservers(selectedLocationConfigHolder);
        } catch (MultiException ex) {
            logger.warn("Could not notify observers about location config change!", ex);
        }
    }//GEN-LAST:event_locationComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox locationComboBox;
    // End of variables declaration//GEN-END:variables

    public void addObserver(Observer<LocationConfigHolder> observer) {
        locationConfigHolderObservable.addObserver(observer);
    }

    public void removeObserver(Observer<LocationConfigHolder> observer) {
        locationConfigHolderObservable.removeObserver(observer);
    }

    public void updateSelection(String locationId) {
        LocationConfigHolder select = null;
        for (int i = 0; i < locationComboBox.getItemCount(); i++) {
            if(!(locationComboBox.getItemAt(i) instanceof LocationConfigHolder)) {
                // combo box has not really been initalized yet
                return;
            }
            if (((LocationConfigHolder) locationComboBox.getItemAt(i)).getConfig().getId().equals(locationId)) {
                select = (LocationConfigHolder) locationComboBox.getItemAt(i);
            }
        }
        if (select == null) {
            return;
        }
        locationComboBox.setSelectedItem(select);
    }

    public static final LocationConfigHolder ALL_LOCATION = new LocationConfigHolder(null);

    public static class LocationConfigHolder implements Comparable<LocationConfigHolder> {

        private final LocationConfigType.LocationConfig config;

        public LocationConfigHolder(LocationConfigType.LocationConfig config) {
            this.config = config;
        }

        @Override
        public String toString() {
            if (isNotSpecified()) {
                return "All";
            }
            return config.getLabel();
        }

        public boolean isNotSpecified() {
            return config == null;
        }

        public LocationConfigType.LocationConfig getConfig() {
            return config;
        }

        @Override
        public int compareTo(LocationConfigHolder o) {
            return toString().compareTo(o.toString());
        }
    }
}
