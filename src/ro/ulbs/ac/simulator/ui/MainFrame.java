package ro.ulbs.ac.simulator.ui;

import ro.ulbs.ac.simulator.architecture.Architecture;
import ro.ulbs.ac.simulator.assembler.Assembler;
import ro.ulbs.ac.simulator.assembler.Error;
import ro.ulbs.ac.simulator.microprogram.ConditieSalt;
import ro.ulbs.ac.simulator.microprogram.Microinstruction;
import ro.ulbs.ac.simulator.microprogram.OperatieMemorie;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;

public class MainFrame extends JFrame {
    //region variables
    private Architecture architecture = new Architecture();
    private Assembler assembler;
    private JDesktopPane desktopPane = new JDesktopPane();
    private RegistersPanel registersPanel = new RegistersPanel();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenu executeMenu = new JMenu("Execute");
    private JMenu viewMenu = new JMenu("View");
    private JMenuItem loadProgram = new JMenuItem("Load program");
    private JMenuItem exitSimulator = new JMenuItem("Exit");
    private JMenuItem runMicroinstruction = new JMenuItem("Microinstruction");
    private JMenuItem runProgram = new JMenuItem("Run program");
    private JMenuItem restartProgram = new JMenuItem("Restart program");
    private JMenuItem viewRegisters = new JCheckBoxMenuItem("Registers");
    private JMenuItem viewCurrentMicroinstruction = new JCheckBoxMenuItem("Current microinstruction");
    private JTabbedPane tabbedPane = new JTabbedPane();
    private DiagramPanel diagramPanel = new DiagramPanel();
    private MicroprogramPanel microprogramPanel = new MicroprogramPanel();
    private CodeMemoryPanel codeMemoryPanel = new CodeMemoryPanel();
    private DataMemoryPanel dataMemoryPanel = new DataMemoryPanel();
    private CurrentMicroinstructionPanel currentMicroinstructionPanel = new CurrentMicroinstructionPanel();
    private ErrorsPanel errorsPanel = new ErrorsPanel();
    private SouthPanel southPanel = new SouthPanel();
    private JFileChooser fileChooser = new JFileChooser();
    //endregion

    public MainFrame() {
        setTitle("Simulator Didactic");
        ImageIcon icon = new ImageIcon("./cpu.png");
        setIconImage(icon.getImage());
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null); //screen center
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        //region menu bar
        fileMenu.setMnemonic(KeyEvent.VK_F);
        executeMenu.setMnemonic(KeyEvent.VK_E);
        viewMenu.setMnemonic(KeyEvent.VK_V);
        runMicroinstruction.setMnemonic(KeyEvent.VK_M);
        runMicroinstruction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean dataMemoryChanged = false;
                if (architecture.getMIR().getOperatieMemorie() == OperatieMemorie.WRITE) {
                    dataMemoryChanged = true;
                }
                architecture.executeOneMicroinstruction();
                registersPanel.updateRegistersTableModel();
                currentMicroinstructionPanel.updateCurrentMicroinstructionTableModel();
                diagramPanel.repaint();
                if (dataMemoryChanged) {
                    dataMemoryPanel.updateDataMemoryTable();
                }
            }
        });
        runMicroinstruction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        runMicroinstruction.setEnabled(false);
        runProgram.setMnemonic(KeyEvent.VK_R);
        runProgram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                architecture.executeAll();
                registersPanel.updateRegistersTableModel();
                currentMicroinstructionPanel.updateCurrentMicroinstructionTableModel();
                dataMemoryPanel.updateDataMemoryTable();
            }
        });
        runProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        runProgram.setEnabled(false);
        restartProgram.setMnemonic(KeyEvent.VK_T);
        restartProgram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                architecture = new Architecture();
                architecture.loadCode(assembler.getCode());
                architecture.loadData(assembler.getData());
                codeMemoryPanel.updateCodeMemoryTable();
                dataMemoryPanel.updateDataMemoryTable();
                currentMicroinstructionPanel.updateCurrentMicroinstructionTableModel();
                registersPanel.updateRegistersTableModel();
            }
        });
        restartProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        restartProgram.setEnabled(false);
        exitSimulator.setMnemonic(KeyEvent.VK_X);
        exitSimulator.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        loadProgram.setMnemonic(KeyEvent.VK_L);
        loadProgram.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File programFile;
                fileChooser.setMultiSelectionEnabled(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Assembly (.asm)", "asm");
                fileChooser.setFileFilter(filter);
                int option = fileChooser.showOpenDialog(MainFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    programFile = fileChooser.getSelectedFile();
                    try {
                        assembler = new Assembler();
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
                        architecture = new Architecture();
                        architecture.loadCode(assembler.getCode());
                        codeMemoryPanel.updateCodeMemoryTable();
                        architecture.loadData(assembler.getData());
                        dataMemoryPanel.updateDataMemoryTable();
                        currentMicroinstructionPanel.updateCurrentMicroinstructionTableModel();
                        runProgram.setEnabled(true);
                        runMicroinstruction.setEnabled(true);
                        restartProgram.setEnabled(true);
                        registersPanel.updateRegistersTableModel();
                        errorsPanel.setVisible(false);

                    } else {
                        errorsPanel.updateErrorsTable();
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
        fileMenu.add(exitSimulator);
        executeMenu.add(runMicroinstruction);
        executeMenu.add(runProgram);
        executeMenu.add(restartProgram);
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
        add(southPanel, BorderLayout.SOUTH);

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
                row.add("R" + i);
                row.add(String.format("%04X", architecture.getRegisterFile()[i] & 0xffff));
                registersTableModel.addRow(row);
            }
            row = new Vector();
            row.add("SP");
            row.add(String.format("%04X", architecture.getSP() & 0xffff));
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("T");
            row.add(String.format("%04X", architecture.getT() & 0xffff));
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("PC");
            row.add(String.format("%04X", architecture.getPC() & 0xffff));
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("IR");
            row.add(String.format("%04X", architecture.getIR() & 0xffff));
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("IVR");
            row.add(String.format("%04X", architecture.getIVR() & 0xffff));
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("ADR");
            row.add(String.format("%04X", architecture.getADR() & 0xffff));
            registersTableModel.addRow(row);
            row = new Vector();
            row.add("MDR");
            row.add(String.format("%04X", architecture.getMDR() & 0xffff));
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
        private BufferedImage image;
        private int imageStartX;
        private int imageStartY = 10;
        private Graphics g;

        public DiagramPanel() {
            try {
                image = ImageIO.read(new File("res/diagram.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            this.g = g;
            imageStartX = (getWidth() - image.getWidth()) / 2;
            g.drawImage(image, imageStartX, imageStartY, image.getWidth(), image.getHeight(), null);
            decodeAndPaint();
        }

        private void decodeAndPaint() {
            SursaRBUSPainter sursaRBUSPainter = new SursaRBUSPainter();
            DestinatieRBUSPainter destinatieRBUSPainter = new DestinatieRBUSPainter();
            switch (architecture.getMIR().getSursaRBUS()) {
                case PD_ALU:
                    sursaRBUSPainter.PD_ALU();
                    sursaRBUSPainter.paintRBUS();
                    break;
                default:
                    break;
            }
            switch (architecture.getMIR().getDestinatieRBUS()) {
                case PM_ADR:
                    destinatieRBUSPainter.PM_ADR();
                    break;
                case PM_T:
                    destinatieRBUSPainter.PM_T();
                    break;
                case PM_PC:
                    destinatieRBUSPainter.PM_PC();
                    break;
                case PM_GPR:
                    destinatieRBUSPainter.PM_GPR();
                    break;
                case PM_MDR:
                    destinatieRBUSPainter.PM_MDR();
                    break;
                case PM_FLAG:
                    destinatieRBUSPainter.PM_FLAG();
                    break;
            }
        }

        private void paintPolygon(int[] xPoints, int[] yPoints) {
            for (int i = 0; i < xPoints.length; i++) {
                xPoints[i] += imageStartX;
                yPoints[i] += imageStartY;
            }

            g.setColor(Color.RED);
            g.fillPolygon(xPoints, yPoints, xPoints.length);
        }

        private class SursaRBUSPainter {
            public void paintRBUS() {
                int[] xPoints = {63, 63, 939, 939};
                int[] yPoints = {590, 594, 594, 590};

                paintPolygon(xPoints, yPoints);
            }

            public void PD_ALU() {
                int[] xPoints = {498, 499, 493, 501, 508, 503, 502};
                int[] yPoints = {382, 578, 578, 587, 578, 578, 382};

                paintPolygon(xPoints, yPoints);
            }
        }

        private class DestinatieRBUSPainter {
            public void PM_ADR() {
                int xPoints[] = {188, 180, 186, 186, 190, 190, 196};
                int yPoints[] = {304, 315, 315, 397, 397, 315, 315};
                paintPolygon(xPoints, yPoints);

                int xPoints2[] = {186, 186, 191, 191};
                int yPoints2[] = {405, 588, 588, 405};
                paintPolygon(xPoints2, yPoints2);
            }

            public void PM_T() {
                int xPoints[] = {745, 737, 743, 743, 747, 747, 752};
                int yPoints[] = {418, 427, 427, 588, 588, 427, 427};
                paintPolygon(xPoints, yPoints);
            }

            public void PM_MDR() {
                int xPoints[] = {250, 243, 248, 248, 252, 252, 257};
                int yPoints[] = {417, 425, 425, 452, 452, 425, 425};
                paintPolygon(xPoints, yPoints);

                int xPoints2[] = {263, 255, 261, 261, 265, 265, 270};
                int yPoints2[] = {480, 489, 489, 588, 588, 489, 489};
                paintPolygon(xPoints2, yPoints2);
            }

            public void PM_GPR() {
                int xPoints[] = {937, 929, 935, 935, 939, 939, 944};
                int yPoints[] = {424, 433, 433, 588, 588, 433, 433};
                paintPolygon(xPoints, yPoints);
            }

            public void PM_PC() {
                int xPoints[] = {683, 675, 681, 681, 685, 685, 690};
                int yPoints[] = {305, 314, 314, 588, 588, 314, 314};
                paintPolygon(xPoints, yPoints);
            }

            public void PM_FLAG() {
                int xPoints[] = {621, 614, 619, 619, 623, 623, 628};
                int yPoints[] = {417, 425, 425, 444, 444, 425, 425};
                paintPolygon(xPoints, yPoints);

                int xPoints2[] = {632, 625, 630, 630, 634, 634, 639};
                int yPoints2[] = {474, 482, 482, 587, 587, 482, 482};
                paintPolygon(xPoints2, yPoints2);
            }
        }
    }

    private class MicroprogramPanel extends JPanel {
        private JScrollPane microprogramScrollPane;
        private JTable microprogramTable;
        private DefaultTableModel microprogramTableModel;
        private int selectedRowNr = 0;
        private String columnNames[] = {"Microaddress", "Label", "Sursa SBUS", "Sursa DBUS", "ALU", "Sursa RBUS",
                "Dest. RBUS", "MEM", "Other", "Condition", "Index", "Eval. mode", "Jump address"};

        public MicroprogramPanel() {
            microprogramTableModel = new DefaultTableModel() {
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
                row.add(String.format("%04X", i));
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

        public String[] getColumnNames() {
            return columnNames;
        }

        public void setSelectedRow(int rowNr) {
            microprogramTable.setRowSelectionInterval(rowNr, rowNr);
            microprogramTable.scrollRectToVisible(new Rectangle(microprogramTable.getCellRect(rowNr, 0, true)));
        }

        public DefaultTableModel getMicroprogramTableModel() {
            return microprogramTableModel;
        }
    }

    private class CodeMemoryPanel extends JPanel {
        private JScrollPane codeMemoryScrollPane;
        private JTable codeMemoryTable;
        private DefaultTableModel codeMemoryTableModel;
        private int offsetColumnsCount = 16;

        public CodeMemoryPanel() {
            codeMemoryTableModel = new DefaultTableModel() {
                @Override
                public int getColumnCount() {
                    return offsetColumnsCount + 1;
                }

                @Override
                public String getColumnName(int index) {
                    if (index == 0) return "Address";
                    return String.format("%02X", index - 1);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            codeMemoryTable = new JTable(codeMemoryTableModel);
            codeMemoryTable.setFocusable(false);
            codeMemoryScrollPane = new JScrollPane(codeMemoryTable);

            setLayout(new BorderLayout());
            add(codeMemoryScrollPane, BorderLayout.CENTER);
        }

        public void updateCodeMemoryTable() {
            codeMemoryTableModel.getDataVector().removeAllElements();
            ByteBuffer codeByteBuffer = architecture.getCodeMemory().getByteBuffer();
            Vector row = new Vector();
            row.add(String.format("%04X", 0));
            for (int i = 0; i < codeByteBuffer.limit(); i++) {
                row.add(String.format("%02X", 0xff & codeByteBuffer.get(i)));
                if ((i + 1) % offsetColumnsCount == 0) {
                    codeMemoryTableModel.addRow(row);
                    row = new Vector();
                    row.add(String.format("%04X", i + 1));
                }
            }
            if (row.size() != 1) {
                codeMemoryTableModel.addRow(row);
            }
        }
    }

    private class DataMemoryPanel extends JPanel {
        private JScrollPane dataMemoryScrollPane;
        private JTable dataMemoryTable;
        private DefaultTableModel dataMemoryTableModel;
        private int offsetColumnsCount = 16;

        public DataMemoryPanel() {
            dataMemoryTableModel = new DefaultTableModel() {
                @Override
                public int getColumnCount() {
                    return offsetColumnsCount + 1;
                }

                @Override
                public String getColumnName(int index) {
                    if (index == 0) return "Address";
                    return String.format("%02X", index - 1);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            dataMemoryTable = new JTable(dataMemoryTableModel);
            dataMemoryTable.setFocusable(false);
            dataMemoryScrollPane = new JScrollPane(dataMemoryTable);

            setLayout(new BorderLayout());
            add(dataMemoryScrollPane, BorderLayout.CENTER);
        }

        public void updateDataMemoryTable() {
            dataMemoryTableModel.getDataVector().removeAllElements();
            ByteBuffer dataByteBuffer = architecture.getDataMemory().getByteBuffer();
            Vector row = new Vector();
            row.add(String.format("%04X", 0));
            for (int i = 0; i < dataByteBuffer.limit(); i++) {
                row.add(String.format("%02X", 0xff & dataByteBuffer.get(i)));
                if ((i + 1) % offsetColumnsCount == 0) {
                    dataMemoryTableModel.addRow(row);
                    row = new Vector();
                    row.add(String.format("%04X", i + 1));
                }
            }
            if (row.size() != 1) {
                dataMemoryTableModel.addRow(row);
            }
        }
    }

    private class CurrentMicroinstructionPanel extends JPanel {
        private JScrollPane currentMicroinstructionScrollPane;
        private JTable currentMicroinstructionTable;
        private DefaultTableModel currentMicroinstructionTableModel;

        public CurrentMicroinstructionPanel() {
            setBorder(BorderFactory.createTitledBorder("Current microinstruction"));
            currentMicroinstructionTableModel = new DefaultTableModel() {
                String columnNames[] = microprogramPanel.getColumnNames();

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

    private class ErrorsPanel extends JPanel {
        private JScrollPane errorsScrollPane;
        private JTable errorsTable;
        private DefaultTableModel errorsTableModel;

        public ErrorsPanel() {
            setVisible(false);
            setBorder(BorderFactory.createTitledBorder("Errors"));
            String[] columnNames = {"Line", "Error message"};
            errorsTableModel = new DefaultTableModel() {
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
            errorsTable = new JTable(errorsTableModel);
            errorsTable.getColumnModel().getColumn(0).setMaxWidth(40);
            errorsScrollPane = new JScrollPane(errorsTable);
            setLayout(new BorderLayout());
            add(errorsScrollPane, BorderLayout.CENTER);
        }

        private void updateErrorsTable() {
            errorsTableModel.getDataVector().removeAllElements();
            for (Error error : assembler.getErrorList()) {
                Vector row = new Vector();
                row.add(error.getLineNumber());
                row.add(error.getMessage());
                errorsTableModel.addRow(row);
            }
            errorsTable.setPreferredScrollableViewportSize(
                    new Dimension(errorsTable.getPreferredSize().width,
                            errorsTable.getRowHeight() * errorsTable.getRowCount()));
            setVisible(true);
        }
    }

    private class SouthPanel extends JPanel {
        public SouthPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.gridx = 0;
            c.gridy = 0;
            add(errorsPanel, c);
            c.gridx = 0;
            c.gridy = 1;
            add(currentMicroinstructionPanel, c);
            pack();
        }
    }
}
