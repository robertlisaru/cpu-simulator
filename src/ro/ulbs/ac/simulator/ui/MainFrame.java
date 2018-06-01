package ro.ulbs.ac.simulator.ui;

import ro.ulbs.ac.simulator.architecture.Architecture;
import ro.ulbs.ac.simulator.assembler.Assembler;
import ro.ulbs.ac.simulator.microprogram.ConditieSalt;
import ro.ulbs.ac.simulator.microprogram.Microinstruction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

public class MainFrame extends JFrame {
    //region variables
    private Architecture architecture = new Architecture();
    private Assembler assembler = new Assembler();
    private JDesktopPane desktopPane = new JDesktopPane();
    private RegistersPanel registersPanel = new RegistersPanel();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenu executeMenu = new JMenu("Execute");
    private JMenu viewMenu = new JMenu("View");
    private JMenuItem loadProgram = new JMenuItem("Load program");
    private JMenuItem executeMicroinstruction = new JMenuItem("Microinstruction");
    private JMenuItem viewRegisters = new JCheckBoxMenuItem("Registers");
    private JMenuItem viewCurrentMicroinstruction = new JCheckBoxMenuItem("Current microinstruction");
    private JTabbedPane tabbedPane = new JTabbedPane();
    private DiagramPanel diagramPanel = new DiagramPanel();
    private MicroprogramPanel microprogramPanel = new MicroprogramPanel();
    private CodeMemoryPanel codeMemoryPanel = new CodeMemoryPanel();
    private DataMemoryPanel dataMemoryPanel = new DataMemoryPanel();
    private CurrentMicroinstructionPanel currentMicroinstructionPanel = new CurrentMicroinstructionPanel();
    //endregion

    public MainFrame() {
        setTitle("Simulator Didactic");
        ImageIcon icon = new ImageIcon("./cpu.png");
        setIconImage(icon.getImage());
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null); //screen center

        //region menu bar
        fileMenu.setMnemonic(KeyEvent.VK_F);
        executeMenu.setMnemonic(KeyEvent.VK_E);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        executeMicroinstruction.setMnemonic(KeyEvent.VK_M);
        executeMicroinstruction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                architecture.executeOneMicroinstruction();
                registersPanel.updateRegistersTableModel();
                currentMicroinstructionPanel.updateCurrentMicroinstructionTableModel();
            }
        });
        executeMicroinstruction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        loadProgram.setMnemonic(KeyEvent.VK_L);
        loadProgram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File programFile;
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Assembly (.asm)", "asm");
                chooser.setFileFilter(filter);
                int option = chooser.showOpenDialog(MainFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    programFile = chooser.getSelectedFile();
                    try {
                        assembler.readOpcodesFromFile(new File("opcodes.txt"));
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    assembler.setAsmFile(programFile);
                    try {
                        assembler.parseFile();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (assembler.getErrorList().isEmpty()) {
                        try {
                            assembler.makeBin();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        architecture.loadCode(assembler.getCode());
                        architecture.loadData(assembler.getData());

                    } else {
                        //print error list
                    }

                }
            }
        });
        viewRegisters.setMnemonic(KeyEvent.VK_R);
        viewRegisters.setSelected(true);
        viewRegisters.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    registersPanel.setVisible(true);
                } else {
                    registersPanel.setVisible(false);
                }
            }
        });
        viewCurrentMicroinstruction.setMnemonic(KeyEvent.VK_M);
        viewCurrentMicroinstruction.setSelected(true);
        viewCurrentMicroinstruction.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    currentMicroinstructionPanel.setVisible(true);
                } else {
                    currentMicroinstructionPanel.setVisible(false);
                }
            }
        });

        fileMenu.add(loadProgram);
        executeMenu.add(executeMicroinstruction);
        viewMenu.add(viewRegisters);
        viewMenu.add(viewCurrentMicroinstruction);
        menuBar.add(fileMenu);
        menuBar.add(executeMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
        //endregion

        tabbedPane.add("Diagram", diagramPanel);
        tabbedPane.add("Microprogram", microprogramPanel);
        tabbedPane.add("Code memory", codeMemoryPanel);
        tabbedPane.add("Data memory", dataMemoryPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(registersPanel, BorderLayout.EAST);
        add(currentMicroinstructionPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        MainFrame mainFrame = new MainFrame();
    }

    private class RegistersPanel extends JPanel {
        private JScrollPane registersScrollPane;
        private JTable registersTable;
        private DefaultTableModel registersTableModel;

        public RegistersPanel() {
            setBorder(BorderFactory.createTitledBorder("Registers"));

            registersTableModel = new DefaultTableModel() {
                String columnNames[] = {"Register", "Data"};

                @Override
                public int getColumnCount() {
                    return columnNames.length;
                }

                @Override
                public String getColumnName(int index) {
                    return columnNames[index];
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            updateRegistersTableModel();

            registersTable = new JTable(registersTableModel);

            registersScrollPane = new JScrollPane(registersTable);
            registersScrollPane.setPreferredSize(new Dimension(200, 285));

            setLayout(new BorderLayout());
            add(registersScrollPane, BorderLayout.CENTER);
        }

        public void updateRegistersTableModel() {
            registersTableModel.getDataVector().removeAllElements();
            Vector row = new Vector();
            for (int i = 0; i < architecture.getRegisterFile().length; i++) {
                row = new Vector();
                row.add("r" + i);
                row.add(architecture.getRegisterFile()[i]);
                registersTableModel.addRow(row);
            }
            row = new Vector();
            row.add("SP");
            row.add(architecture.getSP());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("T");
            row.add(architecture.getT());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("PC");
            row.add(architecture.getPC());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("IR");
            row.add(architecture.getIR());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("IVR");
            row.add(architecture.getIVR());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("ADR");
            row.add(architecture.getADR());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("MDR");
            row.add(architecture.getMDR());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("Z flag");
            row.add(architecture.getFlag().getZ());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("S flag");
            row.add(architecture.getFlag().getS());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("C flag");
            row.add(architecture.getFlag().getC());
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("V flag");
            row.add(architecture.getFlag().getV());
            registersTableModel.addRow(row);
        }
    }

    private class DiagramPanel extends JPanel {
        public DiagramPanel() {

        }
    }

    private class MicroprogramPanel extends JPanel {
        private JScrollPane microprogramScrollPane;
        private JTable microprogramTable;
        private DefaultTableModel microprogramTableModel;
        private int selectedRowNr = 0;

        public MicroprogramPanel() {
            microprogramTableModel = new DefaultTableModel() {
                String columnNames[] = {"Address", "Label", "Sursa SBUS", "Sursa DBUS", "ALU", "Sursa RBUS",
                        "Dest. RBUS", "MEM", "Other", "Condition", "Index", "Eval. mode", "Jump address"};

                @Override
                public int getColumnCount() {
                    return columnNames.length;
                }

                @Override
                public String getColumnName(int index) {
                    return columnNames[index];
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            java.util.List<Microinstruction> microinstructionList =
                    architecture.getMicroprogramMemory().getMicroinstructionList();
            Map<String, Short> labels = architecture.getMicroprogramParser().getLabels();

            for (Integer i = 0; i < microinstructionList.size(); i++) {
                Vector row = new Vector();
                row.add(i);
                for (Map.Entry<String, Short> entry : labels.entrySet()) {
                    if (entry.getValue() == i.shortValue()) {
                        row.add(entry.getKey());
                        break;
                    }
                }
                if (row.size() == 1) {
                    row.add("");
                }

                row.add(microinstructionList.get(i).getSursaSBUS().name());
                row.add(microinstructionList.get(i).getSursaDBUS().name());
                row.add(microinstructionList.get(i).getOperatieALU().name());
                row.add(microinstructionList.get(i).getSursaRBUS().name());
                row.add(microinstructionList.get(i).getDestinatieRBUS().name());
                row.add(microinstructionList.get(i).getOperatieMemorie().name());
                row.add(microinstructionList.get(i).getOtherOperation().name());
                row.add(microinstructionList.get(i).getConditieSalt().name());
                row.add(microinstructionList.get(i).getIndexSalt().name());
                if (microinstructionList.get(i).getConditieSalt() != ConditieSalt.NONE) {
                    if (microinstructionList.get(i).isJumpOnConditionEqualsFalse()) {
                        row.add("Jump on false");
                    } else {
                        row.add("Jump on true");
                    }
                    for (Map.Entry<String, Short> entry : labels.entrySet()) {
                        if (entry.getValue() == microinstructionList.get(i).getMicroadresaSalt()) {
                            row.add(entry.getKey());
                            break;
                        }
                    }
                } else {
                    row.add("NONE");
                    row.add("NONE");
                }

                microprogramTableModel.addRow(row);
            }

            microprogramTable = new JTable(microprogramTableModel);
            microprogramTable.setFocusable(false);
            microprogramScrollPane = new JScrollPane(microprogramTable);

            setLayout(new BorderLayout());
            add(microprogramScrollPane, BorderLayout.CENTER);
        }

        public void setSelectedRow(int rowNr) {
            microprogramTable.setRowSelectionInterval(rowNr, rowNr);
        }

        public DefaultTableModel getMicroprogramTableModel() {
            return microprogramTableModel;
        }
    }

    private class CodeMemoryPanel extends JPanel {
        public CodeMemoryPanel() {
        }
    }

    private class DataMemoryPanel extends JPanel {
        public DataMemoryPanel() {
        }
    }

    private class CurrentMicroinstructionPanel extends JPanel {
        private JScrollPane currentMicroinstructionScrollPane;
        private JTable currentMicroinstructionTable;
        private DefaultTableModel currentMicroinstructionTableModel;

        public CurrentMicroinstructionPanel() {
            setBorder(BorderFactory.createTitledBorder("Current microinstruction"));
            currentMicroinstructionTableModel = new DefaultTableModel() {
                String columnNames[] = {"Address", "Label", "Sursa SBUS", "Sursa DBUS", "ALU", "Sursa RBUS",
                        "Dest. RBUS", "MEM", "Other", "Condition", "Index", "Eval. mode", "Jump address"};

                @Override
                public int getColumnCount() {
                    return columnNames.length;
                }

                @Override
                public String getColumnName(int index) {
                    return columnNames[index];
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            updateCurrentMicroinstructionTableModel();
            currentMicroinstructionTable = new JTable(currentMicroinstructionTableModel);
            currentMicroinstructionScrollPane = new JScrollPane(currentMicroinstructionTable);
            currentMicroinstructionTable.setPreferredScrollableViewportSize(
                    new Dimension(currentMicroinstructionTable.getPreferredSize().width,
                            currentMicroinstructionTable.getRowHeight()));
            setLayout(new BorderLayout());
            currentMicroinstructionTable.setFocusable(false);
            currentMicroinstructionTable.setRowSelectionAllowed(false);
            add(currentMicroinstructionScrollPane, BorderLayout.CENTER);
        }

        private void updateCurrentMicroinstructionTableModel() {
            currentMicroinstructionTableModel.getDataVector().removeAllElements();
            currentMicroinstructionTableModel.addRow((Vector) microprogramPanel.getMicroprogramTableModel()
                    .getDataVector().elementAt(architecture.getMAR()));
            microprogramPanel.setSelectedRow(architecture.getMAR());
        }
    }
}
