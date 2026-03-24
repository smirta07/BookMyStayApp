import java.util.HashMap;
import java.util.Map;
public class BookMyStayApp {
    public static void main(String[] args) {

        // Create room objects
        Room single = new SingleRoom();
        Room doubleroom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Initialize inventory (Single Source of Truth)
        RoomInventory inventory = new RoomInventory();

        inventory.addRoom(single.getRoomType(), 5);
        inventory.addRoom(doubleroom.getRoomType(), 3);
        inventory.addRoom(suite.getRoomType(), 2);

        // Display room details with availability
        System.out.println("=== Centralized Room Inventory ===\n");

        displayRoom(single, inventory);
        displayRoom(doubleroom, inventory);
        displayRoom(suite, inventory);

        // Example update
        System.out.println("\nBooking 1 Single Room...\n");
        inventory.updateAvailability("Single Room", -1);

        displayRoom(single, inventory);

        System.out.println("\nApplication terminated.");
    }

    // Helper method (demonstrates clean separation)
    public static void displayRoom(Room room, RoomInventory inventory) {
        room.displayDetails();
        System.out.println("Available: " + inventory.getAvailability(room.getRoomType()));
        System.out.println("-----------------------------");
    }
}

/**
 * Inventory class managing all room availability.
 * Acts as a single source of truth.
 */
class RoomInventory {

    private Map<String, Integer> availabilityMap;

    // Constructor initializes the HashMap
    public RoomInventory() {
        availabilityMap = new HashMap<>();
    }

    // Add or initialize room type
    public void addRoom(String roomType, int count) {
        availabilityMap.put(roomType, count);
    }

    // Get availability (O(1) lookup)
    public int getAvailability(String roomType) {
        return availabilityMap.getOrDefault(roomType, 0);
    }

    // Update availability (controlled modification)
    public void updateAvailability(String roomType, int change) {
        int current = getAvailability(roomType);
        int updated = current + change;

        if (updated < 0) {
            System.out.println("Error: Not enough rooms available!");
            return;
        }

        availabilityMap.put(roomType, updated);
    }

    // Display entire inventory
    public void displayInventory() {
        System.out.println("=== Inventory Snapshot ===");
        for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}

/**
 * Abstract Room class
 */
abstract class Room {

    private int beds;
    private double size;
    private double price;

    public Room(int beds, double size, double price) {
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public abstract String getRoomType();

    public void displayDetails() {
        System.out.println("Room Type: " + getRoomType());
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sq.ft");
        System.out.println("Price: $" + price);
    }
}

/**
 * Single Room
 */
class SingleRoom extends Room {
    public SingleRoom() {
        super(1, 200, 50);
    }

    public String getRoomType() {
        return "Single Room";
    }
}

/**
 * Double Room
 */
class DoubleRoom extends Room {
    public DoubleRoom() {
        super(2, 350, 90);
    }

    public String getRoomType() {
        return "Double Room";
    }
}

/**
 * Suite Room
 */
class SuiteRoom extends Room {
    public SuiteRoom() {
        super(3, 600, 200);
    }

    public String getRoomType() {
        return "Suite Room";
    }
}
