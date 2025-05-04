import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class GUIClient {
    public static void main(String[] args) {
        JFrame frame = new JFrame("City Query Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JTextField queryField = new JTextField();
        JTextArea responseArea = new JTextArea();
        responseArea.setEditable(false);

        JButton sendButton = new JButton("Send");
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(queryField, BorderLayout.NORTH);
        panel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryField.getText().trim();
                if (!query.isEmpty()) {
                    try (Socket socket = new Socket("localhost", 12345);
                         PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

                        out.println(query);
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line).append("\n");
                        }
                        responseArea.setText(response.toString());
                    } catch (IOException ex) {
                        responseArea.setText("Error: " + ex.getMessage());
                    }
                }
            }
        });
    }
}

