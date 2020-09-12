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

  override def create[T](mod: ModContainer, value: String, clazz: Class[T]): T = {
    val classForName = (name: String) => Class.forName(name, true, FabricLauncherBase.getLauncher.getTargetClassLoader)
    val getMainClass = () => classForName(value)
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[T]

    // two 'try's?? bear with me
    // inner try is for object class not found
    // outer try is for casting exception for both object and class
    try {
      try {
        val instance = classForName(value + "$")
          .getField("MODULE$")
          .get()
        // object is most likely a companion object
        if (!clazz.isInstance(instance)) getMainClass()
        else instance.asInstanceOf[T]
      } catch {
        case _: ClassNotFoundException => getMainClass()
      }
    } catch {
      case cce: ClassCastException => throw new LanguageAdapterException(cce)
    }
  }
}
