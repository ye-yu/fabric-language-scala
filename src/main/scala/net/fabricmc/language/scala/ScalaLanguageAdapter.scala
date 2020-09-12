/*
 * Copyright 2016, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.language.scala


import net.fabricmc.loader.api.{LanguageAdapter, LanguageAdapterException, ModContainer}
import net.fabricmc.loader.launch.common.FabricLauncherBase

class ScalaLanguageAdapter extends LanguageAdapter {

  override def create[T](modContainer: ModContainer, value: String, aClass: Class[T]): T = {
    try {
      val classForName = (name: String) => Class.forName(name, true, FabricLauncherBase.getLauncher.getTargetClassLoader)
      val objectClass = classForName(value + "$").getField("MODULE$").get()
      objectClass match {
        case objectModInitializer: T => objectModInitializer
        case _ => classForName(value)
          .getDeclaredConstructor()
          .newInstance()
          .asInstanceOf[T]
      }
    } catch {
      case cce: ClassCastException => throw new LanguageAdapterException(cce)
    }
  }
}
