package com.jdbc.bean;

import lombok.Data;

@Data
public class Hero {
    private Long id;
    private String name;
    private String camp;

    @Override
    public String toString() {
        return "Hero{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", camp='" + camp + '\'' +
                '}';
    }
}
