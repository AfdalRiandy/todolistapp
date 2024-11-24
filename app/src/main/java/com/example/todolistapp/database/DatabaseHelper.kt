package com.example.todolistapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TodoDB"
        private const val DATABASE_VERSION = 2

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"

        // Todos table
        private const val TABLE_TODOS = "todos"
        private const val COLUMN_TODO_ID = "id"
        private const val COLUMN_USER_ID_FK = "user_id"
        private const val COLUMN_TASK = "task"
        private const val COLUMN_IS_COMPLETED = "is_completed"
        private const val COLUMN_DUE_DATE = "due_date" // New column for due date
        private const val COLUMN_DUE_TIME = "due_time" // New column for due time

    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()

        // Create todos table with foreign key and due_date
        val createTodosTable = """
        CREATE TABLE $TABLE_TODOS (
            $COLUMN_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID_FK INTEGER NOT NULL,
            $COLUMN_TASK TEXT NOT NULL,
            $COLUMN_DUE_DATE INTEGER,  -- Store due date as long (timestamp)
            $COLUMN_DUE_TIME INTEGER,  -- Store due time as long (timestamp)
            $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
            FOREIGN KEY($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
        )
    """.trimIndent()


        db.execSQL(createUsersTable)
        db.execSQL(createTodosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            // Add the due_time column if upgrading
            db.execSQL("ALTER TABLE $TABLE_TODOS ADD COLUMN $COLUMN_DUE_TIME INTEGER")
        }
    }

    // User related operations
    fun registerUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password) // In a real app, hash the password
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun loginUser(email: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    password = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                )
            } else null
        }
    }

    // Modified Todo operations to include user_id and due_date
    fun insertTodo(todo: Todo): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID_FK, todo.userId)
            put(COLUMN_TASK, todo.task)
            put(COLUMN_DUE_DATE, todo.dueDate)  // Storing due date
            put(COLUMN_DUE_TIME, todo.dueTime)  // Storing due time
            put(COLUMN_IS_COMPLETED, if (todo.isCompleted) 1 else 0)
        }
        return db.insert(TABLE_TODOS, null, values)
    }


    fun getAllTodosForUser(userId: Int): List<Todo> {
        val todos = mutableListOf<Todo>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TODOS,
            null,
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_DUE_DATE ASC, $COLUMN_DUE_TIME ASC, $COLUMN_TODO_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                todos.add(
                    Todo(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TODO_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                        task = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK)),
                        dueDate = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_DUE_DATE))) null
                        else it.getLong(it.getColumnIndexOrThrow(COLUMN_DUE_DATE)),
                        dueTime = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_DUE_TIME))) null
                        else it.getLong(it.getColumnIndexOrThrow(COLUMN_DUE_TIME)),
                        isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1
                    )
                )
            }
        }
        return todos
    }


    fun updateTodo(todo: Todo): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK, todo.task)
            put(COLUMN_DUE_DATE, todo.dueDate)  // Updating due date
            put(COLUMN_IS_COMPLETED, if (todo.isCompleted) 1 else 0)
        }
        return db.update(
            TABLE_TODOS,
            values,
            "$COLUMN_TODO_ID = ? AND $COLUMN_USER_ID_FK = ?",
            arrayOf(todo.id.toString(), todo.userId.toString())
        )
    }

    fun deleteTodo(todoId: Int, userId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_TODOS,
            "$COLUMN_TODO_ID = ? AND $COLUMN_USER_ID_FK = ?",
            arrayOf(todoId.toString(), userId.toString())
        )
    }

    fun getUserById(userId: Int): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    password = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                )
            } else null
        }
    }

    fun updateUserProfile(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
        }
        return db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString())
        )
    }

    fun updateUserPassword(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, user.password)
        }
        return db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString())
        )
    }


    fun getDistinctTodoDates(userId: Int, startDate: Long, endDate: Long): List<Long> {
        val dates = mutableListOf<Long>()
        val db = readableDatabase

        val query = """
        SELECT DISTINCT $COLUMN_DUE_DATE 
        FROM $TABLE_TODOS 
        WHERE $COLUMN_USER_ID_FK = ? 
        AND $COLUMN_DUE_DATE >= ? 
        AND $COLUMN_DUE_DATE <= ? 
        ORDER BY $COLUMN_DUE_DATE ASC
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(userId.toString(), startDate.toString(), endDate.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                val dueDate = it.getLong(0)
                if (!it.isNull(0)) {
                    // Normalize the date to start of day
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = dueDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    dates.add(calendar.timeInMillis)
                }
            }
        }
        return dates
    }

    // Optional: Add a method to check if a specific date has todos
    fun hasTasksForDate(userId: Int, date: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        // Set time to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // Set time to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val db = readableDatabase
        val cursor = db.query(
            TABLE_TODOS,
            arrayOf("COUNT(*)"),
            "$COLUMN_USER_ID_FK = ? AND $COLUMN_DUE_DATE >= ? AND $COLUMN_DUE_DATE <= ?",
            arrayOf(userId.toString(), startOfDay.toString(), endOfDay.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            it.moveToFirst()
            it.getInt(0) > 0
        }
    }

    fun getTodosForDateRange(userId: Int, startDate: Long, endDate: Long): List<Todo> {
        val todos = mutableListOf<Todo>()
        val db = readableDatabase

        val selection = """
        $COLUMN_USER_ID_FK = ? AND 
        $COLUMN_DUE_DATE >= ? AND 
        $COLUMN_DUE_DATE <= ?
    """.trimIndent()

        val selectionArgs = arrayOf(
            userId.toString(),
            startDate.toString(),
            endDate.toString()
        )

        val cursor = db.query(
            TABLE_TODOS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_DUE_DATE ASC, $COLUMN_DUE_TIME ASC, $COLUMN_TODO_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                todos.add(
                    Todo(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TODO_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                        task = it.getString(it.getColumnIndexOrThrow(COLUMN_TASK)),
                        dueDate = if (!it.isNull(it.getColumnIndexOrThrow(COLUMN_DUE_DATE)))
                            it.getLong(it.getColumnIndexOrThrow(COLUMN_DUE_DATE))
                        else null,
                        dueTime = if (!it.isNull(it.getColumnIndexOrThrow(COLUMN_DUE_TIME)))
                            it.getLong(it.getColumnIndexOrThrow(COLUMN_DUE_TIME))
                        else null,
                        isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1
                    )
                )
            }
        }
        return todos
    }
}
