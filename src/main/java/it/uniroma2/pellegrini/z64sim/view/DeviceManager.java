/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.devices.Dmac;
import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Modal dialog for managing device bindings: adding/removing devices,
 * assigning IVNs and I/O port addresses, and setting daisy-chain order
 * via drag-and-drop row reordering.
 */
public class DeviceManager extends JDialog {
    private static final Logger log = LoggerFactory.getLogger();

    private static final String DEVICES_PACKAGE = "it.uniroma2.pellegrini.z64sim.devices";
    private static final String DEVICES_PACKAGE_PATH = "it/uniroma2/pellegrini/z64sim/devices";

    // ---- Form bindings (managed by IntelliJ IDEA GUI Designer) ----
    private JPanel contentPane;
    private JPanel deviceManagerPanel;
    private JPanel deviceOrderPanel;
    private JPanel deviceIoBindingPanel;
    private JLabel addDeviceLabel;
    private JComboBox<String> deviceEnumeration;
    private JButton addButton;
    private JButton trashButton;
    private JTable ioPortTable;
    private JTable devicesTable;
    private JButton confirmButton;
    private JButton cancelButton;
    private JButton interfaceButton;

    // ---- Internal state ----
    private final List<Class<? extends Device>> discoveredClasses = new ArrayList<>();
    private DevicesTableModel devicesTableModel;
    private IoPortTableModel ioPortTableModel;

    public DeviceManager() {
        setContentPane(contentPane);
        setModal(true);
        setPreferredSize(new Dimension(600, 400));
        getRootPane().setDefaultButton(confirmButton);

        addDeviceLabel.setText(PropertyBroker.getMessageFromBundle("devmgr.add.device"));

        discoverDevices();
        setupTableModels();
        setupDragAndDrop();
        setupListeners();
        loadCurrentConfig();
    }

    // ========================================================================
    // Device Discovery (JAR-safe)
    // ========================================================================

    private void discoverDevices() {
        try {
            ClassLoader cl = getClass().getClassLoader();
            Enumeration<URL> resources = cl.getResources(DEVICES_PACKAGE_PATH);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    discoverFromDirectory(new File(url.toURI()));
                } else if ("jar".equals(protocol)) {
                    discoverFromJar(url);
                }
            }
        } catch (Exception e) {
            log.error("Failed to discover devices: " + e.getMessage());
        }

        // Also scan the current working directory for external device classes
        discoverFromWorkingDirectory();

        // Sort alphabetically and populate combo box
        discoveredClasses.sort(new Comparator<Class<? extends Device>>() {
            @Override
            public int compare(Class<? extends Device> a, Class<? extends Device> b) {
                return a.getSimpleName().compareTo(b.getSimpleName());
            }
        });
        for (Class<? extends Device> cls : discoveredClasses) {
            deviceEnumeration.addItem(cls.getSimpleName());
        }
    }

    private void discoverFromDirectory(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".class")) continue;
            String className = DEVICES_PACKAGE + "."
                    + file.getName().substring(0, file.getName().length() - 6);
            tryAddDeviceClass(className);
        }
    }

    private void discoverFromJar(URL jarUrl) {
        try {
            // jar:file:/path/to/app.jar!/it/uniroma2/...
            String jarPath = jarUrl.getPath();
            jarPath = jarPath.substring(5, jarPath.indexOf("!"));
            JarFile jar = new JarFile(jarPath);

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(DEVICES_PACKAGE_PATH + "/")
                        && name.endsWith(".class")
                        && !name.contains("$")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    tryAddDeviceClass(className);
                }
            }
            jar.close();
        } catch (Exception e) {
            log.error("Failed to scan JAR for devices: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void tryAddDeviceClass(String className) {
        tryAddDeviceClass(className, null);
    }

    @SuppressWarnings("unchecked")
    private void tryAddDeviceClass(String className, ClassLoader loader) {
        try {
            Class<?> cls;
            if (loader != null) {
                cls = Class.forName(className, true, loader);
            } else {
                cls = Class.forName(className);
            }
            if (Device.class.isAssignableFrom(cls)
                    && !Modifier.isAbstract(cls.getModifiers())
                    && !cls.isInterface()
                    && !discoveredClasses.contains(cls)) {
                discoveredClasses.add((Class<? extends Device>) cls);
            }
        } catch (ClassNotFoundException e) {
            log.error("Could not load class: " + className);
        }
    }

    /**
     * Scan the current working directory for device classes.
     * Looks for .class files under ./it/uniroma2/pellegrini/z64sim/devices/
     * so users can drop compiled device classes next to the JAR.
     */
    @SuppressWarnings("unchecked")
    private void discoverFromWorkingDirectory() {
        try {
            File cwd = new File(System.getProperty("user.dir"));
            File devicesDir = new File(cwd, DEVICES_PACKAGE_PATH);
            if (!devicesDir.isDirectory()) return;

            // Create a classloader rooted at CWD so it can resolve the package
            URLClassLoader cwdLoader = new URLClassLoader(
                    new URL[]{cwd.toURI().toURL()},
                    getClass().getClassLoader());

            File[] files = devicesDir.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (!file.getName().endsWith(".class") || file.getName().contains("$")) continue;
                String className = DEVICES_PACKAGE + "."
                        + file.getName().substring(0, file.getName().length() - 6);
                tryAddDeviceClass(className, cwdLoader);
            }
        } catch (Exception e) {
            log.error("Failed to discover devices from working directory: " + e.getMessage());
        }
    }

    // ========================================================================
    // Table Models
    // ========================================================================

    private void setupTableModels() {
        devicesTableModel = new DevicesTableModel();
        ioPortTable.setModel(devicesTableModel);
        ioPortTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ioPortTableModel = new IoPortTableModel();
        devicesTable.setModel(ioPortTableModel);
    }

    // ---- DeviceEntry: one row in devicesTable ----

    private static class DeviceEntry {
        Device device;
        int ivn;
        boolean removable;
        /**
         * element name --> I/O port address (-1 = unassigned)
         */
        Map<String, Long> portAssignments;

        DeviceEntry(Device device, int ivn, boolean removable) {
            this.device = device;
            this.ivn = ivn;
            this.removable = removable;
            this.portAssignments = new HashMap<>();
            // Initialize all elements as unassigned
            for (IoPortDescriptor desc : device.getDescriptor().getIoPorts()) {
                portAssignments.put(desc.getName(), -1L);
            }
        }

        DeviceEntry(Device device, int ivn) {
            this(device, ivn, true);
        }
    }

    // ---- DevicesTableModel ----

    private class DevicesTableModel extends AbstractTableModel {
        private final List<DeviceEntry> entries = new ArrayList<>();
        private static final int COL_ORDER = 0;
        private static final int COL_NAME = 1;
        private static final int COL_IVN = 2;

        void addEntry(DeviceEntry entry) {
            entries.add(entry);
            fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
        }

        void removeEntry(int row) {
            entries.remove(row);
            fireTableRowsDeleted(row, row);
        }

        DeviceEntry getEntry(int row) {
            return entries.get(row);
        }

        List<DeviceEntry> getEntries() {
            return entries;
        }

        void moveRow(int fromIndex, int toIndex) {
            if (fromIndex == toIndex) return;
            DeviceEntry entry = entries.remove(fromIndex);
            entries.add(toIndex, entry);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case COL_ORDER:
                    return PropertyBroker.getMessageFromBundle("devmgr.col.order");
                case COL_NAME:
                    return PropertyBroker.getMessageFromBundle("ivt.table.device");
                case COL_IVN:
                    return PropertyBroker.getMessageFromBundle("ivt.table.ivn");
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == COL_IVN) return Integer.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == COL_IVN) {
                DeviceEntry entry = entries.get(row);
                // DMAC (non-removable) has IVN=0 hardwired, not editable
                if (!entry.removable) return false;
                // Only interrupt-capable devices have editable IVN
                return entry.device.getDescriptor().isInterruptsEnabled();
            }
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            DeviceEntry e = entries.get(row);
            switch (col) {
                case COL_ORDER:
                    return String.valueOf(row + 1);
                case COL_NAME:
                    return e.device.getDescriptor().getDeviceName();
                case COL_IVN:
                    return e.ivn < 0 ? "—" : e.ivn;
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != COL_IVN) return;
            int newIvn;
            try {
                newIvn = Integer.parseInt(value.toString().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(DeviceManager.this,
                        PropertyBroker.getMessageFromBundle("devmgr.ivn.invalid"),
                        PropertyBroker.getMessageFromBundle("devmgr.ivn.invalid.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newIvn < 0 || newIvn >= Devices.IVT_ENTRIES) {
                JOptionPane.showMessageDialog(DeviceManager.this,
                        PropertyBroker.getMessageFromBundle("devmgr.ivn.invalid"),
                        PropertyBroker.getMessageFromBundle("devmgr.ivn.invalid.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Check for conflicts (only among interrupt-capable devices)
            for (int i = 0; i < entries.size(); i++) {
                if (i != row && entries.get(i).ivn >= 0 && entries.get(i).ivn == newIvn) {
                    JOptionPane.showMessageDialog(DeviceManager.this,
                            PropertyBroker.getMessageFromBundle("devmgr.ivn.conflict",
                                    newIvn, entries.get(i).device.getDescriptor().getDeviceName()),
                            PropertyBroker.getMessageFromBundle("devmgr.ivn.conflict.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            entries.get(row).ivn = newIvn;
            fireTableCellUpdated(row, col);
        }
    }

    // ---- IoPortTableModel ----

    private class IoPortTableModel extends AbstractTableModel {
        private DeviceEntry currentEntry;
        private List<IoPortDescriptor> ports = new ArrayList<>();
        private static final int COL_NAME = 0;
        private static final int COL_TYPE = 1;
        private static final int COL_FLAGS = 2;
        private static final int COL_PORT = 3;

        void showDevice(DeviceEntry entry) {
            this.currentEntry = entry;
            if (entry != null) {
                this.ports = entry.device.getDescriptor().getIoPorts();
            } else {
                this.ports = new ArrayList<>();
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return ports.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case COL_NAME:
                    return PropertyBroker.getMessageFromBundle("devmgr.col.element");
                case COL_TYPE:
                    return PropertyBroker.getMessageFromBundle("devmgr.col.type");
                case COL_FLAGS:
                    return PropertyBroker.getMessageFromBundle("devmgr.col.access");
                case COL_PORT:
                    return PropertyBroker.getMessageFromBundle("devmgr.col.port.address");
                default:
                    return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // DMAC ports are hardwired and not editable
            if (currentEntry != null && !currentEntry.removable) return false;
            return col == COL_PORT;
        }

        @Override
        public Object getValueAt(int row, int col) {
            IoPortDescriptor desc = ports.get(row);
            switch (col) {
                case COL_NAME:
                    return desc.getName();
                case COL_TYPE:
                    return desc.isFlipFlop() ? "FLIP_FLOP" : "REGISTER";
                case COL_FLAGS: {
                    boolean r = desc.isReadable();
                    boolean w = desc.isWritable();
                    if (r && w) return "RW";
                    if (r) return "R";
                    if (w) return "W";
                    return "-";
                }
                case COL_PORT: {
                    if (currentEntry == null) return "";
                    Long addr = currentEntry.portAssignments.get(desc.getName());
                    if (addr == null || addr < 0) return "";
                    return "0x" + Long.toHexString(addr).toUpperCase();
                }
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != COL_PORT || currentEntry == null) return;

            String text = value.toString().trim();
            IoPortDescriptor desc = ports.get(row);

            if (text.isEmpty()) {
                // Clear assignment
                currentEntry.portAssignments.put(desc.getName(), -1L);
                fireTableCellUpdated(row, col);
                return;
            }

            long addr;
            try {
                if (text.startsWith("0x") || text.startsWith("0X")) {
                    addr = Long.parseLong(text.substring(2), 16);
                } else {
                    addr = Long.parseLong(text);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(DeviceManager.this,
                        PropertyBroker.getMessageFromBundle("devmgr.port.invalid"),
                        PropertyBroker.getMessageFromBundle("devmgr.port.invalid.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check for conflicts across ALL devices
            for (DeviceEntry entry : devicesTableModel.getEntries()) {
                for (Map.Entry<String, Long> pa : entry.portAssignments.entrySet()) {
                    if (pa.getValue() == addr) {
                        // Allow if it's the same element on the same device
                        if (entry == currentEntry && pa.getKey().equals(desc.getName())) {
                            continue;
                        }
                        JOptionPane.showMessageDialog(DeviceManager.this,
                                PropertyBroker.getMessageFromBundle("devmgr.port.conflict",
                                        Long.toHexString(addr).toUpperCase(),
                                        entry.device.getDescriptor().getDeviceName(),
                                        pa.getKey()),
                                PropertyBroker.getMessageFromBundle("devmgr.port.conflict.title"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            currentEntry.portAssignments.put(desc.getName(), addr);
            fireTableCellUpdated(row, col);
        }
    }

    // ========================================================================
    // Drag-and-Drop Row Reordering
    // ========================================================================

    private void setupDragAndDrop() {
        ioPortTable.setDragEnabled(true);
        ioPortTable.setDropMode(DropMode.INSERT_ROWS);
        ioPortTable.setTransferHandler(new RowTransferHandler());
    }

    private class RowTransferHandler extends TransferHandler {
        private int draggedRow = -1;

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            draggedRow = ioPortTable.getSelectedRow();
            // Prevent dragging the DMAC row (non-removable)
            if (draggedRow >= 0 && draggedRow < devicesTableModel.getRowCount()
                    && !devicesTableModel.getEntry(draggedRow).removable) {
                return null;
            }
            return new StringSelection(String.valueOf(draggedRow));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop()
                    && support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;

            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int dropRow = dl.getRow();

            if (draggedRow < 0 || draggedRow >= devicesTableModel.getRowCount()) return false;
            if (dropRow > draggedRow) dropRow--;
            if (dropRow < 0) dropRow = 0;
            if (dropRow > devicesTableModel.getRowCount() - 1)
                dropRow = devicesTableModel.getRowCount() - 1;

            devicesTableModel.moveRow(draggedRow, dropRow);
            ioPortTable.setRowSelectionInterval(dropRow, dropRow);
            return true;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            draggedRow = -1;
        }
    }

    // ========================================================================
    // Listeners
    // ========================================================================

    private void setupListeners() {
        // Add button: instantiate selected device, add to table
        addButton.addActionListener(e -> onAdd());

        // Trash button: remove selected device from table
        trashButton.addActionListener(e -> onTrash());

        // Confirm button: validate and apply configuration
        confirmButton.addActionListener(e -> onConfirm());

        // Cancel button: close without saving
        cancelButton.addActionListener(e -> onCancel());

        // Selection listener: show I/O ports for selected device
        ioPortTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = ioPortTable.getSelectedRow();
            if (row >= 0 && row < devicesTableModel.getRowCount()) {
                ioPortTableModel.showDevice(devicesTableModel.getEntry(row));
            } else {
                ioPortTableModel.showDevice(null);
            }
        });

        // Interface button: show schematic for selected device
        interfaceButton.addActionListener(e -> {
            int row = ioPortTable.getSelectedRow();
            if (row < 0 || row >= devicesTableModel.getRowCount()) {
                JOptionPane.showMessageDialog(this,
                        PropertyBroker.getMessageFromBundle("devmgr.select.device"),
                        PropertyBroker.getMessageFromBundle("devmgr.interface.title"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DeviceEntry entry = devicesTableModel.getEntry(row);
            DeviceDescriptor desc = entry.device.getDescriptor();
            InterfaceDialog dialog = new InterfaceDialog(this, desc);
            dialog.setVisible(true);
        });

        // Window close = cancel
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // Escape = cancel
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    // ========================================================================
    // Button Actions
    // ========================================================================

    private void onAdd() {
        int selected = deviceEnumeration.getSelectedIndex();
        if (selected < 0 || selected >= discoveredClasses.size()) return;

        Class<? extends Device> cls = discoveredClasses.get(selected);
        Device device;
        try {
            device = cls.newInstance();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    PropertyBroker.getMessageFromBundle("devmgr.instantiate.failed", cls.getSimpleName(), ex.getMessage()),
                    PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Assign IVN only for interrupt-capable devices
        int defaultIvn = -1;
        if (device.getDescriptor().isInterruptsEnabled()) {
            Set<Integer> usedIvns = new HashSet<>();
            for (DeviceEntry entry : devicesTableModel.getEntries()) {
                if (entry.ivn >= 0) usedIvns.add(entry.ivn);
            }
            defaultIvn = 0;
            while (usedIvns.contains(defaultIvn) && defaultIvn < Devices.IVT_ENTRIES) {
                defaultIvn++;
            }
            if (defaultIvn >= Devices.IVT_ENTRIES) {
                JOptionPane.showMessageDialog(this,
                        PropertyBroker.getMessageFromBundle("devmgr.ivn.slots.full"),
                        PropertyBroker.getMessageFromBundle("devmgr.ivn.slots.full.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        devicesTableModel.addEntry(new DeviceEntry(device, defaultIvn));
    }

    private void onTrash() {
        int row = ioPortTable.getSelectedRow();
        if (row < 0) return;

        // Cannot remove the DMAC (non-removable)
        if (!devicesTableModel.getEntry(row).removable) {
            JOptionPane.showMessageDialog(this,
                    PropertyBroker.getMessageFromBundle("devmgr.cannot.remove"),
                    PropertyBroker.getMessageFromBundle("devmgr.cannot.remove.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        devicesTableModel.removeEntry(row);
        ioPortTableModel.showDevice(null);
    }

    private void onConfirm() {
        List<DeviceEntry> entries = devicesTableModel.getEntries();

        // Validate: check for duplicate IVNs (only among interrupt-capable devices)
        Set<Integer> ivnSet = new HashSet<>();
        for (DeviceEntry entry : entries) {
            if (entry.ivn < 0) continue;  // non-interrupt device, no IVN
            if (!ivnSet.add(entry.ivn)) {
                JOptionPane.showMessageDialog(this,
                        PropertyBroker.getMessageFromBundle("devmgr.duplicate.ivn", entry.ivn),
                        PropertyBroker.getMessageFromBundle("devmgr.validation.error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Validate: check for duplicate port addresses (across all devices)
        // and validate that user ports don't fall in DMAC reserved range
        Map<Long, String> portOwners = new HashMap<>();
        for (DeviceEntry entry : entries) {
            String devName = entry.device.getDescriptor().getDeviceName();
            for (Map.Entry<String, Long> pa : entry.portAssignments.entrySet()) {
                long addr = pa.getValue();
                if (addr < 0) continue;  // unassigned

                // Check reserved DMAC port range for user devices
                if (entry.removable && addr >= Dmac.PORT_BASE && addr < Dmac.PORT_END) {
                    JOptionPane.showMessageDialog(this,
                            PropertyBroker.getMessageFromBundle("devmgr.port.reserved",
                                    Long.toHexString(addr).toUpperCase()),
                            PropertyBroker.getMessageFromBundle("devmgr.validation.error.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String owner = portOwners.get(addr);
                if (owner != null) {
                    JOptionPane.showMessageDialog(this,
                            PropertyBroker.getMessageFromBundle("devmgr.port.assigned.both",
                                    Long.toHexString(addr).toUpperCase(),
                                    owner, devName, pa.getKey()),
                            PropertyBroker.getMessageFromBundle("devmgr.validation.error.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                portOwners.put(addr, devName + " --> " + pa.getKey());
            }
        }

        // Apply: clear old config and rebuild
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        for (DeviceEntry entry : entries) {
            // Skip the DMAC — it is permanent and already registered
            if (!entry.removable) continue;

            DeviceMapping mapping = new DeviceMapping(entry.device, entry.ivn);

            // Assign port bindings
            for (IoPortDescriptor desc : entry.device.getDescriptor().getIoPorts()) {
                Long addr = entry.portAssignments.get(desc.getName());
                if (addr != null && addr >= 0) {
                    mapping.assignPort(addr, desc);
                    devices.registerPort(addr, mapping);
                }
            }

            devices.registerDevice(entry.ivn, mapping);
        }

        // Save configuration to settings (exclude DMAC — it's permanent)
        List<SettingsController.DeviceConfig> configs = new ArrayList<>();
        for (DeviceEntry entry : entries) {
            if (!entry.removable) continue;
            configs.add(new SettingsController.DeviceConfig(
                    entry.device.getClass().getName(),
                    entry.ivn,
                    entry.portAssignments));
        }
        SettingsController.setDeviceConfigs(configs);
        SettingsController.persist();

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    // ========================================================================
    // Load Previous Configuration
    // ========================================================================

    private void loadCurrentConfig() {
        Devices devices = Devices.getInstance();
        List<DeviceMapping> ordered = devices.getAllDevicesOrdered();

        for (DeviceMapping mapping : ordered) {
            boolean removable = !(mapping.getDevice() instanceof Dmac);
            DeviceEntry entry = new DeviceEntry(mapping.getDevice(), mapping.getIvn(), removable);

            // Invert port bindings: port address --> element name --> element name --> port address
            Map<Long, IoPortDescriptor> bindings = mapping.getPortBindings();
            for (Map.Entry<Long, IoPortDescriptor> binding : bindings.entrySet()) {
                entry.portAssignments.put(binding.getValue().getName(), binding.getKey());
            }

            devicesTableModel.addEntry(entry);
        }
    }

    // ========================================================================
    // Generated code (IntelliJ IDEA GUI Designer) — DO NOT EDIT
    // ========================================================================

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        deviceManagerPanel = new JPanel();
        deviceManagerPanel.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(deviceManagerPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addDeviceLabel = new JLabel();
        this.$$$loadLabelText$$$(addDeviceLabel, this.$$$getMessageFromBundle$$$("i18n", "ivt.table.device"));
        deviceManagerPanel.add(addDeviceLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deviceEnumeration = new JComboBox();
        deviceManagerPanel.add(deviceEnumeration, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setIcon(new ImageIcon(getClass().getResource("/images/plus.png")));
        addButton.setText("");
        deviceManagerPanel.add(addButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        trashButton = new JButton();
        trashButton.setIcon(new ImageIcon(getClass().getResource("/images/trash.png")));
        trashButton.setText("");
        deviceManagerPanel.add(trashButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        interfaceButton = new JButton();
        interfaceButton.setIcon(new ImageIcon(getClass().getResource("/images/interface.png")));
        interfaceButton.setText("");
        deviceManagerPanel.add(interfaceButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deviceOrderPanel = new JPanel();
        deviceOrderPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(deviceOrderPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        deviceOrderPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        devicesTable = new JTable();
        scrollPane1.setViewportView(devicesTable);
        deviceIoBindingPanel = new JPanel();
        deviceIoBindingPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(deviceIoBindingPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        deviceIoBindingPanel.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ioPortTable = new JTable();
        scrollPane2.setViewportView(ioPortTable);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        confirmButton = new JButton();
        this.$$$loadButtonText$$$(confirmButton, this.$$$getMessageFromBundle$$$("i18n", "button.ok"));
        panel1.add(confirmButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        this.$$$loadButtonText$$$(cancelButton, this.$$$getMessageFromBundle$$$("i18n", "button.cancel"));
        panel1.add(cancelButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    /**
     * @noinspection ALL
     */
    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
