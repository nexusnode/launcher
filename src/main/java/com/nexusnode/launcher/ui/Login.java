package com.nexusnode.launcher.ui;

import javax.swing.*;

public class Login {
    public static void main(String[] args) {
        JFrame frame = new JFrame("User com.nexusnode.launcher.ui.Login");
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);

        JLabel userLabel = new JLabel("User");
        userLabel.setBounds(10, 20, 100, 30);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100,  20, 150, 30);
        panel.add(userText);

        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(10, 60, 100, 30);
        panel.add(passLabel);

        JTextField passText = new JTextField(20);
        passText.setBounds(100,  60, 150, 30);
        panel.add(passText);

        JButton playButton = new JButton("com.nexusnode.launcher.ui.Login");
        playButton.setBounds(100, 100, 150, 30);
        panel.add(playButton);


        frame.setVisible(true);
    }
}
