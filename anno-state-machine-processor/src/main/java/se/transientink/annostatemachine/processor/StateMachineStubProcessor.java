/*
 * Copyright 2018 Max Ringström
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

package se.transientink.annostatemachine.processor;


import com.jayway.annostatemachine.annotations.IncompleteStateMachine;
import com.jayway.annostatemachine.annotations.StateMachine;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Reads state machine declaration classes with the annotation {@link StateMachine} and generates
 * the corresponding state machine implementation classes. The implementation classes are placed
 * in a sub package of the declaration class' package. The sub package is named "generated".
 */
@SupportedAnnotationTypes("com.jayway.annostatemachine.annotations.IncompleteStateMachine")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
final public class StateMachineStubProcessor extends AbstractProcessor {

    public StateMachineStubProcessor() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(IncompleteStateMachine.class)) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            " State machine "    + ((TypeElement) element).getQualifiedName().toString()
                                    + " is incorrect. See warnings.");
        }
        return true;
    }

}