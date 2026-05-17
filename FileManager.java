import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;

public class FileManager {

    private static final String URI =
            "mongodb+srv://youhanamobdea_db_user:dwQQQcxMd1UHj1JT@cluster0.dmm6lxm.mongodb.net/?appName=Cluster0";
    private static final String DB_NAME = "HotelManagementSystem";

    private static MongoClient client = null;
    private static MongoDatabase database = null;
    private static boolean connectionSuccessful = false;

    static {
        try {
            // Suppress MongoDB logging warnings
            System.setProperty("org.slf4j.simpleLogger.log.org.mongodb.driver", "ERROR");

            System.out.println("Attempting to connect to MongoDB...");
            client = MongoClients.create(URI);
            database = client.getDatabase(DB_NAME);

            // Test connection with timeout
            System.out.println("Testing connection...");
            database.listCollectionNames().first();
            connectionSuccessful = true;
            System.out.println("✅ Successfully connected to MongoDB!");

        } catch (Exception e) {
            System.err.println("❌ Failed to connect to MongoDB!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\n⚠️  APP WILL RUN WITHOUT DATABASE");
            System.err.println("You can add data but it won't be saved to MongoDB.");
            System.err.println("\nTo fix MongoDB connection:");
            System.err.println("1. Check MongoDB Atlas - make sure cluster is running");
            System.err.println("2. Verify your cluster URL in MongoDB Atlas");
            System.err.println("3. Check Network Access settings");
            System.err.println("4. Check your internet connection\n");
        }
    }

    public static boolean isConnected() {
        return connectionSuccessful;
    }

    private static void checkConnection() {
        if (!connectionSuccessful) {
            throw new RuntimeException("Not connected to MongoDB. Check connection settings.");
        }
    }

    private static MongoCollection<Document> guestsCol() {
        checkConnection();
        return database.getCollection("guests");
    }

    private static MongoCollection<Document> roomsCol() {
        checkConnection();
        return database.getCollection("rooms");
    }

    private static MongoCollection<Document> reservationsCol() {
        checkConnection();
        return database.getCollection("reservations");
    }

    public static void saveGuests(ArrayList<Guest> guests) {
        try {
            checkConnection();
            guestsCol().deleteMany(new Document());
            for (Guest g : guests) {
                guestsCol().insertOne(new Document("guestID", g.getGuestID())
                        .append("name", g.getName())
                        .append("contactInfo", g.getContactInfo())
                        .append("address", g.getAddress()));
            }
            System.out.println("✅ Saved " + guests.size() + " guests to MongoDB");
        } catch (Exception e) {
            System.err.println("❌ Error saving guests: " + e.getMessage());
            throw new RuntimeException("Failed to save guests: " + e.getMessage());
        }
    }

    public static void saveRooms(ArrayList<Room> rooms) {
        try {
            checkConnection();
            roomsCol().deleteMany(new Document());
            for (Room r : rooms) {
                roomsCol().insertOne(new Document("roomID", r.getRoomID())
                        .append("roomType", r.getRoomType())
                        .append("available", r.isAvailable())
                        .append("price", r.getPricePerNight()));
            }
            System.out.println("✅ Saved " + rooms.size() + " rooms to MongoDB");
        } catch (Exception e) {
            System.err.println("❌ Error saving rooms: " + e.getMessage());
            throw new RuntimeException("Failed to save rooms: " + e.getMessage());
        }
    }

    public static void saveReservations(ArrayList<Reservation> reservations) {
        try {
            checkConnection();
            reservationsCol().deleteMany(new Document());
            for (Reservation r : reservations) {
                reservationsCol().insertOne(new Document("reservationID", r.getReservationID())
                        .append("guestID", r.getGuestID())
                        .append("roomID", r.getRoomID())
                        .append("checkIn", r.getCheckInDate())
                        .append("checkOut", r.getCheckOutDate()));
            }
            System.out.println("✅ Saved " + reservations.size() + " reservations to MongoDB");
        } catch (Exception e) {
            System.err.println("❌ Error saving reservations: " + e.getMessage());
            throw new RuntimeException("Failed to save reservations: " + e.getMessage());
        }
    }

    public static ArrayList<Guest> loadGuests() {
        ArrayList<Guest> list = new ArrayList<>();
        try {
            checkConnection();
            for (Document d : guestsCol().find()) {
                list.add(new Guest(d.getInteger("guestID"),
                        d.getString("name"),
                        d.getString("contactInfo"),
                        d.getString("address")));
            }
            System.out.println("✅ Loaded " + list.size() + " guests from MongoDB");
        } catch (Exception e) {
            System.err.println("⚠️  Could not load guests: " + e.getMessage());
        }
        return list;
    }

    public static ArrayList<Room> loadRooms() {
        ArrayList<Room> list = new ArrayList<>();
        try {
            checkConnection();
            for (Document d : roomsCol().find()) {
                Room r = new Room(d.getInteger("roomID"),
                        d.getString("roomType"),
                        2,
                        d.getDouble("price"));
                if (!d.getBoolean("available")) r.assignRoom();
                list.add(r);
            }
            System.out.println("✅ Loaded " + list.size() + " rooms from MongoDB");
        } catch (Exception e) {
            System.err.println("⚠️  Could not load rooms: " + e.getMessage());
        }
        return list;
    }

    public static ArrayList<Reservation> loadReservations() {
        ArrayList<Reservation> list = new ArrayList<>();
        try {
            checkConnection();
            for (Document d : reservationsCol().find()) {
                list.add(new Reservation(d.getInteger("reservationID"),
                        d.getInteger("guestID"),
                        d.getInteger("roomID"),
                        d.getString("checkIn"),
                        d.getString("checkOut")));
            }
            System.out.println("✅ Loaded " + list.size() + " reservations from MongoDB");
        } catch (Exception e) {
            System.err.println("⚠️  Could not load reservations: " + e.getMessage());
        }
        return list;
    }

    public static boolean testConnection() {
        return connectionSuccessful;
    }

    public static void closeConnection() {
        if (client != null) {
            client.close();
            System.out.println("MongoDB connection closed");
        }
    }
}