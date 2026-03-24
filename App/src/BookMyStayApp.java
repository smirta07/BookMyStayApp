import java.util.*;
public class BookMyStayApp {
    public static void main(String[] args) {

        // Create room objects
        Room single = new SingleRoom();
        Room doubleroom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Store room objects (Domain Layer)
        List<Room> rooms = new ArrayList<>();
        rooms.add(single);
        rooms.add(doubleroom);
        rooms.add(suite);

        // Initialize inventory (State Layer)
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 5);
        inventory.addRoom("Double Room", 0); // unavailable
        inventory.addRoom("Suite Room", 2);

        // Create Search Service
        SearchService searchService = new SearchService();

        // Perform search (READ-ONLY)
        System.out.println("=== Available Rooms ===\n");
        searchService.searchAvailableRooms(rooms, inventory);

        System.out.println("\nApplication terminated.");
    }
}

/**
 * SearchService handles read-only operations.
 */
class SearchService {

    public void searchAvailableRooms(List<Room> rooms, RoomInventory inventory) {

        for (Room room : rooms) {

            int available = inventory.getAvailability(room.getRoomType());

            // Defensive check: only show available rooms
            if (available > 0) {
                room.displayDetails();
                System.out.println("Available: " + available);
                System.out.println("-----------------------------");
            }
        }
    }
}

/**
 * Centralized Inventory (State Holder)
 */
class RoomInventory {

    private Map<String, Integer> availabilityMap;

    public RoomInventory() {
        availabilityMap = new HashMap<>();
    }

    public void addRoom(String roomType, int count) {
        availabilityMap.put(roomType, count);
    }

    public int getAvailability(String roomType) {
        return availabilityMap.getOrDefault(roomType, 0);
    }

    // NOTE: No update method used in search → read-only safety
}

/**
 * Abstract Room class (Domain Model)
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
