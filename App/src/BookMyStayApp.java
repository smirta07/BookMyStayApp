import java.util.*;
public class BookMyStayApp {
    static class Reservation {
        private String requestId;
        private String guestName;
        private String roomType;
        private String assignedRoomId; // Assigned when confirmed

        public Reservation(String guestName, String roomType) {
            this.requestId = UUID.randomUUID().toString();
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public void assignRoom(String roomId) {
            this.assignedRoomId = roomId;
        }

        @Override
        public String toString() {
            return "Reservation{" +
                    "requestId='" + requestId + '\'' +
                    ", guestName='" + guestName + '\'' +
                    ", roomType='" + roomType + '\'' +
                    ", assignedRoomId='" + assignedRoomId + '\'' +
                    '}';
        }
    }

    // -----------------------------
    // Booking Queue (FIFO)
    // -----------------------------
    private Queue<Reservation> bookingQueue = new LinkedList<>();

    // -----------------------------
    // Inventory & Allocation
    // -----------------------------
    private Map<String, Integer> inventory = new HashMap<>(); // Room type -> available count
    private Map<String, Set<String>> allocatedRooms = new HashMap<>(); // Room type -> allocated room IDs

    public BookMyStayApp() {
        // Initialize room inventory
        inventory.put("Deluxe", 2);
        inventory.put("Standard", 3);
        inventory.put("Suite", 1);

        // Initialize allocatedRooms map
        allocatedRooms.put("Deluxe", new HashSet<>());
        allocatedRooms.put("Standard", new HashSet<>());
        allocatedRooms.put("Suite", new HashSet<>());
    }

    // -----------------------------
    // Submit Booking Request
    // -----------------------------
    public void submitRequest(String guestName, String roomType) {
        if (!inventory.containsKey(roomType)) {
            System.out.println("Invalid room type: " + roomType);
            return;
        }
        Reservation reservation = new Reservation(guestName, roomType);
        bookingQueue.offer(reservation);
        System.out.println("Request added: " + reservation);
    }

    // -----------------------------
    // Process Next Request (Confirm & Allocate Room)
    // -----------------------------
    public void processNextRequest() {
        Reservation reservation = bookingQueue.poll();
        if (reservation == null) {
            System.out.println("No requests to process.");
            return;
        }

        String roomType = reservation.roomType;
        int available = inventory.getOrDefault(roomType, 0);

        if (available <= 0) {
            System.out.println("No available rooms for type: " + roomType + ". Cannot confirm reservation for " + reservation.guestName);
            return;
        }

        // Generate a unique room ID
        String roomId;
        do {
            roomId = roomType.substring(0, 1).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 5);
        } while (allocatedRooms.get(roomType).contains(roomId));

        // Assign room
        reservation.assignRoom(roomId);
        allocatedRooms.get(roomType).add(roomId);

        // Update inventory
        inventory.put(roomType, available - 1);

        System.out.println("Reservation confirmed: " + reservation);
        System.out.println("Remaining " + roomType + " rooms: " + inventory.get(roomType));
    }

    // -----------------------------
    // Display Queue & Inventory
    // -----------------------------
    public void displayQueue() {
        if (bookingQueue.isEmpty()) {
            System.out.println("Booking queue is empty.");
            return;
        }
        System.out.println("\nCurrent Booking Queue:");
        for (Reservation r : bookingQueue) {
            System.out.println(r);
        }
    }

    public void displayInventory() {
        System.out.println("\nCurrent Inventory Status:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue() + " available");
        }
    }

    // -----------------------------
    // Main Method (Demo)
    // -----------------------------
    public static void main(String[] args) {
        BookMyStayApp app = new BookMyStayApp();

        // Submit requests
        app.submitRequest("Alice", "Deluxe");
        app.submitRequest("Bob", "Deluxe");
        app.submitRequest("Charlie", "Suite");
        app.submitRequest("David", "Deluxe"); // Exceeds inventory

        app.displayQueue();
        app.displayInventory();

        // Process all requests
        app.processNextRequest();
        app.processNextRequest();
        app.processNextRequest();
        app.processNextRequest(); // Should fail (no inventory)

        app.displayInventory();
    }
}
