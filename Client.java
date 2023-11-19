package test09;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Client extends JFrame {
    private JTextField nameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextArea contactListArea;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private PersonalAddressBookClientController controller;

    public Client() {
        super("个人通讯录系统");

        // 创建界面组件
        nameField = new JTextField(20);
        addressField = new JTextField(20);
        phoneField = new JTextField(20);
        contactListArea = new JTextArea(10, 30);
        addButton = new JButton("添加");
        updateButton = new JButton("更新");
        deleteButton = new JButton("删除");

        // 添加事件监听器
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.addContact(nameField.getText(), addressField.getText(), phoneField.getText());
                clearFields();
                refreshContactList();
                JOptionPane.showMessageDialog(Client.this, "添加成功!");
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.updateContact(nameField.getText(), addressField.getText(), phoneField.getText());
                clearFields();
                refreshContactList();
                JOptionPane.showMessageDialog(Client.this, "更新成功!");
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.deleteContact(nameField.getText(), addressField.getText(), phoneField.getText());
                clearFields();
                refreshContactList();
                JOptionPane.showMessageDialog(Client.this, "删除成功!");
            }
        });

        // 构建用户界面
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("姓名:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("地址:"));
        inputPanel.add(addressField);
        inputPanel.add(new JLabel("电话:"));
        inputPanel.add(phoneField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(contactListArea), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 设置窗口属性
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setContentPane(mainPanel);

        // 设置字体大小
        setFontSize(16);
    }

    public void setController(PersonalAddressBookClientController controller) {
        this.controller = controller;
    }

    public void setContactList(String contactList) {
        contactListArea.setText(contactList);
    }

    public void clearFields() {
        nameField.setText("");
        addressField.setText("");
        phoneField.setText("");
    }

    public void refreshContactList() {
        controller.refreshContactList();
    }

    public void setFontSize(int size) {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, size);
        nameField.setFont(font);
        addressField.setFont(font);
        phoneField.setFont(font);
        contactListArea.setFont(font);
        addButton.setFont(font);
        updateButton.setFont(font);
        deleteButton.setFont(font);
    }

    public static void main(String[] args) {
        Client client = new Client();

        PersonalAddressBookClientController controller = new PersonalAddressBookClientController(client);
        client.setController(controller);

        client.setVisible(true);
        controller.connectToDatabase("jdbc:mysql://localhost:3306/personalCall", "root", "123456"); // 连接数据库服务器
        controller.refreshContactList(); // 刷新联系人列表
    }

    public static class PersonalAddressBookClientController {
        private Client client;
        private DataBase server;

        public PersonalAddressBookClientController(Client client) {
            this.client = client;
            server = new DataBase();
        }

        public void connectToDatabase(String url, String username, String password) {
            server.connectToDatabase(url, username, password);
        }

        public void addContact(String name, String address, String phone) {
            server.addContact(name, address, phone);
        }

        public void updateContact(String name, String address, String phone) {
            server.updateContact(name, address, phone);
        }

        public void deleteContact(String name, String address, String phone) {
            server.deleteContact(name, address, phone);
        }

        public void refreshContactList() {
            List<String> contacts = server.getContactList();
            StringBuilder listBuilder = new StringBuilder();
            for (String contact : contacts) {
                listBuilder.append(contact).append("\n");
            }
            client.setContactList(listBuilder.toString());
        }

        public void closeConnection() {
            server.closeConnection();
        }
    }

    public static class DataBase {
        private Connection connection;

        public DataBase() {
            connection = null;
        }

        public void connectToDatabase(String url, String username, String password) {
            try {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Connected to database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void addContact(String name, String address, String phone) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO contacts (name, address, phone) VALUES (?, ?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, address);
                statement.setString(3, phone);
                statement.executeUpdate();
                System.out.println("Contact added: " + name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void updateContact(String name, String address, String phone) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE contacts SET address = ?, phone = ? WHERE name = ?")) {
                statement.setString(1, address);
                statement.setString(2, phone);
                statement.setString(3, name);
                statement.executeUpdate();
                System.out.println("Contact updated: " + name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void deleteContact(String name, String address, String phone) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM contacts WHERE name = ? AND address = ? AND phone = ?")) {
                statement.setString(1, name);
                statement.setString(2, address);
                statement.setString(3, phone);
                statement.executeUpdate();
                System.out.println("Contact deleted: " + name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public List<String> getContactList() {
            List<String> contacts = new ArrayList<>();
            try (Statement statement = connection.createStatement()) {
                String query = "SELECT * FROM contacts";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String address = resultSet.getString("address");
                    String phone = resultSet.getString("phone");
                    contacts.add(name + "\t" + address + "\t" + phone);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return contacts;
        }

        public void closeConnection() {
            try {
                if (connection != null) {
                    connection.close();
                    System.out.println("Disconnected from database");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}