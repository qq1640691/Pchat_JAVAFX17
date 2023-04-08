module demo{
    requires java.desktop;
    requires javacv;
    requires javafx.controls;
    requires javafx.graphics;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.handler;
    requires io.netty.common;
    requires io.netty.buffer;
    requires oshi.core;

    opens com.example to javacv,javafx.controls,java.desktop,javafx.graphics;
    exports com.example;
}