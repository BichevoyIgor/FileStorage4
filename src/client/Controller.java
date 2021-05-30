package client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Socket socket;
    private Stage stage;
    private final int PORT = 9997;
    private DataInputStream in;
    private DataOutputStream out;
    private final String HOST = "localhost";
    private String currentPath;

    @FXML
    public TextField clientTextField;
    @FXML
    public ListView<String> clientFileList;
    @FXML
    public ComboBox diskBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connect();
        //закрытие окна по крестику
        Platform.runLater(() -> {
            stage = (Stage) clientTextField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                stage.close();
                disconnect();
            });
        });

        diskBoxChange();
        loadTableList();

        //открытие папки при двойном клике мыши
        clientFileList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    ObservableList<String> f = clientFileList.getSelectionModel().getSelectedItems();
                    String path = String.format("%s%s", currentPath, f.get(0).substring(0, f.get(0).length()));
                    if (Files.isDirectory(Path.of(path))) {
                        clientTextField.setText(path + "\\");
                        currentPath = clientTextField.getText();
                        loadTableList();
                    }else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Для дальнейшего перехода просьба выбрать папку", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            }
        });
    }

    // заполняем комбобокс дисками файловой системы
    private void diskBoxChange() {
        diskBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            diskBox.getItems().add(p.toString());
        }
        diskBox.getSelectionModel().select(0); // устанавливаем диск по умолчанию
        currentPath = diskBox.getSelectionModel().getSelectedItem().toString();
    }

    //отрисовка списка файлов в клиентской таблице файлов
    private void loadTableList() {
        String[] filesList = new File(currentPath.toString()).list();
        clientFileList.getItems().clear();
        for (int i = 0; i < filesList.length; i++) {
            clientFileList.getItems().add(filesList[i]);
        }
        clientTextField.setText(currentPath);
    }

    //метод подключения к серверу
    public void connect() {
        try {
            socket = new Socket(HOST, PORT);
            System.out.println("Connect success");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод отключения от сервера
    public void disconnect() {
        if (socket != null) {
            try {
                System.out.println("Посылаю команду COMMAND_EXIT");
                out.writeUTF("COMMAND_EXIT");
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //реализация функции закрытия окна и выхода из приложения
    public void exit(ActionEvent actionEvent) {
        disconnect();
        Platform.exit();
    }

    //кнопка перехода в родительский каталог
    public void btnClientFolderUp(ActionEvent actionEvent) {
        Path upperPath = Paths.get(clientTextField.getText()).getParent();
        if (upperPath != null) {
            currentPath = upperPath.toString() + "\\";
            loadTableList();
        }
    }

    //кнопка смены дисков
    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        currentPath = element.getSelectionModel().getSelectedItem();
        loadTableList();
    }
}
