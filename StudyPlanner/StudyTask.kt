
package sa.edu.iau.cshj.studyplanner

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val course: String,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean = false
)

fun sampleStudyTasks(): List<StudyTask> = listOf(
    StudyTask(1, "Complete Kotlin Lab", "CSC402", true),
    StudyTask(2, "Study Power BI", "CSC403", false),
    StudyTask(3, "Review Image Processing", "ARTI407", true),
    StudyTask(4, "Prepare Presentation", "CSC401", false),
    StudyTask(5, "Finish Assignment", "CSC402", false)
)
