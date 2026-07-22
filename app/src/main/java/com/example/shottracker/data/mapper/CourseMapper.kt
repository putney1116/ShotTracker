package com.example.shottracker.data.mapper

import com.example.shottracker.data.local.entity.CourseEntity
import com.example.shottracker.data.local.entity.HoleInfoEntity
import com.example.shottracker.data.local.entity.TeeEntity
import com.example.shottracker.domain.model.Course
import com.example.shottracker.domain.model.HoleInfo
import com.example.shottracker.domain.model.Tee

fun CourseEntity.toDomain(): Course = Course(
    id = courseId,
    name = name,
    city = city,
    state = state
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    courseId = id,
    name = name,
    city = city,
    state = state
)

fun HoleInfoEntity.toDomain(): HoleInfo = HoleInfo(
    id = holeInfoId,
    courseId = courseId,
    holeNumber = holeNumber,
    par = par,
    greenFrontLat = greenFrontLat,
    greenFrontLng = greenFrontLng,
    greenCenterLat = greenCenterLat,
    greenCenterLng = greenCenterLng,
    greenBackLat = greenBackLat,
    greenBackLng = greenBackLng,
    notes = notes
)

fun HoleInfo.toEntity(): HoleInfoEntity = HoleInfoEntity(
    holeInfoId = id,
    courseId = courseId,
    holeNumber = holeNumber,
    par = par,
    greenFrontLat = greenFrontLat,
    greenFrontLng = greenFrontLng,
    greenCenterLat = greenCenterLat,
    greenCenterLng = greenCenterLng,
    greenBackLat = greenBackLat,
    greenBackLng = greenBackLng,
    notes = notes
)

fun TeeEntity.toDomain(): Tee = Tee(
    id = teeId,
    courseId = courseId,
    name = name,
    color = color,
    rating = rating,
    slope = slope,
    totalDistance = totalDistance
)

fun Tee.toEntity(): TeeEntity = TeeEntity(
    teeId = id,
    courseId = courseId,
    name = name,
    color = color,
    rating = rating,
    slope = slope,
    totalDistance = totalDistance
)

