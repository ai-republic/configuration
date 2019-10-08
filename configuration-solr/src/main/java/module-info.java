/**
 * 
 */
/**
 * @author Torsten
 *
 */
module configuration.solr {
    exports com.airepublic.configuration.service.solr;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires configuration.api;
    requires exception;
    requires java.annotation;
    requires javax.inject;
    requires solr.solrj;
}