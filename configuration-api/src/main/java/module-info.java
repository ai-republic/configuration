/**
 * 
 */
/**
 * @author Torsten
 *
 */
module configuration.api {
    exports com.airepublic.configuration.service;
    exports com.airepublic.configuration.api;

    requires com.airepublic.logging.java;
    requires exception;
    requires jakarta.enterprise.cdi.api;
    requires jakarta.inject;
    requires transitive jakarta.interceptor.api;
    requires transitive java.ws.rs;
}