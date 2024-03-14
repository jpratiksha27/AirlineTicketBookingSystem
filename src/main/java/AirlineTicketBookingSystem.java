import java.sql.*;
import java.util.Scanner;

public class AirlineTicketBookingSystem {

    private static Connection connection;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:4306/Airline", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (connection != null) {
            System.out.println("Connected to the database.");
            displayMenu();
        } else {
            System.out.println("Failed to connect to the database.");
        }
    }

    private static void displayMenu() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\nWelcome to the Airline Ticket Booking System");
            System.out.println("1. Insert Passenger Details");
            System.out.println("2. Insert Flight Details");
            System.out.println("3. Book Ticket");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. Display Vacancy Seat Details");
            System.out.println("6. Update Passenger Information");
            System.out.println("7. Update Flight Details");
            System.out.println("8. Exit");

            System.out.print("\nEnter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    insertPassengerDetails();
                    break;
                case 2:
                    insertFlightDetails();
                    break;
                case 3:
                    bookTicket();
                    break;
                case 4:
                    cancelTicket();
                    break;
                case 5:
                    displayVacancySeatDetails();
                    break;
                case 6:
                    updatePassengerInformation();
                    break;
                case 7:
                    updateFlightDetails();
                    break;
                case 8:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 8.");
            }
        }

        // Close the scanner and database connection before exiting the program
        scanner.close();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertPassengerDetails() {
    	try {
            System.out.println("Enter passenger name:");
            String name = scanner.next();
            System.out.println("Enter passenger age:");
            int age = scanner.nextInt();
            System.out.println("Enter passenger phone number:");
            String phoneNumber = scanner.next();

            String query = "INSERT INTO Passenger (name, age, phone_number) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.executeUpdate();
            System.out.println("Passenger details inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertFlightDetails() {
    	try {
            System.out.println("Enter flight number:");
            String flightNumber = scanner.next();
            System.out.println("Enter departure location:");
            String departure = scanner.next();
            System.out.println("Enter destination:");
            String destination = scanner.next();
            System.out.println("Enter capacity:");
            int capacity = scanner.nextInt();

            String query = "INSERT INTO Flight (flight_number, departure, destination, capacity, remaining_seats) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, flightNumber);
            preparedStatement.setString(2, departure);
            preparedStatement.setString(3, destination);
            preparedStatement.setInt(4, capacity);
            preparedStatement.setInt(5, capacity); // Initially, all seats are available
            preparedStatement.executeUpdate();
            System.out.println("Flight details inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void bookTicket() {
        // Implement method to book a ticket using JDBC
    	try {
            System.out.println("Enter passenger ID:");
            int passengerId = scanner.nextInt();
            System.out.println("Enter flight ID:");
            int flightId = scanner.nextInt();

            // Check if the selected flight has available seats
            PreparedStatement checkAvailabilityStatement = connection.prepareStatement("SELECT remaining_seats FROM Flight WHERE flight_id = ?");
            checkAvailabilityStatement.setInt(1, flightId);
            ResultSet resultSet = checkAvailabilityStatement.executeQuery();
            if (resultSet.next()) {
                int remainingSeats = resultSet.getInt("remaining_seats");
                if (remainingSeats > 0) {
                    // Book the ticket
                    PreparedStatement bookTicketStatement = connection.prepareStatement("INSERT INTO Ticket (passenger_id, flight_id) VALUES (?, ?)");
                    bookTicketStatement.setInt(1, passengerId);
                    bookTicketStatement.setInt(2, flightId);
                    bookTicketStatement.executeUpdate();
                    
                    // Update remaining seats in the Flight table
                    PreparedStatement updateSeatsStatement = connection.prepareStatement("UPDATE Flight SET remaining_seats = remaining_seats - 1 WHERE flight_id = ?");
                    updateSeatsStatement.setInt(1, flightId);
                    updateSeatsStatement.executeUpdate();
                    
                    System.out.println("Ticket booked successfully.");
                } else {
                    System.out.println("Sorry, no available seats for the selected flight.");
                }
            } else {
                System.out.println("Flight not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void cancelTicket() {
        // Implement method to cancel a ticket using JDBC
    	try {
            System.out.println("Enter ticket ID to cancel:");
            int ticketId = scanner.nextInt();
            
            // Get flight ID associated with the ticket
            PreparedStatement getFlightIdStatement = connection.prepareStatement("SELECT flight_id FROM Ticket WHERE ticket_id = ?");
            getFlightIdStatement.setInt(1, ticketId);
            ResultSet resultSet = getFlightIdStatement.executeQuery();
            int flightId = -1;
            if (resultSet.next()) {
                flightId = resultSet.getInt("flight_id");
            }

            if (flightId != -1) {
                // Cancel the ticket
                PreparedStatement cancelTicketStatement = connection.prepareStatement("DELETE FROM Ticket WHERE ticket_id = ?");
                cancelTicketStatement.setInt(1, ticketId);
                cancelTicketStatement.executeUpdate();
                
                // Update remaining seats in the Flight table
                PreparedStatement updateSeatsStatement = connection.prepareStatement("UPDATE Flight SET remaining_seats = remaining_seats + 1 WHERE flight_id = ?");
                updateSeatsStatement.setInt(1, flightId);
                updateSeatsStatement.executeUpdate();
                
                System.out.println("Ticket canceled successfully.");
            } else {
                System.out.println("Ticket not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayVacancySeatDetails() {
        // Implement method to display vacancy seat details for all flights using JDBC
    	try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Flight");
            System.out.println("Flight ID\tFlight Number\tDeparture\tDestination\tCapacity\tRemaining Seats");
            while (resultSet.next()) {
                int flightId = resultSet.getInt("flight_id");
                String flightNumber = resultSet.getString("flight_number");
                String departure = resultSet.getString("departure");
                String destination = resultSet.getString("destination");
                int capacity = resultSet.getInt("capacity");
                int remainingSeats = resultSet.getInt("remaining_seats");
                System.out.println(flightId + "\t\t" + flightNumber + "\t\t" + departure + "\t\t" + destination + "\t\t" + capacity + "\t\t" + remainingSeats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updatePassengerInformation() {
        // Implement method to update passenger information using JDBC
    	try {
            System.out.println("Enter passenger ID to update:");
            int passengerId = scanner.nextInt();
            System.out.println("Enter new name:");
            String newName = scanner.next();
            System.out.println("Enter new age:");
            int newAge = scanner.nextInt();
            System.out.println("Enter new phone number:");
            String newPhoneNumber = scanner.next();

            PreparedStatement updateStatement = connection.prepareStatement("UPDATE Passenger SET name = ?, age = ?, phone_number = ? WHERE passenger_id = ?");
            updateStatement.setString(1, newName);
            updateStatement.setInt(2, newAge);
            updateStatement.setString(3, newPhoneNumber);
            updateStatement.setInt(4, passengerId);
            updateStatement.executeUpdate();
            
            System.out.println("Passenger information updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateFlightDetails() {
        // Implement method to update flight details using JDBC
    	try {
            System.out.println("Enter flight ID to update:");
            int flightId = scanner.nextInt();
            System.out.println("Enter new flight number:");
            String newFlightNumber = scanner.next();
            System.out.println("Enter new departure location:");
            String newDeparture = scanner.next();
            System.out.println("Enter new destination:");
            String newDestination = scanner.next();
            System.out.println("Enter new capacity:");
            int newCapacity = scanner.nextInt();

            PreparedStatement updateStatement = connection.prepareStatement("UPDATE Flight SET flight_number = ?, departure = ?, destination = ?, capacity = ?, remaining_seats = ? WHERE flight_id = ?");
            updateStatement.setString(1, newFlightNumber);
            updateStatement.setString(2, newDeparture);
            updateStatement.setString(3, newDestination);
            updateStatement.setInt(4, newCapacity);
            updateStatement.setInt(5, newCapacity); // Initially, all seats are available
            updateStatement.setInt(6, flightId);
            updateStatement.executeUpdate();
            
            System.out.println("Flight information updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
