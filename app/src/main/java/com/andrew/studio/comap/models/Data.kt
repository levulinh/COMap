package com.andrew.studio.comap.models

class Data {
    var lat: Double = 0.0
    var long: Double = 0.0
    var covalue: Int = 0
    var temperature: Int = 0
    var humidity: Int = 0

    constructor(lat: Double, long: Double, covalue: Int, temperature: Int, humidity: Int) {
        this.lat = lat
        this.long = long
        this.covalue = covalue
        this.temperature = temperature
        this.humidity = humidity
    }
}