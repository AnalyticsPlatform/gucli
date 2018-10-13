package ru.sber.cb.ap.gusli.actor.core.diff.categoryDtoDiffer

import org.scalatest.{FlatSpec, Matchers}
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, WorkflowDto}

class GetDiffSpec extends FlatSpec with Matchers {

  import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDtoDiffer._

  "A getDiff" should "return the CategoryEquals for the equal category's dto" in {
    val c1 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"))
    val c2 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"))

    getDiff(c1, c2) should be(CategoryDtoEquals(c1, c2))
  }

  it should "return the CategoryDelta for the non-equal category's dto" in {
    val c1 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"))
    val c2 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=2"))

    getDiff(c1, c2) should be(CategoryDtoDelta(c1))
  }

  it should "throw RuntimeException if the category has a different name" in {
    val c1 = CategoryDto("category", Map("targetA" -> "targetB"), Map("init" -> "set x=1"))
    val c2 = CategoryDto("category2", Map("targetA" -> "targetB"), Map("init" -> "set x=1"))

    a[RuntimeException] should be thrownBy {
      getDiff(c1, c2)
    }
  }

  it should "not contains the equal subcategories" in {
    val subCat = CategoryDto("subCat", Map("target" -> "targetNew"), Map("init" -> "set x=1"))
    val c1 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"), subcategories = Set(subCat))
    val c2 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=2"), subcategories = Set(subCat))

    getDiff(c1, c2) should be(
      CategoryDtoDelta(
        CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"))
      )
    )
  }

  it should "contains the differ subcategories" in {
    val subCat = CategoryDto("subCat", Map("target" -> "targetNew"), Map("init" -> "set x=1"))
    val c1 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"), subcategories = Set(subCat))
    val c2 = CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=2"))

    getDiff(c1, c2) should be(
      CategoryDtoDelta(
        CategoryDto("cat", Map("target" -> "targetNew"), Map("init" -> "set x=1"), subcategories = Set(subCat))
      )
    )
  }

  it should "not contains the equal workflows" in {
    val wf1 = WorkflowDto("wf-1", Map("sql" -> "select 1 as s"))
    val wf11 = WorkflowDto("wf-11", Map("sql" -> "select 11 as s"))
    val wf12 = WorkflowDto("wf-12", Map("sql" -> "select 12 as s"))
    val subCat = CategoryDto("subCat", entities = Set(1, 2), workflows = Set(wf11, wf12))
    val subCat2 = CategoryDto("subCat", entities = Set(1, 2, 3), workflows = Set(wf11, wf1))

    val c1 = CategoryDto(name = "cat",
      sqlMap = Map("target" -> "targetNew"),
      init = Map("init" -> "set x=1"),
      subcategories = Set(subCat),
      workflows = Set(wf1, wf11)
    )

    val c2 = CategoryDto(name = "cat",
      sqlMap = Map("target" -> "targetNew"),
      init = Map("init" -> "set x=2"),
      subcategories = Set(subCat2),
      workflows = Set(wf11)
    )

    getDiff(c1, c2) match {
      case CategoryDtoDelta(dto) =>
        dto should be(
          CategoryDto(name = "cat",
            sqlMap = Map("target" -> "targetNew"),
            init = Map("init" -> "set x=1"),
            workflows = Set(wf1),
            subcategories = Set(
              CategoryDto(
                name = "subCat",
                entities = Set(1, 2),
                workflows = Set(wf12))
            )
          )
        )
    }
  }

  it should "return the CategoryDelta for differ category at 3 depth length" in {
    val wf111 = WorkflowDto("wf-111", Map("sql" -> "select 2 as s"))
    val wf112 = WorkflowDto("wf-111", Map("sql" -> "select 1 as s"))
    val c111 = CategoryDto("cat-111", workflows = Set(wf111))
    val c112 = CategoryDto("cat-111", workflows = Set(wf112))
    val c11 = CategoryDto("cat-11", subcategories = Set(c111))
    val c12 = CategoryDto("cat-11", subcategories = Set(c112))
    val c1 = CategoryDto("cat", subcategories = Set(c11))
    val c2 = CategoryDto("cat", subcategories = Set(c12))

    getDiff(c1, c2) match {
      case CategoryDtoDelta(dto) =>
        dto should be(CategoryDto("cat",
          subcategories = Set(
            CategoryDto("cat-11",
              subcategories = Set(
                CategoryDto("cat-111",
                  workflows = Set(wf111))
              )
            )
          )
        )
        )
    }
  }
}
