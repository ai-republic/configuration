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

    requires org.apache.commons.lang3;
    requires exception;
    requires cdi.api;
    requires javax.inject;
    requires transitive javax.interceptor.api;
    requires transitive java.ws.rs;
    requires org.slf4j;
}