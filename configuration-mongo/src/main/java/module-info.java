/**
 * 
 */
/**
 * @author Torsten
 *
 */
module configuration.mongo {
    exports com.airepublic.configuration.service.mongo;

    requires org.apache.commons.lang3;
    requires transitive configuration.api;
    requires exception;
    requires javax.inject;
    requires cdi.api;
    requires java.annotation;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires mongo.java.driver;
}