package com.example.remindme.data.place

import com.example.remindme.data.LatLng
import com.example.remindme.data.distanceMeters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceSearchTest {

    private val stockholm = LatLng(59.3301, 18.0586)

    @Test
    fun the_bounding_box_reaches_the_radius_in_every_direction() {
        val box = boundingBox(stockholm, 40_000.0)
        val north = LatLng(box.northEast.latitude, stockholm.longitude)
        val east = LatLng(stockholm.latitude, box.northEast.longitude)
        val south = LatLng(box.southWest.latitude, stockholm.longitude)
        assertEquals(40_000.0, distanceMeters(stockholm, north), 300.0)
        assertEquals(40_000.0, distanceMeters(stockholm, east), 300.0)
        assertEquals(40_000.0, distanceMeters(stockholm, south), 300.0)
    }

    @Test
    fun the_bounding_box_stays_valid_near_the_poles_and_the_date_line() {
        val box = boundingBox(LatLng(89.9, 179.9), 40_000.0)
        assertTrue(box.northEast.latitude <= 90.0)
        assertTrue(box.northEast.longitude <= 180.0)
        assertTrue(box.southWest.longitude >= -180.0)
    }

    @Test
    fun a_business_name_becomes_the_label() {
        assertEquals("ICA Kvantum", placeLabel("ICA Kvantum", "Rädisvägen", "12", "Rädisvägen 12, 123 45 Stockholm, Sweden"))
    }

    @Test
    fun a_house_number_or_street_as_feature_name_falls_back_to_the_address_line() {
        val line = "Rädisvägen 12, 123 45 Stockholm, Sweden"
        assertEquals("Rädisvägen 12", placeLabel("12", "Rädisvägen", "12", line))
        assertEquals("Rädisvägen 12", placeLabel("Rädisvägen", "Rädisvägen", null, line))
        assertEquals("Rädisvägen 12", placeLabel(null, null, null, line))
    }
}
