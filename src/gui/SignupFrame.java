package gui;

import com.bus.dao.CustomerDaoImpl;
import java.awt.*;
import javax.swing.*;

public class SignupFrame extends JFrame {

    public SignupFrame() {
        setTitle("Customer Signup");
        setSize(420, 360);
        setLayout(new GridLayout(8, 2, 8, 8));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextField user = new JTextField();
        JTextField pass = new JTextField();
        JTextField fn = new JTextField();
        JTextField ln = new JTextField();
        JTextField addr = new JTextField();
        JTextField mob = new JTextField();

        JLabel msg = new JLabel("");

        JButton submit = new JButton("Submit");
        JButton back = new JButton("Back");

        submit.addActionListener(e -> {
            try {
                CustomerDaoImpl dao = new CustomerDaoImpl();
                String result = dao.cusSignUp(user.getText().trim(), pass.getText().trim(), fn.getText().trim(),
                        ln.getText().trim(), addr.getText().trim(), mob.getText().trim());

                msg.setText(result);

                if (result.toLowerCase().contains("success")) {
                    new LoginFrame();
                    dispose();
                }
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        back.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        add(new JLabel("Username:")); add(user);
        add(new JLabel("Password:")); add(pass);
        add(new JLabel("First Name:")); add(fn);
        add(new JLabel("Last Name:")); add(ln);
        add(new JLabel("Address:")); add(addr);
        add(new JLabel("Mobile:")); add(mob);
        add(msg);
        add(submit); add(back);

        setVisible(true);
    }
}
