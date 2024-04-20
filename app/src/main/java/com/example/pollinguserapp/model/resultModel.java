package com.example.pollinguserapp.model;

public class resultModel {
    private Float average_percent, error;
    private int l,u;
    public resultModel(){}
    public resultModel( Float average_percent, Float error,int l , int u){
       this.average_percent = average_percent;
       this.error = error;
       this.l = l;
       this.u = u;

    }

    public int getL() {
        return l;
    }

    public int getU() {
        return u;
    }

    public Float getAverage_percent() {
        return average_percent;
    }

    public Float getError() {
        return error;
    }
}
