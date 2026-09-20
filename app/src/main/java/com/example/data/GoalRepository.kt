package com.example.data

import com.example.model.Goal
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GoalRepository(private val goalDao: GoalDao) {
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()

    suspend fun getGoalById(id: Long): Goal? = goalDao.getGoalById(id)

    suspend fun getGoalsWithReminders(): List<Goal> = goalDao.getGoalsWithReminders()

    suspend fun getGoalsWithAlarms(): List<Goal> = goalDao.getGoalsWithAlarms()

    suspend fun insert(goal: Goal): Long = goalDao.insertGoal(goal)

    suspend fun update(goal: Goal) = goalDao.updateGoal(goal)

    suspend fun delete(goal: Goal) = goalDao.deleteGoal(goal)

    suspend fun deleteById(id: Long) = goalDao.deleteGoalById(id)

    suspend fun replaceAll(goals: List<Goal>) {
        goalDao.deleteAllGoals()
        goalDao.insertAll(goals)
    }

    suspend fun incrementProgress(goalId: Long, amount: Int = 1) {
        val goal = goalDao.getGoalById(goalId) ?: return
        val newCount = (goal.currentCount + amount).coerceAtLeast(0)
        val isNowCompleted = newCount >= goal.targetCount
        updateGoalProgress(goal, newCount, isNowCompleted)
    }

    suspend fun toggleComplete(goalId: Long) {
        val goal = goalDao.getGoalById(goalId) ?: return
        val willBeCompleted = !goal.isCompleted
        val newCount = if (willBeCompleted) goal.targetCount else 0
        updateGoalProgress(goal, newCount, willBeCompleted)
    }

    private suspend fun updateGoalProgress(goal: Goal, newCount: Int, isCompleted: Boolean) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val historyList = if (goal.historyDates.isEmpty()) mutableListOf() else goal.historyDates.split(",").toMutableList()

        val updatedStreak = if (isCompleted && !goal.isCompleted) {
            if (!historyList.contains(today)) {
                historyList.add(today)
            }
            goal.streak + 1
        } else if (!isCompleted && goal.isCompleted) {
            historyList.remove(today)
            (goal.streak - 1).coerceAtLeast(0)
        } else {
            goal.streak
        }

        val updatedBest = maxOf(goal.bestStreak, updatedStreak)

        val updatedGoal = goal.copy(
            currentCount = newCount,
            isCompleted = isCompleted,
            streak = updatedStreak,
            bestStreak = updatedBest,
            lastCompletedDate = if (isCompleted) today else goal.lastCompletedDate,
            historyDates = historyList.joinToString(",")
        )
        goalDao.updateGoal(updatedGoal)
    }
}
