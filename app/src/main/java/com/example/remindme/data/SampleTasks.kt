package com.example.remindme.data

// Fake data for previews. Places sit north of Stockholm Central (sampleLocation),
// at roughly 150 m, 900 m and 3.2 km. Deliberately unsorted.
private val now = System.currentTimeMillis()
private const val HOUR = 60 * 60 * 1000L
private const val DAY = 24 * HOUR
private val today = todayEpochDay()

val sampleLocation = LatLng(59.3301, 18.0586)

val sampleTasks: List<TaskEntity> = listOf(
    TaskEntity(
        id = 2,
        title = "Call Mum",
        createdAt = now - 2 * DAY,
        dueDate = today,
        dueTimeMinutes = 18 * 60,
    ),
    TaskEntity(
        id = 4,
        title = "Water the plants",
        createdAt = now - DAY,
        placeName = "Home",
        latitude = 59.3589,
        longitude = 18.0586,
        radiusMeters = 100,
    ),
    TaskEntity(
        id = 5,
        title = "Return library books",
        completed = true,
        createdAt = now - 5 * DAY,
        completedAt = now - DAY,
        placeName = "Maple Library",
        latitude = 59.3430,
        longitude = 18.0540,
        radiusMeters = 200,
    ),
    TaskEntity(
        id = 1,
        title = "Buy apples",
        createdAt = now - 3 * HOUR,
        placeName = "Green Basket",
        latitude = 59.33145,
        longitude = 18.0586,
        radiusMeters = 200,
    ),
    TaskEntity(
        id = 6,
        title = "Buy birthday card",
        completed = true,
        createdAt = now - 4 * DAY,
        completedAt = now - 2 * DAY,
        placeName = "Corner Shop",
        dueDate = today,
        dueTimeMinutes = 11 * 60 + 30,
    ),
    TaskEntity(
        id = 3,
        title = "Pick up prescription",
        createdAt = now - 6 * HOUR,
        dueDate = today,
        dueTimeMinutes = 17 * 60,
        placeName = "Oak Street Pharmacy",
        latitude = 59.3382,
        longitude = 18.0586,
        radiusMeters = 200,
    ),
)

val sampleActive: List<TaskWithDistance> get() = orderActive(sampleTasks.filterNot { it.completed }, sampleLocation)
val sampleCompleted: List<TaskEntity> get() = orderCompleted(sampleTasks.filter { it.completed })
