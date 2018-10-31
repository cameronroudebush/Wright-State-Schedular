package WSS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

/**
 * This class is used to control the FXML for the main login display
 *
 * @author Cameron Roudebush
 */
public class LoginController implements Initializable {

    private String UID;
    private String PIN;
    PrintStream log;
    @FXML
    private TextField uid;
    @FXML
    private TextField pin;
    @FXML
    private ProgressIndicator progressIndicator;

    public LoginController() {
        try {
            this.log = new PrintStream(new File("log.txt"));
        } catch (FileNotFoundException ex) {
            System.out.println("Error, file could not be created.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFormatter<TextField> userFormatter = new TextFormatter<>(e -> {
            if (e.getControlNewText().length() > 9) {
                return null;
            }
            return e;
        });
        uid.setTextFormatter(userFormatter);
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) throws IOException {
        progressIndicator.setVisible(true);
        if (uid.getText().isEmpty() || pin.getText().isEmpty()) {
            progressIndicator.setVisible(false);
            log.println("Empty login");
            Alert regError = new Alert(AlertType.ERROR, "Either your password or uid box is empty.");
            regError.setHeaderText("Empty login");
            regError.showAndWait();
        } else {
            WingsExpressConnector connector = new WingsExpressConnector(pin.getText(), uid.getText(), log);
            int loginTest = connector.loginTestOnly();
            if (loginTest == 1) {
                progressIndicator.setVisible(false);
                log.println("Incorrect login");
                Alert regError = new Alert(Alert.AlertType.ERROR, "You seem to have miss typed your login info.");
                regError.setHeaderText("Incorrect login");
                regError.showAndWait();
            } else {
                progressIndicator.setVisible(false);
                log.println("Sucessful login");
                UID = uid.getText();
                PIN = pin.getText();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/WSS_Main.fxml"));
                Parent newPane = fxmlLoader.load();
                MainController controller = fxmlLoader.<MainController>getController();
                controller.setInfo(UID, PIN, log);
                Scene scene = new Scene(newPane, 640, 450);
                Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                appStage.setScene(scene);
                appStage.show();
            }
        }
    }
}