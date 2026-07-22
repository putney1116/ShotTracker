package com.example.shottracker.data.mapper

import com.example.shottracker.data.local.entity.HoleScoreEntity
import com.example.shottracker.data.local.entity.RoundEntity
import com.example.shottracker.data.local.entity.ShotEntity
import com.example.shottracker.domain.model.HoleScore
import com.example.shottracker.domain.model.Round
import com.example.shottracker.domain.model.RoundStatus
import com.example.shottracker.domain.model.Shot
import java.time.Instant

fun RoundEntity.toDomain(holeScores: List<HoleScore> = emptyList()): Round = Round(
    id = roundId,
    teeId = teeId,
    courseName = courseName,
    startTime = Instant.ofEpochMilli(startTime),
    endTime = endTime?.let { Instant.ofEpochMilli(it) },
    status = RoundStatus.entries.find { it.name == status } ?: RoundStatus.IN_PROGRESS,
    holesPlayed = holesPlayed,
    totalScore = totalScore,
    totalPutts = totalPutts,
    totalPenalties = totalPenalties,
    pcc = pcc,
    totalAdjustment = totalAdjustment,
    handicapIndex = handicapIndex,
    holeScores = holeScores
)

fun Round.toEntity(): RoundEntity = RoundEntity(
    roundId = id,
    teeId = teeId,
    courseName = courseName,
    startTime = startTime.toEpochMilli(),
    endTime = endTime?.toEpochMilli(),
    status = status.name,
    holesPlayed = holesPlayed,
    totalScore = totalScore,
    totalPutts = totalPutts,
    totalPenalties = totalPenalties,
    pcc = pcc,
    totalAdjustment = totalAdjustment,
    handicapIndex = handicapIndex
)

fun HoleScoreEntity.toDomain(shots: List<Shot> = emptyList()): HoleScore = HoleScore(
    id = holeScoreId,
    roundId = roundId,
    holeNumber = holeNumber,
    par = par,
    score = score,
    putts = putts,
    penalties = penalties,
    fairwayHit = fairwayHit,
    greenInRegulation = greenInRegulation,
    adjustment = adjustment,
    shots = shots
)

fun HoleScore.toEntity(): HoleScoreEntity = HoleScoreEntity(
    holeScoreId = id,
    roundId = roundId,
    holeNumber = holeNumber,
    par = par,
    score = score,
    putts = putts,
    penalties = penalties,
    fairwayHit = fairwayHit,
    greenInRegulation = greenInRegulation,
    adjustment = adjustment
)

fun ShotEntity.toDomain(clubName: String? = null): Shot = Shot(
    id = shotId,
    holeScoreId = holeScoreId,
    clubId = clubId,
    clubName = clubName,
    shotNumber = shotNumber,
    latitude = latitude,
    longitude = longitude,
    distanceYards = distanceYards,
    timestamp = Instant.ofEpochMilli(timestamp)
)

fun Shot.toEntity(): ShotEntity = ShotEntity(
    shotId = id,
    holeScoreId = holeScoreId,
    clubId = clubId,
    shotNumber = shotNumber,
    latitude = latitude,
    longitude = longitude,
    distanceYards = distanceYards,
    timestamp = timestamp.toEpochMilli()
)
