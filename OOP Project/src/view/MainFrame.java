package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class MainFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField idTxt, capTxt, statusTxt, extra1Txt, extra2Txt, searchTxt;
    private JComboBox<String> typeCombo;
    private JLabel lblExtra1, lblExtra2;
    
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnRefresh, btnCalc;
    private JPanel chartDashboardPanel; // Panel container for JFreeChart integration

    public MainFrame() {
        setTitle("Transit Network Operations & Analytics Panel");
        setSize(1100, 750); // Increased height slightly to accommodate charts cleanly
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // Forms Input Area
        JPanel leftInputForm = new JPanel(new GridBagLayout());
        leftInputForm.setBorder(BorderFactory.createTitledBorder("Vehicle Profile Entry"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; leftInputForm.add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; typeCombo = new JComboBox<>(new String[]{"Bus", "Train"}); leftInputForm.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; leftInputForm.add(new JLabel("Vehicle ID:"), gbc);
        gbc.gridx = 1; idTxt = new JTextField(12); leftInputForm.add(idTxt, gbc);

        gbc.gridx = 0; gbc.gridy = 2; leftInputForm.add(new JLabel("Passenger Capacity:"), gbc);
        gbc.gridx = 1; capTxt = new JTextField(12); leftInputForm.add(capTxt, gbc);

        gbc.gridx = 0; gbc.gridy = 3; leftInputForm.add(new JLabel("Operational Status:"), gbc);
        gbc.gridx = 1; statusTxt = new JTextField(12); leftInputForm.add(statusTxt, gbc);

        gbc.gridx = 0; gbc.gridy = 4; lblExtra1 = new JLabel("Route Number:"); leftInputForm.add(lblExtra1, gbc);
        gbc.gridx = 1; extra1Txt = new JTextField(12); leftInputForm.add(extra1Txt, gbc);

        gbc.gridx = 0; gbc.gridy = 5; lblExtra2 = new JLabel("Driver Name:"); leftInputForm.add(lblExtra2, gbc);
        gbc.gridx = 1; extra2Txt = new JTextField(12); leftInputForm.add(extra2Txt, gbc);

        JPanel actionBtnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnAdd = new JButton("Add Record");
        btnUpdate = new JButton("Update / Edit");
        btnDelete = new JButton("Soft Delete");
        btnCalc = new JButton("Calculate Fleet Metrics");

        actionBtnGrid.add(btnAdd); actionBtnGrid.add(btnUpdate);
        actionBtnGrid.add(btnDelete); actionBtnGrid.add(btnCalc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        leftInputForm.add(actionBtnGrid, gbc);
        add(leftInputForm, BorderLayout.WEST);

        // Grid Log Table Visual Panel Container
        JPanel centerGridPanel = new JPanel(new BorderLayout(5, 5));
        centerGridPanel.setBorder(BorderFactory.createTitledBorder("Active Fleet Inventory Log Entries"));

        String[] headers = {"Vehicle ID", "Type", "Capacity", "Status", "Route / Line Details", "Driver / Coach Specs"};
        tableModel = new DefaultTableModel(headers, 0);
        table = new JTable(tableModel);
        centerGridPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Top Filter Bar Actions layout
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchTxt = new JTextField(15);
        btnSearch = new JButton("Search ID");
        btnRefresh = new JButton("Reload Records");
        searchBarPanel.add(new JLabel("Filter by Target ID:"));
        searchBarPanel.add(searchTxt);
        searchBarPanel.add(btnSearch);
        searchBarPanel.add(btnRefresh);
        centerGridPanel.add(searchBarPanel, BorderLayout.NORTH);

        add(centerGridPanel, BorderLayout.CENTER);

        // Bottom Dashboard Panel Container for live JFreeChart graphs
        chartDashboardPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        chartDashboardPanel.setBorder(BorderFactory.createTitledBorder("Real-Time Fleet Graph Analytics Dashboard"));
        chartDashboardPanel.setPreferredSize(new Dimension(1100, 240));
        add(chartDashboardPanel, BorderLayout.SOUTH);

        // Handle dynamically updating UI configuration properties based on type dropdown Selection
        typeCombo.addActionListener(e -> {
            if (typeCombo.getSelectedItem().equals("Bus")) {
                lblExtra1.setText("Route Number:");
                lblExtra2.setText("Driver Name:");
            } else {
                lblExtra1.setText("Line Name:");
                lblExtra2.setText("Coach Count:");
            }
        });
    }

    // Public method invoked by the controller to push fresh, live charts into the dashboard view
    public void updateCharts(JFreeChart pieChart, JFreeChart barChart) {
        chartDashboardPanel.removeAll();
        chartDashboardPanel.add(new ChartPanel(pieChart));
        chartDashboardPanel.add(new ChartPanel(barChart));
        chartDashboardPanel.revalidate();
        chartDashboardPanel.repaint();
    }

    public String getVehicleType() { return (String) typeCombo.getSelectedItem(); }
    public String getVehicleID() { return idTxt.getText().trim(); }
    public String getCapacity() { return capTxt.getText().trim(); }
    public String getStatus() { return statusTxt.getText().trim(); }
    public String getExtraField1() { return extra1Txt.getText().trim(); }
    public String getExtraField2() { return extra2Txt.getText().trim(); }
    public String getSearchQuery() { return searchTxt.getText().trim(); }
    public JTable getDataTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }

    public void clearInputForms() {
        idTxt.setText(""); capTxt.setText(""); statusTxt.setText("");
        extra1Txt.setText(""); extra2Txt.setText(""); searchTxt.setText("");
    }

    public void bindActionListeners(ActionListener listener) {
        btnAdd.addActionListener(listener);
        btnUpdate.addActionListener(listener);
        btnDelete.addActionListener(listener);
        btnSearch.addActionListener(listener);
        btnRefresh.addActionListener(listener);
        btnCalc.addActionListener(listener);
    }
}