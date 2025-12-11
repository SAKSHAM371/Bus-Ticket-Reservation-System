package com.bus.dao;

import com.bus.bean.Customer;
import com.bus.exceptions.BusException;
import com.bus.exceptions.CustomerException;
import com.bus.utility.DButil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement; // if not present you can remove color usage or import
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDaoImpl implements CustomerDao {

    @Override
    public String cusSignUp(String username, String password, String firstName, String lastName, String address,
            String mobile) {

        String message = "Sign up Failed";

        try (Connection conn = DButil.provideConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customer(username, password, firstName, lastName, address, mobile) VALUES (?,?,?,?,?,?)");

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, address);
            ps.setString(6, mobile);

            int x = ps.executeUpdate();

            if (x > 0) message = "Sign up Successful";

        } catch (SQLException e) {
            message = e.getMessage();
        }

        return message;
    }

    @Override
    public String cusSignUp(Customer customer) {
        return cusSignUp(customer.getUsername(), customer.getPassword(), customer.getFirstName(),
                customer.getLastName(), customer.getAddress(), customer.getMobile());
    }

    @Override
    public Customer cusLogin(String username, String password) throws CustomerException {

        Customer customer = null;

        try (Connection conn = DButil.provideConnection()) {

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM customer WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int cusId = rs.getInt("cusId");
                String usernamee = rs.getString("username");
                String passwordd = rs.getString("password");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String address = rs.getString("address");
                String mobile = rs.getString("mobile");

                customer = new Customer(cusId, usernamee, passwordd, firstName, lastName, address, mobile);
            } else {
                throw new CustomerException("Invalid username or password");
            }

        } catch (SQLException e) {
            throw new CustomerException(e.getMessage());
        }

        return customer;
    }

    @Override
    public String bookTicket(String bName, int cusId, int no) throws BusException {

        String message = "Ticket Booking failed";

        try (Connection conn = DButil.provideConnection()) {

            System.out.println("DEBUG: Starting bookTicket. bName='" + bName + "' cusId=" + cusId + " no=" + no);

            // find bus - case-insensitive exact match (safer)
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM bus WHERE LOWER(bName) = LOWER(?)");
            ps.setString(1, bName.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int busNo = rs.getInt("busNo");
                int totalSeats = rs.getInt("totalSeats");
                int availSeats = rs.getInt("availSeats");
                Date departure = rs.getDate("departure");
                int fare = rs.getInt("fare");

                System.out.println("DEBUG: busNo=" + busNo + ", totalSeats=" + totalSeats + ", availSeats=" + availSeats
                        + ", departure=" + departure + ", fare=" + fare);

                PreparedStatement ps1 = conn.prepareStatement("SELECT DATEDIFF(?,CURRENT_DATE()) as date");
                ps1.setDate(1, departure);

                ResultSet rs1 = ps1.executeQuery();
                int days = 0;
                if (rs1.next()) {
                    days = rs1.getInt("date");
                }
                System.out.println("DEBUG: days until departure=" + days);

                if (days <= 0) {
                    throw new BusException("Booking is not available for this date");
                } else if (availSeats >= no) {
                    int seatFrom = totalSeats - availSeats + 1;
                    int seatTo = seatFrom + no - 1;
                    int totalFare = fare * no;

                    System.out.println("DEBUG: seatFrom=" + seatFrom + ", seatTo=" + seatTo + ", totalFare=" + totalFare);

                    PreparedStatement ps2 = conn.prepareStatement(
                            "INSERT INTO booking(cusId, busNo, seatFrom, seatTo) VALUES (?, ?, ?, ?)");
                    ps2.setInt(1, cusId);
                    ps2.setInt(2, busNo);
                    ps2.setInt(3, seatFrom);
                    ps2.setInt(4, seatTo);

                    int x = ps2.executeUpdate();
                    System.out.println("DEBUG: insert booking returned x=" + x);

                    if (x > 0) {

                        PreparedStatement ps3 = conn
                                .prepareStatement("UPDATE bus SET availSeats = ? WHERE busNo = ?");
                        availSeats = availSeats - no;
                        ps3.setInt(1, availSeats);
                        ps3.setInt(2, busNo);
                        int y = ps3.executeUpdate();

                        System.out.println("DEBUG: update bus avail returned y=" + y);

                        if (y <= 0)
                            throw new BusException("Available Seat is not updated");

                        System.out.println("Booking details:");
                        System.out.println("Customer Id : " + cusId);
                        System.out.println("Bus No      : " + busNo);
                        System.out.println("Seat No     : from " + seatFrom + " to " + seatTo);
                        System.out.println("Total Fare  : " + totalFare);

                        message = "Ticket Booked Successfully";
                    } else {
                        System.out.println("DEBUG: insert reported x<=0");
                        throw new BusException("Booking insert failed");
                    }

                } else {
                    throw new BusException("Not enough available seats");
                }

            } else {
                throw new BusException("Bus with " + bName + " is not available");
            }

        } catch (SQLException e) {
            System.err.println("DEBUG SQLException: " + e.getMessage());
            e.printStackTrace();
            throw new BusException(e.getMessage());
        } catch (BusException be) {
            System.err.println("DEBUG BusException: " + be.getMessage());
            throw be;
        } catch (Exception ex) {
            System.err.println("DEBUG Other Exception: " + ex.getMessage());
            ex.printStackTrace();
            throw new BusException(ex.getMessage());
        }

        return message;
    }

    @Override
    public String cancelTicket(String bName, int cusId) throws BusException {
        String message = "Ticket cancellation failed";

        try (Connection conn = DButil.provideConnection()) {

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM bus WHERE bName = ?");
            ps.setString(1, bName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int busNo = rs.getInt("busNo");
                int availSeats = rs.getInt("availSeats");

                PreparedStatement ps1 = conn
                        .prepareStatement("SELECT * FROM booking WHERE busNo = ? AND cusId = ?");
                ps1.setInt(1, busNo);
                ps1.setInt(2, cusId);

                ResultSet rs1 = ps1.executeQuery();
                boolean flag = false;
                int count = 0;

                while (rs1.next()) {
                    flag = true;
                    int seatFrom = rs1.getInt("seatFrom");
                    int seatTo = rs1.getInt("seatTo");
                    count += seatTo - seatFrom + 1;
                }

                if (flag) {

                    PreparedStatement ps2 = conn.prepareStatement("DELETE FROM booking WHERE busNo = ? AND cusId = ?");
                    ps2.setInt(1, busNo);
                    ps2.setInt(2, cusId);

                    int x = ps2.executeUpdate();
                    if (x > 0) {

                        PreparedStatement ps3 = conn
                                .prepareStatement("UPDATE bus SET availSeats = ? WHERE busNo = ?");
                        availSeats = availSeats + count;
                        ps3.setInt(1, availSeats);
                        ps3.setInt(2, busNo);
                        int y = ps3.executeUpdate();

                        if (y <= 0)
                            throw new BusException("Available Seat is not updated");

                        message = "Ticket cancelled Successfully";
                    }

                } else
                    message = "No booking found";

            } else {
                throw new BusException("Bus with " + bName + " is not available");
            }

        } catch (SQLException e) {
            throw new BusException(e.getMessage());
        }

        return message;

    }

    @Override
    public void viewTicket(int cusId) {
        boolean flag = false;

        try (Connection conn = DButil.provideConnection()) {
            PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM booking WHERE cusId = ?");
            ps1.setInt(1, cusId);

            ResultSet rs1 = ps1.executeQuery();

            while (rs1.next()) {
                flag = true;
                System.out.println("---------------------------------------------");
                System.out.println("Bus Id : " + rs1.getInt("bId"));
                System.out.println("Bus No : " + rs1.getInt("busNo"));
                System.out.println("Total tickets : " + (rs1.getInt("seatTo") - rs1.getInt("seatFrom") + 1));
                if (rs1.getBoolean("status"))
                    System.out.println("Status : Booked");
                else
                    System.out.println("Status : Pending");
                System.out.println("----------------------------------------------");
            }

            if (!flag)
                System.out.println("No tickets found");
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }

    }
}
