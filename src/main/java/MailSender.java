import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

public class MailSender extends JFrame {
    private JTextField fromField = new JTextField();
    private JTextField toField = new JTextField();
    private JTextField subjectField = new JTextField();
    private JComboBox<String> mailSmtpHostComboBox = new JComboBox<>();
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JTextArea contentTextArea = new JTextArea();
    private File file;

    private MailSender() {
        InitializeUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MailSender client = new MailSender();
            client.setVisible(true);
        });
    }

    private void InitializeUI() {
        setTitle("Send E-mail Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(400, 280));

        getContentPane().setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(6, 2));
        headerPanel.add(new JLabel("From:"));
        headerPanel.add(fromField);

        headerPanel.add(new JLabel("To:"));
        headerPanel.add(toField);

        headerPanel.add(new JLabel("Subject:"));
        headerPanel.add(subjectField);

        headerPanel.add(new JLabel("STMP Server:"));
        headerPanel.add(mailSmtpHostComboBox);
        mailSmtpHostComboBox.addItem("smtp.gmail.com");

        headerPanel.add(new JLabel("Username:"));
        headerPanel.add(usernameField);

        headerPanel.add(new JLabel("Password:"));
        headerPanel.add(passwordField);

        // Body Panel
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BorderLayout());
        bodyPanel.add(new JLabel("Message:"), BorderLayout.NORTH);
        bodyPanel.add(contentTextArea, BorderLayout.CENTER);


        JFileChooser jFileChooser = new JFileChooser();
        JButton openButton = new JButton("Open a File...");
        openButton.addActionListener(actionEvent -> {
            int returnVal = jFileChooser.showOpenDialog(MailSender.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = jFileChooser.getSelectedFile();
                System.out.print(file.getAbsolutePath());
            }
        });

        bodyPanel.add(openButton, BorderLayout.SOUTH);

        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BorderLayout());
        JButton sendMailButton = new JButton("Send E-mail");
        sendMailButton.addActionListener(new SendEmailActionListener());

        footerPanel.add(sendMailButton, BorderLayout.SOUTH);

        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(bodyPanel, BorderLayout.CENTER);
        getContentPane().add(footerPanel, BorderLayout.SOUTH);


    }

    private class SendEmailActionListener implements ActionListener {
        SendEmailActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Properties props = new Properties();
            props.put("mail.smtp.host", mailSmtpHostComboBox.getSelectedItem());
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            Session session = Session.getDefaultInstance(props);
            try {
                InternetAddress fromAddress = new InternetAddress(fromField.getText());
                InternetAddress toAddress = new InternetAddress(toField.getText());

                javax.mail.Message message = new MimeMessage(session);
                message.setFrom(fromAddress);
                message.setRecipient(javax.mail.Message.RecipientType.TO, toAddress);
                message.setSubject(subjectField.getText());

                // Create the message part
                BodyPart messageBodyPart = new MimeBodyPart();

                // Create a multipar message
                Multipart multipart = new MimeMultipart();

                // Set text message part

                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file.getAbsoluteFile());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(file.getName());
//                messageBodyPart.setContent(contentTextArea.getText(), "text/html");
                multipart.addBodyPart(messageBodyPart);

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(contentTextArea.getText(), "text/html; charset=utf-8");

                multipart.addBodyPart(htmlPart); // <-- second

                // Send the complete message parts
                message.setContent(multipart, "text/html");


                Transport.send(message, usernameField.getText(),
                        new String(passwordField.getPassword()));
            } catch (MessagingException ex) {
                ex.printStackTrace();
            }
        }
    }
}