package com.juice.spy.entity;

public class Stock {
    private String code;
    private String name;
    private String type;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this.getClass() == obj.getClass()){
            return code.equals(((Stock)obj).code);
        }
        return false;
    }

}
