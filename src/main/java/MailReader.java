import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

class MailReader {
    private Folder inbox;

    public static void main(String[] args) {
        new MailReader();
    }

    public MailReader() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
            }

            JFrame frame = new JFrame("Mail Receiver");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new TestPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }

    public class TestPane extends JPanel {

        private JPanel mainList;

        public TestPane() {
            setLayout(new BorderLayout());

            mainList = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1;
            gbc.weighty = 1;
            mainList.add(new JPanel(), gbc);
            add(new JScrollPane(mainList));

            JButton add = new JButton("Reload");
            add.addActionListener(e -> {
                try {

                    checkMail();
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            });

            add(add, BorderLayout.SOUTH);
            connect();
            try {
                checkMail();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        private void checkMail() throws MessagingException {
            if (inbox.isOpen()) {
                inbox.close(true);
            }
            inbox.open(Folder.READ_ONLY);
            System.out.println("No of Unread Messages : " + inbox.getUnreadMessageCount());

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.CONTENT_INFO);
            inbox.fetch(messages, fp);

            try {
                printAllMessages(messages);
            } catch (Exception ex) {
                System.out.println("Exception arise at the time of read mail");
                ex.printStackTrace();
            }
        }


        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 600);
        }

        public void printAllMessages(javax.mail.Message[] msgs) throws Exception {
            mainList.removeAll();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1;
            gbc.weighty = 1;
            mainList.add(new JPanel(), gbc);
            for (int i = 0; i < msgs.length; i++) {
                System.out.println("MESSAGE #" + (i + 1) + ":");
                JPanel panel = new JPanel(new GridLayout(4, 1));
                Message msg = msgs[i];
                panel.add(new JLabel("Subject: " + msg.getSubject()));
                InternetAddress internetAddress = (InternetAddress) msg.getFrom()[0];
                panel.add(new JLabel("From: " + internetAddress.getAddress()));
                panel.add(new JLabel("Message: " + getTextFromMessage(msg)));

                JButton download = new JButton("Download File");
                if (hasAttachment(msg)) {
                    panel.add(download);
                }

                download.addActionListener(actionEvent -> downloadAttachment(msg));
                panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
                GridBagConstraints gbc1 = new GridBagConstraints();
                gbc1.gridwidth = GridBagConstraints.REMAINDER;
                gbc1.weightx = 1;
                gbc1.fill = GridBagConstraints.HORIZONTAL;
                mainList.add(panel, gbc1, 0);

                revalidate();
                repaint();
            }

        }

    }

    private void downloadAttachment(Message msg) {
        try {
            if (msg.getContentType().contains("multipart")) {
                Multipart multiPart = (Multipart) msg.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        String fileName = part.getFileName();
                        part.saveFile("E:\\" + File.separator + fileName);
                    }
                }

            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasAttachment(Message msg) throws MessagingException, IOException {
        if (msg.getContentType().contains("multipart")) {
            Multipart multiPart = (Multipart) msg.getContent();
            int numberOfParts = multiPart.getCount();

            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    return true;
                }
            }

        }
        return false;
    }

    private void connect() {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", "dmitrizubenco@gmail.com" ,"123231123z");

            inbox = store.getFolder("Inbox");


        } catch (MessagingException e1) {
            e1.printStackTrace();
        }
    }

    private String getTextFromMessage(javax.mail.Message message) throws Exception {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws Exception {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }
}