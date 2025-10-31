package com.yrlee.tp08tourapi.data;

import androidx.annotation.NonNull;

public class CodeItem {
    public String code;
    public String name;
    public CodeItem(){};
    public CodeItem(String name, String code){
        this.name = name;
        this.code = code;
    }
    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
