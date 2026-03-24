import java.util.*;
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
    // Reservation (Guest Request)
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

        public String getRequestId() {
            return requestId;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }

        public String getAssignedRoomId() {
            return assignedRoomId;
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public void assignRoom(String roomId) {
            this.assignedRoomId = roomId;
        }

        public void cancel() {
            this.isCancelled = true;
        }

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

        public Service(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name + " ($" + price + ")";
        }
    }

    // -----------------------------
    // Booking Queue & Inventory
    // -----------------------------
    private Queue<Reservation> bookingQueue = new LinkedList<>();
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private Stack<String> rollbackStack = new Stack<>();

    // Add-On Services: ReservationID -> List<Service>
    private Map<String, List<Service>> reservationServices = new HashMap<>();

    // Booking History
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
        if (!inventory.containsKey(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }
    }

    private void validatePrice(double price) throws InvalidBookingException {
        if (price < 0) {
            throw new InvalidBookingException("Base price cannot be negative: " + price);
        }
    }

    private void validateInventory(String roomType) throws InvalidBookingException {
        int available = inventory.getOrDefault(roomType, 0);
        if (available <= 0) {
            throw new InvalidBookingException("No available rooms for type: " + roomType);
        }
    }

    // -----------------------------
    // Submit Booking Request
    // -----------------------------
    public void submitRequest(String guestName, String roomType, double basePrice) {
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
    // Process Next Request (Confirm & Allocate Room)
    // -----------------------------
    public void processNextRequest() {
        Reservation reservation = bookingQueue.poll();
        if (reservation == null) {
            System.out.println("No requests to process.");
            return;
        }

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
            rollbackStack.push(roomId);  // Track for potential cancellation
            inventory.put(roomType, inventory.get(roomType) - 1);

            bookingHistory.add(reservation);
            System.out.println("Reservation confirmed: " + reservation);
            System.out.println("Remaining " + roomType + " rooms: " + inventory.get(roomType));

        } catch (InvalidBookingException e) {
            System.out.println("Failed to process reservation for " + reservation.getGuestName() + ": " + e.getMessage());
        }
    }

    // -----------------------------
    // Booking Cancellation
    // -----------------------------
    public void cancelReservation(String reservationId) {
        try {
            Optional<Reservation> optionalReservation = bookingHistory.stream()
                    .filter(r -> r.getRequestId().equals(reservationId))
                    .findFirst();

            if (!optionalReservation.isPresent()) {
                throw new CancellationException("Reservation ID not found: " + reservationId);
            }

            Reservation reservation = optionalReservation.get();

            if (reservation.isCancelled()) {
                throw new CancellationException("Reservation already cancelled: " + reservationId);
            }

            // Rollback inventory
            String roomType = reservation.getRoomType();
            String roomId = reservation.getAssignedRoomId();

            if (!rollbackStack.isEmpty() && rollbackStack.peek().equals(roomId)) {
                rollbackStack.pop(); // LIFO rollback
            } else {
                rollbackStack.remove(roomId); // Remove from stack if out of order
            }

            allocatedRooms.get(roomType).remove(roomId);
            inventory.put(roomType, inventory.getOrDefault(roomType, 0) + 1);

            reservation.cancel();
            System.out.println("Reservation cancelled successfully: " + reservation);

        } catch (CancellationException e) {
            System.out.println("Cancellation failed: " + e.getMessage());
        }
    }

    // -----------------------------
    // Display Booking History & Inventory
    // -----------------------------
    public void displayBookingHistory() {
        if (bookingHistory.isEmpty()) {
            System.out.println("No confirmed reservations yet.");
            return;
        }
        System.out.println("\nBooking History:");
        for (Reservation r : bookingHistory) {
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

        // Submit and process bookings
        app.submitRequest("Alice", "Deluxe", 150.0);
        app.submitRequest("Bob", "Standard", 100.0);

        app.processNextRequest();
        app.processNextRequest();

        // Display history and inventory before cancellation
        app.displayBookingHistory();
        app.displayInventory();

        // Cancel Alice's reservation
        String aliceId = app.bookingHistory.get(0).getRequestId();
        app.cancelReservation(aliceId);

        // Attempt to cancel again (should fail)
        app.cancelReservation(aliceId);

        // Display final booking history and inventory
        app.displayBookingHistory();
        app.displayInventory();
    }
}
