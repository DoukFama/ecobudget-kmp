package com.example.viewmodel

import com.example.model.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EcoBudgetUiStateTest {

  @Test
  fun budget_usage_is_computed_from_spent_and_budget() {
    val state = EcoBudgetUiState(
      monthlyBudget = 500_000.0,
      totalSpent = 250_000.0
    )
    assertEquals(0.5f, state.budgetUsageRatio)
    assertEquals(50, state.budgetUsagePercentage)
  }

  @Test
  fun budget_usage_ratio_is_capped_at_one() {
    val state = EcoBudgetUiState(
      monthlyBudget = 100_000.0,
      totalSpent = 250_000.0
    )
    assertEquals(1.0f, state.budgetUsageRatio)
  }

  @Test
  fun all_categories_selected_when_filter_is_empty() {
    assertTrue(EcoBudgetUiState().isAllCategoriesSelected)
  }

  @Test
  fun filter_is_not_all_when_a_subset_is_selected() {
    val state = EcoBudgetUiState(selectedCategories = setOf(Category.TRANSPORT))
    assertFalse(state.isAllCategoriesSelected)
  }
}
