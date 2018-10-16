package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Entity._
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.clone.EntityHierarchyCloner.{EntityHierarchyCloneSuccessful, RootEntityCantBeCloned}


class EntityHierarchyClonerSpec extends ActorBaseTest("EntityHierarchyClonerSpec") {
  "An EntityHierarchyCloner" when {
    val entityProbe = TestProbe()
    val receiver = TestProbe()
    val meta = EntityMetaDefault(0, "root", "", None)
    val root: ActorRef = system.actorOf(Entity(meta))

    "try clone root entity" should {
      system.actorOf(EntityHierarchyCloner(entityProbe.ref, root, receiver.ref))
      "send back RootEntityCantBeCloned" in {
        entityProbe.expectMsg(GetParent())
        entityProbe.reply(NoParentResponse)
        entityProbe.expectMsg(GetEntityMeta())
        entityProbe.reply(EntityMetaResponse(EntityMetaDefault(0, "category", "", None)))

        receiver.expectMsg(RootEntityCantBeCloned)
      }
    }
  }

  "An EntityHierarchyCloner" when {
    "created to clone entity" should {
      val toRoot = TestProbe()
      val entity1 = TestProbe()
      val receiver = TestProbe()

      system.actorOf(EntityHierarchyCloner(entity1.ref, toRoot.ref, receiver.ref))
      "send back SingleEntityToRootCloneSuccessful(...)" in {
        val fromRoot = TestProbe()
        val metaToClone = EntityMetaDefault(1, "e-1", "category/e-1", Some(0))

        entity1.expectMsg(GetParent())
        entity1.reply(ParentResponse(fromRoot.ref))
        entity1.expectMsg(GetEntityMeta())
        entity1.reply(EntityMetaResponse(metaToClone))

        fromRoot.expectMsg(GetParent())
        fromRoot.reply(NoParentResponse)
        fromRoot.expectMsg(GetEntityMeta())
        fromRoot.reply(EntityMetaResponse(EntityMetaDefault(0, "category", "category", None)))

        val clonedE1 = TestProbe()
        toRoot.expectMsg(AddChildEntity(metaToClone))
        toRoot.reply(EntityCreated(clonedE1.ref))

        receiver.expectMsgPF() {
          case EntityHierarchyCloneSuccessful(e, m) =>
            assert(m == metaToClone)
            assert(e == clonedE1.ref)
        }
      }
    }
  }


  "A SingleEntityToRootClone" when {
    val receiver = TestProbe()
    val meta = EntityMetaDefault(0, "category", "", None)
    val toRoot: ActorRef = system.actorOf(Entity(meta))
    val fromRoot: ActorRef = system.actorOf(Entity(meta))
    fromRoot ! AddChildEntity(EntityMetaDefault(1, "e-1", "category/e-1", Some(0)))
    var e1: ActorRef = null
    var e11: ActorRef = null
    var e111: ActorRef = null
    expectMsgPF() {
      case EntityCreated(e) =>
        e1 = e
        e1 ! AddChildEntity(EntityMetaDefault(11, "e-11", "category/e-1/e-11", Some(1)))

        expectMsgPF() {
          case EntityCreated(ee) =>
            e11 = ee
            ee ! AddChildEntity(EntityMetaDefault(111, "e-111", "category/e-1/e-11/e-111", Some(11)))

            expectMsgPF() {
              case EntityCreated(eee) =>
                e111 = eee
            }
        }
    }
    "created to clone entity of 3 depth length" should {
      system.actorOf(EntityHierarchyCloner(e111, toRoot, receiver.ref))
      "send back SingleEntityToRootCloneSuccessful(....)" in {
        receiver.expectMsgPF() {
          case EntityHierarchyCloneSuccessful(e, m) =>
            assert(m == EntityMetaDefault(111, "e-111", "category/e-1/e-11/e-111", Some(11)))
            e ! GetParent()
            expectMsgPF() {
              case ParentResponse(p) =>
                p ! GetEntityMeta()
                expectMsg(EntityMetaResponse(EntityMetaDefault(11, "e-11", "category/e-1/e-11", Some(1))))
                p ! GetParent()
                expectMsgPF() {
                  case ParentResponse(pp) =>
                    pp ! GetEntityMeta()
                    expectMsg(EntityMetaResponse(EntityMetaDefault(1, "e-1", "category/e-1", Some(0))))
                }
            }
        }
      }
    }
  }
}