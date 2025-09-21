package org.xhy.common.constants;

public enum RpcSerialization {
    JSON("json"),
    JDK("jdk");

    public String name;
    RpcSerialization(String type){
        this.name = type;
    }

    public static RpcSerialization get(String type){
        for (RpcSerialization value : values()) {
            if (value.name.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
