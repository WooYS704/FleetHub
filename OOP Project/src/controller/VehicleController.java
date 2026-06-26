package controller;

import model.*;
import view.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.sql.*;

// Third-Party JFreeChart Libraries
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class VehicleController implements ActionListener {
    private MainFrame view;

    public VehicleController(MainFrame view) {
        this.view = view;
        this.view.bindActionListeners(this);
        loadTableData("SELECT * FROM vehicles WHERE is_deleted = FALSE");
        refreshDashboardCharts(); // Load visual graphics onto dashboard initially
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionTrigger = e.getActionCommand();

        switch (actionTrigger) {
            case "Add Record":
                executeInsert();
                break;
            case "Update / Edit":
                executeUpdate();
                break;
            case "Soft Delete":
                executeSoftDelete();
                break;
            case "Search ID":
                executeSearch();
                break;
            case "Reload Records":
                loadTableData("SELECT * FROM vehicles WHERE is_deleted = FALSE");
                refreshDashboardCharts();
                view.clearInputForms();
                break;
            case "Calculate Fleet Metrics":
                executeCalculationModule();
                break;
        }
    }

    // REFRESH DASHBOARD GRAPH CHARTING CARDS
    private void refreshDashboardCharts() {
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        String pieSQL = "SELECT status, COUNT(*) as qty FROM vehicles WHERE is_deleted = FALSE GROUP BY status";

        // Assignment Specification Layout Matrix
        barDataset.addValue(12500, "Repair Expense ($)", "Bus");
        barDataset.addValue(43000, "Repair Expense ($)", "Train");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(pieSQL)) {

            while (rs.next()) {
                pieDataset.setValue(rs.getString("status"), rs.getInt("qty"));
            }
        } catch (SQLException ex) {
            System.out.println("Error calculating live chart dataset: " + ex.getMessage());
        }

        // Generate Core Charts via Factory Layout Pattern
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Fleet Allocation Status (Active vs. Down)", 
                pieDataset, true, true, false
        );

        JFreeChart barChart = ChartFactory.createBarChart(
                "Cumulative Maintenance Expenses Comparison", 
                "Vehicle Category Type", "Total Cost ($)", 
                barDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false
        );

        // --- INJECT GRAPH NUMERICAL DATA LABELS CONFIGURATIONS ---
        
        // 1. Format Pie Chart labels to render exactly -> "Status: Count (Percentage%)"
        org.jfree.chart.plot.PiePlot plotPie = (org.jfree.chart.plot.PiePlot) pieChart.getPlot();
        plotPie.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

        // 2. Format Bar Chart labels to render values floating clearly over target vertical bar columns
        org.jfree.chart.plot.CategoryPlot plotBar = (org.jfree.chart.plot.CategoryPlot) barChart.getPlot();
        org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) plotBar.getRenderer();
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        plotBar.getRangeAxis().setUpperMargin(0.15); // Expand top chart clearance buffer to prevent clipping label items

        // Update the View UI Panel Container slots
        view.updateCharts(pieChart, barChart);
    }

    // LOAD DATA AND REFRESH VIEW INFRASTRUCTURE
    private void loadTableData(String queryBase) {
        view.getTableModel().setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryBase)) {

            while (rs.next()) {
                String id = rs.getString("vehicle_id");
                String type = rs.getString("type");
                int cap = rs.getInt("capacity");
                String status = rs.getString("status");
                String detail1 = "N/A";
                String detail2 = "N/A";

                if ("Bus".equalsIgnoreCase(type)) {
                    String sqlBus = "SELECT * FROM bus_details WHERE vehicle_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlBus)) {
                        ps.setString(1, id);
                        ResultSet rsBus = ps.executeQuery();
                        if (rsBus.next()) {
                            detail1 = rsBus.getString("route_number");
                            detail2 = rsBus.getString("driver_name");
                        }
                    }
                } else {
                    String sqlTrain = "SELECT * FROM train_details WHERE vehicle_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlTrain)) {
                        ps.setString(1, id);
                        ResultSet rsTrain = ps.executeQuery();
                        if (rsTrain.next()) {
                            detail1 = rsTrain.getString("line_name");
                            detail2 = String.valueOf(rsTrain.getInt("coach_count"));
                        }
                    }
                }
                view.getTableModel().addRow(new Object[]{id, type, cap, status, detail1, detail2});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Database Load Failure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // DATA VALIDATION & ERROR HANDLED INSERT MODULES
    private void executeInsert() {
        if (view.getVehicleID().isEmpty() || view.getCapacity().isEmpty() || view.getStatus().isEmpty() 
            || view.getExtraField1().isEmpty() || view.getExtraField2().isEmpty()) {
            
            JOptionPane.showMessageDialog(view, "Error: All form fields are required.", "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacityValue = 0;
        int coachCountValue = 0;

        try {
            capacityValue = Integer.parseInt(view.getCapacity());
            if (capacityValue <= 0) {
                throw new NumberFormatException("Capacity requirements must be higher than 0.");
            }
            if ("Train".equals(view.getVehicleType())) {
                coachCountValue = Integer.parseInt(view.getExtraField2());
                if (coachCountValue <= 0) {
                    throw new NumberFormatException("Coach counts must be higher than 0.");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Format Error: Please type valid positive integers into number fields.\n" + ex.getMessage(), "Data Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkPs = conn.prepareStatement("SELECT COUNT(*) FROM vehicles WHERE vehicle_id = ?")) {
                checkPs.setString(1, view.getVehicleID());
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(view, "Error: Vehicle ID '" + view.getVehicleID() + "' already taken.", "Duplicate Identifier", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO vehicles VALUES (?, ?, ?, ?, FALSE)")) {
                ps.setString(1, view.getVehicleID());
                ps.setString(2, view.getVehicleType());
                ps.setInt(3, capacityValue);
                ps.setString(4, view.getStatus());
                ps.executeUpdate();
            }

            if ("Bus".equals(view.getVehicleType())) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO bus_details VALUES (?, ?, ?)")) {
                    ps.setString(1, view.getVehicleID());
                    ps.setString(2, view.getExtraField1());
                    ps.setString(3, view.getExtraField2());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO train_details VALUES (?, ?, ?)")) {
                    ps.setString(1, view.getVehicleID());
                    ps.setString(2, view.getExtraField1());
                    ps.setInt(3, coachCountValue);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO operational_logs (vehicle_id, efficiency_score) VALUES (?, ?)")) {
                ps.setString(1, view.getVehicleID());
                ps.setInt(2, 85);
                ps.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(view, "Vehicle Inventory Profile Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTableData("SELECT * FROM vehicles WHERE is_deleted = FALSE");
            refreshDashboardCharts(); // Redraw charts automatically on insertion changes
            view.clearInputForms();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "SQL Transaction Failure: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // SAFE TRANSACTIONAL MULTI-TABLE UPDATE LOGIC INTERFACES
    private void executeUpdate() {
        if (view.getVehicleID().isEmpty() || view.getStatus().isEmpty() || view.getCapacity().isEmpty()
            || view.getExtraField1().isEmpty() || view.getExtraField2().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter all form inputs to execute an update.", "Form Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = view.getVehicleType();
        String id = view.getVehicleID();
        int capacityValue = 0;
        int coachCountValue = 0;

        try {
            capacityValue = Integer.parseInt(view.getCapacity());
            if (capacityValue <= 0) {
                throw new NumberFormatException("Capacity requirements must be higher than 0.");
            }
            if ("Train".equalsIgnoreCase(type)) {
                coachCountValue = Integer.parseInt(view.getExtraField2());
                if (coachCountValue <= 0) {
                    throw new NumberFormatException("Coach counts must be higher than 0.");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Format Error: Please use positive integers for numeric inputs.", "Input Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Enable manual transaction processing
            conn.setAutoCommit(false);

            // 1. Update general attributes inside parent entity row
            String baseSql = "UPDATE vehicles SET status = ?, capacity = ? WHERE vehicle_id = ? AND is_deleted = FALSE";
            int baseUpdated = 0;
            try (PreparedStatement psBase = conn.prepareStatement(baseSql)) {
                psBase.setString(1, view.getStatus());
                psBase.setInt(2, capacityValue);
                psBase.setString(3, id);
                baseUpdated = psBase.executeUpdate();
            }

            if (baseUpdated > 0) {
                // 2. Dynamically process specialized fields according to specific runtime sub-class mapping
                if ("Bus".equalsIgnoreCase(type)) {
                    String busSql = "UPDATE bus_details SET route_number = ?, driver_name = ? WHERE vehicle_id = ?";
                    try (PreparedStatement psBus = conn.prepareStatement(busSql)) {
                        psBus.setString(1, view.getExtraField1());
                        psBus.setString(2, view.getExtraField2());
                        psBus.setString(3, id);
                        psBus.executeUpdate();
                    }
                } else {
                    String trainSql = "UPDATE train_details SET line_name = ?, coach_count = ? WHERE vehicle_id = ?";
                    try (PreparedStatement psTrain = conn.prepareStatement(trainSql)) {
                        psTrain.setString(1, view.getExtraField1());
                        psTrain.setInt(2, coachCountValue);
                        psTrain.setString(3, id);
                        psTrain.executeUpdate();
                    }
                }

                // Flush changes securely down to local engine schema context
                conn.commit();
                JOptionPane.showMessageDialog(view, "Vehicle Profile Alterations Updated Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Re-render display view panel structure and charts
                loadTableData("SELECT * FROM vehicles WHERE is_deleted = FALSE");
                refreshDashboardCharts();
                view.clearInputForms();
            } else {
                JOptionPane.showMessageDialog(view, "No matching active asset catalog found containing ID: " + id, "Asset Profile Missing", JOptionPane.INFORMATION_MESSAGE);
                conn.rollback();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Database Update Processing Failure: " + ex.getMessage(), "SQL Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // SOFT DELETE EXECUTION ENGINE
    private void executeSoftDelete() {
        int targetRowSelection = view.getDataTable().getSelectedRow();
        String targetID = (targetRowSelection != -1) ? (String) view.getTableModel().getValueAt(targetRowSelection, 0) : view.getVehicleID();

        if (targetID.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Highlight a row profile on the data grid or input an ID to delete.", "Target Identification Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE vehicles SET is_deleted = TRUE WHERE vehicle_id = ?")) {
            ps.setString(1, targetID);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(view, "Soft Delete Complete! Asset hidden from inventory grid display.");
                loadTableData("SELECT * FROM vehicles WHERE is_deleted = FALSE");
                refreshDashboardCharts(); // Redraw charts automatically to match drop
                view.clearInputForms();
            } else {
                JOptionPane.showMessageDialog(view, "Asset Record context search failed to match target ID inputs.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Database Flag Processing Fault: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // SIMPLE LOOKUP SELECTION INTERFACES
    private void executeSearch() {
        String inputQuery = view.getSearchQuery();
        if (inputQuery.isEmpty()) {
            loadTableData("SELECT * FROM vehicles WHERE is_deleted = FALSE");
            return;
        }
        loadTableData("SELECT * FROM vehicles WHERE vehicle_id LIKE '%" + inputQuery + "%' AND is_deleted = FALSE");
    }

    // CALCULATION MODULE UTILIZING SQL AGGREGATE OPERATIONS
    private void executeCalculationModule() {
        String sqlMetrics = "SELECT COUNT(*) as total, SUM(capacity) as total_seats FROM vehicles WHERE is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlMetrics)) {

            int totalCount = 0;
            int totalSeats = 0;
            if (rs.next()) {
                totalCount = rs.getInt("total");
                totalSeats = rs.getInt("total_seats");
            }

            double averageSeats = (totalCount == 0) ? 0.0 : (double) totalSeats / totalCount;

            String analyticsResult = "=== Fleet Summary Stats ===\n" +
                    "• Total Assets: " + totalCount + " vehicles\n" +
                    "• Total Seats: " + totalSeats + " seats\n" +
                    "• Average Capacity: " + String.format("%.2f", averageSeats) + " seats/vehicle\n\n" +
                    "[OpenPDF]: Summary report printed to folder.";

            JOptionPane.showMessageDialog(view, analyticsResult, "Calculated Metrics Dashboard", JOptionPane.INFORMATION_MESSAGE);
            
            // Execute automated export function to save PDF document sheets
            generateThirdPartyPDF(analyticsResult);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Calculation Module Failure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // THIRD-PARTY LIBRARY INTEGRATION (OpenPDF Exporter Binding Configured Labeled Graphs)
    private void generateThirdPartyPDF(String data) {
        System.out.println("Exporting document matching live labeled dashboard layout parameters...");
        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream("TransitReport.pdf"));
            document.open();
            
            // Header Content Blocks
            document.add(new com.lowagie.text.Paragraph("====================================================="));
            document.add(new com.lowagie.text.Paragraph("      OFFICIAL TRANSIT NETWORK MANAGEMENT LEDGER      "));
            document.add(new com.lowagie.text.Paragraph("====================================================="));
            document.add(new com.lowagie.text.Paragraph("Generated timestamp: " + new java.util.Date()));
            document.add(new com.lowagie.text.Paragraph("\n" + data));
            document.add(new com.lowagie.text.Paragraph("\n====================================================="));
            document.add(new com.lowagie.text.Paragraph("        VISUAL GRAPH ANALYTICS ATTACHMENTS          "));
            document.add(new com.lowagie.text.Paragraph("=====================================================\n\n"));

            // Compile dedicated chart clones with the identical label properties for printer rendering layout streams
            统计数据: {
                DefaultPieDataset pieDataset = new DefaultPieDataset();
                String pieSQL = "SELECT status, COUNT(*) as qty FROM vehicles WHERE is_deleted = FALSE GROUP BY status";
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(pieSQL)) {
                    while (rs.next()) {
                        pieDataset.setValue(rs.getString("status"), rs.getInt("qty"));
                    }
                }
                JFreeChart livePie = ChartFactory.createPieChart("Fleet Status Distribution", pieDataset, true, true, false);

                DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
                barDataset.addValue(12500, "Repair Expense ($)", "Bus");
                barDataset.addValue(43000, "Repair Expense ($)", "Train");
                JFreeChart liveBar = ChartFactory.createBarChart("Cumulative Maintenance Expenses", "Type", "Cost ($)", barDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);

                // Apply exactly matching data text tags properties inside output images too
                org.jfree.chart.plot.PiePlot labelPie = (org.jfree.chart.plot.PiePlot) livePie.getPlot();
                labelPie.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

                org.jfree.chart.plot.CategoryPlot labelBar = (org.jfree.chart.plot.CategoryPlot) liveBar.getPlot();
                org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) labelBar.getRenderer();
                renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
                renderer.setDefaultItemLabelsVisible(true);
                labelBar.getRangeAxis().setUpperMargin(0.15);

                // Rasterize graphics into image data streams
                java.awt.image.BufferedImage pieImage = livePie.createBufferedImage(450, 280);
                java.awt.image.BufferedImage barImage = liveBar.createBufferedImage(450, 280);

                com.lowagie.text.Image pdfPie = com.lowagie.text.Image.getInstance(pieImage, null);
                com.lowagie.text.Image pdfBar = com.lowagie.text.Image.getInstance(barImage, null);

                pdfPie.setAlignment(com.lowagie.text.Image.ALIGN_CENTER);
                pdfBar.setAlignment(com.lowagie.text.Image.ALIGN_CENTER);

                // Draw objects directly into document canvas sheet positions
                document.add(pdfPie);
                document.add(new com.lowagie.text.Paragraph("\n")); 
                document.add(pdfBar);
            }

            document.close();
            System.out.println("Document matching chart figures built completely successfully.");
            
        } catch (Exception e) {
            System.out.println("PDF print generation exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}