/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.codegen;

import com.sun.codemodel.*;
import org.renjin.invoke.codegen.args.ArgConverterStrategies;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.sexp.SEXP;

import static com.sun.codemodel.JExpr.lit;

public class VarArgApplyBuilder extends ApplyMethodBuilder {


  private VarArgParser parser;

  public VarArgApplyBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    super(codeModel, invoker, primitive);
  }

  @Override
  protected void apply(JBlock parent) {
    JvmMethod overload = primitive.getOverloads().get(0);

    parser = new VarArgParser(this, parent, overload);

    convertArgs(parser.getArgumentProcessingBlock());

    // try S3 dispatch
    genericDispatchStrategy.beforePrimitiveCalled(parent, parser, this, call, argNamesArray, argsArray);

    // finally invoke the underlying function
    JInvocation invocation = classRef(overload.getDeclaringClass()).staticInvoke(overload.getName());
    for(JExpression argument : parser.getArguments()) {
      invocation.arg(argument);
    }

    CodeModelUtils.returnSexp(context, codeModel, parent,  overload, invocation);
  }

  private void convertArgs(JBlock parent) {
    
    
    // convert the positional arguments
    int posIndex = 0;
    for(VarArgParser.PositionalArg posArgument : parser.getPositionalArguments()) {
      parent.assign(posArgument.getVariable(), convert(posArgument.getFormal(), nextArgAsSexp()));
      if(posIndex == 0) {
        genericDispatchStrategy.afterFirstArgIsEvaluated(this, call, posArgument.getVariable(), argNamesArray, argsArray, parent);
      }
      posIndex++;
    }
    
    JVar firstArgVar = parent.decl(codeModel.BOOLEAN, "firstArg", JExpr.TRUE);

    // now we consume remaining args
    JWhileLoop loop = parent._while(hasMoreArguments());
    matchVarArg(firstArgVar, loop.body());
    loop.body().assignPlus(argumentIndex, lit(1));
  }

  private void matchVarArg(JVar firstArgVar, JBlock block) {

    JExpression component = argsArray.component(argumentIndex);

    JExpression evaluateCall;
    if(primitive.isMissingAllowedInVarArgs()) {
      evaluateCall = classRef(WrapperRuntime.class).staticInvoke("forceOrMissing").arg(context).arg(component);
    } else {
      evaluateCall =  component.invoke("force").arg(context);
    }

    JVar evaluated = block.decl(classRef(SEXP.class), "evaluated", evaluateCall);

    // If the function has no positional arguments, then we need to check
    // the first argument for dispatch
    if(parser.getPositionalArguments().isEmpty()) {
      JBlock firstArgBlock = block._if(firstArgVar)._then();
      genericDispatchStrategy.afterFirstArgIsEvaluated(this, call, evaluated, argNamesArray, argsArray, firstArgBlock);
      block.assign(firstArgVar, JExpr.FALSE);
    }

    JVar name = block.decl(classRef(String.class), "name", argNamesArray.component(argumentIndex));
    block._if(name.eq(JExpr._null()))._then().assign(name, lit(""));

    // otherwise we may need to check it against named flags
    IfElseBuilder matchSequence = new IfElseBuilder(block);
    for(JvmMethod.Argument namedFlag : parser.getNamedFlags().keySet()) {
      matchSequence._if(lit(namedFlag.getName()).invoke("equals").arg(name))
              .assign(parser.getNamedFlags().get(namedFlag), convert(namedFlag, evaluated));

    }
    matchSequence._else().invoke(parser.getVarArgBuilder(), "add").arg(name).arg(evaluated);
  }

  private JExpression hasMoreArguments() {
    return argumentIndex.lt(argsArray.ref("length"));
  }

  private JExpression convert(JvmMethod.Argument formal, JExpression sexp) {
    return ArgConverterStrategies.findArgConverterStrategy(formal).convertArgument(this, sexp);
  }

}
