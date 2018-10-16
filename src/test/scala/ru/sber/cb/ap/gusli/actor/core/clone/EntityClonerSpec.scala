package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Entity._
import ru.sber.cb.ap.gusli.actor.core.Project.{EntityFound, EntityNotFound, FindEntity}
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.clone.EntityCloner.EntitiesCloneSuccessful

class EntityClonerSpec extends ActorBaseTest("EntityClonerSpec") {
  val receiver = TestProbe()
  val metaRoot = EntityMetaDefault(0, "category", "", None)
  val fromRoot: ActorRef = system.actorOf(Entity(metaRoot))
  fromRoot ! AddChildEntity(EntityMetaDefault(1, "e-1", "category/e-1", Some(0)))
  var e1: ActorRef = null
  var e2: ActorRef = null
  var e21: ActorRef = null
  var e11: ActorRef = null
  var e111: ActorRef = null
  var e112: ActorRef = null
  expectMsgPF() {
    case EntityCreated(e) =>
      e1 = e
      e1 ! AddChildEntity(EntityMetaDefault(11, "e-11", "category/e-1/e-11", Some(1)))

      expectMsgPF() {
        case EntityCreated(ee) =>
          e11 = ee

          e11 ! AddChildEntity(EntityMetaDefault(111, "e-111", "category/e-1/e-11/e-111", Some(11)))
          expectMsgPF() {
            case EntityCreated(eee) =>
              e111 = eee
          }

          e11 ! AddChildEntity(EntityMetaDefault(112, "e-112", "category/e-1/e-11/e-112", Some(11)))
          expectMsgPF() {
            case EntityCreated(eee) =>
              e112 = eee
          }
      }
  }
  fromRoot ! AddChildEntity(EntityMetaDefault(2, "e-2", "category/e-2", Some(0)))
  expectMsgPF() {
    case EntityCreated(e) =>
      e2 = e

      e2 ! AddChildEntity(EntityMetaDefault(21, "e-21", "category/e-21", Some(2)))
      expectMsgPF() {
        case EntityCreated(ee) =>
          e21 = ee
      }
  }

  "An EntityCloner with empty root entity" when {
    val toRoot: ActorRef = system.actorOf(Entity(metaRoot))
    "toRoot don't contains entity with id 111" in {
      toRoot ! FindEntity(111L)
      expectMsg(EntityNotFound(111L))
    }

    "toRoot do not contains entity with id 21" in {
      toRoot ! FindEntity(21)
      expectMsg(EntityNotFound(21))
    }

    "created to clone entity of 3 depth length" should {
      "send back EntitiesCloneSuccessful" in {
        system.actorOf(EntityCloner(fromRoot, toRoot, Set(111L, 112L, 21L), receiver.ref))

        receiver.expectMsg(EntitiesCloneSuccessful)
      }

      "toRoot contains two children" in {
        toRoot ! GetChildren()
        expectMsgPF() {
          case ChildrenEntityList(list) =>
            assert(list.size == 2)
        }
      }

      "toRoot contains entity with id 111" in {
        toRoot ! FindEntity(111L)
        expectMsgPF() {
          case EntityFound(m, _) =>
            assert(m == EntityMetaDefault(111, "e-111", "category/e-1/e-11/e-111", Some(11)))
        }
      }
      "toRoot contains entity with id 112" in {
        toRoot ! FindEntity(112L)
        expectMsgPF() {
          case EntityFound(m, _) =>
            assert(m == EntityMetaDefault(112, "e-112", "category/e-1/e-11/e-112", Some(11)))
        }
      }

      "toRoot contains entity with id 21" in {
        toRoot ! FindEntity(112L)
        expectMsgPF() {
          case EntityFound(m, _) =>
            assert(m == EntityMetaDefault(21, "e-21", "category/e-21", Some(2)))
        }
      }
    }
  }
}