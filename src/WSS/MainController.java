package WSS;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Timer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;

/**
 * This class is used to control the FXML for main UI display
 *
 * @author Cameron Roudebush
 */
public class MainController implements Initializable {

    private String UID;
    private String PIN;
    PrintStream log;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private DatePicker scheduleDate;
    @FXML
    private ToggleGroup meridiem;
    @FXML
    private ToggleGroup semesterButtons;
    @FXML
    private ComboBox scheduleTimeHour;
    @FXML
    private ComboBox scheduleTimeMinute;
    @FXML
    private ComboBox scheduleTimeSeconds;
    @FXML
    private TextField clockField;
    @FXML
    private TextField crnBox1;
    @FXML
    private TextField crnBox2;
    @FXML
    private TextField crnBox3;
    @FXML
    private TextField crnBox4;
    @FXML
    private TextField crnBox5;
    @FXML
    private TextField crnBox6;
    @FXML
    private TextField crnBox7;
    @FXML
    private TextField crnBox8;
    @FXML
    private RadioButton spring;
    @FXML
    private RadioButton fall;
    @FXML
    private RadioButton summer;
    @FXML
    private RadioButton am;
    @FXML
    private RadioButton pm;

    private ArrayList<TextField> crnBoxes = new ArrayList();
    Clock clock;

    public MainController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        crnBoxes.add(crnBox1);
        crnBoxes.add(crnBox2);
        crnBoxes.add(crnBox3);
        crnBoxes.add(crnBox4);
        crnBoxes.add(crnBox5);
        crnBoxes.add(crnBox6);
        crnBoxes.add(crnBox7);
        crnBoxes.add(crnBox8);
        for (int i = 0; i < crnBoxes.size(); i++) {
            crnBoxes.get(i).setPromptText("12345");
            TextFormatter<TextField> tf = new TextFormatter<>(e -> {
                if (e.getControlNewText().length() > 5) {
                    return null;
                }
                return e;
            });
            crnBoxes.get(i).setTextFormatter(tf);
        }
        scheduleDate.setValue(LocalDate.now());
        scheduleDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });
        clock = new Clock(clockField, log);
        Timer timer = new Timer();
        timer.schedule(clock, 0, 1000);
        scheduleTimeHour.getItems().addAll("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                scheduleTimeMinute.getItems().add("0" + i);
                scheduleTimeSeconds.getItems().add("0" + i);
            } else {
                scheduleTimeMinute.getItems().add(i);
                scheduleTimeSeconds.getItems().add(i);
            }
        }
    }

    /**
     * Set some stuff we will need to use later on
     */
    public void setInfo(String UID, String PIN, PrintStream log) {
        this.PIN = PIN;
        this.UID = UID;
        this.log = log;
    }

    @FXML
    protected void handleSchedule(ActionEvent event) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        int emptyCrnBoxes = 0;
        String selectedMeridiem;
        String semesterString = "";
        int semester;
        if (semesterButtons.getSelectedToggle() == fall) {
            semester = 80;
            semesterString = "fall";
        } else if (semesterButtons.getSelectedToggle() == summer) {
            semester = 40;
            semesterString = "Summer";
        } else if (semesterButtons.getSelectedToggle() == spring) {
            semester = 30;
            semesterString = "Spring";
        } else {
            semester = 0;
        }
        if (meridiem.getSelectedToggle() == am) {
            selectedMeridiem = "AM";
        } else {
            selectedMeridiem = "PM";
        }
        if (scheduleTimeHour.getSelectionModel().getSelectedItem() == null || scheduleTimeMinute.getSelectionModel().getSelectedItem() == null
                || scheduleTimeSeconds.getSelectionModel().getSelectedItem() == null || meridiem.getSelectedToggle() == null) {
            Alert tooLate = new Alert(AlertType.ERROR, "Please make sure to fill in all of the time drop down boxes and select a meridiem!");
            tooLate.setHeaderText("Time error");
            tooLate.showAndWait();
        } else {
            String timeToSchedule = scheduleTimeHour.getSelectionModel().getSelectedItem().toString() + ":" + scheduleTimeMinute.getSelectionModel().getSelectedItem().toString() + ":"
                    + scheduleTimeSeconds.getSelectionModel().getSelectedItem().toString() + " " + selectedMeridiem;
            for (int i = 0; i < crnBoxes.size(); i++) {
                if (crnBoxes.get(i).getText().isEmpty()) {
                    emptyCrnBoxes++;
                }
            }
            DateFormat formater = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            Time currentTimeFormat = null;
            Time timeToScheduleFormat = null;
            try {
                timeToScheduleFormat = new Time((formater.parse(scheduleDate.getValue().format(dateFormatter) + " " + timeToSchedule).getTime()));
                currentTimeFormat = new Time((formater.parse(clock.getCurrentDateAndTime().substring(14, clock.getCurrentDateAndTime().length())).getTime()));
            } catch (ParseException ex) {
                log.println("Parse error for checking dates");
                log.print(ex.toString());
                log.println();
            }

            if (UID.isEmpty() || PIN.isEmpty()) {
                Alert regError = new Alert(AlertType.ERROR, "Either your password or uid box is empty.");
                regError.setHeaderText("Empty login");
                regError.showAndWait();
            } else if (scheduleDate.getValue().format(dateFormatter).length() < 4) {
                Alert badDate = new Alert(AlertType.ERROR, "You must include a date following the MM/DD/YYYY format.");
                badDate.setHeaderText("Date format error");
                badDate.showAndWait();
            } else if (semesterButtons.getSelectedToggle() == null) {
                Alert noToggle = new Alert(AlertType.ERROR, "You must select a semester.");
                noToggle.setHeaderText("Semester Selection");
                noToggle.showAndWait();
            } else if (emptyCrnBoxes == crnBoxes.size()) {
                Alert noToggle = new Alert(AlertType.ERROR, "You have not inserted any CRN's.");
                noToggle.setHeaderText("No CRN's");
                noToggle.showAndWait();
            } else if (timeToScheduleFormat.before(currentTimeFormat)) {
                Alert tooLate = new Alert(AlertType.ERROR, "The time you have selected has already passed. Please select a new time.");
                tooLate.setHeaderText("The time has passed");
                tooLate.showAndWait();
            } else {
                String scheduleYear = scheduleDate.getValue().format(dateFormatter).substring(6, 10);
                if (semester == 30) {
                    int tempYear = Integer.parseInt(scheduleYear) + 1;
                    log.println("Spring semester detected, adjusting scheduleYear to " + tempYear);
                    scheduleYear = Integer.toString(tempYear);
                }
                WingsExpressConnector connector = new WingsExpressConnector(PIN, UID, scheduleYear + semester, log);
                int loginTest = connector.loginTest();
                switch (loginTest) {
                    case 1: {
                        Alert regError = new Alert(Alert.AlertType.ERROR, "You seem to have miss typed your login info.");
                        regError.setHeaderText("Incorrect login");
                        regError.showAndWait();
                        break;
                    }
                    case 2: {
                        Alert regError = new Alert(Alert.AlertType.ERROR, "You currently have a hold on your account. Please check wings express if you have an alternative PIN or a hold of some type.");
                        regError.setHeaderText("Hold error");
                        regError.showAndWait();
                        break;
                    }
                    case 3: {
                        Alert regError = new Alert(Alert.AlertType.ERROR, "You have forgotten to either accept/decline the wright state insurance and the acknowledgment of financial responsibility.");
                        regError.setHeaderText("Required ackowledgment error");
                        regError.showAndWait();
                        break;
                    }
                    default:
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Submit?");
                        alert.setHeaderText(null);
                        ButtonType buttonYes = new ButtonType("Yes");
                        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(buttonYes, buttonNo);
                        alert.setContentText("All preliminary tests have passed but please note this program does not test for valid CRN numbers. "
                                + "Currently you will be registering for the following date/semester and time: \n"
                                + scheduleDate.getValue().format(dateFormatter) + " " + semesterString + " " + timeToSchedule
                                + "\nIf you would like to continue with this info press yes. If something looks wrong press no and please correct it.");
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonYes) {
                            Stack<String> crns = new Stack();
                            for (int i = 0; i < crnBoxes.size(); i++) {
                                if (!crnBoxes.get(i).getText().isEmpty()) {
                                    crns.push(crnBoxes.get(i).getText());
                                }
                            }
                            String dateTime = clock.getCurrentDateAndTime().substring(14, clock.getCurrentDateAndTime().length());
                            ScheduleWaiter waiter = new ScheduleWaiter(clock, scheduleDate.getValue().format(dateFormatter), timeToSchedule, PIN, UID, semester, crns, log);
                            progressIndicator.setVisible(true);
                            Thread waiterThread = new Thread(waiter);
                            waiterThread.start();
                        }
                        break;
                }
            }
        }
    }
}
