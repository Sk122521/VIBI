package com.example.pollinguserapp.utils;


import com.example.pollinguserapp.model.resultModel;

public class ResultUtils{

    private int AGD;
    private int GD;
    public ResultUtils(int AGD, int GD){
       this.AGD = AGD*100;
       this.GD = GD;
    }

    public resultModel getResult(){
        double s = (double) AGD / GD;

        int n = GD; // Assuming n = 100
        double m = n * s;
        double d = Math.sqrt(n * s * (1 - s));
        double μ_lower = m - 1.654 * d;
        double μ_upper = m + 1.654 * d;

        double p_lower = μ_lower / GD;
        double p_upper = μ_upper / GD;

        // Calculate the lower and upper percentages of p and ignore the decimal part
        double p_lower_percentage =  (p_lower * 100);
        double p_upper_percentage =  (p_upper * 100);

       int l = (int) p_lower_percentage;
       int u = (int) p_upper_percentage+1;


        Float p_average_percentage = (float) ((p_lower_percentage +  p_upper_percentage) / 2);

        // Calculate the error (difference) from the average percentage
        Float error = (float) Math.abs(p_upper_percentage - p_average_percentage);

       // Float percentageerror = (error/p_average_percentage)*100;

        resultModel  resultModel = new resultModel(p_average_percentage,error,l,u);
        return  resultModel;
    }

}
