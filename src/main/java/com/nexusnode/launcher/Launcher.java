package com.nexusnode.launcher;


import javax.swing.*;

public class Launcher {
    public static void main(String[] args) {
        JFrame frame = new JFrame();

        JButton playButton = new JButton("Play");
        playButton.setBounds(100, 100, 100, 50);

        frame.add(playButton);
        frame.setSize(400, 500);
        frame.setLayout(null);
        frame.setVisible(true);
    }
}
