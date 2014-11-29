ShotTraker DB Package
=====================

This package should contain all classes associated with opening, creating, reading, updating
and deleting the database and tables used by ShotTracker

## DataBaseHelper ##

This class should handle opening and/or creating the database structure, including updating
the database based on version number, as well as creating all necessary tables.

*11/29/2014* - Still a lot I don't understand in here. Need to understand how to properly set the
database up so it opens from a file that is stored somewhere on the device, maybe under app assets?
Also need to make sure database is getting closed properly.

## ShotTrackerDBDAO ##

This is the generic class for database access operations (DAO). All DAO classes should extend this.

*11/29/2014*
Current idea for extended DAO classes:
- **PlayerDAO**: Player specific DAO operations. Should use *Player* objects and deal specifically
  with the Player table.
- **ClubDAO**: Club specific DAO operations. Should use *Club* objects and deal with the *Club*
  table. Also deals with adding clubs to a players "Bag" through interactions with the *Bag* table.
  Here, does it make sense to use PlayerID only, or use a Player object when filling "Bag" table?
- **CourseDAO**: All Course specific DAO operations. This will be a large one, dealing with Course,
  SubCourse, CourseHole, CourseHoleInfo tables using Course, SubCourse, CourseHole, and CourseHoleInfo
  objects.
- **RoundDAO**: All Round specific DAO operations. This will deal with Round, RoundHole, Shot,
  ShotType, and ShotLink DB tables using Round, RoundHole, Shot, and ShotType objects.
- **StatisticsDAO**: Deal with complicated database queries that will be used heavily when compiling
  statistics.

## Resources ##

I've been leaning heavily on the
[androidopentutorials sqlite](http://androidopentutorials.com/android-sqlite-join-multiple-tables-example/)
tutorial when thinking about and writing the above.

Also useful:
- [NotePad android example](https://android.googlesource.com/platform/development/+/05523fb0b48280a5364908b00768ec71edb847a2/samples/NotePad/src/com/example/android/notepad/NotePadProvider.java)
- [Simple android DB tutorial](http://www.techotopia.com/index.php/An_Android_SQLite_Database_Tutorial)
- [Android SQLiteOpenHelper Doc](http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html)
- [Android SQLiteDatabase Doc](http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html)
- [TutorialsPoint android DB](http://www.tutorialspoint.com/android/android_sqlite_database.htm)
- [TutorialsPoint SQLite](http://www.tutorialspoint.com/sqlite/index.htm)