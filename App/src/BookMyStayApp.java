import java.util.*;
public class BookMyStayApp {
    static class Reservation {
        private String requestId;
        private String guestName;
        private String roomType;
        private String assignedRoomId;
        private double basePrice;

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
                    ", basePrice=$" + basePrice +
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
    // Booking Queue
    // -----------------------------
    private Queue<Reservation> bookingQueue = new LinkedList<>();

    // Inventory & Allocation
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();

    // Add-On Services: ReservationID -> List<Service>
    private Map<String, List<Service>> reservationServices = new HashMap<>();

    // -----------------------------
    // Booking History (Use Case 8)
    // -----------------------------
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
    // Submit Booking Request
    // -----------------------------
    public void submitRequest(String guestName, String roomType, double basePrice) {
        if (!inventory.containsKey(roomType)) {
            System.out.println("Invalid room type: " + roomType);
            return;
        }
        Reservation reservation = new Reservation(guestName, roomType, basePrice);
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

        String roomType = reservation.getRoomType();
        int available = inventory.getOrDefault(roomType, 0);

        if (available <= 0) {
            System.out.println("No available rooms for type: " + roomType + ". Cannot confirm reservation for " + reservation.getGuestName());
            return;
        }

        // Generate unique room ID
        String roomId;
        do {
            roomId = roomType.substring(0, 1).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 5);
        } while (allocatedRooms.get(roomType).contains(roomId));

        reservation.assignRoom(roomId);
        allocatedRooms.get(roomType).add(roomId);
        inventory.put(roomType, available - 1);

        // Add to booking history (chronological order preserved)
        bookingHistory.add(reservation);

        System.out.println("Reservation confirmed: " + reservation);
        System.out.println("Remaining " + roomType + " rooms: " + inventory.get(roomType));
    }

    // -----------------------------
    // Add-On Service Selection
    // -----------------------------
    public void addServiceToReservation(String reservationId, Service service) {
        List<Service> services = reservationServices.getOrDefault(reservationId, new ArrayList<>());
        services.add(service);
        reservationServices.put(reservationId, services);
        System.out.println("Added service to reservation " + reservationId + ": " + service);
    }

    public double calculateTotalServiceCost(String reservationId) {
        List<Service> services = reservationServices.getOrDefault(reservationId, Collections.emptyList());
        return services.stream().mapToDouble(Service::getPrice).sum();
    }

    public void displayReservationServices(String reservationId) {
        List<Service> services = reservationServices.getOrDefault(reservationId, Collections.emptyList());
        if (services.isEmpty()) {
            System.out.println("No services selected for reservation " + reservationId);
        } else {
            System.out.println("Services for reservation " + reservationId + ": " + services);
            System.out.println("Total additional cost: $" + calculateTotalServiceCost(reservationId));
        }
    }

    // -----------------------------
    // Reporting & History
    // -----------------------------
    public void displayBookingHistory() {
        if (bookingHistory.isEmpty()) {
            System.out.println("No confirmed reservations yet.");
            return;
        }
        System.out.println("\nBooking History (chronological):");
        for (Reservation r : bookingHistory) {
            System.out.println(r);
        }
    }

    public void generateSummaryReport() {
        System.out.println("\nBooking Summary Report:");
        Map<String, Long> countByRoomType = new HashMap<>();
        for (Reservation r : bookingHistory) {
            countByRoomType.put(r.getRoomType(), countByRoomType.getOrDefault(r.getRoomType(), 0L) + 1);
        }

        for (Map.Entry<String, Long> entry : countByRoomType.entrySet()) {
            System.out.println("Room Type: " + entry.getKey() + " | Bookings: " + entry.getValue());
        }
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
        app.submitRequest("Alice", "Deluxe", 150.0);
        app.submitRequest("Bob", "Standard", 100.0);
        app.submitRequest("Charlie", "Suite", 250.0);

        // Process all bookings
        app.processNextRequest();
        app.processNextRequest();
        app.processNextRequest();

        // Add-on services
        String aliceRoomId = app.allocatedRooms.get("Deluxe").iterator().next();
        app.addServiceToReservation(aliceRoomId, new Service("Breakfast", 20.0));
        app.addServiceToReservation(aliceRoomId, new Service("Spa", 50.0));

        // Display history and reports
        app.displayBookingHistory();
        app.generateSummaryReport();
    }
}
