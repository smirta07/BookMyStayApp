import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class BookMyStayApp implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PERSISTENCE_FILE = "bookmyStayData.ser";

    // -----------------------------
    // Custom Exceptions
    // -----------------------------
    static class InvalidBookingException extends Exception {
        public InvalidBookingException(String message) { super(message); }
    }

    static class CancellationException extends Exception {
        public CancellationException(String message) { super(message); }
    }

    // -----------------------------
    // Reservation
    // -----------------------------
    static class Reservation implements Serializable {
        private String requestId;
        private String guestName;
        private String roomType;
        private String assignedRoomId;
        private double basePrice;
        private boolean isCancelled = false;

        public Reservation(String guestName, String roomType, double basePrice) {
            this.requestId = UUID.randomUUID().toString();
            this.guestName = guestName;
            this.roomType = roomType;
            this.basePrice = basePrice;
        }

        public String getRequestId() { return requestId; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        public String getAssignedRoomId() { return assignedRoomId; }
        public boolean isCancelled() { return isCancelled; }

        public void assignRoom(String roomId) { this.assignedRoomId = roomId; }
        public void cancel() { this.isCancelled = true; }

        @Override
        public String toString() {
            return "Reservation{" +
                    "requestId='" + requestId + '\'' +
                    ", guestName='" + guestName + '\'' +
                    ", roomType='" + roomType + '\'' +
                    ", assignedRoomId='" + assignedRoomId + '\'' +
                    ", basePrice=$" + basePrice +
                    ", cancelled=" + isCancelled +
                    '}';
        }
    }

    // -----------------------------
    // Add-On Service
    // -----------------------------
    static class Service implements Serializable {
        private String name;
        private double price;
        public Service(String name, double price) { this.name = name; this.price = price; }
        public double getPrice() { return price; }
        @Override
        public String toString() { return name + " ($" + price + ")"; }
    }

    // -----------------------------
    // Shared Booking Data
    // -----------------------------
    private Queue<Reservation> bookingQueue = new LinkedList<>();
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private Stack<String> rollbackStack = new Stack<>();
    private Map<String, List<Service>> reservationServices = new HashMap<>();
    private List<Reservation> bookingHistory = new ArrayList<>();

    public BookMyStayApp() {
        inventory.put("Deluxe", 2);
        inventory.put("Standard", 3);
        inventory.put("Suite", 1);

        allocatedRooms.put("Deluxe", new HashSet<>());
        allocatedRooms.put("Standard", new HashSet<>());
        allocatedRooms.put("Suite", new HashSet<>());
    }

    // -----------------------------
    // Validation Methods
    // -----------------------------
    private void validateRoomType(String roomType) throws InvalidBookingException {
        if (!inventory.containsKey(roomType)) throw new InvalidBookingException("Invalid room type: " + roomType);
    }

    private void validatePrice(double price) throws InvalidBookingException {
        if (price < 0) throw new InvalidBookingException("Base price cannot be negative: " + price);
    }

    private void validateInventory(String roomType) throws InvalidBookingException {
        int available = inventory.getOrDefault(roomType, 0);
        if (available <= 0) throw new InvalidBookingException("No available rooms for type: " + roomType);
    }

    // -----------------------------
    // Booking Submission & Processing
    // -----------------------------
    public synchronized void submitRequest(String guestName, String roomType, double basePrice) {
        try {
            validateRoomType(roomType);
            validatePrice(basePrice);
            Reservation reservation = new Reservation(guestName, roomType, basePrice);
            bookingQueue.offer(reservation);
            System.out.println("Request added: " + reservation);
        } catch (InvalidBookingException e) {
            System.out.println("Booking submission failed: " + e.getMessage());
        }
    }

    public void processNextRequest() {
        Reservation reservation;
        synchronized (this) { reservation = bookingQueue.poll(); }
        if (reservation == null) { System.out.println("No requests to process."); return; }

        synchronized (this) { // Critical section
            try {
                validateRoomType(reservation.getRoomType());
                validateInventory(reservation.getRoomType());

                String roomId;
                do {
                    roomId = reservation.getRoomType().substring(0, 1).toUpperCase() + "-" +
                            UUID.randomUUID().toString().substring(0, 5);
                } while (allocatedRooms.get(reservation.getRoomType()).contains(roomId));

                reservation.assignRoom(roomId);
                allocatedRooms.get(reservation.getRoomType()).add(roomId);
                rollbackStack.push(roomId);
                inventory.put(reservation.getRoomType(), inventory.get(reservation.getRoomType()) - 1);
                bookingHistory.add(reservation);

                System.out.println("Reservation confirmed: " + reservation);
                System.out.println("Remaining " + reservation.getRoomType() + " rooms: " +
                        inventory.get(reservation.getRoomType()));

            } catch (InvalidBookingException e) {
                System.out.println("Failed to process reservation for " + reservation.getGuestName() + ": " + e.getMessage());
            }
        }
    }

    // -----------------------------
    // Data Persistence
    // -----------------------------
    public void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PERSISTENCE_FILE))) {
            synchronized (this) {
                oos.writeObject(this);
            }
            System.out.println("System state saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save system state: " + e.getMessage());
        }
    }

    public static BookMyStayApp restoreState() {
        File file = new File(PERSISTENCE_FILE);
        if (!file.exists()) {
            System.out.println("No previous state found. Starting fresh.");
            return new BookMyStayApp();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            BookMyStayApp app = (BookMyStayApp) ois.readObject();
            System.out.println("System state restored successfully.");
            return app;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to restore system state: " + e.getMessage());
            System.out.println("Starting with a fresh system state.");
            return new BookMyStayApp();
        }
    }

    // -----------------------------
    // Display Booking History & Inventory
    // -----------------------------
    public synchronized void displayBookingHistory() {
        System.out.println("\nBooking History:");
        for (Reservation r : bookingHistory) System.out.println(r);
    }

    public synchronized void displayInventory() {
        System.out.println("\nInventory Status:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue() + " available");
        }
    }

    // -----------------------------
    // Main Method (Demo)
    // -----------------------------
    public static void main(String[] args) throws InterruptedException {
        // Restore previous state or start fresh
        BookMyStayApp app = BookMyStayApp.restoreState();

        // Submit new requests
        app.submitRequest("Alice", "Deluxe", 150.0);
        app.submitRequest("Bob", "Standard", 100.0);

        // Process bookings
        app.processNextRequest();
        app.processNextRequest();

        // Display state
        app.displayBookingHistory();
        app.displayInventory();

        // Save state before shutdown
        app.saveState();
    }
}
