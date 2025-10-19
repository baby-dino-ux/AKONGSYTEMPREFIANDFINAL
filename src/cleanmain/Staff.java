package cleanmain;

import config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Staff {

    private final Scanner sc = new Scanner(System.in);
    private final config db = new config();
    private final int userId;

    /**
     * Staff constructor – links to logged-in user
     */
    public Staff(int userId) {
        this.userId = userId;
    }

    /**
     * Main Staff Dashboard Menu
     */
    public void showDashboard() {
        int choiceStaff = 0;

        do {
            System.out.println("\n===============================");
            System.out.println("        STAFF DASHBOARD        ");
            System.out.println("===============================");
            System.out.println("1. View Service Packages");
            System.out.println("2. View Employee Availability");
            System.out.println("3. Create Booking");
            System.out.println("4. View My Bookings");
            System.out.println("5. Update Booking Status");
            System.out.println("6. Generate Receipt");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");

            choiceStaff = readInt();

            switch (choiceStaff) {
                case 1:
                    viewServicePackages();
                    break;
                case 2:
                    viewAvailableEmployees();
                    break;
                case 3:
                    createBooking();
                    break;
                case 4:
                    viewMyBookings();
                    break;
                case 5:
                    updateBookingStatus();
                    break;
                case 6:
                    generateReceipt();
                    break;
                case 7:
                    System.out.println("Logging out... Returning to main menu.");
                    return;
                default:
                    System.out.println("❌ Invalid choice! Please enter 1–7.");
            }
        } while (choiceStaff != 7);
    }

    /**
     * 1️⃣ View available service packages
     */
    private void viewServicePackages() {
        System.out.println("\n--- SERVICE PACKAGES ---");
        String svcQuery = "SELECT * FROM tbl_service";
        String[] svcHeaders = {"ID", "Name", "Description", "Price"};
        String[] svcCols = {"s_id", "s_name", "s_description", "s_price"};
        db.viewRecords(svcQuery, svcHeaders, svcCols);
    }

    /**
     * 2️⃣ View employee availability
     */
    private void viewAvailableEmployees() {
        System.out.println("\n--- EMPLOYEE AVAILABILITY ---");
        String empQuery = "SELECT * FROM tbl_employee";
        String[] empHeaders = {"ID", "Name", "Role", "Status"};
        String[] empCols = {"e_id", "e_name", "e_role", "e_status"};
        db.viewRecords(empQuery, empHeaders, empCols);
    }

    /**
     * 3️⃣ Create a new booking – with availability check
     */
    private void createBooking() {
        System.out.println("\n--- CREATE NEW BOOKING ---");

        // Show available services first
        System.out.println("\nAVAILABLE SERVICES:");
        String svcQuery = "SELECT * FROM tbl_service";
        String[] svcHeaders = {"ID", "Name", "Description", "Price"};
        String[] svcCols = {"s_id", "s_name", "s_description", "s_price"};
        db.viewRecords(svcQuery, svcHeaders, svcCols);

        System.out.print("\nEnter Service ID: ");
        int sId = readInt();

        // Show available employees
        System.out.println("\nAVAILABLE EMPLOYEES:");
        String availableEmpQuery = "SELECT * FROM tbl_employee WHERE e_status = 'Available'";
        String[] aEmpHeaders = {"ID", "Name", "Role", "Status"};
        String[] aEmpCols = {"e_id", "e_name", "e_role", "e_status"};
        db.viewRecords(availableEmpQuery, aEmpHeaders, aEmpCols);

        System.out.print("\nEnter Employee ID (from available list): ");
        int eId = readInt();

        System.out.print("Enter Booking Date (YYYY-MM-DD): ");
        String bDate = readLine();

        String addBooking = "INSERT INTO tbl_booking(user_id, service_id, employee_id, b_date, b_status) VALUES (?, ?, ?, ?, ?)";
        try {
            db.addRecord(addBooking, userId, sId, eId, bDate, "Pending");

            // Set employee to busy
            String updateEmp = "UPDATE tbl_employee SET e_status = ? WHERE e_id = ?";
            db.updateRecord(updateEmp, "Busy", eId);

            System.out.println("✅ Booking created successfully! Employee marked as Busy.");
            System.out.println("\n💡 Tip: Go to 'View My Bookings' (option 4) to see your booking details.");
        } catch (Exception ex) {
            System.out.println("❌ Failed to create booking: " + ex.getMessage());
        }
    }

    /**
     * 4️⃣ View all bookings made by this staff member
     */
    private void viewMyBookings() {
        System.out.println("\n--- MY BOOKINGS ---");
        String myBookingsQuery = "SELECT b.b_id, s.s_name, s.s_price, e.e_name, b.b_date, b.b_status " +
                "FROM tbl_booking b " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "JOIN tbl_employee e ON b.employee_id = e.e_id " +
                "WHERE b.user_id = ?";
        
        try {
            List<Map<String, Object>> bookings = db.fetchRecords(myBookingsQuery, userId);
            
            if (bookings == null || bookings.isEmpty()) {
                System.out.println("No bookings found.");
            } else {
                System.out.println("─────────────────────────────────────────────────────────────────────");
                System.out.printf("%-5s %-20s %-15s %-12s %-10s %-10s%n", 
                    "ID", "Service", "Employee", "Date", "Price", "Status");
                System.out.println("─────────────────────────────────────────────────────────────────────");
                
                for (Map<String, Object> booking : bookings) {
                    System.out.printf("%-5s %-20s %-15s %-12s ₱%-9s %-10s%n",
                        safeGet(booking, "b_id"),
                        truncate(safeGet(booking, "s_name"), 20),
                        truncate(safeGet(booking, "e_name"), 15),
                        safeGet(booking, "b_date"),
                        safeGet(booking, "s_price"),
                        safeGet(booking, "b_status"));
                }
                System.out.println("─────────────────────────────────────────────────────────────────────");
            }
        } catch (Exception ex) {
            System.out.println("❌ Error fetching bookings: " + ex.getMessage());
        }
    }

    /**
     * 5️⃣ Update booking status manually
     */
    private void updateBookingStatus() {
        System.out.println("\n--- UPDATE BOOKING STATUS ---");
        
        // Show user's bookings first
        viewMyBookings();
        
        System.out.print("\nEnter Booking ID to update: ");
        int bId = readInt();
        
        System.out.println("\nSelect Status:");
        System.out.println("1. Pending");
        System.out.println("2. Confirmed");
        System.out.println("3. Completed");
        System.out.println("4. Cancelled");
        System.out.print("Enter choice (1-4): ");
        int statusChoice = readInt();
        
        String status;
        switch (statusChoice) {
            case 1:
                status = "Pending";
                break;
            case 2:
                status = "Confirmed";
                break;
            case 3:
                status = "Completed";
                break;
            case 4:
                status = "Cancelled";
                break;
            default:
                System.out.println("❌ Invalid choice!");
                return;
        }
        
        String updateStatus = "UPDATE tbl_booking SET b_status = ? WHERE b_id = ? AND user_id = ?";
        try {
            db.updateRecord(updateStatus, status, bId, userId);
            System.out.println("✅ Booking status updated to: " + status);
        } catch (Exception ex) {
            System.out.println("❌ Failed to update booking status: " + ex.getMessage());
        }
    }

    /**
     * 6️⃣ Generate a receipt for a booking
     */
    private void generateReceipt() {
        System.out.println("\n--- GENERATE RECEIPT ---");
        
        // Show user's bookings first
        viewMyBookings();
        
        System.out.print("\nEnter Booking ID to generate receipt: ");
        int bId = readInt();

        String rQuery = "SELECT b.b_id, u.u_name, s.s_name, s.s_price, e.e_name, b.b_date, b.b_status " +
                "FROM tbl_booking b " +
                "JOIN tbl_user u ON b.user_id = u.u_id " +
                "JOIN tbl_service s ON b.service_id = s.s_id " +
                "JOIN tbl_employee e ON b.employee_id = e.e_id " +
                "WHERE b.b_id = ? AND b.user_id = ?";

        try {
            List<Map<String, Object>> rec = db.fetchRecords(rQuery, bId, userId);
            if (rec != null && !rec.isEmpty()) {
                Map<String, Object> r = rec.get(0);
                System.out.println("\n╔═══════════════════════════════════════╗");
                System.out.println("║        BOOKING RECEIPT                 ║");
                System.out.println("╠═══════════════════════════════════════╣");
                System.out.println("║ Booking ID: " + padRight(safeGet(r, "b_id"), 26) + "║");
                System.out.println("║ Customer:   " + padRight(safeGet(r, "u_name"), 26) + "║");
                System.out.println("║ Service:    " + padRight(safeGet(r, "s_name"), 26) + "║");
                System.out.println("║ Employee:   " + padRight(safeGet(r, "e_name"), 26) + "║");
                System.out.println("║ Date:       " + padRight(safeGet(r, "b_date"), 26) + "║");
                System.out.println("║ Status:     " + padRight(safeGet(r, "b_status"), 26) + "║");
                System.out.println("╠═══════════════════════════════════════╣");
                System.out.println("║ Total Price: ₱" + padRight(safeGet(r, "s_price"), 24) + "║");
                System.out.println("╚═══════════════════════════════════════╝");
            } else {
                System.out.println("❌ Booking not found or doesn't belong to you!");
            }
        } catch (Exception ex) {
            System.out.println("❌ Error generating receipt: " + ex.getMessage());
        }
    }

    /**
     * Safely read integer input
     */
    private int readInt() {
        try {
            String line = sc.nextLine();
            return Integer.parseInt(line.trim());
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * Safely read text input
     */
    private String readLine() {
        try {
            String line = sc.nextLine();
            if (line.isEmpty()) {
                line = sc.nextLine();
            }
            return line.trim();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Avoid null values when fetching from map
     */
    private String safeGet(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : v.toString();
    }

    /**
     * Truncate string to max length
     */
    private String truncate(String str, int maxLen) {
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    /**
     * Pad string to the right
     */
    private String padRight(String str, int length) {
        if (str.length() >= length) {
            return str.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }
}