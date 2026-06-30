package com.wiredi.maven.services;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Service for working with Xpp3Dom configuration elements.
 */
public class Xpp3DomService {

    /**
     * Gets an existing configuration or creates a new one if it doesn't exist.
     *
     * @param configObj the configuration object (can be null or Xpp3Dom)
     * @return the Xpp3Dom configuration
     */
    public Xpp3Dom getOrCreateConfiguration(Object configObj) {
        if (configObj instanceof Xpp3Dom) {
            return (Xpp3Dom) configObj;
        }
        return new Xpp3Dom("configuration");
    }

    /**
     * Gets an existing child element or creates a new one if it doesn't exist.
     *
     * @param parent the parent Xpp3Dom element
     * @param childName the name of the child element
     * @return the child Xpp3Dom element
     */
    public Xpp3Dom getOrCreateChild(Xpp3Dom parent, String childName) {
        Xpp3Dom child = parent.getChild(childName);
        if (child == null) {
            child = new Xpp3Dom(childName);
            parent.addChild(child);
        }
        return child;
    }
}
