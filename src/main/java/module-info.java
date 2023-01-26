module com.example.chatclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens Chatclient to javafx.fxml;
    exports Chatclient;
}