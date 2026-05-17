import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HotelManagementSystemGUI extends JFrame {

    private HotelManagementSystem system;
    private JTable guestTable, roomTable, reservationTable;
    private DefaultTableModel guestModel, roomModel, reservationModel;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color DARK_BG = new Color(44, 62, 80);
    private final Color LIGHT_BG = new Color(236, 240, 241);

    public HotelManagementSystemGUI(HotelManagementSystem system) {
        this.system = system;
        setTitle("🏨 Hotel Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Test MongoDB connection at startup
        if (!FileManager.testConnection()) {
            showWarningDialog("⚠️ MongoDB Not Connected",
                    "Database connection failed. You can still use the app,\nbut data won't be saved to MongoDB.");
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.add("👥 Guests", guestPanel());
        tabs.add("🛏️ Rooms", roomPanel());
        tabs.add("📅 Reservations", reservationPanel());
        tabs.add("📊 Reports", reportPanel());

        add(tabs);

        // Add status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(DARK_BG);
        JLabel status = new JLabel(FileManager.isConnected() ?
                "🟢 MongoDB Connected" : "🔴 MongoDB Disconnected");
        status.setForeground(Color.BLACK);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(status);

        JLabel date = new JLabel("  |  📅 Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        date.setForeground(Color.BLACK);
        date.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(date);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =================== GUEST PANEL ===================
    private JPanel guestPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        p.setBackground(LIGHT_BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(LIGHT_BG);

        JButton add = createStyledButton("+ Add Guest", PRIMARY_COLOR);
        add.addActionListener(e -> addGuestDialog());
        top.add(add);

        JButton refresh = createStyledButton("Refresh", new Color(52, 152, 219));
        refresh.addActionListener(e -> refreshGuestTable());
        top.add(refresh);

        JButton save = createStyledButton("Save to MongoDB", SUCCESS_COLOR);
        save.addActionListener(e -> saveGuestsToMongo());
        top.add(save);

        JButton load = createStyledButton("Load from MongoDB", new Color(155, 89, 182));
        load.addActionListener(e -> loadGuestsFromMongo());
        top.add(load);

        p.add(top, BorderLayout.NORTH);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(LIGHT_BG);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Search Guests",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                Color.BLACK
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(Color.BLACK);
        searchPanel.add(searchLabel);

        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchPanel.add(searchField);

        JComboBox<String> searchTypeCombo = new JComboBox<>(new String[]{"Search by Name", "Search by ID"});
        searchTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchPanel.add(searchTypeCombo);

        JButton searchButton = createStyledButton("Search", new Color(230, 126, 34));
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            String searchType = (String) searchTypeCombo.getSelectedItem();
            searchGuests(searchTerm, searchType);
        });
        searchPanel.add(searchButton);

        JButton clearButton = createStyledButton("Show All", new Color(149, 165, 166));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshGuestTable();
        });
        searchPanel.add(clearButton);

        // Create a wrapper panel for search and table
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(LIGHT_BG);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Contact", "Address"};
        guestModel = new DefaultTableModel(columns, 0);
        guestTable = new JTable(guestModel);
        styleTable(guestTable);
        JScrollPane scroll = new JScrollPane(guestTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        centerPanel.add(scroll, BorderLayout.CENTER);

        p.add(centerPanel, BorderLayout.CENTER);

        refreshGuestTable();
        return p;
    }

    private void searchGuests(String searchTerm, String searchType) {
        if (searchTerm.isEmpty()) {
            refreshGuestTable();
            return;
        }

        guestModel.setRowCount(0);
        ArrayList<Guest> allGuests = system.guestBST.toArrayList();
        boolean found = false;

        if (searchType.equals("Search by ID")) {
            try {
                int searchId = Integer.parseInt(searchTerm);
                for (Guest g : allGuests) {
                    if (g.getGuestID() == searchId) {
                        guestModel.addRow(new Object[]{
                                g.getGuestID(), g.getName(), g.getContactInfo(), g.getAddress()
                        });
                        found = true;
                    }
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid ID! Please enter a number.");
                return;
            }
        } else { // Search by Name
            for (Guest g : allGuests) {
                if (g.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    guestModel.addRow(new Object[]{
                            g.getGuestID(), g.getName(), g.getContactInfo(), g.getAddress()
                    });
                    found = true;
                }
            }
        }

        if (!found) {
            showWarningDialog("No Results", "No guests found matching: " + searchTerm);
            refreshGuestTable();
        }
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(new Color(189, 195, 199));
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_BG);
        table.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    }

    private void addGuestDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();

        panel.add(new JLabel("Guest ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Guest",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();

                if (name.isEmpty()) {
                    showErrorDialog("Name cannot be empty!");
                    return;
                }

                system.addGuest(id, name, phone, address);
                refreshGuestTable();
                showSuccessDialog("Guest added successfully!");
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid ID! Please enter a number.");
            }
        }
    }

    private void saveGuestsToMongo() {
        try {
            FileManager.saveGuests(system.guestBST.toArrayList());
            showSuccessDialog("Guests saved to MongoDB!");
        } catch (Exception ex) {
            showErrorDialog("Save failed: " + ex.getMessage());
        }
    }

    private void loadGuestsFromMongo() {
        try {
            system.guestBST = new GuestBST();
            ArrayList<Guest> loaded = FileManager.loadGuests();
            for (Guest g : loaded) {
                system.guestBST.add(g);
            }
            refreshGuestTable();
            showSuccessDialog("Loaded " + loaded.size() + " guests from MongoDB!");
        } catch (Exception ex) {
            showErrorDialog("Load failed: " + ex.getMessage());
        }
    }

    private void refreshGuestTable() {
        guestModel.setRowCount(0);
        for (Guest g : system.guestBST.toArrayList()) {
            guestModel.addRow(new Object[]{
                    g.getGuestID(), g.getName(), g.getContactInfo(), g.getAddress()
            });
        }
    }

    // =================== ROOM PANEL ===================
    private JPanel roomPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        p.setBackground(LIGHT_BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(LIGHT_BG);

        JButton add = createStyledButton("➕ Add Room", PRIMARY_COLOR);
        add.addActionListener(e -> addRoomDialog());
        top.add(add);

        JButton refresh = createStyledButton("🔄 Refresh", new Color(52, 152, 219));
        refresh.addActionListener(e -> refreshRoomTable());
        top.add(refresh);

        JButton save = createStyledButton("💾 Save to MongoDB", SUCCESS_COLOR);
        save.addActionListener(e -> saveRoomsToMongo());
        top.add(save);

        JButton load = createStyledButton("📥 Load from MongoDB", new Color(155, 89, 182));
        load.addActionListener(e -> loadRoomsFromMongo());
        top.add(load);

        p.add(top, BorderLayout.NORTH);

        String[] columns = {"Room ID", "Type", "Price/Night", "Status"};
        roomModel = new DefaultTableModel(columns, 0);
        roomTable = new JTable(roomModel);
        styleTable(roomTable);
        JScrollPane scroll = new JScrollPane(roomTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        p.add(scroll, BorderLayout.CENTER);

        refreshRoomTable();
        return p;
    }

    private void addRoomDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField idField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Single", "Double", "Suite", "Deluxe"});
        JTextField priceField = new JTextField();

        panel.add(new JLabel("Room ID:"));
        panel.add(idField);
        panel.add(new JLabel("Room Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Price per Night:"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String type = (String) typeCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());

                system.addRoom(new Room(id, type, 2, price));
                refreshRoomTable();
                showSuccessDialog("Room added successfully!");
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid input! Check ID and price.");
            }
        }
    }

    private void saveRoomsToMongo() {
        try {
            FileManager.saveRooms(system.roomList);
            showSuccessDialog("Rooms saved to MongoDB!");
        } catch (Exception ex) {
            showErrorDialog("Save failed: " + ex.getMessage());
        }
    }

    private void loadRoomsFromMongo() {
        try {
            system.roomList = FileManager.loadRooms();
            refreshRoomTable();
            showSuccessDialog("Loaded " + system.roomList.size() + " rooms from MongoDB!");
        } catch (Exception ex) {
            showErrorDialog("Load failed: " + ex.getMessage());
        }
    }

    private void refreshRoomTable() {
        roomModel.setRowCount(0);
        for (Room r : system.roomList) {
            roomModel.addRow(new Object[]{
                    r.getRoomID(),
                    r.getRoomType(),
                    String.format("$%.2f", r.getPricePerNight()),
                    r.isAvailable() ? "✅ Available" : "❌ Occupied"
            });
        }
    }

    // =================== RESERVATION PANEL ===================
    private JPanel reservationPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        p.setBackground(LIGHT_BG);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(LIGHT_BG);

        JButton add = createStyledButton("➕ Make Reservation", PRIMARY_COLOR);
        add.addActionListener(e -> makeReservationDialog());
        top.add(add);

        JButton refresh = createStyledButton("🔄 Refresh", new Color(52, 152, 219));
        refresh.addActionListener(e -> refreshReservationTable());
        top.add(refresh);

        JButton save = createStyledButton("💾 Save to MongoDB", SUCCESS_COLOR);
        save.addActionListener(e -> saveReservationsToMongo());
        top.add(save);

        JButton load = createStyledButton("📥 Load from MongoDB", new Color(155, 89, 182));
        load.addActionListener(e -> loadReservationsFromMongo());
        top.add(load);

        p.add(top, BorderLayout.NORTH);

        String[] columns = {"Res ID", "Guest ID", "Room ID", "Room Type", "Check-In", "Check-Out", "Nights", "Status"};
        reservationModel = new DefaultTableModel(columns, 0);
        reservationTable = new JTable(reservationModel);
        styleTable(reservationTable);
        JScrollPane scroll = new JScrollPane(reservationTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        p.add(scroll, BorderLayout.CENTER);

        refreshReservationTable();
        return p;
    }

    private void makeReservationDialog() {
        // Get available rooms
        ArrayList<Room> availableRooms = new ArrayList<>();
        for (Room r : system.roomList) {
            if (r.isAvailable()) {
                availableRooms.add(r);
            }
        }

        if (availableRooms.isEmpty()) {
            showErrorDialog("No available rooms!");
            return;
        }

        // Get all guests
        ArrayList<Guest> allGuests = system.guestBST.toArrayList();
        if (allGuests.isEmpty()) {
            showErrorDialog("No guests in the system! Please add guests first.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField resIdField = new JTextField();

        // Guest dropdown with details
        String[] guestOptions = new String[allGuests.size()];
        for (int i = 0; i < allGuests.size(); i++) {
            Guest g = allGuests.get(i);
            guestOptions[i] = String.format("ID: %d - %s (%s)",
                    g.getGuestID(), g.getName(), g.getContactInfo());
        }
        JComboBox<String> guestCombo = new JComboBox<>(guestOptions);

        // Room dropdown with details
        String[] roomOptions = new String[availableRooms.size()];
        for (int i = 0; i < availableRooms.size(); i++) {
            Room r = availableRooms.get(i);
            roomOptions[i] = String.format("Room %d - %s ($%.2f/night)",
                    r.getRoomID(), r.getRoomType(), r.getPricePerNight());
        }
        JComboBox<String> roomCombo = new JComboBox<>(roomOptions);

        // Date fields with today's date as default
        LocalDate today = LocalDate.now();
        JTextField checkInField = new JTextField(today.toString());
        JTextField checkOutField = new JTextField(today.plusDays(1).toString());

        JLabel estimatedCost = new JLabel("Estimated Cost: $0.00");
        estimatedCost.setFont(new Font("Segoe UI", Font.BOLD, 13));
        estimatedCost.setForeground(Color.BLACK);

        // Update cost when dates change
        Runnable updateCost = () -> {
            try {
                LocalDate in = LocalDate.parse(checkInField.getText());
                LocalDate out = LocalDate.parse(checkOutField.getText());
                long nights = ChronoUnit.DAYS.between(in, out);
                if (nights > 0 && roomCombo.getSelectedIndex() >= 0) {
                    Room selectedRoom = availableRooms.get(roomCombo.getSelectedIndex());
                    double cost = nights * selectedRoom.getPricePerNight();
                    estimatedCost.setText(String.format("Estimated Cost: $%.2f (%d nights)", cost, nights));
                }
            } catch (Exception ex) {
                estimatedCost.setText("Estimated Cost: Invalid dates");
            }
        };

        checkInField.addCaretListener(ev -> updateCost.run());
        checkOutField.addCaretListener(ev -> updateCost.run());
        roomCombo.addActionListener(ev -> updateCost.run());

        panel.add(new JLabel("Reservation ID:"));
        panel.add(resIdField);
        panel.add(new JLabel("Select Guest:"));
        panel.add(guestCombo);
        panel.add(new JLabel("Select Room:"));
        panel.add(roomCombo);
        panel.add(new JLabel("Check-In (YYYY-MM-DD):"));
        panel.add(checkInField);
        panel.add(new JLabel("Check-Out (YYYY-MM-DD):"));
        panel.add(checkOutField);
        panel.add(new JLabel(""));
        panel.add(estimatedCost);

        updateCost.run(); // Initial calculation

        int result = JOptionPane.showConfirmDialog(this, panel, "Make Reservation",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int resId = Integer.parseInt(resIdField.getText().trim());
                Guest selectedGuest = allGuests.get(guestCombo.getSelectedIndex());
                Room selectedRoom = availableRooms.get(roomCombo.getSelectedIndex());
                String checkIn = checkInField.getText().trim();
                String checkOut = checkOutField.getText().trim();

                // Validate dates
                LocalDate in = LocalDate.parse(checkIn);
                LocalDate out = LocalDate.parse(checkOut);
                if (!out.isAfter(in)) {
                    showErrorDialog("Check-out date must be after check-in date!");
                    return;
                }

                system.makeReservation(resId, selectedGuest.getGuestID(), selectedRoom.getRoomID(), checkIn, checkOut);
                selectedRoom.assignRoom(); // Mark room as occupied
                refreshReservationTable();
                refreshRoomTable();
                showSuccessDialog("Reservation created successfully!");
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid Reservation ID! Please enter a number.");
            } catch (Exception ex) {
                showErrorDialog("Error: " + ex.getMessage());
            }
        }
    }

    private void saveReservationsToMongo() {
        try {
            FileManager.saveReservations(system.reservationList);
            showSuccessDialog("Reservations saved to MongoDB!");
        } catch (Exception ex) {
            showErrorDialog("Save failed: " + ex.getMessage());
        }
    }

    private void loadReservationsFromMongo() {
        try {
            system.reservationList = FileManager.loadReservations();
            refreshReservationTable();
            showSuccessDialog("Loaded " + system.reservationList.size() + " reservations!");
        } catch (Exception ex) {
            showErrorDialog("Load failed: " + ex.getMessage());
        }
    }

    private void refreshReservationTable() {
        reservationModel.setRowCount(0);
        for (Reservation res : system.reservationList) {
            // Find room details
            String roomType = "Unknown";
            for (Room r : system.roomList) {
                if (r.getRoomID() == res.getRoomID()) {
                    roomType = r.getRoomType();
                    break;
                }
            }

            // Calculate nights
            long nights = 0;
            try {
                LocalDate in = LocalDate.parse(res.getCheckInDate());
                LocalDate out = LocalDate.parse(res.getCheckOutDate());
                nights = ChronoUnit.DAYS.between(in, out);
            } catch (Exception e) {
                nights = 0;
            }

            reservationModel.addRow(new Object[]{
                    res.getReservationID(),
                    res.getGuestID(),
                    res.getRoomID(),
                    roomType,
                    res.getCheckInDate(),
                    res.getCheckOutDate(),
                    nights,
                    res.getStatus()
            });
        }
    }

    // =================== REPORT PANEL ===================
    private JPanel reportPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        p.setBackground(LIGHT_BG);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setMargin(new Insets(15, 15, 15, 15));
        textArea.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(LIGHT_BG);

        JButton gen = createStyledButton("📊 Generate Detailed Report", PRIMARY_COLOR);
        gen.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("═══════════════════════════════════════════════════════\n");
            report.append("        🏨 HOTEL MANAGEMENT SYSTEM REPORT\n");
            report.append("═══════════════════════════════════════════════════════\n\n");
            report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))).append("\n\n");

            // Guest Statistics
            report.append("👥 GUEST STATISTICS:\n");
            report.append("─────────────────────────────────────────────────────\n");
            int totalGuests = system.guestBST.toArrayList().size();
            report.append(String.format("Total Guests: %d\n\n", totalGuests));

            // Room Statistics
            report.append("🛏️  ROOM STATISTICS:\n");
            report.append("─────────────────────────────────────────────────────\n");
            int totalRooms = system.roomList.size();
            int available = 0, occupied = 0;
            double totalRevenuePotential = 0;

            for (Room r : system.roomList) {
                if (r.isAvailable()) available++;
                else occupied++;
                totalRevenuePotential += r.getPricePerNight();
            }

            report.append(String.format("Total Rooms: %d\n", totalRooms));
            report.append(String.format("Available: %d (%.1f%%)\n", available, totalRooms > 0 ? (available * 100.0 / totalRooms) : 0));
            report.append(String.format("Occupied: %d (%.1f%%)\n", occupied, totalRooms > 0 ? (occupied * 100.0 / totalRooms) : 0));
            report.append(String.format("Daily Revenue Potential: $%.2f\n\n", totalRevenuePotential));

            // Reservation Statistics
            report.append("📅 RESERVATION STATISTICS:\n");
            report.append("─────────────────────────────────────────────────────\n");
            int totalReservations = system.reservationList.size();
            int active = 0, cancelled = 0;
            long totalNights = 0;
            double estimatedRevenue = 0;

            for (Reservation res : system.reservationList) {
                if (res.getStatus().equals("Active")) active++;
                else if (res.getStatus().equals("Cancelled")) cancelled++;

                try {
                    LocalDate in = LocalDate.parse(res.getCheckInDate());
                    LocalDate out = LocalDate.parse(res.getCheckOutDate());
                    long nights = ChronoUnit.DAYS.between(in, out);
                    totalNights += nights;

                    // Calculate revenue
                    for (Room room : system.roomList) {
                        if (room.getRoomID() == res.getRoomID()) {
                            estimatedRevenue += nights * room.getPricePerNight();
                            break;
                        }
                    }
                } catch (Exception ex) {}
            }

            report.append(String.format("Total Reservations: %d\n", totalReservations));
            report.append(String.format("Active: %d\n", active));
            report.append(String.format("Cancelled: %d\n", cancelled));
            report.append(String.format("Total Nights Booked: %d\n", totalNights));
            report.append(String.format("Estimated Revenue: $%.2f\n\n", estimatedRevenue));

            // Room Type Breakdown
            report.append("🏷️  ROOM TYPE BREAKDOWN:\n");
            report.append("─────────────────────────────────────────────────────\n");
            java.util.Map<String, Integer> roomTypes = new java.util.HashMap<>();
            for (Room r : system.roomList) {
                roomTypes.put(r.getRoomType(), roomTypes.getOrDefault(r.getRoomType(), 0) + 1);
            }
            for (String roomType : roomTypes.keySet()) {
                report.append(String.format("%s: %d rooms\n", roomType, roomTypes.get(roomType)));
            }

            report.append("\n═══════════════════════════════════════════════════════\n");
            report.append("                  END OF REPORT\n");
            report.append("═══════════════════════════════════════════════════════\n");

            textArea.setText(report.toString());
        });
        buttonPanel.add(gen);

        p.add(buttonPanel, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // Helper methods for dialogs
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }
}