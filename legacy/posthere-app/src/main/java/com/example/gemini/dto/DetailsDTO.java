package com.example.gemini.dto;

public class DetailsDTO {
    private String fname;
    private String lname;
    private String city;

    // No-args constructor
    public DetailsDTO() {
    }

    // All-args constructor
    public DetailsDTO(String fname, String lname, String city) {
        this.fname = fname;
        this.lname = lname;
        this.city = city;
    }

    // Getters and Setters
    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "DetailsDTO{" +
                "fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
