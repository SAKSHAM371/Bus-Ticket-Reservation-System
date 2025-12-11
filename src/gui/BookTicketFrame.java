package gui;

import com.bus.bean.Customer;
import com.bus.dao.CustomerDaoImpl;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

public class BookTicketFrame extends JFrame {

    private Customer customer;
    private JComboBox<String> busCombo;
    private JTextField seatsField;
    private JLabel message;

    public BookTicketFrame(Customer c) {
        this.customer = c;

        setTitle("Book Ticket");
        setSize(450, 250);
        setLayout(new GridLayout(5, 2, 10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        busCombo = new JComboBox<>();
        seatsField = new JTextField();
        message = new JLabel("");

        // Load bus names from DB
        for (String b : loadBusNames()) {
            busCombo.addItem(b);
        }

        JButton bookBtn = new JButton("Book");
        JButton backBtn = new JButton("Back");

        // BOOK button action
        bookBtn.addActionListener(e -> {
            String busName = (String) busCombo.getSelectedItem();
            String seatStr = seatsField.getText().trim();

            if (busName == null) {
                message.setText("No buses available.");
                return;
            }
            if (seatStr.isEmpty()) {
                message.setText("Enter number of seats.");
                return;
            }

            int seatCount;
            try {
                seatCount = Integer.parseInt(seatStr);
            } catch (NumberFormatException ex) {
                message.setText("Seats must be a number.");
                return;
            }

            try {
                CustomerDaoImpl dao = new CustomerDaoImpl();
                String result = dao.bookTicket(busName, customer.getCusId(), seatCount);
                message.setText(result);
            } catch (Exception ex) {
                message.setText("Booking failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // BACK button action
        backBtn.addActionListener(e -> {
            new DashboardFrame(customer);
            dispose();
        });

        add(new JLabel("Select Bus:")); add(busCombo);
        add(new JLabel("Number of Seats:")); add(seatsField);
        add(message);
        add(bookBtn); add(backBtn);

        setVisible(true);
    }

    // Load bus names from DB
    private java.util.List<String> loadBusNames() {
        java.util.List<String> list = new ArrayList<>();
        try (Connection conn = com.bus.utility.DButil.provideConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT bName FROM bus");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("bName"));
            }

        } catch (Exception e) {
            System.err.println("Unable to load buses: " + e.getMessage());
        }
        return list;
    }
}
