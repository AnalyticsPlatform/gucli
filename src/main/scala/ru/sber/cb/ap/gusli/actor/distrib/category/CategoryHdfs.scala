package ru.sber.cb.ap.gusli.actor.distrib.category

import java.nio.file.Path

import ru.sber.cb.ap.gusli.actor.core.dto.CategoryDto

object CategoryHdfs {
  def apply(hdfsPath: Path, currCat: CategoryDto, parCat: CategoryDto,
    fullCatName: String): CategoryHdfs = new CategoryHdfs(hdfsPath, currCat, parCat, fullCatName)
  
  
}

class CategoryHdfs(hdfsPath: Path, currCat: CategoryDto, parCat: CategoryDto, fullCatName: String) {
  private val thisHdfsPath = hdfsPath.resolve("user").resolve("__USER__").resolve(fullCatName)
  
  if(currCat.init != parCat.init)
    writeInit()
  
  
  private def writeInit() = ()
}