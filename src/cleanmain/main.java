/*
 * Cleaning Service Booking System
 */
package cleanmain;

import config.config;
import java.util.Scanner;

public class main {

    // Reusable method: View all users
    public static void viewUser() {
        String votersQuery = "SELECT * FROM tbl_user";
        String[] votersHeaders = {"ID", "Name", "Email", "Address", "Contact Number", "Type", "Password", "Status"};
        String[] votersColumns = {"u_id", "u_name", "u_email", "u_address", "u_contact", "u_type", "u_pass", "u_status"};
        config conf = new config();
        conf.viewRecords(votersQuery, votersHeaders, votersColumns);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        config db = new config();
        db.connectDB();

        while (true) {
            System.out.println("===========================================");
            System.out.println("WELCOME TO CLEANING SERVICE BOOKING SYSTEM");
            System.out.println("===========================================");
            System.out.println("1. Log in");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter Your Choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1: // LOGIN
                    System.out.print("Enter User Email: ");
                    String ema = sc.next();
                    System.out.print("Enter Password: ");
                    String pas = sc.next();

                    // Fetch user by email
                    String qry = "SELECT * FROM tbl_user WHERE u_email = ?";
                    java.util.List<java.util.Map<String, Object>> result = db.fetchRecords(qry, ema);

                    if (result.isEmpty()) {
                        System.out.println("INVALID CREDENTIALS.");
                    } else {
                        java.util.Map<String, Object> user = result.get(0);
                        String storedPass = user.get("u_pass").toString();

                        // ✅ Try to match both plain text and hashed passwords
                        boolean passwordMatch = false;

                        try {
                            String hashedInput = config.hashPassword(pas);
                            if (storedPass.equals(pas) || storedPass.equals(hashedInput)) {
                                passwordMatch = true;
                            }
                        } catch (Exception e) {
                            // fallback if hashing fails
                            if (storedPass.equals(pas)) {
                                passwordMatch = true;
                            }
                        }

                        if (!passwordMatch) {
                            System.out.println("INVALID CREDENTIALS.");
                        } else {
                            String stat = user.get("u_status").toString();
                            String type = user.get("u_type").toString();
                            int userId = Integer.parseInt(user.get("u_id").toString());

                            if (stat.equalsIgnoreCase("Pending")) {
                                System.out.println("Account is Pending. Contact the Admin!");
                            } else {
                                System.out.println("LOGIN SUCCESS!");

                                if (type.equalsIgnoreCase("Admin")) {
                                    Admin admin = new Admin();
                                    admin.Admin(); // when Admin logs out, return here
                                } else if (type.equalsIgnoreCase("Staff")) {
                                    Staff staff = new Staff(userId);
                                    staff.showDashboard(); // when Staff logs out, return here
                                }

                                // After logout, continue to show main menu
                                System.out.println("\nReturning to Main Menu...\n");
                            }
                        }
                    }
                    break;

                case 2: // REGISTER
                    System.out.print("Enter User Name: ");
                    String name = sc.next();
                    System.out.print("Enter User Email: ");
                    String email = sc.next();

                    // Check for existing email
                    while (true) {
                        String qryCheck = "SELECT * FROM tbl_user WHERE u_email = ?";
                        java.util.List<java.util.Map<String, Object>> resultCheck = db.fetchRecords(qryCheck, email);

                        if (resultCheck.isEmpty()) {
                            break;
                        } else {
                            System.out.print("Email already exists, Enter another Email: ");
                            email = sc.next();
                        }
                    }

                    System.out.print("Enter User Address: ");
                    String address = sc.next();
                    System.out.print("Enter User Contact Number: ");
                    String contact = sc.next();

                    System.out.print("Enter user Type (1 - Admin / 2 - Staff): ");
                    int type = sc.nextInt();
                    while (type > 2 || type < 1) {
                        System.out.print("Invalid, choose between 1 & 2 only: ");
                        type = sc.nextInt();
                    }
                    String tp = (type == 1) ? "Admin" : "Staff";

                    System.out.print("Enter Password: ");
                    String pass = sc.next();

                    // ✅ Hash password before saving
                    String hashedPass = config.hashPassword(pass);

                    String sql = "INSERT INTO tbl_user(u_name, u_email, u_address, u_contact, u_type, u_pass, u_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    db.addRecord(sql, name, email, address, contact, tp, hashedPass, "Pending");
                    System.out.println("Registration successful! Please wait for admin approval.\n");
                    break;

                case 3: // EXIT
                    System.out.println("Thank you! Program ended.");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
