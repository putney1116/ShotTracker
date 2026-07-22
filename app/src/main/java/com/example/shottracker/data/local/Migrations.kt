package com.example.shottracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: added penalty tracking
 *   hole_scores.penalties (nullable INTEGER)
 *   rounds.totalPenalties (nullable INTEGER)
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_scores ADD COLUMN penalties INTEGER")
        db.execSQL("ALTER TABLE rounds ADD COLUMN totalPenalties INTEGER")
    }
}

/**
 * v2 -> v3: added Playing Conditions Calculation per round
 *   rounds.pcc (nullable INTEGER, treated as 0 in handicap differential when null)
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rounds ADD COLUMN pcc INTEGER")
    }
}

/**
 * v3 -> v4: added per-course hole notes
 *   hole_info.notes (nullable TEXT; null = no note)
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_info ADD COLUMN notes TEXT")
    }
}

/**
 * v4 -> v5: added per-hole score adjustment for handicap (Net Double Bogey / ESC).
 *   hole_scores.adjustment (nullable INTEGER; null/0 = no adjustment)
 *   rounds.totalAdjustment (nullable INTEGER; aggregate over the round's holes)
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE hole_scores ADD COLUMN adjustment INTEGER")
        db.execSQL("ALTER TABLE rounds ADD COLUMN totalAdjustment INTEGER")
    }
}

/**
 * v5 -> v6: added optional total tee distance (yards).
 *   tees.totalDistance (nullable INTEGER; null = unset)
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tees ADD COLUMN totalDistance INTEGER")
    }
}

/**
 * v6 -> v7: per-tee per-hole handicap (stroke index) + per-round handicap-index snapshot.
 *   tee_hole_info.yardage is now nullable (rebuilt table)
 *   tee_hole_info.handicap (nullable INTEGER; null = no stroke index for this hole/tee)
 *   rounds.handicapIndex (nullable REAL; snapshotted at round-start)
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tee_hole_info RENAME TO tee_hole_info_old")
        db.execSQL("""
            CREATE TABLE tee_hole_info (
              teeHoleInfoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              teeId INTEGER NOT NULL,
              holeInfoId INTEGER NOT NULL,
              yardage INTEGER,
              handicap INTEGER,
              FOREIGN KEY(teeId) REFERENCES tees(teeId) ON DELETE CASCADE,
              FOREIGN KEY(holeInfoId) REFERENCES hole_info(holeInfoId) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO tee_hole_info (teeHoleInfoId, teeId, holeInfoId, yardage, handicap)
            SELECT teeHoleInfoId, teeId, holeInfoId, yardage, NULL FROM tee_hole_info_old
        """.trimIndent())
        // Drop the old table BEFORE recreating indexes — renaming carried the old
        // indexes along with the table, so the names are still in use until the
        // old table is dropped.
        db.execSQL("DROP TABLE tee_hole_info_old")
        db.execSQL("CREATE INDEX index_tee_hole_info_teeId ON tee_hole_info(teeId)")
        db.execSQL("CREATE INDEX index_tee_hole_info_holeInfoId ON tee_hole_info(holeInfoId)")
        db.execSQL("ALTER TABLE rounds ADD COLUMN handicapIndex REAL")
    }
}
