import java.util.*;
import java.util.concurrent.*;
public class BookMyStayApp {
    static class InvalidBookingException extends Exception {
        public InvalidBookingException(String message) {
            super(message);
        }
    }

    static class CancellationException extends Exception {
        public CancellationException(String message) {
            super(message);
        }
    }

    // -----------------------------
    // Reservation
    // -----------------------------
    static class Reservation {
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
    static class Service {
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
    private final Queue<Reservation> bookingQueue = new LinkedList<>();
    private final Map<String, Integer> inventory = new HashMap<>();
    private final Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private final Stack<String> rollbackStack = new Stack<>();
    private final Map<String, List<Service>> reservationServices = new HashMap<>();
    private final List<Reservation> bookingHistory = new ArrayList<>();

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
    // Thread-safe Booking Submission
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

    // -----------------------------
    // Thread-safe Processing of Booking Requests
    // -----------------------------
    public void processNextRequest() {
        Reservation reservation;
        synchronized (this) {
            reservation = bookingQueue.poll();
        }
        if (reservation == null) {
            System.out.println("No requests to process.");
            return;
        }

        synchronized (this) { // Critical section for allocation
            String roomType = reservation.getRoomType();
            try {
                validateRoomType(roomType);
                validateInventory(roomType);

                // Generate unique room ID
                String roomId;
                do {
                    roomId = roomType.substring(0, 1).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 5);
                } while (allocatedRooms.get(roomType).contains(roomId));

                reservation.assignRoom(roomId);
                allocatedRooms.get(roomType).add(roomId);
                rollbackStack.push(roomId);
                inventory.put(roomType, inventory.get(roomType) - 1);
                bookingHistory.add(reservation);

                System.out.println("Reservation confirmed: " + reservation);
                System.out.println("Remaining " + roomType + " rooms: " + inventory.get(roomType));

            } catch (InvalidBookingException e) {
                System.out.println("Failed to process reservation for " + reservation.getGuestName() + ": " + e.getMessage());
            }
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
    // Concurrent Booking Simulation
    // -----------------------------
    public static void main(String[] args) throws InterruptedException {
        BookMyStayApp app = new BookMyStayApp();

        // Simulate 5 guests submitting requests concurrently
        ExecutorService executor = Executors.newFixedThreadPool(5);
        String[] guestNames = {"Alice", "Bob", "Charlie", "David", "Eva"};
        String[] roomTypes = {"Deluxe", "Standard", "Suite", "Deluxe", "Standard"};
        double[] prices = {150.0, 100.0, 250.0, 150.0, 100.0};

        for (int i = 0; i < guestNames.length; i++) {
            final int index = i;
            executor.submit(() -> app.submitRequest(guestNames[index], roomTypes[index], prices[index]));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n--- Processing Bookings Concurrently ---\n");

        // Simulate multiple threads processing bookings
        ExecutorService processor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            processor.submit(app::processNextRequest);
        }

        processor.shutdown();
        processor.awaitTermination(5, TimeUnit.SECONDS);

        // Display final state
        app.displayBookingHistory();
        app.displayInventory();
    }
}
