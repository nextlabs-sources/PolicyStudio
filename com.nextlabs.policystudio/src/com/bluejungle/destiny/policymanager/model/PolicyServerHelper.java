package com.bluejungle.destiny.policymanager.model;

import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

public class PolicyServerHelper {
    // get component type based on its full name
    public static String getTypeFromComponentName(String name) {
        int pos = name.indexOf(PQLParser.SEPARATOR);
        if (pos == -1) {
            throw new IllegalArgumentException("Invalid component name format: " + name);
        }
        String type = name.substring(0, pos);
        return type.toUpperCase();
    }

    public static String getComponentEnumFormName(String name) {
        String type = getTypeFromComponentName(name);
        return type;
    }

    // get the spec type for the component based on its name
    public static SpecType getSpecType(String name) {
        if (name.equals("APPLICATION")) {
            return SpecType.APPLICATION;
        }
        if (name.equals("HOST")) {
            return SpecType.HOST;
        }
        if (name.equals("RESOURCE")) {
            return SpecType.RESOURCE;
        }
        if (name.equals("USER")) {
            return SpecType.USER;
        }
        if (name.equals("ACTION")) {
            return SpecType.ACTION;
        }
        if (name.equals("PORTAL")) {
            return SpecType.PORTAL;
        }
        if (name.equals("SERVER")) {
            return SpecType.RESOURCE;
        }
        if (name.equals("POLICY")) {
            return SpecType.ILLEGAL;
        }
        return SpecType.RESOURCE;
    }

    // get entity type based on short name
    @SuppressWarnings("deprecation")
    public static EntityType getEntityType(String name) {
        if (name.equals("APPLICATION")) {
            return EntityType.APPLICATION;
        }
        if (name.equals("HOST")) {
            return EntityType.HOST;
        }
        if (name.equals("RESOURCE")) {
            return EntityType.RESOURCE;
        }
        if (name.equals("USER")) {
            return EntityType.USER;
        }
        if (name.equals("ACTION")) {
            return EntityType.ACTION;
        }
        if (name.equals("PORTAL")) {
            return EntityType.PORTAL;
        }
        if (name.equals("SERVER")) {
            return EntityType.RESOURCE;
        }
        return EntityType.RESOURCE;
    }
}
