package gui;

import com.bus.bean.Customer;
import java.awt.*;
import javax.swing.*;

public class DashboardFrame extends JFrame {

    Customer customer;

    public DashboardFrame(Customer c) {
        this.customer = c;

        setTitle("Customer Dashboard");
        setSize(380, 220);
        setLayout(new GridLayout(4, 1, 10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String name = (c.getFirstName() != null && !c.getFirstName().isEmpty()) ? c.getFirstName() : c.getUsername();
        JLabel welcome = new JLabel("Welcome, " + name, SwingConstants.CENTER);

        JButton book = new JButton("Book Ticket");
        JButton view = new JButton("View Tickets (Console)");
        JButton logout = new JButton("Logout");

        book.addActionListener(e -> {
            new BookTicketFrame(customer);
            dispose();
        });

        view.addActionListener(e -> {
            // For now view is console-based: call DAO.viewTicket
            new Thread(() -> {
                try {
                    com.bus.dao.CustomerDaoImpl dao = new com.bus.dao.CustomerDaoImpl();
                    dao.viewTicket(customer.getCusId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        logout.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        add(welcome);
        add(book);
        add(view);
        add(logout);

        setVisible(true);
    }
}
