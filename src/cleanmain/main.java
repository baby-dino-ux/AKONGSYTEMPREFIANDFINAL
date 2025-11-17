package cleanmain;

import config.config;
import java.util.Scanner;
import java.util.List;
import java.util.Map;

public class main {
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

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: // LOGIN
                    System.out.print("Enter User Email: ");
                    String email = sc.nextLine().trim();
                    System.out.print("Enter Password: ");
                    String password = sc.nextLine().trim();

                    String qry = "SELECT * FROM tbl_user WHERE u_email = ?";
                    List<Map<String, Object>> result = db.fetchRecords(qry, email);

                    if (result.isEmpty()) {
                        System.out.println("INVALID CREDENTIALS.");
                    } else {
                        Map<String, Object> user = result.get(0);
                        String storedPass = user.get("u_pass").toString();
                        String hashedInput = config.hashPassword(password);

                        if (hashedInput != null && (storedPass.equals(password) || storedPass.equals(hashedInput))) {
                            String status = user.get("u_status").toString();
                            String type = user.get("u_type").toString();
                            int userId = Integer.parseInt(user.get("u_id").toString());

                            if (status.equalsIgnoreCase("Pending")) {
                                System.out.println("Account is Pending. Contact the Admin!");
                            } else {
                                System.out.println("LOGIN SUCCESS!");
                                if (type.equalsIgnoreCase("Admin")) {
                                    Admin admin = new Admin();
                                    admin.showDashboard();
                                } else if (type.equalsIgnoreCase("Staff")) {
                                    Staff staff = new Staff(userId);
                                    staff.showDashboard();
                                } else if (type.equalsIgnoreCase("Employee")) {
                                    Employee employee = new Employee(userId);
                                    employee.showDashboard();
                                }
                                System.out.println("\nReturning to Main Menu...\n");
                            }
                        } else {
                            System.out.println("INVALID CREDENTIALS.");
                        }
                    }
                    break;

                case 2: // REGISTER (Admin, Staff, Employee only)
                    System.out.print("Enter User Name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Enter User Email: ");
                    String newEmail = sc.nextLine().trim();

                    String qryCheck = "SELECT * FROM tbl_user WHERE u_email = ?";
                    while (!db.fetchRecords(qryCheck, newEmail).isEmpty()) {
                        System.out.print("Email already exists, Enter another Email: ");
                        newEmail = sc.nextLine().trim();
                    }

                    System.out.print("Enter User Address: ");
                    String address = sc.nextLine().trim();
                    System.out.print("Enter User Contact Number: ");
                    String contact = sc.nextLine().trim();

                    System.out.println("\nSelect User Type:");
                    System.out.println("1. Admin");
                    System.out.println("2. Staff");
                    System.out.println("3. Employee");
                    System.out.print("Enter choice (1-3): ");

                    int type;
                    try {
                        type = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Registration cancelled.");
                        continue;
                    }

                    if (type < 1 || type > 3) {
                        System.out.println("Invalid choice. Registration cancelled.");
                        continue;
                    }

                    String userType;
                    switch (type) {
                        case 1: userType = "Admin"; break;
                        case 2: userType = "Staff"; break;
                        default: userType = "Employee"; break;
                    }

                    System.out.print("Enter Password: ");
                    String pass = sc.nextLine().trim();
                    String hashedPass = config.hashPassword(pass);

                    if (hashedPass == null) {
                        System.out.println("Error in password processing. Registration cancelled.");
                        continue;
                    }

                    String sql = "INSERT INTO tbl_user(u_name, u_email, u_address, u_contact, u_type, u_pass, u_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    db.addRecord(sql, name, newEmail, address, contact, userType, hashedPass, "Pending");

                    if (userType.equals("Employee")) {
                        List<Map<String, Object>> newUser = db.fetchRecords("SELECT u_id FROM tbl_user WHERE u_email = ?", newEmail);
                        if (!newUser.isEmpty()) {
                            int newUserId = Integer.parseInt(newUser.get(0).get("u_id").toString());
                            String empSql = "INSERT INTO tbl_employee(user_id, e_name, e_role, e_status) VALUES (?, ?, ?, ?)";
                            db.addRecord(empSql, newUserId, name, "Cleaner", "Available");
                        }
                    }

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