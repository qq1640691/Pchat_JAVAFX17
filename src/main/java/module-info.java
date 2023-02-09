module demo{
    requires java.desktop;
    requires javacv;
    requires javafx.controls;
    requires javafx.graphics;

    opens com.example to javacv,javafx.controls,java.desktop,javafx.graphics;
    exports com.example;
}