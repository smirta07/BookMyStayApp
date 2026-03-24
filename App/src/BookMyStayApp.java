public class BookMyStayApp {
    public static void main(String[] args) {

        // Create room objects (Polymorphism)
        Room single = new SingleRoom();
        Room doubleroom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Static availability (simple variables)
        int singleAvailable = 5;
        int doubleAvailable = 3;
        int suiteAvailable = 2;

        // Display details
        System.out.println("=== Room Details & Availability ===\n");

        single.displayDetails();
        System.out.println("Available: " + singleAvailable);
        System.out.println("-----------------------------");

        doubleroom.displayDetails();
        System.out.println("Available: " + doubleAvailable);
        System.out.println("-----------------------------");

        suite.displayDetails();
        System.out.println("Available: " + suiteAvailable);
        System.out.println("-----------------------------");

        System.out.println("Application terminated.");
    }
}

/**
 * Abstract class representing a generic Room.
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

    public int getBeds() {
        return beds;
    }

    public double getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    // Abstract method
    public abstract String getRoomType();

    // Common method
    public void displayDetails() {
        System.out.println("Room Type: " + getRoomType());
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sq.ft");
        System.out.println("Price: $" + price);
    }
}

/**
 * Single Room implementation
 */
class SingleRoom extends Room {

    public SingleRoom() {
        super(1, 200, 50);
    }

    @Override
    public String getRoomType() {
        return "Single Room";
    }
}

/**
 * Double Room implementation
 */
class DoubleRoom extends Room {

    public DoubleRoom() {
        super(2, 350, 90);
    }

    @Override
    public String getRoomType() {
        return "Double Room";
    }
}

/**
 * Suite Room implementation
 */
class SuiteRoom extends Room {

    public SuiteRoom() {
        super(3, 600, 200);
    }

    @Override
    public String getRoomType() {
        return "Suite Room";
    }
}
