package com.jayway.annostatemachine;


public class ParameterRef {
    private final String type;
    private final String name;

    public ParameterRef(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof  ParameterRef) && ((ParameterRef) o).name.equals(name);
    }

    @Override
    public int hashCode() {
        return 17 + name.hashCode();
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
