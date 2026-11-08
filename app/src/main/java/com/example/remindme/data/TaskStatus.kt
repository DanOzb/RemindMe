package com.example.remindme.data

val TaskWithDistance.isInRange: Boolean
    get() {
        val radius = task.radiusMeters ?: return false
        return distanceMeters != null && distanceMeters <= radius
    }

/** Time windows aren't modelled yet, so this is just [isInRange] for now. */
val TaskWithDistance.isActiveNow: Boolean
    get() = !task.completed && isInRange
