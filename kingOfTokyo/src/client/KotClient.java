package client;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.BorderLayout;

public class KotClient {
	public static void CreateAndShowGUI() {
		JFrame frame = new JFrame("KingOfTokyo");
		JButton btnNewButton = new JButton("New button");
		frame.getContentPane().add(btnNewButton, BorderLayout.SOUTH);
		
	}
	
	
	
	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CreateAndShowGUI();
            }
        });
    }
}
