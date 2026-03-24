import java.util.LinkedList;
import java.util.Queue;
import java.time.LocalDateTime;
import java.util.UUID;
public class BookMyStayApp {
    static class Reservation {
        private String requestId;
        private String guestName;
        private String roomType;
        private LocalDateTime requestTime;

        public Reservation(String guestName, String roomType) {
            this.requestId = UUID.randomUUID().toString();
            this.guestName = guestName;
            this.roomType = roomType;
            this.requestTime = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return "Reservation{" +
                    "requestId='" + requestId + '\'' +
                    ", guestName='" + guestName + '\'' +
                    ", roomType='" + roomType + '\'' +
                    ", requestTime=" + requestTime +
                    '}';
        }
    }

    // -----------------------------
    // Booking Queue
    // -----------------------------
    private Queue<Reservation> bookingQueue;

    // Constructor (matches class name exactly!)
    public BookMyStayApp() {
        bookingQueue = new LinkedList<>();
    }

    // Submit booking request
    public void submitRequest(String guestName, String roomType) {
        Reservation reservation = new Reservation(guestName, roomType);
        bookingQueue.offer(reservation);
        System.out.println("Request added to queue: " + reservation);
    }

    // Process next request (FIFO)
    public Reservation processNextRequest() {
        Reservation reservation = bookingQueue.poll();
        if (reservation != null) {
            System.out.println("Processing request: " + reservation);
        } else {
            System.out.println("No requests to process.");
        }
        return reservation;
    }

    // Display all queued requests
    public void displayQueue() {
        if (bookingQueue.isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }
        System.out.println("\nCurrent Booking Queue:");
        for (Reservation r : bookingQueue) {
            System.out.println(r);
        }
    }

    // -----------------------------
    // Main Method (Demo)
    // -----------------------------
    public static void main(String[] args) {

        BookMyStayApp app = new BookMyStayApp();

        app.submitRequest("Alice", "Deluxe");
        app.submitRequest("Bob", "Standard");
        app.submitRequest("Charlie", "Suite");

        app.displayQueue();

        app.processNextRequest();
        app.processNextRequest();

        app.displayQueue();
    }
}
