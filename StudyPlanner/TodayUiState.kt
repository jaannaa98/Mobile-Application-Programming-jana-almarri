
package sa.edu.iau.cshj.studyplanner

data class TodayUiState(
    val isLoading: Boolean = true,
    val tasks: List<StudyTask> = emptyList(),
    val message: String? = null
)

data class PlannerUiState(
    val greeting: String = "Welcome to StudyPlanner!",
    val tasks: List<StudyTask> = emptyList()
)
