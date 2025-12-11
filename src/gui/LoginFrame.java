package gui;

import com.bus.bean.Customer;
import com.bus.dao.CustomerDaoImpl;
import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        setTitle("Bus Reservation - Login");
        setSize(380, 260);
        setLayout(new GridLayout(5, 2, 10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel userLbl = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLbl = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JLabel msg = new JLabel("");

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Signup");

        loginBtn.addActionListener(e -> {
            try {
                CustomerDaoImpl dao = new CustomerDaoImpl();
                Customer c = dao.cusLogin(userField.getText().trim(), new String(passField.getPassword()).trim());

                new DashboardFrame(c);
                dispose();
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        signupBtn.addActionListener(e -> {
            new SignupFrame();
            dispose();
        });

        add(userLbl); add(userField);
        add(passLbl); add(passField);
        add(msg);
        add(loginBtn); add(signupBtn);

        setVisible(true);
    }
}
