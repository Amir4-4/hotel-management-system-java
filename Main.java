import javax.swing.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("       Hotel Management System Starting...");
        System.out.println("═══════════════════════════════════════════════════════");

        HotelManagementSystem system = new HotelManagementSystem();

        // Try to load data from MongoDB (non-blocking)
        System.out.println("\nAttempting to load data from MongoDB...");
        try {
            // Load guests
            ArrayList<Guest> loadedGuests = FileManager.loadGuests();
            for (Guest g : loadedGuests) {
                system.guestBST.add(g);
            }

            // Load rooms
            system.roomList = FileManager.loadRooms();

            // Load reservations
            system.reservationList = FileManager.loadReservations();

            if (FileManager.isConnected()) {
                System.out.println("✅ Data loaded successfully from MongoDB!");
                System.out.println("   - Guests: " + loadedGuests.size());
                System.out.println("   - Rooms: " + system.roomList.size());
                System.out.println("   - Reservations: " + system.reservationList.size());
            } else {
                System.out.println("⚠️  MongoDB not connected - Starting with empty data");
            }

        } catch (Exception e) {
            System.err.println("⚠️  Could not load data from MongoDB: " + e.getMessage());
            System.err.println("Starting with empty data...");
        }

        // Launch GUI on Event Dispatch Thread
        System.out.println("\nLaunching GUI...");
        SwingUtilities.invokeLater(() -> {
            try {
                HotelManagementSystemGUI gui = new HotelManagementSystemGUI(system);
                gui.setVisible(true);
                System.out.println("✅ GUI launched successfully!");
                System.out.println("\n═══════════════════════════════════════════════════════");
                System.out.println("       System Ready! Enjoy managing your hotel!");
                System.out.println("═══════════════════════════════════════════════════════\n");
            } catch (Exception e) {
                System.err.println("❌ FATAL ERROR: Could not launch GUI!");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch GUI:\n" + e.getMessage(),
                        "Fatal Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        // Add shutdown hook to close MongoDB connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down Hotel Management System...");
            FileManager.closeConnection();
            System.out.println("Goodbye!");
        }));
    }
}