package ru.sber.cb.ap.gusli.actor.core.entity

import akka.actor.ActorRef
import ru.sber.cb.ap.gusli.actor.core.Entity._
import ru.sber.cb.ap.gusli.actor.core.{ActorBaseTest, Entity, EntityMetaDefault}

class EntityGetParentSpec extends ActorBaseTest("EntityGetParentSpec") {

  "The Root Entity " when {
    val meta = EntityMetaDefault(0, "root-entity", "file.ent", None)
    val root: ActorRef = system.actorOf(Entity(meta))
    "receive GetParent" should {
      "send back NoParentResponse" in {
        root ! GetParent()
        expectMsg(NoParentResponse)
      }
    }

    "The child Entity" when {
      "receive GetParent" should {
        "send back ParentResponse(root)" in {
          val meta = EntityMetaDefault(1, "child-entity", "file.ent", None)
          root ! AddChildEntity(meta)
          expectMsgPF() {
            case EntityCreated(childEntity) =>
              childEntity ! GetParent()
              expectMsg(ParentResponse(root))
          }
        }
      }
    }
  }
}